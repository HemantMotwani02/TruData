package com.aidataquality.service;

import com.aidataquality.exception.DataQualityException;
import com.aidataquality.exception.UnsupportedFileFormatException;
import com.aidataquality.model.enums.FileFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for ingesting data from various sources and formats
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataIngestionService {

    private final ObjectMapper objectMapper;

    /**
     * Ingest data from a file upload
     */
    public List<Map<String, Object>> ingestFromFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            FileFormat format = detectFileFormat(filename);
            
            log.info("Ingesting file: {} with format: {}", filename, format);
            
            return switch (format) {
                case CSV -> ingestCSV(file.getInputStream());
                case JSON -> ingestJSON(file.getInputStream());
                case XLSX -> ingestXLSX(file.getInputStream());
                case PARQUET -> ingestParquet(file.getInputStream());
                default -> throw new UnsupportedFileFormatException("Unsupported file format: " + format);
            };
        } catch (IOException e) {
            throw new DataQualityException("Error reading file: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest data from a URL
     */
    public List<Map<String, Object>> ingestFromUrl(String urlString) {
        try {
            log.info("Ingesting data from URL: {}", urlString);
            URL url = new URL(urlString);
            
            String filename = urlString.substring(urlString.lastIndexOf('/') + 1);
            FileFormat format = detectFileFormat(filename);
            
            try (InputStream inputStream = url.openStream()) {
                return switch (format) {
                    case CSV -> ingestCSV(inputStream);
                    case JSON -> ingestJSON(inputStream);
                    case XLSX -> ingestXLSX(inputStream);
                    default -> throw new UnsupportedFileFormatException("Unsupported file format from URL: " + format);
                };
            }
        } catch (IOException e) {
            throw new DataQualityException("Error downloading file from URL: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest data from inline JSON payload
     */
    public List<Map<String, Object>> ingestFromInlineData(String jsonData) {
        try {
            log.info("Ingesting inline JSON data");
            JsonNode rootNode = objectMapper.readTree(jsonData);
            
            List<Map<String, Object>> data = new ArrayList<>();
            
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    data.add(objectMapper.convertValue(node, Map.class));
                }
            } else if (rootNode.isObject()) {
                data.add(objectMapper.convertValue(rootNode, Map.class));
            } else {
                throw new DataQualityException("Invalid JSON format. Expected array or object.");
            }
            
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error parsing inline JSON data: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest CSV data
     */
    private List<Map<String, Object>> ingestCSV(InputStream inputStream) {
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {
                
                for (CSVRecord record : csvParser) {
                    Map<String, Object> row = new HashMap<>();
                    for (String header : csvParser.getHeaderNames()) {
                        String rawValue = record.get(header);
                        // FIXED: Normalize empty strings and "null" text to actual null
                        row.put(header, normalizeValue(rawValue));
                    }
                    data.add(row);
                }
            }
            
            log.info("Successfully ingested {} rows from CSV", data.size());
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error reading CSV file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Normalize value - converts empty strings and "null" text to actual null
     */
    private Object normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        
        // Trim the value
        String trimmed = value.trim();
        
        // Empty string becomes null
        if (trimmed.isEmpty()) {
            return null;
        }
        
        // Text "null", "NULL", "Null" becomes null
        if (trimmed.equalsIgnoreCase("null")) {
            return null;
        }
        
        // Also handle common null representations
        if (trimmed.equals("N/A") || trimmed.equals("n/a") || 
            trimmed.equals("NA") || trimmed.equals("#N/A")) {
            return null;
        }
        
        return value;
    }

    /**
     * Ingest JSON data
     */
    private List<Map<String, Object>> ingestJSON(InputStream inputStream) {
        try {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            List<Map<String, Object>> data = new ArrayList<>();
            
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    data.add(objectMapper.convertValue(node, Map.class));
                }
            } else if (rootNode.isObject()) {
                data.add(objectMapper.convertValue(rootNode, Map.class));
            }
            
            log.info("Successfully ingested {} rows from JSON", data.size());
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error reading JSON file: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest XLSX data
     */
    private List<Map<String, Object>> ingestXLSX(InputStream inputStream) {
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            
            try (Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0); // Read first sheet
                Iterator<Row> rowIterator = sheet.iterator();
                
                // Get headers from first row
                List<String> headers = new ArrayList<>();
                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    for (Cell cell : headerRow) {
                        headers.add(getCellValueAsString(cell));
                    }
                }
                
                // Read data rows
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Map<String, Object> rowData = new HashMap<>();
                    
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = row.getCell(i);
                        rowData.put(headers.get(i), getCellValue(cell));
                    }
                    data.add(rowData);
                }
            }
            
            log.info("Successfully ingested {} rows from XLSX", data.size());
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error reading XLSX file: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest Parquet data (basic implementation)
     */
    private List<Map<String, Object>> ingestParquet(InputStream inputStream) {
        // Note: Full Parquet support requires Hadoop dependencies
        // This is a placeholder for the implementation
        log.warn("Parquet format support is limited. Consider using CSV or JSON for full functionality.");
        throw new UnsupportedFileFormatException("Parquet format requires additional configuration. Please use CSV, JSON, or XLSX format.");
    }

    /**
     * Detect file format from filename
     */
    private FileFormat detectFileFormat(String filename) {
        if (filename == null) {
            return FileFormat.UNKNOWN;
        }
        
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (extension) {
            case "csv" -> FileFormat.CSV;
            case "json" -> FileFormat.JSON;
            case "xlsx", "xls" -> FileFormat.XLSX;
            case "parquet" -> FileFormat.PARQUET;
            default -> FileFormat.UNKNOWN;
        };
    }

    /**
     * Get cell value from Excel cell
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) 
                ? cell.getDateCellValue() 
                : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}

