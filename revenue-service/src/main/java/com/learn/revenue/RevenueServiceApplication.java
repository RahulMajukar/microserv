package com.learn.revenue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.learn.revenue", "com.learn.common"})
public class RevenueServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevenueServiceApplication.class, args);
    }
}
