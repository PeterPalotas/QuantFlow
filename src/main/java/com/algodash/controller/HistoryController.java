package com.algodash.controller;

import com.algodash.model.Tick;
import com.algodash.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

import com.algodash.model.Candle;

//Controls the endpoint that returns the historical data.
//At the time of writing this we only get 1 minute data from binance. this is prone to change in future versions
@RestController
public class HistoryController {

    @Autowired
    private BinanceService binanceService;

    @CrossOrigin
    @GetMapping("/history")
    public List<Candle> getHistory() {  // Change return type to Candle
        return binanceService.getHistory();
    }
}