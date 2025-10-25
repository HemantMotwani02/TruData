package com.aidataquality.model.dto;

import com.aidataquality.model.enums.QualityLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Complete response object for data quality analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityResponse {
    
    @JsonProperty("analysisId")
    private String analysisId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("healthScore")
    private Double healthScore;
    
    @JsonProperty("qualityLevel")
    private QualityLevel qualityLevel;
    
    @JsonProperty("summary")
    private DatasetSummary summary;
    
    @JsonProperty("qualityMetrics")
    private QualityMetrics qualityMetrics;
    
    @JsonProperty("columnProfiles")
    private List<ColumnProfile> columnProfiles;
    
    @JsonProperty("issues")
    private List<DataQualityIssue> issues;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
    
    @JsonProperty("piiFindings")
    private PIIFindings piiFindings;
    
    @JsonProperty("duplicateAnalysis")
    private DuplicateAnalysis duplicateAnalysis;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
}

