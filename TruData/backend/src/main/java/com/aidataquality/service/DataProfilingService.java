package com.aidataquality.service;

import com.aidataquality.model.dto.ColumnProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for profiling data columns
 */
@Service
@Slf4j
public class DataProfilingService {

    /**
     * Profile all columns in the dataset
     */
    public List<ColumnProfile> profileData(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        
        log.info("Profiling dataset with {} rows", data.size());
        
        List<ColumnProfile> profiles = new ArrayList<>();
        Set<String> columns = data.get(0).keySet();
        
        for (String column : columns) {
            ColumnProfile profile = profileColumn(data, column);
            profiles.add(profile);
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
            computeNumericStatistics(values, builder);
        } else {
            computeCategoricalStatistics(values, builder);
        }
        
        // Detect outliers and quality issues
        List<String> qualityIssues = detectQualityIssues(values, dataType, nullCount, totalCount, uniqueCount);
        builder.qualityIssues(qualityIssues);
        
        return builder.build();
    }

    /**
     * Infer data type of column
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
     * Check if value looks like a date
     */
    private boolean isDate(String value) {
        // FIXED: Added support for DD-MM-YYYY and DD.MM.YYYY formats
        return value.matches("\\d{4}-\\d{2}-\\d{2}.*") ||      // YYYY-MM-DD
               value.matches("\\d{2}/\\d{2}/\\d{4}.*") ||       // MM/DD/YYYY or DD/MM/YYYY
               value.matches("\\d{2}-\\d{2}-\\d{4}.*") ||       // DD-MM-YYYY
               value.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*");     // DD.MM.YYYY
    }

    /**
     * Compute numeric statistics
     */
    private void computeNumericStatistics(List<Object> values, ColumnProfile.ColumnProfileBuilder builder) {
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
            
            List<Object> outliers = numericValues.stream()
                .filter(v -> v < lowerBound || v > upperBound)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
            
            if (!outliers.isEmpty()) {
                builder.hasOutliers(true)
                       .outlierValues(outliers);
            }
        }
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
     * Detect quality issues in column
     */
    private List<String> detectQualityIssues(List<Object> values, String dataType, 
                                              long nullCount, long totalCount, long uniqueCount) {
        List<String> issues = new ArrayList<>();
        
        double nullPercentage = totalCount > 0 ? (nullCount * 100.0 / totalCount) : 0.0;
        
        if (nullPercentage > 50) {
            issues.add("High null percentage: " + String.format("%.2f%%", nullPercentage));
        } else if (nullPercentage > 20) {
            issues.add("Moderate null percentage: " + String.format("%.2f%%", nullPercentage));
        }
        
        if (uniqueCount == 1 && nullCount == 0) {
            issues.add("Column has only one unique value (constant column)");
        }
        
        if (uniqueCount == totalCount - nullCount && totalCount > 10) {
            issues.add("All values are unique (possibly an identifier)");
        }
        
        // FIXED: Added value validation for NUMERIC columns
        if ("NUMERIC".equals(dataType)) {
            long invalidNumericCount = 0;
            for (Object value : values) {
                if (value == null) continue;
                
                try {
                    double numValue = value instanceof Number 
                        ? ((Number) value).doubleValue() 
                        : Double.parseDouble(value.toString());
                    
                    // Check for unrealistic values
                    if (Double.isInfinite(numValue) || Double.isNaN(numValue)) {
                        invalidNumericCount++;
                    }
                } catch (NumberFormatException e) {
                    invalidNumericCount++;
                }
            }
            
            if (invalidNumericCount > 0) {
                issues.add(String.format("Contains %d invalid numeric values", invalidNumericCount));
            }
        }
        
        return issues;
    }
    
    /**
     * Check if numeric value is negative (for validation)
     */
    private boolean isNegativeNumeric(Object value) {
        if (value == null) return false;
        
        try {
            double numValue = value instanceof Number 
                ? ((Number) value).doubleValue() 
                : Double.parseDouble(value.toString());
            return numValue < 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Check if value is a valid email format
     */
    private boolean isValidEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        // Basic email regex pattern
        return value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

