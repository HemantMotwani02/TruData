package com.aidataquality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for detecting bias in datasets
 */
@Service
@Slf4j
public class BiasDetectionService {

    private static final Set<String> SENSITIVE_ATTRIBUTES = Set.of(
        "gender", "sex", "race", "ethnicity", "age", "religion",
        "nationality", "disability", "sexual_orientation"
    );

    /**
     * Detect potential bias in dataset
     */
    public Map<String, Object> detectBias(List<Map<String, Object>> data) {
        Map<String, Object> biasReport = new HashMap<>();
        
        if (data == null || data.isEmpty()) {
            biasReport.put("biasDetected", false);
            biasReport.put("description", "No data to analyze");
            return biasReport;
        }
        
        boolean biasDetected = false;
        List<String> findings = new ArrayList<>();
        
        // Check for sensitive attributes
        Set<String> columns = data.get(0).keySet();
        List<String> sensitiveColumns = new ArrayList<>();
        
        for (String column : columns) {
            if (isSensitiveAttribute(column)) {
                sensitiveColumns.add(column);
                biasDetected = true;
            }
        }
        
        if (!sensitiveColumns.isEmpty()) {
            findings.add("Sensitive attributes detected: " + String.join(", ", sensitiveColumns));
            findings.add("These columns may introduce bias in ML models");
            
            // Analyze distribution of sensitive attributes
            for (String column : sensitiveColumns) {
                Map<String, Long> distribution = analyzeDistribution(data, column);
                if (isImbalanced(distribution)) {
                    findings.add("Imbalanced distribution in '" + column + "': " + distribution);
                }
            }
        }
        
        biasReport.put("biasDetected", biasDetected);
        biasReport.put("sensitiveColumns", sensitiveColumns);
        biasReport.put("findings", findings);
        biasReport.put("description", biasDetected 
            ? "Potential bias detected in dataset. Review sensitive attributes."
            : "No obvious bias indicators detected.");
        
        return biasReport;
    }

    /**
     * Check if column name suggests sensitive attribute
     */
    private boolean isSensitiveAttribute(String columnName) {
        String normalized = columnName.toLowerCase().replaceAll("[_\\s-]", "");
        
        for (String sensitive : SENSITIVE_ATTRIBUTES) {
            if (normalized.contains(sensitive)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Analyze distribution of values in a column
     */
    private Map<String, Long> analyzeDistribution(List<Map<String, Object>> data, String column) {
        Map<String, Long> distribution = new HashMap<>();
        
        for (Map<String, Object> row : data) {
            Object value = row.get(column);
            String key = value != null ? value.toString() : "null";
            distribution.put(key, distribution.getOrDefault(key, 0L) + 1);
        }
        
        return distribution;
    }

    /**
     * Check if distribution is imbalanced
     */
    private boolean isImbalanced(Map<String, Long> distribution) {
        if (distribution.size() <= 1) {
            return false;
        }
        
        long total = distribution.values().stream().mapToLong(Long::longValue).sum();
        long max = distribution.values().stream().mapToLong(Long::longValue).max().orElse(0);
        
        // Consider imbalanced if the most common value represents > 80% of data
        return (max * 1.0 / total) > 0.8;
    }
}

