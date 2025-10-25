package com.aidataquality.service;

import com.aidataquality.model.dto.*;
import com.aidataquality.model.enums.DataType;
import com.aidataquality.model.enums.FileFormat;
import com.aidataquality.model.enums.QualityLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main orchestration service that coordinates the entire data quality analysis workflow
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataQualityOrchestrationService {

    private final DataIngestionService dataIngestionService;
    private final DataProfilingService dataProfilingService;
    private final QualityMetricsService qualityMetricsService;
    private final HealthScoreService healthScoreService;
    private final PIIDetectionService piiDetectionService;
    private final BiasDetectionService biasDetectionService;

    /**
     * Analyze data quality from file upload
     */
    public DataQualityResponse analyzeFromFile(MultipartFile file, DataQualityRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Starting data quality analysis for file: {}", file.getOriginalFilename());
        
        // Step 1: Data Ingestion
        List<Map<String, Object>> data = dataIngestionService.ingestFromFile(file);
        
        // Step 2-4: Process and analyze
        DataQualityResponse response = processAndAnalyze(data, request, file.getOriginalFilename());
        
        long processingTime = System.currentTimeMillis() - startTime;
        response.setProcessingTimeMs(processingTime);
        
        log.info("Analysis complete. Health Score: {}, Processing Time: {}ms", 
                 response.getHealthScore(), processingTime);
        
        return response;
    }

    /**
     * Analyze data quality from URL
     */
    public DataQualityResponse analyzeFromUrl(String url, DataQualityRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Starting data quality analysis from URL: {}", url);
        
        // Step 1: Data Ingestion
        List<Map<String, Object>> data = dataIngestionService.ingestFromUrl(url);
        
        // Step 2-4: Process and analyze
        DataQualityResponse response = processAndAnalyze(data, request, url);
        
        long processingTime = System.currentTimeMillis() - startTime;
        response.setProcessingTimeMs(processingTime);
        
        log.info("Analysis complete. Health Score: {}", response.getHealthScore());
        
        return response;
    }

    /**
     * Analyze data quality from inline JSON
     */
    public DataQualityResponse analyzeFromInlineData(String jsonData, DataQualityRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Starting data quality analysis from inline data");
        
        // Step 1: Data Ingestion
        List<Map<String, Object>> data = dataIngestionService.ingestFromInlineData(jsonData);
        
        // Step 2-4: Process and analyze
        DataQualityResponse response = processAndAnalyze(data, request, "inline-data");
        
        long processingTime = System.currentTimeMillis() - startTime;
        response.setProcessingTimeMs(processingTime);
        
        log.info("Analysis complete. Health Score: {}", response.getHealthScore());
        
        return response;
    }

    /**
     * Core processing and analysis workflow
     */
    private DataQualityResponse processAndAnalyze(List<Map<String, Object>> data, 
                                                   DataQualityRequest request,
                                                   String sourceName) {
        // Generate analysis ID
        String analysisId = UUID.randomUUID().toString();
        
        // Step 2: Data Profiling
        log.info("Step 1/4: Profiling data...");
        List<ColumnProfile> columnProfiles = dataProfilingService.profileData(data);
        
        // Step 3: Quality Metrics Computation
        log.info("Step 2/4: Computing quality metrics...");
        QualityMetrics qualityMetrics = qualityMetricsService.computeMetrics(
            data, 
            columnProfiles, 
            request.getSchemaDefinition()
        );
        
        // PII Detection
        PIIFindings piiFindings = null;
        if (Boolean.TRUE.equals(request.getPerformPIICheck())) {
            log.info("Step 3/4: Detecting PII...");
            Map<String, List<String>> piiByColumn = piiDetectionService.detectPII(data);
            
            // Update column profiles with PII information
            for (ColumnProfile profile : columnProfiles) {
                if (piiByColumn.containsKey(profile.getColumnName())) {
                    profile.setHasPII(true);
                    profile.setPiiTypes(piiByColumn.get(profile.getColumnName()));
                }
            }
            
            piiFindings = PIIFindings.builder()
                .piiDetected(!piiByColumn.isEmpty())
                .totalPIIColumns(piiByColumn.size())
                .piiByColumn(piiByColumn)
                .recommendations(piiDetectionService.generateRecommendations(piiByColumn))
                .build();
        }
        
        // Bias Detection
        if (Boolean.TRUE.equals(request.getPerformBiasCheck())) {
            log.info("Detecting bias...");
            Map<String, Object> biasReport = biasDetectionService.detectBias(data);
            
            qualityMetrics.setBiasDetected((Boolean) biasReport.get("biasDetected"));
            qualityMetrics.setBiasDescription((String) biasReport.get("description"));
            qualityMetrics.setBiasScore(
                Boolean.TRUE.equals(biasReport.get("biasDetected")) ? 60.0 : 95.0
            );
        }
        
        // Step 4: Health Score Aggregation
        log.info("Step 4/4: Computing health score...");
        double healthScore = healthScoreService.computeHealthScore(qualityMetrics);
        QualityLevel qualityLevel = healthScoreService.determineQualityLevel(healthScore);
        
        // Generate issues and recommendations
        List<DataQualityIssue> issues = healthScoreService.generateIssues(
            columnProfiles, qualityMetrics, piiFindings
        );
        
        List<String> recommendations = healthScoreService.generateRecommendations(
            healthScore, qualityMetrics, issues
        );
        
        // Generate duplicate analysis
        DuplicateAnalysis duplicateAnalysis = healthScoreService.generateDuplicateAnalysis(
            data, columnProfiles
        );
        
        // Build dataset summary
        DatasetSummary summary = DatasetSummary.builder()
            .fileFormat(detectFileFormat(sourceName))
            .dataType(request.getDataType() != null ? request.getDataType() : DataType.TABULAR)
            .rowCount((long) data.size())
            .columnCount((long) (data.isEmpty() ? 0 : data.get(0).size()))
            .totalCells((long) data.size() * (data.isEmpty() ? 0 : data.get(0).size()))
            .hasHeader(true)
            .columnNames(data.isEmpty() ? new String[0] : data.get(0).keySet().toArray(new String[0]))
            .build();
        
        // Build final response
        return DataQualityResponse.builder()
            .analysisId(analysisId)
            .timestamp(LocalDateTime.now())
            .healthScore(healthScore)
            .qualityLevel(qualityLevel)
            .summary(summary)
            .qualityMetrics(qualityMetrics)
            .columnProfiles(columnProfiles)
            .issues(issues)
            .recommendations(recommendations)
            .piiFindings(piiFindings)
            .duplicateAnalysis(duplicateAnalysis)
            .build();
    }

    /**
     * Detect file format from source name
     */
    private FileFormat detectFileFormat(String sourceName) {
        if (sourceName == null || !sourceName.contains(".")) {
            return FileFormat.JSON;
        }
        
        String extension = sourceName.substring(sourceName.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (extension) {
            case "csv" -> FileFormat.CSV;
            case "json" -> FileFormat.JSON;
            case "xlsx", "xls" -> FileFormat.XLSX;
            case "parquet" -> FileFormat.PARQUET;
            default -> FileFormat.UNKNOWN;
        };
    }
}

