package com.algodash.strategy;

import com.algodash.model.Candle;
import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;
import com.algodash.model.TimeFrame;

/*
 TradingStrategy is the structural contract for all bot logic.
 It ensures that any trading algorithm can be "plugged in" to the BinanceService
 without changing the core networking or financial code.
 */
public interface TradingStrategy {

    /**
     *
     * @return the bots name
     */
    String getName();

    /**
     * This is called during Warmup AND Live trading for every tick.
     * Use this to update indicators that require tick-level granularity, or for warming up. DON'T TRADE HERE
     */
    void update(Tick tick);

    /**
     * This is called when a candle closes for a specific timeframe.
     * All decision-making logic for BUY/SELL signals should go here.
     *
     * @param closedBar The fully formed closed candle.
     * @param timeFrame The timeframe of the closed candle.
     * @return A StrategyResult containing the signal and any relevant indicators.
     */
    StrategyResult onCandleClose(Candle closedBar, TimeFrame timeFrame);
}