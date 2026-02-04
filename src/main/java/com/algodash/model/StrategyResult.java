package com.algodash.model;

import java.util.HashMap;
import java.util.Map;

//A DATA TRANSFER OBJECT

//this class acts as the "official report" issued by a trading strategy after it analyzes a price tick.
/*
1. SIGNAL DELIVERY: Tells the system whether to BUY, SELL, or HOLD.
2. DATA BUNDLING: Carries the indicator values (e.g., SMA, Bollinger Bands).
3. STYLING INSTRUCTIONS: Maps specific indicators to hex colors for the frontend chart.
 */
public class StrategyResult {
    private final String signal; //"BUY", "SELL", "HOLD"

    private final Map<String, Double> indicators = new HashMap<>();

    // colours for lines like:  ' "SMA" :"#ffa500" '
    private final Map<String, String> colors = new HashMap<>();

    public StrategyResult(String signal) {
        this.signal = signal;
    }

    public void addIndicator(String name, double value, String hexColor) {
        indicators.put(name, value);
        colors.put(name, hexColor);
    }


    public String getSignal() { return signal; }
    public Map<String, Double> getIndicators() { return indicators; }
    public Map<String, String> getColors() { return colors; }
}