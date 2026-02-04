package com.algodash.indicators;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleMovingAverage implements Indicator {
    private final int period;
    private final Queue<Double> window = new LinkedList<>();
    private double sum = 0.0;
    private double currentValue = 0.0;

    public SimpleMovingAverage(int period) {
        this.period = period;
    }

    @Override
    public void update(double price) {
        window.add(price);
        sum += price;

        if (window.size() > period) {
            sum -= window.poll();
        }

        if (window.size() == period) {
            currentValue = sum / period;
        }
    }

    @Override
    public double getValue() {
        return currentValue;
    }

    @Override
    public String getName() {
        return "SMA (" + period + ")";
    }

    @Override
    public boolean isReady() {
        return window.size() == period;
    }
}