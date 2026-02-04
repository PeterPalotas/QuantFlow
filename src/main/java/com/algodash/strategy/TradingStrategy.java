package com.algodash.strategy;

import com.algodash.model.StrategyResult;
import com.algodash.model.Tick;

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
     * This is called during Warmup AND Live trading.
     * Update your indicators here. NEVER trade here.
     */
    void update(Tick tick);

    /**
     * This is called ONLY during Live trading.
     * Return your BUY/SELL signals here.
     */
    StrategyResult analyze(Tick tick);
}