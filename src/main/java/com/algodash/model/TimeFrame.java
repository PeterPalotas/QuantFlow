package com.algodash.model;


//TimeFrame is an enumeration that contains definitions for different timeframes
//THis ensures standard times. and lets us use  timeframe specific logic for the rest of the code

//Hopefully later, i can make it so strategies can be tested on different timeframes, or work based on different timeframes
//This standardisation avoids different definitions of a timeframe in diff

public enum TimeFrame {
    S5(5),
    M1(60),// 1 Minute
    M5(300),// 5 Minutes
    M15(900),// 15 Minutes
    H1(3600);// 1 Hour

    private final int seconds;

    TimeFrame(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() { return seconds; }
    public long getMillis() { return seconds * 1000L; }
}