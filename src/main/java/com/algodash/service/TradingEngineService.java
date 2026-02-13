package com.algodash.service;

import com.algodash.model.Candle;
import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;
import com.algodash.strategy.TradingStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * The TradingEngineService acts as the central orchestrator of the application.
 * It coordinates the data flow between various services, managing the trading lifecycle
 * from fetching historical data and warming up the strategy to processing live market ticks,
 * generating trading signals, executing trades, and updating the dashboard's state.
 */
@Service
public class TradingEngineService {

    @Autowired
    private BinanceDataService dataService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private DashboardStateService dashboardStateService;

    @Autowired
    private TradingStrategy strategy;

    @Autowired
    private BarAggregatorService barAggregatorService;

    @PostConstruct
    public void startEngine() {
        System.out.println("🚀 Trading Engine starting...");

        // set the callback for the BarAggregatorService first
        barAggregatorService.setOnBarCloseCallback(this::onBarClosed);

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

    // This method receives ticks from the BinanceDataService and feeds them to the BarAggregatorService.
    private void onTickReceived(Tick tick) {
        // 1. Perform a lightweight dashboard update for live price action
        dashboardStateService.updateLiveTick(tick);

        // 2. Feed the tick to the bar aggregator to check if a bar has closed
        barAggregatorService.addTick(tick);
    }

    // This method is the callback from BarAggregatorService when a bar closes.
    private void onBarClosed(Map.Entry<Candle, TimeFrame> barEntry) {
        Candle closedBar = barEntry.getKey();
        TimeFrame timeFrame = barEntry.getValue();

        System.out.printf("📊 %s Bar Closed: Open=%.2f, High=%.2f, Low=%.2f, Close=%.2f%n",
                timeFrame.name(), closedBar.getOpen(), closedBar.getHigh(), closedBar.getLow(), closedBar.getClose());

        // Call the strategy's onCandleClose method for decision making (there should be no trading decisions in ontick/update)
        StrategyResult result = strategy.onCandleClose(closedBar, timeFrame);

        //act on the signal (if any)
        portfolioService.processSignal(result.getSignal(), closedBar.getClose(), 150.0);

        // Note: The dashboard state uses the latest TICK for display, not the closed bar.
        // We'll pass a Tick representation of the closed bar's close price for the DashboardState update.
        dashboardStateService.updateState(new Tick(closedBar.getCloseTime(), closedBar.getClose(), 0), result);
    }
}
