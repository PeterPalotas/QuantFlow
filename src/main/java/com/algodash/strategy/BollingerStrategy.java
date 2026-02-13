package com.algodash.strategy;

import com.algodash.indicators.BollingerBand;
import com.algodash.model.Candle;
import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;
import com.algodash.service.BarSeries;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

//An example of a strategy.
//Buys when the prie goes below the bottom bollinger
//sells when price goes above the top bolliner.

//This is a simple example botfor the current state of the project.
//I want to make scripting bots easier, so i will get rid of the nesceccity to detect if a candle just closed
//I aim to create functions like onCandlecClose() which will have a parameter for the closed candle to work with maybe.
//Very simple for now but its a proof of concept of the dashboard working
@Component
@Primary
public class BollingerStrategy implements TradingStrategy {

    private final BollingerBand bb = new BollingerBand(20, 2.0);

    @Override
    public String getName() {
        return "Bollinger Reversion (1m)";
    }

    @Override
    public void update(Tick tick) {
        // The 'update' method is jus used for warming up indicators with historical data.
        // For this strategy, the main logic is on candle close
        // We check if the price is valid to avoid updating indicators with dummy ticks.
        // I GENERALLY DO NOT RECCOMMEND TRADING IN THE UPDATE CLASS. its ultra-high frequency and false positives may occur
        if (tick.getPrice() > 0) {
            bb.update(tick.getPrice());
        }
    }

    @Override
    public StrategyResult onCandleClose(Candle closedBar, TimeFrame timeFrame) {
        // This strategy only operates on the 1-minute timeframe.
        if (timeFrame != TimeFrame.M1) {
            return new StrategyResult("HOLD");
        }

        // The indicator is now updated here for live trading, right before making a decision.
        // Note: The 'update' method above handles the initial warmup.
        bb.update(closedBar.getClose());

        String signal = "HOLD";
        if (bb.isReady()) {
            double closePrice = closedBar.getClose();

            //BUY Logic
            if (closePrice <= bb.getLower()) {
                signal = "BUY";
                System.out.printf("[%s] BUY SIGNAL: Closed $%.2f below Lower Band $%.2f%n",
                        getName(), closePrice, bb.getLower());
            }
            //SELL Logic
            else if (closePrice >= bb.getUpper()) {
                signal = "SELL";
                System.out.printf("[%s] SELL SIGNAL: Closed $%.2f above Upper Band $%.2f%n",
                        getName(), closePrice, bb.getUpper());
            }
        }

        //Dashboard Updates
        StrategyResult result = new StrategyResult(signal);

        // We send indicator data with every result so the chart lines are smooth.
        if (bb.isReady()) {
            result.addIndicator("Middle", bb.getMiddle(), "#ffa500");
            result.addIndicator("Upper", bb.getUpper(), "#00E396");
            result.addIndicator("Lower", bb.getLower(), "#FF4560");
        }

        return result;
    }
}