package com.algodash.indicators;

//Eventually I will change all indicators to follow this interface

public interface Indicator {

    /**
     * Updates the indicator with the newest price.
     * @param price The latest price tick.
     */
    void update(double price);

    /**
     * @return The current calculated value of the indicator.
     */
    double getValue();

    /**
     * @return The name of this indicator (e.g., "SMA (10)").
     */
    String getName();

    /**
     * @return True if the indicator has enough data to be valid.
     */
    boolean isReady();
}