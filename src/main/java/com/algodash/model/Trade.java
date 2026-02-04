package com.algodash.model;


//Trade is a class that acts as a  Java object to store the final result of every single transaction that has been done
//its like a receipt essentially
public class Trade {
    private final long timestamp;
    private final String type;
    private final double price;
    private final double amount;
    private final double pnl;
    private final double fee;


    public Trade(String type, double price, double amount, double pnl, double fee) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.price = price;
        this.amount = amount;
        this.pnl = pnl;
        this.fee = fee;
    }

    public long getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getAmount() { return amount; }
    public double getPnl() { return pnl; }
    public double getFee() { return fee; }
}