package com.aidataquality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for AI Data Quality Tool
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class AiDataQualityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDataQualityApplication.class, args);
    }
}

