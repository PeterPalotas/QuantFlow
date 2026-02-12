package com.algodash.service;

import com.algodash.model.DashboardState;
import com.algodash.model.Tick;
import com.algodash.model.StrategyResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

//The point of this class is to update the dashboard state. when a new tick comes in,
//the dashboard state is the object that collecs all the data and getsit ready to be sent to the frontend

@Service
public class DashboardStateService {

    private DashboardState currentState;

    @Autowired
    private PortfolioService portfolioService;

    @PostConstruct
    public void init() {
        // Initialize with a dummy state similar to original BinanceService
        Tick dummyTick = new Tick(System.currentTimeMillis(), -1, 0);
        currentState = new DashboardState(
                dummyTick,
                "WAITING",
                "white",
                portfolioService.getWallet(),
                new HashMap<>(),
                new HashMap<>()
        );
    }


    /**
     * This class manages the historical ticks in order to build tick history.
     * @param historyTick
     */
    public void updateHistoryState(Tick historyTick) {

        currentState = new DashboardState(
                historyTick,
                "READY",
                "white",
                portfolioService.getWallet(),
                new HashMap<>(),//No indicators yet, so this is okay, we want them initialised after for now
                new HashMap<>()
        );

    }
    /**
     * Updates the current DashboardState based on the latest tick, strategy result.
     * This method will be called by the TradingEngineService.
     */
    public void updateState(Tick tick, StrategyResult result) {
        String signal = result.getSignal();
        String color = "white";

        if ("BUY".equals(signal)) {
            color = "#00ff00";
        } else if ("SELL".equals(signal)) {
            color = "#ff0000";
        }

        // Update performance metrics (like drawdown) in the wallet
        portfolioService.getWallet().updateDrawdown(portfolioService.getWallet().getTotalValue(tick.getPrice()));

        currentState = new DashboardState(
                tick,
                signal,
                color,
                portfolioService.getWallet(),
                result.getIndicators(),
                result.getColors()
        );
    }

    /**
     * Returns the current DashboardState object for the frontend.
     */
    public DashboardState getCurrentState() {
        return currentState;
    }
}
