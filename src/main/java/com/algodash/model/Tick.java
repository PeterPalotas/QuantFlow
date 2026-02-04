package com.algodash.model;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


//Tick represents a single, individual trade event from the exchange.
//A tick contains the below information
/*
Timestamp: The exact millisecond the trade occurred.
Price: The dollar value at which the transaction was executed.
Quantity: The amount of Bitcoin (BTC) that changed hands.
 */

public class Tick {

    private final long timestamp;
    private final double price;
    private final double quantity;

    public Tick(long timestamp, double price, double quantity) {
        this.timestamp = timestamp;
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() { return price; }
    public double getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        //Fix the scientific notation to decimal
        DecimalFormat df = new DecimalFormat("0.00000");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault());

        return String.format("[%s] BTC: $%.2f (Size: %s)",
                timeFormatter.format(Instant.ofEpochMilli(timestamp)),
                price,
                df.format(quantity));
    }
}