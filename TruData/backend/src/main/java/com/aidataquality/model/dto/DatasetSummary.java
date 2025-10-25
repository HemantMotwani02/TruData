package com.aidataquality.model.dto;

import com.aidataquality.model.enums.DataType;
import com.aidataquality.model.enums.FileFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary information about the dataset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetSummary {
    
    private FileFormat fileFormat;
    private DataType dataType;
    private Long rowCount;
    private Long columnCount;
    private Long totalCells;
    private Long fileSizeBytes;
    private String encoding;
    private Boolean hasHeader;
    private String[] columnNames;
}

