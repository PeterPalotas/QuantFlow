package com.algodash.model;

import java.util.ArrayList;
import java.util.List;

//The wallet class is the financial core of the system.
//simulates exchange fees, and tracks performance analytics.

//Stores what assets are being held.
//Execute trades
//Tracks financial metrics **MAYBE MIGRATE THIS TO A SEPARATE CLASS LATER**

public class Wallet {
    private static final double TRADING_FEE_RATE = 0.001;
    private double cash;
    private double crypto; // Amount of Bitcoin held

    private int wins = 0;
    private int losses = 0;
    private double grossProfit = 0.0;
    private double grossLoss = 0.0;

    private double peakPortfolioValue = 0.0;
    private double maxDrawdown = 0.0;

    private List<Trade> tradeHistory = new ArrayList<>();
    //how much we spent on the current BTC
    private double totalCostOfHoldings = 0.0;


    public Wallet(double startingCash) {
        this.cash = startingCash;
        this.crypto = 0.0; // Start with 0 Bitcoin

        this.peakPortfolioValue = startingCash;
    }

    // ACTION: Buy bitcoin
    public void buy(double price, double amount) {
        //Amount represents the amount of bitcoin to buy in $
        //so if bitcoin is worth $100, and amount is $10, then we buy amount/price = 0.1 BTC

        if (cash > amount) {
            double fee = amount * TRADING_FEE_RATE;
            double netAmount = amount-fee;
            double amountBought = netAmount / price;
            this.crypto += amountBought;
            this.cash -= amount;

            this.totalCostOfHoldings+=amount;

            tradeHistory.add(new Trade("BUY", price, amount, 0, fee));


            System.out.printf("WALLET: Bought %.4f BTC @ $%.2f%n", amountBought, price);
        }
    }


    public void sellAll(double price) {
        if (crypto > 0.0001) {
            double grossValue = crypto * price;
            double fee = grossValue * TRADING_FEE_RATE;
            double netCashReceived = grossValue - fee;
            double profit = netCashReceived - totalCostOfHoldings;


            this.cash += netCashReceived;
            this.crypto = 0.0;


            this.totalCostOfHoldings = 0.0;

            if (profit > 0) {
                wins++;
                grossProfit += profit;
            } else {
                losses++;
                grossLoss += Math.abs(profit);
            }


            tradeHistory.add(new Trade("SELL", price, netCashReceived, profit, fee));

            //System.out.printf("WALLET: Sold for $%.2f @ $%.2f%n", cashReceived, price);
        }
    }

    public void updateDrawdown(double currentPortfolioValue) {
        // 1. Is this a new All-Time High?
        if (currentPortfolioValue > peakPortfolioValue) {
            peakPortfolioValue = currentPortfolioValue;
        }

        // 2. Calculate Drawdown % (How far are we from the peak?)
        double currentDrawdown = (peakPortfolioValue - currentPortfolioValue) / peakPortfolioValue;

        // 3. Track the WORST drawdown ever seen
        if (currentDrawdown > maxDrawdown) {
            maxDrawdown = currentDrawdown;
        }
    }

    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public double getWinRate() {
        if (wins + losses == 0) return 0.0;
        return (double) wins / (wins + losses);
    }
    public double getProfitFactor() {
        if (grossLoss == 0) return grossProfit > 0 ? 99.9 : 0.0; // Infinite if no loss
        return grossProfit / grossLoss;
    }
    public double getMaxDrawdown() { return maxDrawdown; }

    public double getTotalValue(double currentPrice) {
        return cash + (crypto * currentPrice);
    }

    // Getters for the UI
    public double getCash() { return cash; }
    public List<Trade> getTradeHistory() { return tradeHistory; }
    public double getCrypto() { return crypto; }
}