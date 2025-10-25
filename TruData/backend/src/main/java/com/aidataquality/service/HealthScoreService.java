package com.aidataquality.service;

import com.aidataquality.model.dto.*;
import com.aidataquality.model.enums.QualityLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for computing overall health score and generating report
 */
@Service
@Slf4j
public class HealthScoreService {

    // Weights for different quality dimensions
    private static final double COMPLETENESS_WEIGHT = 0.25;
    private static final double UNIQUENESS_WEIGHT = 0.20;
    private static final double VALIDITY_WEIGHT = 0.20;
    private static final double CONSISTENCY_WEIGHT = 0.15;
    private static final double ACCURACY_WEIGHT = 0.15;
    private static final double TIMELINESS_WEIGHT = 0.05;

    /**
     * Compute overall health score
     */
    public double computeHealthScore(QualityMetrics metrics) {
        double healthScore = 
            metrics.getCompletenessScore() * COMPLETENESS_WEIGHT +
            metrics.getUniquenessScore() * UNIQUENESS_WEIGHT +
            metrics.getValidityScore() * VALIDITY_WEIGHT +
            metrics.getConsistencyScore() * CONSISTENCY_WEIGHT +
            metrics.getAccuracyScore() * ACCURACY_WEIGHT +
            metrics.getTimelinessScore() * TIMELINESS_WEIGHT;
        
        log.info("Computed overall health score: {}", healthScore);
        return Math.round(healthScore * 100.0) / 100.0;
    }

    /**
     * Determine quality level from health score
     */
    public QualityLevel determineQualityLevel(double healthScore) {
        if (healthScore >= 90) return QualityLevel.EXCELLENT;
        if (healthScore >= 75) return QualityLevel.GOOD;
        if (healthScore >= 60) return QualityLevel.FAIR;
        if (healthScore >= 40) return QualityLevel.POOR;
        return QualityLevel.CRITICAL;
    }

    /**
     * Generate data quality issues list
     */
    public List<DataQualityIssue> generateIssues(List<ColumnProfile> columnProfiles,
                                                  QualityMetrics metrics,
                                                  PIIFindings piiFindings) {
        List<DataQualityIssue> issues = new ArrayList<>();
        
        // Check completeness issues
        if (metrics.getNullPercentage() > 20) {
            issues.add(DataQualityIssue.builder()
                .issueType("COMPLETENESS")
                .severity(metrics.getNullPercentage() > 50 ? "HIGH" : "MEDIUM")
                .description(String.format("%.2f%% of cells are null", metrics.getNullPercentage()))
                .affectedRows(metrics.getNullCells())
                .recommendation("Consider imputation strategies or data collection improvements")
                .build());
        }
        
        // Check uniqueness issues
        if (metrics.getDuplicatePercentage() > 5) {
            issues.add(DataQualityIssue.builder()
                .issueType("DUPLICATES")
                .severity(metrics.getDuplicatePercentage() > 20 ? "HIGH" : "MEDIUM")
                .description(String.format("%.2f%% of rows are duplicates", metrics.getDuplicatePercentage()))
                .affectedRows(metrics.getDuplicateRows())
                .recommendation("Remove duplicate records or investigate data collection process")
                .build());
        }
        
        // Check column-specific issues
        for (ColumnProfile profile : columnProfiles) {
            if (profile.getQualityIssues() != null && !profile.getQualityIssues().isEmpty()) {
                for (String issue : profile.getQualityIssues()) {
                    issues.add(DataQualityIssue.builder()
                        .issueType("COLUMN_QUALITY")
                        .severity("MEDIUM")
                        .columnName(profile.getColumnName())
                        .description(issue)
                        .recommendation("Review and clean column data")
                        .build());
                }
            }
            
            // Check for outliers
            if (Boolean.TRUE.equals(profile.getHasOutliers())) {
                issues.add(DataQualityIssue.builder()
                    .issueType("OUTLIERS")
                    .severity("LOW")
                    .columnName(profile.getColumnName())
                    .description("Outliers detected in numeric column")
                    .recommendation("Review outliers to determine if they are errors or valid extreme values")
                    .build());
            }
        }
        
        // PII issues
        if (piiFindings != null && Boolean.TRUE.equals(piiFindings.getPiiDetected())) {
            issues.add(DataQualityIssue.builder()
                .issueType("PII_DETECTED")
                .severity("HIGH")
                .description(String.format("PII detected in %d column(s)", piiFindings.getTotalPIIColumns()))
                .recommendation("Implement data masking, encryption, or anonymization")
                .build());
        }
        
        return issues;
    }

