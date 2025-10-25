package com.aidataquality.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * PII detection findings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PIIFindings {
    
    private Boolean piiDetected;
    private Integer totalPIIColumns;
    private Map<String, List<String>> piiByColumn; // column name -> PII types
    private List<String> recommendations;
}

