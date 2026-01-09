package com.example.contact.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.contact.infrastructure.adapter.out.persistence")
public class PersistenceConfig {
}
