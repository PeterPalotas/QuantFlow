package com.algodash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//this is the single file that tells Java to start running the software as a web application.
@SpringBootApplication
public class DashApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashApplication.class, args);
    }
}