package com.algodash.service;

import com.algodash.model.*;
import com.algodash.strategy.TradingStrategy;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Currently acts as the brain of the application.
//The primary porpuse of this service is to talk with the binance API, and return prices
//Other features like initialising candle history, working with the wallet, and passing data to and from the bots
//Have been added quickly and I want to separate them all apart.
//I want to repair this service such that it serves its initial porpuse of talking to binance exclusively.
//In future updates to the project I will work to separate the features of this class apart

@Service
public class BinanceService {

    private WebSocketClient client;
    private final Wallet wallet = new Wallet(1000.0);
    private Tick latestTick;
    private DashboardState currentState;

    //The list to store historical candles for the frontend
    private final List<Candle> history = new ArrayList<>();

    @Autowired
    private TradingStrategy bot;

    @PostConstruct
    public void start() {

        // We start with -1 so it's obvious this is "invalid" data, otherwise the final candle opens at -1 and closes at
        //current price
        Tick dummyTick = new Tick(System.currentTimeMillis(), -1, 0);

        currentState = new DashboardState(
                dummyTick,
                "WAITING",
                "white",
                wallet,
                new HashMap<>(),
                new HashMap<>()
        );

        //WARM UP (Fetch History)
        System.out.println("Warming up strategy with historical data...");
        warmUpBot();

        //before we even connect to WebSocket, set the current state to the last known price.
        //this prevents the "Jump from $0 to $80,000" issue(in previous comment)
        if (!history.isEmpty()) {
            Candle lastCandle = history.get(history.size() - 1);

            //create a tick representing the latest history
            Tick historyTick = new Tick(
                    lastCandle.getCloseTime(),
                    lastCandle.getClose(),
                    0
            );

            //update the Dashboard State immediately
            currentState = new DashboardState(
                    historyTick,
                    "READY",
                    "white",
                    wallet,
                    new HashMap<>(),//No indicators yet, so this is okay, we want them initialised after for now
                    new HashMap<>()
            );

            System.out.println("State initialized with history price: $" + lastCandle.getClose());
        }

        //CONNECT LIVE (WebSocket)
        startConnection();
    }

    private void warmUpBot() {
        try {
            String symbol = "BTCUSDT";
            String interval = "1m";
            String url = String.format("https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=50", symbol, interval);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray klines = new JSONArray(response.body());
            System.out.println("📥 Retrieved " + klines.length() + " historical candles.");

            history.clear();

            for (int i = 0; i < klines.length(); i++) {
                JSONArray data = klines.getJSONArray(i);
                long timestamp = data.getLong(0);
                double open = data.getDouble(1);  // Open
                double high = data.getDouble(2);  // High
                double low = data.getDouble(3);   // Low
                double close = data.getDouble(4); // Close
                double volume = data.getDouble(5);

                //SAVE FULL CANDLE (For Visuals)
                history.add(new Candle(timestamp, open, high, low, close));

                //FEED DATA TO BOT
                // The bot only cares about the Close price for initial parameters for indicators (LIke the SMA or bollinger)
                bot.update(new Tick(timestamp, close, volume));
            }
            System.out.println("✅ Bot is warmed up!");

        } catch (Exception e) {
            System.err.println("❌ Failed to warm up: " + e.getMessage());
        }
    }

    public void startConnection() {
        try {
            URI binanceUri = new URI("wss://stream.binance.com:9443/ws/btcusdt@trade");
            client = new WebSocketClient(binanceUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("✅ Service Connected to Binance!");
                }

                @Override
                public void onMessage(String message) {
                    JSONObject json = new JSONObject(message);
                    Tick tick = new Tick(json.getLong("T"), json.getDouble("p"), json.getDouble("q"));

                    latestTick = tick;

                    //Update Math
                    bot.update(tick);

                    //Make decision
                    StrategyResult result = bot.analyze(tick);

                    String signal = result.getSignal();
                    String color = "white";

                    if (signal.equals("BUY")){
                        color = "#00ff00";
                        wallet.buy(tick.getPrice(), 150.0);
                    } else if (signal.equals("SELL")){
                        color = "#ff0000";
                        wallet.sellAll(tick.getPrice());
                    }

                    currentState = new DashboardState(tick, signal, color, wallet, result.getIndicators(), result.getColors());
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("❌ Connection Closed");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public Tick getLatestTick() { return latestTick; }
    public DashboardState getCurrentState() { return currentState; }
    public List<Candle> getHistory() {
        return history;
    }

}
