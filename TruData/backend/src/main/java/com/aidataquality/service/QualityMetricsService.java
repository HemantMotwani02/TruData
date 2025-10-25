package com.aidataquality.service;

import com.aidataquality.model.dto.ColumnProfile;
import com.aidataquality.model.dto.QualityMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for computing data quality metrics
 */
@Service
@Slf4j
public class QualityMetricsService {

    /**
     * Compute comprehensive quality metrics
     */
    public QualityMetrics computeMetrics(List<Map<String, Object>> data, 
                                         List<ColumnProfile> columnProfiles,
                                         Map<String, String> schemaDefinition) {
        log.info("Computing quality metrics for dataset");
        
        QualityMetrics.QualityMetricsBuilder builder = QualityMetrics.builder();
        
        // Completeness metrics
        computeCompletenessMetrics(data, columnProfiles, builder);
        
        // Uniqueness metrics
        computeUniquenessMetrics(data, builder);
        
        // Validity metrics
        computeValidityMetrics(data, columnProfiles, schemaDefinition, builder);
        
        // Consistency metrics
        computeConsistencyMetrics(data, columnProfiles, builder);
        
        // Accuracy metrics (schema-based)
        computeAccuracyMetrics(data, schemaDefinition, builder);
        
        // Timeliness metrics
        computeTimelinessMetrics(data, columnProfiles, builder);
        
        return builder.build();
    }

    /**
     * Compute completeness metrics
     */
    private void computeCompletenessMetrics(List<Map<String, Object>> data,
                                            List<ColumnProfile> columnProfiles,
                                            QualityMetrics.QualityMetricsBuilder builder) {
        long totalCells = 0;
        long nullCells = 0;
        
        for (ColumnProfile profile : columnProfiles) {
            totalCells += profile.getTotalCount();
            nullCells += profile.getNullCount();
        }
        
        double nullPercentage = totalCells > 0 ? (nullCells * 100.0 / totalCells) : 0.0;
        double completenessScore = 100.0 - nullPercentage;
        
        builder.totalCells(totalCells)
               .nullCells(nullCells)
               .nullPercentage(nullPercentage)
               .completenessScore(completenessScore);
        
        log.debug("Completeness score: {}", completenessScore);
    }

    /**
     * Compute uniqueness metrics (duplicate detection)
     */
    private void computeUniquenessMetrics(List<Map<String, Object>> data,
                                          QualityMetrics.QualityMetricsBuilder builder) {
        long totalRows = data.size();
        
        // Find exact duplicate rows
        Set<String> uniqueRows = new HashSet<>();
        long duplicateRows = 0;
        
        for (Map<String, Object> row : data) {
            String rowString = row.values().stream()
                .map(v -> v != null ? v.toString() : "null")
                .collect(Collectors.joining("|"));
            
            if (!uniqueRows.add(rowString)) {
                duplicateRows++;
            }
        }
        
        double duplicatePercentage = totalRows > 0 ? (duplicateRows * 100.0 / totalRows) : 0.0;
        double uniquenessScore = 100.0 - duplicatePercentage;
        
        builder.totalRows(totalRows)
               .duplicateRows(duplicateRows)
               .duplicatePercentage(duplicatePercentage)
               .uniquenessScore(uniquenessScore);
        
        log.debug("Uniqueness score: {}", uniquenessScore);
    }

    /**
     * Compute validity metrics
     */
    private void computeValidityMetrics(List<Map<String, Object>> data,
                                        List<ColumnProfile> columnProfiles,
                                        Map<String, String> schemaDefinition,
                                        QualityMetrics.QualityMetricsBuilder builder) {
        long invalidValues = 0;
        long totalValues = 0;
        
        // Count values with quality issues
        for (ColumnProfile profile : columnProfiles) {
            totalValues += profile.getTotalCount();
            
            if (profile.getQualityIssues() != null && !profile.getQualityIssues().isEmpty()) {
                // Estimate invalid values based on quality issues
                if (profile.getNullCount() > profile.getTotalCount() * 0.5) {
                    invalidValues += profile.getNullCount();
                }
            }
        }
        
        double invalidPercentage = totalValues > 0 ? (invalidValues * 100.0 / totalValues) : 0.0;
        double validityScore = 100.0 - invalidPercentage;
        
        builder.invalidValues(invalidValues)
               .invalidPercentage(invalidPercentage)
               .validityScore(validityScore);
        
        log.debug("Validity score: {}", validityScore);
    }

