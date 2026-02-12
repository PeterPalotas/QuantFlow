package com.algodash.service;

import com.algodash.model.Wallet;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

    // The PortfolioService creates and holds the single Wallet instance.
    private final Wallet wallet = new Wallet(1000.0);


    public void processSignal(String signal, double price, double amountToBuy) {
        if ("BUY".equals(signal)) {
            wallet.buy(price, amountToBuy);
        } else if ("SELL".equals(signal)) {
            wallet.sellAll(price);
        }
    }

    public Wallet getWallet() {
        return this.wallet;
    }
}
