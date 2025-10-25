package com.aidataquality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for detecting Personally Identifiable Information (PII)
 */
@Service
@Slf4j
public class PIIDetectionService {

    // PII detection patterns
    private static final Map<String, Pattern> PII_PATTERNS = new HashMap<>();
    
    static {
        PII_PATTERNS.put("EMAIL", Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"));
        PII_PATTERNS.put("PHONE", Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b"));
        PII_PATTERNS.put("SSN", Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));
        PII_PATTERNS.put("CREDIT_CARD", Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"));
        PII_PATTERNS.put("ZIP_CODE", Pattern.compile("\\b\\d{5}(?:-\\d{4})?\\b"));
        PII_PATTERNS.put("IP_ADDRESS", Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"));
    }
    
    // Common PII column name patterns
    private static final Set<String> PII_COLUMN_NAMES = Set.of(
        "email", "e-mail", "mail",
        "phone", "telephone", "mobile", "cell",
        "ssn", "social_security",
        "password", "pwd",
        "credit_card", "cc", "card_number",
        "address", "street", "location",
        "name", "firstname", "lastname", "fullname",
        "dob", "date_of_birth", "birthdate"
    );

    /**
     * Detect PII in dataset
     */
    public Map<String, List<String>> detectPII(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, List<String>> piiByColumn = new HashMap<>();
        
        // Get column names from first row
        Set<String> columns = data.get(0).keySet();
        
        for (String column : columns) {
            List<String> piiTypes = new ArrayList<>();
            
            // Check column name
            if (isPIIColumnName(column)) {
                piiTypes.add("COLUMN_NAME_MATCH");
            }
            
            // Sample values from column
            List<String> sampleValues = getSampleValues(data, column, 100);
            
            // Check patterns
            for (Map.Entry<String, Pattern> entry : PII_PATTERNS.entrySet()) {
                if (hasPatternMatch(sampleValues, entry.getValue())) {
                    piiTypes.add(entry.getKey());
                }
            }
            
            if (!piiTypes.isEmpty()) {
                piiByColumn.put(column, piiTypes);
                log.info("PII detected in column '{}': {}", column, piiTypes);
            }
        }
        
        return piiByColumn;
    }

    /**
     * Check if column name suggests PII
     */
    private boolean isPIIColumnName(String columnName) {
        String normalized = columnName.toLowerCase().replaceAll("[_\\s-]", "");
        
        for (String piiName : PII_COLUMN_NAMES) {
            String normalizedPII = piiName.toLowerCase().replaceAll("[_\\s-]", "");
            if (normalized.contains(normalizedPII)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get sample values from column
     */
    private List<String> getSampleValues(List<Map<String, Object>> data, String column, int maxSamples) {
        List<String> samples = new ArrayList<>();
        int count = 0;
        
        for (Map<String, Object> row : data) {
            if (count >= maxSamples) break;
            
            Object value = row.get(column);
            if (value != null) {
                samples.add(value.toString());
                count++;
            }
        }
        
        return samples;
    }

    /**
     * Check if any value matches the pattern
     */
    private boolean hasPatternMatch(List<String> values, Pattern pattern) {
        int matchCount = 0;
        
        for (String value : values) {
            if (pattern.matcher(value).find()) {
                matchCount++;
            }
        }
        
        // Consider it PII if more than 10% of samples match
        return values.size() > 0 && (matchCount * 1.0 / values.size()) > 0.1;
    }

    /**
     * Generate PII recommendations
     */
    public List<String> generateRecommendations(Map<String, List<String>> piiByColumn) {
        List<String> recommendations = new ArrayList<>();
        
        if (piiByColumn.isEmpty()) {
            recommendations.add("No PII detected. Data appears safe for general use.");
            return recommendations;
        }
        
        recommendations.add("PII detected in " + piiByColumn.size() + " column(s). Consider the following:");
        recommendations.add("1. Encrypt or hash sensitive columns before storage");
        recommendations.add("2. Implement access controls and audit logging");
        recommendations.add("3. Consider anonymization or pseudonymization techniques");
        recommendations.add("4. Ensure compliance with GDPR, CCPA, or other privacy regulations");
        recommendations.add("5. Remove or mask PII if not necessary for analysis");
        
        return recommendations;
    }
}

