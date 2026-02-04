package com.algodash.service;

import com.algodash.model.Candle;
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;

//Takes thousands of individual Tick events and organizes them into clean, time based Candle objects.
public class BarSeries {
    private final long intervalMillis;
    private Candle currentCandle;
    private Candle lastClosedCandle;
    private boolean isNewCandleClosed = false;

    public BarSeries(TimeFrame timeFrame) {
        this.intervalMillis = timeFrame.getMillis();
    }

    /**
     * Ingests a tick. Returns TRUE if a candle just closed (Signal to trade).
     */
    public boolean addTick(Tick tick) {
        isNewCandleClosed = false;
        long tradeTime = tick.getTimestamp();
        double price = tick.getPrice();

        // Calculate the "Bucket" start time for this tick
        // e.g. 12:01:45 -> 12:01:00 bucket
        long bucketStart = (tradeTime / intervalMillis) * intervalMillis;

        //Initialize First Candle
        if (currentCandle == null) {
            currentCandle = new Candle(bucketStart, intervalMillis, price);
            return false;
        }

        //Check if we moved to a NEW bucket
        if (bucketStart >= currentCandle.getCloseTime()) {
            //CLOSE the old candle
            currentCandle.close();
            lastClosedCandle = currentCandle;
            isNewCandleClosed = true;

            //START the new candle
            currentCandle = new Candle(bucketStart, intervalMillis, price);
        }

        //Update the Current Candle (the live data one)
        currentCandle.addTick(price, tick.getQuantity());

        return isNewCandleClosed;
    }

    public Candle getLastClosedCandle() {
        return lastClosedCandle;
    }

    public double getCurrentPrice() {
        return currentCandle != null ? currentCandle.getClose() : 0.0;
    }
}