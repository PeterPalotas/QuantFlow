package com.algodash.strategy;

import com.algodash.model.Candle;
import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;
import com.algodash.service.IndicatorManagerService; // New import
import org.springframework.beans.factory.annotation.Autowired; // New import if not already there
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.Num; // New import

//An example of a strategy.
//Buys when the price goes below the bottom bollinger
//sells when price goes above the top band.

//This is a simple example bot for the current state of the project.
@Component
@Primary
public class BollingerStrategy implements TradingStrategy {

    @Autowired
    private IndicatorManagerService indicatorManager; // Inject the new manager

    // Define unique names for our indicators
    private static final String BB_NAME = "BollingerBands_M1";
    private static final int BB_BAR_COUNT = 20;
    private static final double BB_MULTIPLIER = 2.0;

    // Constructor to register indicators
    @Autowired // Spring will use this constructor for injection
    public BollingerStrategy(IndicatorManagerService indicatorManager) {
        this.indicatorManager = indicatorManager;
        // Register the Bollinger Bands this strategy needs
        indicatorManager.registerBollingerBands(BB_NAME, TimeFrame.M1, BB_BAR_COUNT, BB_MULTIPLIER);
    }


    @Override
    public String getName() {
        return "Bollinger Reversion (1m)";
    }

    @Override
    public void update(Tick tick) {
        // The 'update' method's primary role is now for warming up (if needed)
        // or for tick-based strategies. For Bollinger Bands, ta4j handles
        // updating when a bar is added to its series, so this method is empty.
    }

    @Override
    public StrategyResult onCandleClose(Candle closedBar, TimeFrame timeFrame) {
        // This strategy only operates on the 1-minute timeframe.
        if (timeFrame != TimeFrame.M1) {
            return new StrategyResult("HOLD");
        }

        // Retrieve the indicator values from the manager
        Num middle = indicatorManager.getValue(BB_NAME + "_middle");
        Num upper = indicatorManager.getValue(BB_NAME + "_upper");
        Num lower = indicatorManager.getValue(BB_NAME + "_lower");

        String signal = "HOLD";
        // Ensure indicators have enough data to be calculated (e.g., bb.isReady())
        if (middle != null && upper != null && lower != null) {
            double closePrice = closedBar.getClose();

            //BUY Logic
            if (closePrice <= lower.doubleValue()) {
                signal = "BUY";
                System.out.printf("[%s] BUY SIGNAL: Closed $%.2f below Lower Band $%.2f%n",
                        getName(), closePrice, lower.doubleValue());
            }
            //SELL Logic
            else if (closePrice >= upper.doubleValue()) {
                signal = "SELL";
                System.out.printf("[%s] SELL SIGNAL: Closed $%.2f above Upper Band $%.2f%n",
                        getName(), closePrice, upper.doubleValue());
            }
        }

        //Dashboard Updates
        StrategyResult result = new StrategyResult(signal);

        // We send indicator data with every result so the chart lines are smooth.
        if (middle != null && upper != null && lower != null) {
            result.addIndicator("Middle", middle.doubleValue(), "#ffa500");
            result.addIndicator("Upper", upper.doubleValue(), "#00E396");
            result.addIndicator("Lower", lower.doubleValue(), "#FF4560");
        }

        return result;
    }
}