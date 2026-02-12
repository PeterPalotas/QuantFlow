package com.algodash.model;

//This is the class used to handle candle behaviour such as
//rendering historical candles and constructing current ones
//marking candles as closed

//The key feature is that instead of storing every trade, the add tick method updates the candles state in real time
public class Candle {
    private final long openTime;
    private final long closeTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private boolean isClosed;

    //constructor for Historical Data (Open, High, Low, Close known)
    public Candle(long openTime, double open, double high, double low, double close, double volume) {
        this.openTime = openTime;
        this.closeTime = openTime + 60000; // Assume 1 minute for history
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.isClosed = true;
    }

    //Constructor for Live Data
    public Candle(long openTime, long durationMillis, double openPrice) {
        this.openTime = openTime;
        this.closeTime = openTime + durationMillis;
        this.open = openPrice;
        this.high = openPrice;
        this.low = openPrice;
        this.close = openPrice;
        this.volume = 0;
        this.isClosed = false;
    }

    //logic to build candle live
    public void addTick(double price, double quantity) {
        if (isClosed) return;
        this.close = price;
        if (price > this.high) this.high = price;
        if (price < this.low) this.low = price;
        this.volume += quantity;
    }

    //function to close the candle
    public void close() { this.isClosed = true; }

    //getters
    public long getOpenTime() { return openTime; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public long getCloseTime() { return closeTime; }
    public double getVolume() { return volume; }
}