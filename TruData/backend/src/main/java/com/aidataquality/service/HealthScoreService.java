package com.aidataquality.service;

import com.aidataquality.model.dto.*;
import com.aidataquality.model.enums.QualityLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for computing overall health score and generating report
 * ENHANCED with penalty system and override rules for accurate scoring
 */
@Service
@Slf4j
public class HealthScoreService {

    // UPDATED WEIGHTS for different quality dimensions
    private static final double COMPLETENESS_WEIGHT = 0.25;  // 25%
    private static final double UNIQUENESS_WEIGHT = 0.15;    // 15% (reduced from 20%)
    private static final double VALIDITY_WEIGHT = 0.20;      // 20%
    private static final double CONSISTENCY_WEIGHT = 0.12;   // 12% (reduced from 15%)
    private static final double ACCURACY_WEIGHT = 0.13;      // 13% (reduced from 15%)
    private static final double TIMELINESS_WEIGHT = 0.05;    // 5%
    private static final double BIAS_WEIGHT = 0.10;          // 10% (NEW)

    /**
     * Compute overall health score with penalties and override rules
     */
    public double computeHealthScore(QualityMetrics metrics, PIIFindings piiFindings, Map<String, Object> biasReport) {
        // Step 1: Calculate base score with weighted dimensions
        double baseScore = 
            metrics.getCompletenessScore() * COMPLETENESS_WEIGHT +
            metrics.getUniquenessScore() * UNIQUENESS_WEIGHT +
            metrics.getValidityScore() * VALIDITY_WEIGHT +
            metrics.getConsistencyScore() * CONSISTENCY_WEIGHT +
            metrics.getAccuracyScore() * ACCURACY_WEIGHT +
            metrics.getTimelinessScore() * TIMELINESS_WEIGHT;
        
        // Add bias dimension
        double biasScore = metrics.getBiasScore() != null ? metrics.getBiasScore() : 95.0;
        baseScore += biasScore * BIAS_WEIGHT;
        
        log.debug("Base score before penalties: {}", baseScore);
        
        // Step 2: Apply penalties
        double biasPenalty = calculateBiasPenalty(biasReport);
        double piiPenalty = calculatePIIPenalty(piiFindings);
        
        double totalPenalty = biasPenalty + piiPenalty;
        double finalScore = Math.max(0, baseScore - totalPenalty);
        
        log.debug("Penalties - Bias: {}, PII: {}, Total: {}", biasPenalty, piiPenalty, totalPenalty);
        
        // Step 3: Apply catastrophic override rules
        finalScore = applyCatastrophicOverrides(finalScore, metrics);
        
        log.info("Final health score: {} (base: {}, penalties: {})", finalScore, baseScore, totalPenalty);
        return Math.round(finalScore * 100.0) / 100.0;
    }
    
    /**
     * Legacy method for backward compatibility (without penalties)
     */
    public double computeHealthScore(QualityMetrics metrics) {
        return computeHealthScore(metrics, null, null);
    }
    
    /**
     * Calculate bias penalty (0-15 points)
     */
    private double calculateBiasPenalty(Map<String, Object> biasReport) {
        if (biasReport == null || !Boolean.TRUE.equals(biasReport.get("biasDetected"))) {
            return 0.0;
        }
        
        // Extract dominance percentage
        Double dominancePercentage = (Double) biasReport.get("dominancePercentage");
        if (dominancePercentage == null) {
            return 5.0; // Default penalty if bias detected but no percentage
        }
        
        // Gradual penalty based on dominance
        if (dominancePercentage < 60) {
            return 0.0;
        } else if (dominancePercentage >= 90) {
            return 15.0;  // Maximum penalty
        } else {
            // Linear scaling between 60% and 90%
            return (dominancePercentage - 60) * 0.5;  // 0-15 range
        }
    }
    
    /**
     * Calculate PII penalty (0-15 points)
     */
    private double calculatePIIPenalty(PIIFindings piiFindings) {
        if (piiFindings == null || !Boolean.TRUE.equals(piiFindings.getPiiDetected())) {
            return 0.0;
        }
        
        int piiColumnCount = piiFindings.getTotalPIIColumns();
        
        if (piiColumnCount == 0) {
            return 0.0;
        } else if (piiColumnCount == 1) {
            return 3.0;
        } else if (piiColumnCount == 2) {
            return 7.0;
        } else {
            // 3+ columns: 7 + (n-2) * 3, max 15
            return Math.min(15.0, 7.0 + (piiColumnCount - 2) * 3.0);
        }
    }
    
    /**
     * Apply catastrophic override rules
     */
    private double applyCatastrophicOverrides(double currentScore, QualityMetrics metrics) {
        // Rule 1: Less than 5 rows = score of 5
        if (metrics.getTotalRows() < 5) {
            log.warn("CATASTROPHIC: Less than 5 rows detected. Overriding score to 5.");
            return Math.min(currentScore, 5.0);
        }
        
        // Rule 2: More than 90% nulls = score of 10
        if (metrics.getNullPercentage() > 90) {
            log.warn("CATASTROPHIC: >90% nulls detected. Overriding score to 10.");
            return Math.min(currentScore, 10.0);
        }
        
        // Rule 3: 80%+ duplicates = score of 20
        if (metrics.getDuplicatePercentage() >= 80) {
            log.warn("CATASTROPHIC: ≥80% duplicates detected. Overriding score to 20.");
            return Math.min(currentScore, 20.0);
        }
        
        // Rule 4: More than 80% invalid values = score of 15
        if (metrics.getInvalidPercentage() > 80) {
            log.warn("CATASTROPHIC: >80% invalid values detected. Overriding score to 15.");
            return Math.min(currentScore, 15.0);
        }
        
        return currentScore;
    }

    /**
     * Determine quality level from health score (UPDATED thresholds)
     */
    public QualityLevel determineQualityLevel(double healthScore) {
        if (healthScore >= 85) return QualityLevel.EXCELLENT;  // Was 90
        if (healthScore >= 70) return QualityLevel.GOOD;       // Was 75
        if (healthScore >= 50) return QualityLevel.FAIR;       // Was 60
        if (healthScore >= 30) return QualityLevel.POOR;       // Was 40
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