    /**
     * Generate recommendations based on analysis
     */
    public List<String> generateRecommendations(double healthScore,
                                                 QualityMetrics metrics,
                                                 List<DataQualityIssue> issues) {
        List<String> recommendations = new ArrayList<>();
        
        QualityLevel level = determineQualityLevel(healthScore);
        
        recommendations.add(String.format("Overall Data Quality: %s (Score: %.2f/100)", level, healthScore));
        
        // General recommendations based on quality level
        switch (level) {
            case EXCELLENT -> recommendations.add("Your data quality is excellent! Continue monitoring for consistency.");
            case GOOD -> recommendations.add("Good data quality. Address minor issues to achieve excellence.");
            case FAIR -> recommendations.add("Fair data quality. Several improvements needed for production use.");
            case POOR -> recommendations.add("Poor data quality. Significant improvements required before using this data.");
            case CRITICAL -> recommendations.add("Critical data quality issues! Immediate action required.");
        }
        
        // Specific recommendations
        if (metrics.getCompletenessScore() < 80) {
            recommendations.add("Improve data completeness by addressing missing values");
        }
        
        if (metrics.getUniquenessScore() < 85) {
            recommendations.add("Remove or investigate duplicate records");
        }
        
        if (metrics.getValidityScore() < 85) {
            recommendations.add("Validate data against business rules and constraints");
        }
        
        if (!issues.isEmpty()) {
            recommendations.add(String.format("Address %d identified issues (see issues list for details)", issues.size()));
        }
        
        return recommendations;
    }

    /**
     * Generate duplicate analysis
     */
    public DuplicateAnalysis generateDuplicateAnalysis(List<Map<String, Object>> data,
                                                        List<ColumnProfile> columnProfiles) {
        Set<String> uniqueRows = new HashSet<>();
        List<Integer> duplicateRowIndices = new ArrayList<>();
        long totalDuplicates = 0;
        
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            String rowString = row.values().stream()
                .map(v -> v != null ? v.toString() : "null")
                .reduce("", (a, b) -> a + "|" + b);
            
            if (!uniqueRows.add(rowString)) {
                duplicateRowIndices.add(i);
                totalDuplicates++;
            }
        }
        
        double duplicatePercentage = data.size() > 0 
            ? (totalDuplicates * 100.0 / data.size()) : 0.0;
        
        // Analyze duplicates by column
        Map<String, Long> duplicatesByColumn = new HashMap<>();
        for (ColumnProfile profile : columnProfiles) {
            long nonNullCount = profile.getTotalCount() - profile.getNullCount();
            long duplicatesInColumn = nonNullCount - profile.getUniqueCount();
            if (duplicatesInColumn > 0) {
                duplicatesByColumn.put(profile.getColumnName(), duplicatesInColumn);
            }
        }
        
        return DuplicateAnalysis.builder()
            .totalDuplicates(totalDuplicates)
            .duplicatePercentage(duplicatePercentage)
            .duplicateRowIndices(duplicateRowIndices.size() > 100 
                ? duplicateRowIndices.subList(0, 100) 
                : duplicateRowIndices)
            .duplicatesByColumn(duplicatesByColumn)
            .hasExactDuplicates(totalDuplicates > 0)
            .hasFuzzyDuplicates(false) // Would require more sophisticated analysis
            .build();
    }
}

