package com.example.asynctaskflow.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.asynctaskflow.core.repository")
@EntityScan(basePackages = "com.example.asynctaskflow.core.model")
public class TaskFlowRepositoryConfig {
}
