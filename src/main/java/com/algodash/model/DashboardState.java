package com.algodash.model;
import com.algodash.model.Wallet;

import java.util.List;
import java.util.Map;

//THIS CLASS IS A DATA TRANSFER OBJECT (DTO)
//It's a specialised container designed to bundle all the critical information from backend and ship it to
// frontend in a single "package."
//NOTE, the data is actually shipped by com.algodash.MainController.java

public class DashboardState {
    private final Tick tick;
    private final String signal;
    private final String color;

    private final double portfolioValue;
    private final double cash;
    private final double crypto;

    private final List<Trade> trades;

    //Tracking
    private final int wins;
    private final int losses;
    private final double winRate;
    private final double profitFactor;
    private final double maxDrawdown;
    //indicators
    private final Map<String, Double> indicators;
    private final Map<String, String> indicatorColors;
    public DashboardState(Tick tick, String signal, String color, Wallet wallet, Map<String, Double> indicators, Map<String, String> indicatorColors) {
        this.tick = tick;
        this.signal = signal;
        this.color = color;



        this.portfolioValue = wallet.getTotalValue(tick.getPrice());
        wallet.updateDrawdown(this.portfolioValue);

        //stats
        this.wins = wallet.getWins();
        this.losses = wallet.getLosses();
        this.winRate = wallet.getWinRate();
        this.profitFactor = wallet.getProfitFactor();
        this.maxDrawdown = wallet.getMaxDrawdown();


        this.cash = wallet.getCash();
        this.crypto = wallet.getCrypto();
        this.trades = wallet.getTradeHistory();

        this.indicators = indicators;
        this.indicatorColors = indicatorColors;

    }
    public int getWins() {return wins;    }
    public int getLosses(){return losses;}
    public double getWinRate(){return winRate;}
    public double getProfitFactor(){return profitFactor;}
    public double getMaxDrawdown(){return maxDrawdown;}

    public Map<String, Double> getIndicators() { return indicators; }
    public Map<String, String> getIndicatorColors() { return indicatorColors; }

    public Tick getTick() { return tick; }
    public String getSignal() { return signal; }
    public String getColor() { return color; }

    public List<Trade> getTrades() {return trades;}

    public double getPortfolioValue() { return portfolioValue; }
    public double getCash() { return cash; }
    public double getCrypto() { return crypto; }
}