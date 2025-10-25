package com.aidataquality.service;

import com.aidataquality.model.dto.ColumnProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FIXED VERSION - Service for profiling data columns
 * 
 * FIXES:
 * 1. Better date format detection (supports DD-MM-YYYY, DD.MM.YYYY, etc.)
 * 2. Handles empty data gracefully
 * 3. Better outlier detection with context
 */
@Service
@Slf4j
public class DataProfilingService {

    /**
     * Profile all columns in the dataset
     */
    public List<ColumnProfile> profileData(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            log.warn("Data is empty, returning empty profile list");
            return Collections.emptyList();
        }
        
        log.info("Profiling dataset with {} rows", data.size());
        
        List<ColumnProfile> profiles = new ArrayList<>();
        Set<String> columns = data.get(0).keySet();
        
        if (columns.isEmpty()) {
            log.warn("No columns found in data");
            return Collections.emptyList();
        }
        
        for (String column : columns) {
            try {
                ColumnProfile profile = profileColumn(data, column);
                profiles.add(profile);
            } catch (Exception e) {
                log.error("Error profiling column '{}': {}", column, e.getMessage(), e);
                // Continue with other columns
            }
        }
        
        return profiles;
    }

    /**
     * Profile a single column
     */
    private ColumnProfile profileColumn(List<Map<String, Object>> data, String columnName) {
        ColumnProfile.ColumnProfileBuilder builder = ColumnProfile.builder();
        builder.columnName(columnName);
        
        // Collect values
        List<Object> values = data.stream()
            .map(row -> row.get(columnName))
            .collect(Collectors.toList());
        
        long totalCount = values.size();
        long nullCount = values.stream().filter(Objects::isNull).count();
        long nonNullCount = totalCount - nullCount;
        
        // Get unique values
        Set<Object> uniqueValues = values.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        long uniqueCount = uniqueValues.size();
        
        builder.totalCount(totalCount)
               .nullCount(nullCount)
               .uniqueCount(uniqueCount)
               .nullPercentage(totalCount > 0 ? (nullCount * 100.0 / totalCount) : 0.0)
               .uniquePercentage(nonNullCount > 0 ? (uniqueCount * 100.0 / nonNullCount) : 0.0);
        
        // Determine data type and compute statistics
        String dataType = inferDataType(values);
        builder.dataType(dataType);
        
        if ("NUMERIC".equals(dataType)) {
            computeNumericStatistics(values, builder, columnName);
        } else {
            computeCategoricalStatistics(values, builder);
        }
        
        // Detect outliers and quality issues
        List<String> qualityIssues = detectQualityIssues(values, dataType, nullCount, totalCount, uniqueCount);
        builder.qualityIssues(qualityIssues);
        
        return builder.build();
    }

    /**
     * ✅ IMPROVED: Infer data type of column with better date detection
     */
    private String inferDataType(List<Object> values) {
        long numericCount = 0;
        long dateCount = 0;
        long nonNullCount = 0;
        
        for (Object value : values) {
            if (value == null) continue;
            
            nonNullCount++;
            
            if (isNumeric(value)) {
                numericCount++;
            }
            
            if (isDate(value.toString())) {
                dateCount++;
            }
        }
        
        if (nonNullCount == 0) return "UNKNOWN";
        
        double numericRatio = numericCount * 1.0 / nonNullCount;
        double dateRatio = dateCount * 1.0 / nonNullCount;
        
        if (numericRatio > 0.8) return "NUMERIC";
        if (dateRatio > 0.8) return "DATE";
        
        return "CATEGORICAL";
    }

    /**
     * Check if value is numeric
     */
    private boolean isNumeric(Object value) {
        if (value instanceof Number) return true;
        
        try {
            Double.parseDouble(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * ✅ IMPROVED: Check if value looks like a date - supports more formats
     */
    private boolean isDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        // Common date patterns
        return value.matches("\\d{4}-\\d{2}-\\d{2}.*") ||       // YYYY-MM-DD (ISO)
               value.matches("\\d{2}/\\d{2}/\\d{4}.*") ||       // MM/DD/YYYY (US)
               value.matches("\\d{2}-\\d{2}-\\d{4}.*") ||       // DD-MM-YYYY (EU)
               value.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*") ||   // DD.MM.YYYY (DE)
               value.matches("\\d{4}/\\d{2}/\\d{2}.*") ||       // YYYY/MM/DD
               value.matches("\\d{1,2}\\s+\\w+\\s+\\d{4}") ||   // 15 January 2020
               value.matches("\\w+\\s+\\d{1,2},?\\s+\\d{4}");   // January 15, 2020
    }

    /**
     * ✅ IMPROVED: Compute numeric statistics with better outlier detection
     */
    private void computeNumericStatistics(List<Object> values, 
                                          ColumnProfile.ColumnProfileBuilder builder,
                                          String columnName) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        List<Double> numericValues = new ArrayList<>();
        
        for (Object value : values) {
            if (value == null) continue;
            
            try {
                double numValue;
                if (value instanceof Number) {
                    numValue = ((Number) value).doubleValue();
                } else {
                    numValue = Double.parseDouble(value.toString());
                }
                stats.addValue(numValue);
                numericValues.add(numValue);
            } catch (NumberFormatException e) {
                // Skip non-numeric values
            }
        }
        
        if (stats.getN() > 0) {
            builder.mean(stats.getMean())
                   .median(stats.getPercentile(50))
                   .stdDev(stats.getStandardDeviation())
                   .min(stats.getMin())
                   .max(stats.getMax())
                   .q1(stats.getPercentile(25))
                   .q3(stats.getPercentile(75));
            
            // Detect outliers using IQR method
            double q1 = stats.getPercentile(25);
            double q3 = stats.getPercentile(75);
            double iqr = q3 - q1;
            double lowerBound = q1 - 1.5 * iqr;
            double upperBound = q3 + 1.5 * iqr;
            
            // ✅ IMPROVED: Also flag negative values for inherently positive columns
            List<Object> outliers = new ArrayList<>();
            for (Double value : numericValues) {
                boolean isOutlier = value < lowerBound || value > upperBound;
                
                // Check for invalid values based on column name
                boolean isInvalid = isInvalidValueForColumn(columnName, value);
                
                if (isOutlier || isInvalid) {
                    outliers.add(value);
                }
            }
            
            // Get unique outliers, limited to 10
            List<Object> uniqueOutliers = outliers.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
            
            if (!uniqueOutliers.isEmpty()) {
                builder.hasOutliers(true)
                       .outlierValues(uniqueOutliers);
            }
        }
    }

    /**
     * ✅ NEW: Check if value is invalid for specific column types
     */
    private boolean isInvalidValueForColumn(String columnName, double value) {
        String lowerColName = columnName.toLowerCase();
        
        // Age should be positive and reasonable
        if (lowerColName.contains("age")) {
            return value < 0 || value > 150;
        }
        
        // Prices, salaries, costs should be positive
        if (lowerColName.contains("price") || 
            lowerColName.contains("salary") ||
            lowerColName.contains("cost") ||
            lowerColName.contains("amount")) {
            return value < 0;
        }
        
        // Counts and quantities should be non-negative
        if (lowerColName.contains("count") || 
            lowerColName.contains("quantity") ||
            lowerColName.contains("number")) {
            return value < 0;
        }
        
        // Percentages should be 0-100
        if (lowerColName.contains("percent") || 
            lowerColName.contains("rate") ||
            lowerColName.endsWith("%")) {
            return value < 0 || value > 100;
        }
        
        return false;
    }

    /**
     * Compute categorical statistics
     */
    private void computeCategoricalStatistics(List<Object> values, ColumnProfile.ColumnProfileBuilder builder) {
        Map<String, Long> valueCounts = values.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                Object::toString,
                Collectors.counting()
            ));
        
        // Get top 10 values
        List<String> topValues = valueCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        builder.valueCounts(valueCounts.entrySet().stream()
                .limit(20)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
               .topValues(topValues);
    }

    /**
     * ✅ IMPROVED: Detect quality issues in column
     */
    private List<String> detectQualityIssues(List<Object> values, String dataType, 
                                              long nullCount, long totalCount, long uniqueCount) {
        List<String> issues = new ArrayList<>();
        
        double nullPercentage = totalCount > 0 ? (nullCount * 100.0 / totalCount) : 0.0;
        
        // Check null percentage
        if (nullPercentage > 50) {
            issues.add(String.format("High null percentage: %.2f%% (more than half the data is missing)", nullPercentage));
        } else if (nullPercentage > 20) {
            issues.add(String.format("Moderate null percentage: %.2f%% (significant missing data)", nullPercentage));
        } else if (nullPercentage > 10) {
            issues.add(String.format("Low null percentage: %.2f%% (some missing data)", nullPercentage));
        }
        
        // Check for constant columns
        if (uniqueCount == 1 && nullCount == 0) {
            issues.add("Column has only one unique value (constant column - may not be useful)");
        }
        
        // Check for potential identifier columns
        long nonNullCount = totalCount - nullCount;
        if (uniqueCount == nonNullCount && totalCount > 10) {
            issues.add("All non-null values are unique (possibly an identifier or key column)");
        }
        
        // ✅ NEW: Check for very low uniqueness in categorical data
        if ("CATEGORICAL".equals(dataType) && nonNullCount > 10) {
            double uniqueRatio = uniqueCount * 1.0 / nonNullCount;
            if (uniqueRatio < 0.1) {
                issues.add(String.format("Very low diversity: only %d unique values for %d rows (%.1f%%)", 
                    uniqueCount, nonNullCount, uniqueRatio * 100));
            }
        }
        
        return issues;
    }
}