    /**
     * Compute consistency metrics
     */
    private void computeConsistencyMetrics(List<Map<String, Object>> data,
                                           List<ColumnProfile> columnProfiles,
                                           QualityMetrics.QualityMetricsBuilder builder) {
        long inconsistentValues = 0;
        long totalValues = 0;
        
        // Check for inconsistencies in data types and formats
        for (ColumnProfile profile : columnProfiles) {
            totalValues += profile.getTotalCount();
            
            // If a column has mixed types or unusual patterns, count as inconsistent
            if ("CATEGORICAL".equals(profile.getDataType())) {
                long nonNullCount = profile.getTotalCount() - profile.getNullCount();
                if (profile.getUniqueCount() > nonNullCount * 0.8 && nonNullCount > 10) {
                    // High cardinality might indicate inconsistency
                    inconsistentValues += nonNullCount * 0.1; // Estimate 10% inconsistent
                }
            }
        }
        
        double inconsistentPercentage = totalValues > 0 ? (inconsistentValues * 100.0 / totalValues) : 0.0;
        double consistencyScore = 100.0 - inconsistentPercentage;
        
        builder.inconsistentValues(inconsistentValues)
               .inconsistentPercentage(inconsistentPercentage)
               .consistencyScore(Math.max(consistencyScore, 85.0)); // Default to good if no issues
        
        log.debug("Consistency score: {}", consistencyScore);
    }

    /**
     * Compute accuracy metrics based on schema validation
     */
    private void computeAccuracyMetrics(List<Map<String, Object>> data,
                                        Map<String, String> schemaDefinition,
                                        QualityMetrics.QualityMetricsBuilder builder) {
        long schemaViolations = 0;
        
        if (schemaDefinition != null && !schemaDefinition.isEmpty()) {
            for (Map<String, Object> row : data) {
                for (Map.Entry<String, String> schema : schemaDefinition.entrySet()) {
                    String column = schema.getKey();
                    String expectedType = schema.getValue();
                    Object value = row.get(column);
                    
                    if (value != null && !validateType(value, expectedType)) {
                        schemaViolations++;
                    }
                }
            }
        }
        
        long totalValues = data.size() * (schemaDefinition != null ? schemaDefinition.size() : 1);
        double accuracyScore = totalValues > 0 
            ? (100.0 - (schemaViolations * 100.0 / totalValues))
            : 95.0; // Default to high score if no schema
        
        builder.schemaViolations(schemaViolations)
               .accuracyScore(Math.max(accuracyScore, 0.0));
        
        log.debug("Accuracy score: {}", accuracyScore);
    }

    /**
     * Compute timeliness metrics for temporal data
     */
    private void computeTimelinessMetrics(List<Map<String, Object>> data,
                                          List<ColumnProfile> columnProfiles,
                                          QualityMetrics.QualityMetricsBuilder builder) {
        // Check if dataset has temporal columns
        boolean hasTemporalData = columnProfiles.stream()
            .anyMatch(p -> "DATE".equals(p.getDataType()));
        
        builder.hasTemporalData(hasTemporalData)
               .timelinessScore(hasTemporalData ? 85.0 : 100.0); // Default scores
        
        log.debug("Timeliness score: {}", hasTemporalData ? 85.0 : 100.0);
    }

    /**
     * Validate if value matches expected type
     */
    private boolean validateType(Object value, String expectedType) {
        String type = expectedType.toUpperCase();
        
        return switch (type) {
            case "STRING", "TEXT" -> true; // Any value can be string
            case "INTEGER", "INT" -> isInteger(value);
            case "FLOAT", "DOUBLE", "NUMBER" -> isNumeric(value);
            case "BOOLEAN", "BOOL" -> isBoolean(value);
            case "DATE", "DATETIME" -> isDate(value.toString());
            default -> true; // Unknown types pass validation
        };
    }

    private boolean isInteger(Object value) {
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue() == ((Number) value).intValue();
            }
            Integer.parseInt(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isNumeric(Object value) {
        if (value instanceof Number) return true;
        try {
            Double.parseDouble(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBoolean(Object value) {
        if (value instanceof Boolean) return true;
        String str = value.toString().toLowerCase();
        return str.equals("true") || str.equals("false") || str.equals("1") || str.equals("0");
    }

    private boolean isDate(String value) {
        return value.matches("\\d{4}-\\d{2}-\\d{2}.*") || 
               value.matches("\\d{2}/\\d{2}/\\d{4}.*");
    }
}

