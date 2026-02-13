package com.algodash.service;

import com.algodash.model.Candle; // Reusing Candle as Bar
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;
import org.springframework.stereotype.Service;

import java.util.AbstractMap; // For SimpleEntry
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;


//This service is responsible for taking raw ticks and aggregating them into completed bars for various timeframes,
// notifying the TradingEngineService whenever a bar closes.
//this will be used to know when a new bar opens on each time frame, allows implementing things like onBarClose()
@Service
public class BarAggregatorService {

    private final Map<TimeFrame, BarSeries> barSeriesMap = new EnumMap<>(TimeFrame.class);

    // Callback for when a bar closes, providing the closed Candle and its TimeFrame
    private Consumer<Map.Entry<Candle, TimeFrame>> onBarCloseCallback;

    public BarAggregatorService() {
        barSeriesMap.put(TimeFrame.M1, new BarSeries(TimeFrame.M1));
        // Future: can easily add more timeframes here, e.g., barSeriesMap.put(TimeFrame.M5, new BarSeries(TimeFrame.M5));
    }

    /**
     * Sets the callback function to be invoked when a bar closes for any managed timeframe.
     * This will typically be set by the TradingEngineService during its initialization.
     */
    public void setOnBarCloseCallback(Consumer<Map.Entry<Candle, TimeFrame>> callback) {
        this.onBarCloseCallback = callback;
    }

    /**
     * Adds a new tick to all managed BarSeries.
     * If a bar closes for any timeframe, the onBarCloseCallback is invoked with the closed Candle and its TimeFrame.
     *
     * @param tick The incoming Tick from the market.
     */
    public void addTick(Tick tick) {
        if (onBarCloseCallback == null) {
            //This means the TradingEngineService hasn't registered its callback yet.
            //It's crucial for the engine to set this up early.
            System.err.println("BarAggregatorService: onBarCloseCallback not set. Closed bars will not be processed.");
            return;
        }

        for (Map.Entry<TimeFrame, BarSeries> entry : barSeriesMap.entrySet()) {
            TimeFrame timeFrame = entry.getKey();
            BarSeries barSeries = entry.getValue();

            boolean candleJustClosed = barSeries.addTick(tick);

            if (candleJustClosed) {
                Candle closedBar = barSeries.getLastClosedCandle();
                if (closedBar != null) {
                    //notify that a bar has closed, passing both the bar and its timeframe
                    onBarCloseCallback.accept(new AbstractMap.SimpleEntry<>(closedBar, timeFrame));
                }
            }
        }
    }
}
