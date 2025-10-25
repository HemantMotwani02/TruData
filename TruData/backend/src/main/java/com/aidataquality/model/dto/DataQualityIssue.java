package com.aidataquality.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a specific data quality issue found
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityIssue {
    
    private String issueType;
    private String severity;
    private String columnName;
    private String description;
    private Long affectedRows;
    private String recommendation;
}

