package com.aidataquality.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Analysis of duplicate data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateAnalysis {
    
    private Long totalDuplicates;
    private Double duplicatePercentage;
    private List<Integer> duplicateRowIndices;
    private Map<String, Long> duplicatesByColumn; // column name -> duplicate count
    private Boolean hasExactDuplicates;
    private Boolean hasFuzzyDuplicates;
}

