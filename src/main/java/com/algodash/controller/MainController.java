package com.algodash.controller;

import com.algodash.model.DashboardState;
import com.algodash.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//This is the controller that returns information from the backend to the frontend

//It manages the /price endpoint which returns the full state from binance


@RestController
public class MainController {

    @Autowired
    private BinanceService binanceService;

    @GetMapping("/price")
    public DashboardState getPrice() {
        return binanceService.getCurrentState();
    }
}