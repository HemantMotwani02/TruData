package com.aidataquality.model.dto;

import com.aidataquality.model.enums.DataType;
import com.aidataquality.model.enums.SensitivityLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request object for data quality analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityRequest {
    
    @JsonProperty("dataType")
    private DataType dataType = DataType.TABULAR;
    
    @JsonProperty("schemaDefinition")
    private Map<String, String> schemaDefinition;
    
    @JsonProperty("sensitivityLevel")
    private SensitivityLevel sensitivityLevel = SensitivityLevel.INTERNAL;
    
    @JsonProperty("performBiasCheck")
    private Boolean performBiasCheck = false;
    
    @JsonProperty("performPIICheck")
    private Boolean performPIICheck = true;
    
    @JsonProperty("customRules")
    private Map<String, Object> customRules;
    
    @JsonProperty("inlineData")
    private String inlineData;
    
    @JsonProperty("dataUrl")
    private String dataUrl;
}

