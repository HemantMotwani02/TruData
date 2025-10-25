package com.aidataquality.service;

import com.aidataquality.exception.DataQualityException;
import com.aidataquality.exception.UnsupportedFileFormatException;
import com.aidataquality.model.enums.FileFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * FIXED VERSION - Service for ingesting data from various sources and formats
 * 
 * FIXES:
 * 1. Empty strings converted to null
 * 2. Text "null" converted to actual null
 * 3. Handles empty/blank files gracefully
 * 4. Better error messages
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
            // Check if file is empty
            if (file.isEmpty()) {
                throw new DataQualityException("File is empty. Please provide a file with data.");
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || filename.trim().isEmpty()) {
                throw new DataQualityException("File name is missing.");
            }
            
            FileFormat format = detectFileFormat(filename);
            
            log.info("Ingesting file: {} with format: {}", filename, format);
            
            List<Map<String, Object>> data = switch (format) {
                case CSV -> ingestCSV(file.getInputStream());
                case JSON -> ingestJSON(file.getInputStream());
                case XLSX -> ingestXLSX(file.getInputStream());
                case PARQUET -> ingestParquet(file.getInputStream());
                default -> throw new UnsupportedFileFormatException("Unsupported file format: " + format);
            };
            
            // Validate data is not empty
            if (data == null || data.isEmpty()) {
                throw new DataQualityException("File contains no data rows. Please provide a file with at least one data row.");
            }
            
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error reading file: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest data from a URL
     */
    public List<Map<String, Object>> ingestFromUrl(String urlString) {
        try {
            if (urlString == null || urlString.trim().isEmpty()) {
                throw new DataQualityException("URL is empty or null.");
            }
            
            log.info("Ingesting data from URL: {}", urlString);
            URL url = new URL(urlString);
            
            String filename = urlString.substring(urlString.lastIndexOf('/') + 1);
            FileFormat format = detectFileFormat(filename);
            
            try (InputStream inputStream = url.openStream()) {
                List<Map<String, Object>> data = switch (format) {
                    case CSV -> ingestCSV(inputStream);
                    case JSON -> ingestJSON(inputStream);
                    case XLSX -> ingestXLSX(inputStream);
                    default -> throw new UnsupportedFileFormatException("Unsupported file format from URL: " + format);
                };
                
                if (data == null || data.isEmpty()) {
                    throw new DataQualityException("URL returned no data.");
                }
                
                return data;
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
            if (jsonData == null || jsonData.trim().isEmpty()) {
                throw new DataQualityException("Inline data is empty or null.");
            }
            
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
            
            if (data.isEmpty()) {
                throw new DataQualityException("JSON data contains no records.");
            }
            
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error parsing inline JSON data: " + e.getMessage(), e);
        }
    }

    /**
     * Ingest CSV data
     * 
     * FIXES:
     * - Empty strings → null
     * - Text "null" → null
     * - Whitespace-only → null
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
                     .setAllowMissingColumnNames(true)
                     .build())) {
                
                // Check if headers exist
                List<String> headers = csvParser.getHeaderNames();
                if (headers == null || headers.isEmpty()) {
                    throw new DataQualityException("CSV file has no headers. Please provide a file with column names.");
                }
                
                // Check for all-empty headers
                boolean hasValidHeader = headers.stream().anyMatch(h -> h != null && !h.trim().isEmpty());
                if (!hasValidHeader) {
                    throw new DataQualityException("CSV file headers are empty. Please provide valid column names.");
                }
                
                int rowCount = 0;
                for (CSVRecord record : csvParser) {
                    // Skip completely empty rows
                    if (isEmptyRecord(record)) {
                        log.debug("Skipping empty row at line {}", record.getRecordNumber());
                        continue;
                    }
                    
                    Map<String, Object> row = new HashMap<>();
                    for (String header : headers) {
                        String value = record.get(header);
                        
                        // ✅ FIX 1: Convert empty strings and "null" text to actual null
                        row.put(header, normalizeValue(value));
                    }
                    data.add(row);
                    rowCount++;
                }
                
                if (rowCount == 0) {
                    throw new DataQualityException("CSV file contains headers but no data rows.");
                }
                
                log.info("Successfully ingested {} rows from CSV (skipped empty rows)", data.size());
            }
            
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error reading CSV file: " + e.getMessage(), e);
        }
    }

    /**
     * Check if CSV record is completely empty
     */
    private boolean isEmptyRecord(CSVRecord record) {
        for (int i = 0; i < record.size(); i++) {
            String value = record.get(i);
            if (value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value.trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalize value: convert empty strings and "null" text to actual null
     * 
     * Handles:
     * - null → null
     * - "" (empty) → null
     * - "   " (whitespace) → null
     * - "null" (text) → null
     * - "NULL" (text) → null
     * - "valid data" → "valid data"
     */
    private Object normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        
        String trimmed = value.trim();
        
        // Convert empty strings to null
        if (trimmed.isEmpty()) {
            return null;
        }
        
        // Convert text "null" to actual null (case insensitive)
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        
        // Return the original value (not trimmed, to preserve user data)
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
                if (rootNode.isEmpty()) {
                    throw new DataQualityException("JSON array is empty.");
                }
                for (JsonNode node : rootNode) {
                    data.add(objectMapper.convertValue(node, Map.class));
                }
            } else if (rootNode.isObject()) {
                data.add(objectMapper.convertValue(rootNode, Map.class));
            } else {
                throw new DataQualityException("JSON must be an object or array.");
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
                
                if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                    throw new DataQualityException("Excel file is empty or has no sheets.");
                }
                
                Iterator<Row> rowIterator = sheet.iterator();
                
                // Get headers from first row
                List<String> headers = new ArrayList<>();
                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    for (Cell cell : headerRow) {
                        String header = getCellValueAsString(cell);
                        headers.add(header != null && !header.trim().isEmpty() ? header : "Column_" + cell.getColumnIndex());
                    }
                }
                
                if (headers.isEmpty()) {
                    throw new DataQualityException("Excel file has no headers.");
                }
                
                // Read data rows
                int rowCount = 0;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    
                    // Skip empty rows
                    if (isEmptyRow(row)) {
                        continue;
                    }
                    
                    Map<String, Object> rowData = new HashMap<>();
                    
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = row.getCell(i);
                        Object value = getCellValue(cell);
                        
                        // Normalize value (convert empty strings and "null" to null)
                        if (value instanceof String) {
                            value = normalizeValue((String) value);
                        }
                        
                        rowData.put(headers.get(i), value);
                    }
                    data.add(rowData);
                    rowCount++;
                }
                
                if (rowCount == 0) {
                    throw new DataQualityException("Excel file contains headers but no data rows.");
                }
            }
            
            log.info("Successfully ingested {} rows from XLSX", data.size());
            return data;
        } catch (IOException e) {
            throw new DataQualityException("Error reading XLSX file: " + e.getMessage(), e);
        }
    }

    /**
     * Check if Excel row is completely empty
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                Object value = getCellValue(cell);
                if (value != null) {
                    String strValue = value.toString().trim();
                    if (!strValue.isEmpty() && !"null".equalsIgnoreCase(strValue)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Ingest Parquet data (basic implementation)
     */
    private List<Map<String, Object>> ingestParquet(InputStream inputStream) {
        log.warn("Parquet format support is limited. Consider using CSV or JSON for full functionality.");
        throw new UnsupportedFileFormatException("Parquet format requires additional configuration. Please use CSV, JSON, or XLSX format.");
    }

    /**
     * Detect file format from filename
     */
    private FileFormat detectFileFormat(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return FileFormat.UNKNOWN;
        }
        
        if (!filename.contains(".")) {
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
            case BLANK -> null;
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
            case BLANK -> "";
            default -> "";
        };
    }
}

