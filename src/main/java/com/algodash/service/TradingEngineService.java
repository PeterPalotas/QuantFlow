package com.algodash.service;

import com.algodash.model.Candle;
import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;
import com.algodash.strategy.TradingStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * The TradingEngineService acts as the central orchestrator of the application.
 * It coordinates the data flow between various services, managing the trading lifecycle
 * from fetching historical data and warming up the strategy to processing live market ticks,
 * generating trading signals, executing trades, and updating the dashboard's state.
 */
@Service
public class TradingEngineService {

    // Note: The application will not run until BinanceDataService is created.
    @Autowired
    private BinanceDataService dataService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private DashboardStateService dashboardStateService;

    @Autowired
    private TradingStrategy strategy;

    @PostConstruct
    public void startEngine() {
        System.out.println("🚀 Trading Engine starting...");

        //Fetch history from the Data Service and warm up the strategy
        List<Candle> history = dataService.fetchHistoricalCandles();
        System.out.println("📥 Retrieved " + history.size() + " historical candles for warmup.");
        history.forEach(candle -> {
            strategy.update(new Tick(candle.getCloseTime(), candle.getClose(), 0));
        });
        System.out.println("✅ Strategy is warmed up.");


        //Set the initial dashboard state using the last historical price
        if (!history.isEmpty()) {
            Candle lastCandle = history.get(history.size() - 1);
            Tick historyTick = new Tick(
                    lastCandle.getCloseTime(),
                    lastCandle.getClose(),
                    0
            );
            dashboardStateService.updateHistoryState(historyTick);
            System.out.println("✅ Dashboard state initialized with history price: $" + lastCandle.getClose());
        }

        //Start the live data stream
        System.out.println("📡 Connecting to live data stream...");
        dataService.startLiveStream(this::onTickReceived);
    }

    // This is the core logic loop for every live tick received.
    private void onTickReceived(Tick tick) {


        strategy.update(tick);

        StrategyResult result = strategy.analyze(tick);

        portfolioService.processSignal(result.getSignal(), tick.getPrice(), 150.0);

        dashboardStateService.updateState(tick, result);
    }
}
