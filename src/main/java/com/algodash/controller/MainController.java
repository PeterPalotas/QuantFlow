package com.algodash.controller;

import com.algodash.model.DashboardState;
import com.algodash.service.DashboardStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//This is the controller that returns information from the backend to the frontend

//It manages the /price endpoint which returns the full state from DashboardSateService


@RestController
public class MainController {

    @Autowired
    private DashboardStateService dashboardStateService;

    @GetMapping("/price")
    public DashboardState getPrice() {
        return dashboardStateService.getCurrentState();
    }
}