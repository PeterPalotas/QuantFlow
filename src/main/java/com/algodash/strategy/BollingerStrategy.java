package com.algodash.strategy;

import com.algodash.indicators.BollingerBand;
import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;
import com.algodash.service.BarSeries;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

//An example of a strategy.
//Buys when the prie goes below the bottom bollinger
//sells when price goes above the top bollinger.

//This is a simple example botfor the current state of the project.
//I want to make scripting bots easier, so i will get rid of the nesceccity to detect if a candle just closed
//I aim to create functions like onCandlecClose() which will have a parameter for the closed candle to work with maybe.
//Very simple for now but its a proof of concept of the dashboard working
@Component
@Primary
public class BollingerStrategy implements TradingStrategy {

    private final BarSeries series = new BarSeries(TimeFrame.M1);
    private final BollingerBand bb = new BollingerBand(20, 2.0);

    private boolean candleJustClosed = false;

    @Override
    public String getName() {
        return "Bollinger Reversion (1m)";
    }

    @Override
    public void update(Tick tick) {

        candleJustClosed = series.addTick(tick);

        //Update indicators if we have a new closed candle
        if (candleJustClosed) {
            double closePrice = series.getLastClosedCandle().getClose();
            bb.update(closePrice);
        }
    }

    //Runs only during live trading.
    @Override
    public StrategyResult analyze(Tick tick) {
        String signal = "HOLD";

        // We only check for trades if a candle JUST closed (and math was updated)
        if (candleJustClosed && bb.isReady()) {
            double closePrice = series.getLastClosedCandle().getClose();

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

        //We send indicator data every tick so the chart lines are smooth,
        //even if the value only changes once per minute.
        if (bb.isReady()) {
            result.addIndicator("Middle", bb.getMiddle(), "#ffa500");
            result.addIndicator("Upper", bb.getUpper(), "#00E396");
            result.addIndicator("Lower", bb.getLower(), "#FF4560");
        }

        return result;
    }
}