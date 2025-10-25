package com.aidataquality.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Profile information for a single column
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnProfile {
    
    private String columnName;
    private String dataType;
    private Long totalCount;
    private Long nullCount;
    private Long uniqueCount;
    private Double nullPercentage;
    private Double uniquePercentage;
    
    // Statistical metrics for numeric columns
    private Double mean;
    private Double median;
    private Double stdDev;
    private Double min;
    private Double max;
    private Double q1;
    private Double q3;
    
    // For categorical columns
    private Map<String, Long> valueCounts;
    private List<String> topValues;
    
    // Data quality indicators
    private Boolean hasPII;
    private List<String> piiTypes;
    private Boolean hasOutliers;
    private List<Object> outlierValues;
    private Boolean hasAnomalies;
    private List<String> qualityIssues;
}

