package com.algodash.indicators;

import java.util.LinkedList;
import java.util.Queue;

//A basic bollinger band indicator that can be used for writing bots
// I want to refactor indicators to be a lot more like the ones in MQL5 and force all of them to implement
// the Indicator interface
public class BollingerBand {

    private final int period;
    private final double multiplier;
    private final Queue<Double> window = new LinkedList<>();
    private double sum = 0.0;

    private double middleBand = 0.0;
    private double upperBand = 0.0;
    private double lowerBand = 0.0;

    public BollingerBand(int period, double multiplier) {
        this.period = period;
        this.multiplier = multiplier;
    }

    public void update(double price) {
        window.add(price);
        sum += price;

        if (window.size() > period) {
            sum -= window.poll();
        }

        if (window.size() == period) {

            middleBand = sum / period;

            //calculate standard deviation
            double varianceSum = 0.0;
            for (double p : window) {
                varianceSum += Math.pow(p - middleBand, 2);
            }
            double stdDev = Math.sqrt(varianceSum / period);

            upperBand = middleBand + (multiplier * stdDev);
            lowerBand = middleBand - (multiplier * stdDev);
        }
    }

    public boolean isReady() {
        return window.size() == period;
    }

    public double getMiddle() { return middleBand; }
    public double getUpper() { return upperBand; }
    public double getLower() { return lowerBand; }

    public String getName() {
        return "Bollinger (" + period + "," + multiplier + ")";
    }
}