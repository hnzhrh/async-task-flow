package com.example.asynctaskflow.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.asynctaskflow")
@EnableScheduling
public class AsyncTaskFlowDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncTaskFlowDemoApplication.class, args);
    }
}
