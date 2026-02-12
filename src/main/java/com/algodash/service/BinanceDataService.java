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
import java.util.List;
import java.util.function.Consumer;

//Currently acts as the brain of the application.
//The primary porpuse of this service is to talk with the binance API, and return prices
//Other features like initialising candle history, working with the wallet, and passing data to and from the bots
//Have been added quickly and I want to separate them all apart.
//I want to repair this service such that it serves its initial porpuse of talking to binance exclusively.
//In future updates to the project I will work to separate the features of this class apart

@Service
public class BinanceDataService {

    private WebSocketClient client;

    //The list to store historical candles for the frontend

    private final List<Candle> history = new ArrayList<>();

    public List<Candle> fetchHistoricalCandles() {
        try {
            String symbol = "BTCUSDT";
            String interval = "1m";
            String url = String.format("https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=50", symbol, interval);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray klines = new JSONArray(response.body());

            history.clear();

            for (int i = 0; i < klines.length(); i++) {
                JSONArray data = klines.getJSONArray(i);
                long timestamp = data.getLong(0);
                double open = data.getDouble(1);
                double high = data.getDouble(2);
                double low = data.getDouble(3);
                double close = data.getDouble(4);
                double volume = data.getDouble(5);

                //SAVE FULL CANDLE (For Visuals) - now with volume
                history.add(new Candle(timestamp, open, high, low, close, volume));
            }
            System.out.println("✅ Historical data fetched!");

        } catch (Exception e) {
            System.err.println("❌ Failed to fetch history: " + e.getMessage());
        }
        return history;
    }

    public void startLiveStream(Consumer<Tick> onTickReceived) {
        try {
            URI binanceUri = new URI("wss://stream.binance.com:9443/ws/btcusdt@trade");
            client = new WebSocketClient(binanceUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("✅ Data Service Connected to Binance!");
                }

                @Override
                public void onMessage(String message) {
                    // Parse the message into a tick
                    JSONObject json = new JSONObject(message);
                    Tick tick = new Tick(json.getLong("T"), json.getDouble("p"), json.getDouble("q"));

                    // Pass the tick to the consumer (which will be TradingEngineService::onTickReceived)
                    onTickReceived.accept(tick);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("❌ Data Service Connection Closed");
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

    public List<Candle> getHistory() {
        return history;
    }

}
