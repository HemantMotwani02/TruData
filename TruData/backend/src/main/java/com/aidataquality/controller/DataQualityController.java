package com.aidataquality.controller;

import com.aidataquality.model.dto.DataQualityRequest;
import com.aidataquality.model.dto.DataQualityResponse;
import com.aidataquality.service.DataQualityOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for data quality analysis endpoints
 */
@RestController
@RequestMapping("/api/v1/data-quality")
@Tag(name = "Data Quality", description = "Data Quality Analysis API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DataQualityController {

    private final DataQualityOrchestrationService orchestrationService;

    @PostMapping(value = "/analyze/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analyze data quality from file upload",
               description = "Upload a file (CSV, JSON, XLSX, Parquet) and get comprehensive data quality analysis")
    public ResponseEntity<DataQualityResponse> analyzeFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dataType", required = false) String dataType,
            @RequestParam(value = "sensitivityLevel", required = false) String sensitivityLevel,
            @RequestParam(value = "performBiasCheck", required = false, defaultValue = "false") Boolean performBiasCheck,
            @RequestParam(value = "performPIICheck", required = false, defaultValue = "true") Boolean performPIICheck) {
        
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        DataQualityRequest request = DataQualityRequest.builder()
            .performBiasCheck(performBiasCheck)
            .performPIICheck(performPIICheck)
            .build();
        
        DataQualityResponse response = orchestrationService.analyzeFromFile(file, request);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/url")
    @Operation(summary = "Analyze data quality from URL",
               description = "Provide a URL to a data file and get comprehensive data quality analysis")
    public ResponseEntity<DataQualityResponse> analyzeFromUrl(
            @Valid @RequestBody DataQualityRequest request) {
        
        log.info("Received URL analysis request: {}", request.getDataUrl());
        
        if (request.getDataUrl() == null || request.getDataUrl().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        DataQualityResponse response = orchestrationService.analyzeFromUrl(
            request.getDataUrl(), 
            request
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/inline")
    @Operation(summary = "Analyze data quality from inline JSON",
               description = "Provide JSON data directly and get comprehensive data quality analysis")
    public ResponseEntity<DataQualityResponse> analyzeFromInline(
            @Valid @RequestBody DataQualityRequest request) {
        
        log.info("Received inline data analysis request");
        
        if (request.getInlineData() == null || request.getInlineData().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        DataQualityResponse response = orchestrationService.analyzeFromInlineData(
            request.getInlineData(), 
            request
        );
        
        return ResponseEntity.ok(response);
    }
}

