package com.aidataquality.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Quality metrics computed for the dataset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityMetrics {
    
    // Completeness metrics
    private Double completenessScore;
    private Long totalCells;
    private Long nullCells;
    private Double nullPercentage;
    
    // Uniqueness metrics
    private Double uniquenessScore;
    private Long totalRows;
    private Long duplicateRows;
    private Double duplicatePercentage;
    
    // Validity metrics
    private Double validityScore;
    private Long invalidValues;
    private Double invalidPercentage;
    
    // Consistency metrics
    private Double consistencyScore;
    private Long inconsistentValues;
    private Double inconsistentPercentage;
    
    // Accuracy metrics (based on schema validation)
    private Double accuracyScore;
    private Long schemaViolations;
    
    // Timeliness (for temporal data)
    private Double timelinessScore;
    private Boolean hasTemporalData;
    
    // Bias detection
    private Double biasScore;
    private Boolean biasDetected;
    private String biasDescription;
}

