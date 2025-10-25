# TruData API Documentation

Complete API reference for the TruData platform.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API is open for development. In production, implement authentication using:
- JWT tokens
- API keys
- OAuth 2.0

## Content Types

- **Request**: `application/json` or `multipart/form-data`
- **Response**: `application/json`

## Endpoints

### 1. Health Check

Check if the service is running.

**Endpoint:** `GET /api/health`

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-10-25T10:30:00",
  "service": "AI Data Quality Tool",
  "version": "1.0.0"
}
```

**Status Codes:**
- `200 OK` - Service is healthy

---

### 2. Analyze File Upload

Analyze data quality from an uploaded file.

**Endpoint:** `POST /api/v1/data-quality/analyze/file`

**Content-Type:** `multipart/form-data`

**Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| file | File | Yes | - | Data file (CSV, JSON, XLSX, Parquet) |
| dataType | String | No | TABULAR | Data type: TABULAR, TEXT, IMAGE, METADATA |
| sensitivityLevel | String | No | INTERNAL | Sensitivity: PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED |
| performPIICheck | Boolean | No | true | Enable PII detection |
| performBiasCheck | Boolean | No | false | Enable bias detection |

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/v1/data-quality/analyze/file \
  -F "file=@data.csv" \
  -F "performPIICheck=true" \
  -F "performBiasCheck=false"
```

**Response:** See [Response Format](#response-format)

**Status Codes:**
- `200 OK` - Analysis completed successfully
- `400 Bad Request` - Invalid file or parameters
- `413 Payload Too Large` - File exceeds size limit
- `415 Unsupported Media Type` - File format not supported
- `500 Internal Server Error` - Processing error

---

### 3. Analyze from URL

Analyze data quality from a URL.

**Endpoint:** `POST /api/v1/data-quality/analyze/url`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "dataUrl": "https://example.com/data.csv",
  "dataType": "TABULAR",
  "sensitivityLevel": "INTERNAL",
  "performPIICheck": true,
  "performBiasCheck": false,
  "schemaDefinition": {
    "name": "STRING",
    "age": "INTEGER",
    "email": "STRING"
  }
}
```

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/v1/data-quality/analyze/url \
  -H "Content-Type: application/json" \
  -d '{
    "dataUrl": "https://example.com/data.csv",
    "performPIICheck": true
  }'
```

**Response:** See [Response Format](#response-format)

**Status Codes:**
- `200 OK` - Analysis completed successfully
- `400 Bad Request` - Invalid URL or parameters
- `500 Internal Server Error` - Download or processing error

---

### 4. Analyze Inline Data

Analyze data quality from inline JSON.

**Endpoint:** `POST /api/v1/data-quality/analyze/inline`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "inlineData": "[{\"name\":\"John\",\"age\":30},{\"name\":\"Jane\",\"age\":25}]",
  "dataType": "TABULAR",
  "sensitivityLevel": "INTERNAL",
  "performPIICheck": true,
  "performBiasCheck": false,
  "schemaDefinition": {
    "name": "STRING",
    "age": "INTEGER"
  }
}
```

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/v1/data-quality/analyze/inline \
  -H "Content-Type: application/json" \
  -d '{
    "inlineData": "[{\"name\":\"John\",\"age\":30}]",
    "performPIICheck": true
  }'
```

**Response:** See [Response Format](#response-format)

**Status Codes:**
- `200 OK` - Analysis completed successfully
- `400 Bad Request` - Invalid JSON or parameters
- `500 Internal Server Error` - Processing error

---

## Response Format

All analysis endpoints return the same response structure:

```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-10-25T10:30:00",
  "healthScore": 85.5,
  "qualityLevel": "GOOD",
  "summary": {
    "fileFormat": "CSV",
    "dataType": "TABULAR",
    "rowCount": 1000,
    "columnCount": 10,
    "totalCells": 10000,
    "fileSizeBytes": 52428,
    "encoding": "UTF-8",
    "hasHeader": true,
    "columnNames": ["id", "name", "email", "age"]
  },
  "qualityMetrics": {
    "completenessScore": 92.5,
    "totalCells": 10000,
    "nullCells": 750,
    "nullPercentage": 7.5,
    "uniquenessScore": 88.0,
    "totalRows": 1000,
    "duplicateRows": 120,
    "duplicatePercentage": 12.0,
    "validityScore": 95.0,
    "invalidValues": 50,
    "invalidPercentage": 0.5,
    "consistencyScore": 87.5,
    "inconsistentValues": 125,
    "inconsistentPercentage": 1.25,
    "accuracyScore": 90.0,
    "schemaViolations": 100,
    "timelinessScore": 100.0,
    "hasTemporalData": false,
    "biasScore": 95.0,
    "biasDetected": false,
    "biasDescription": "No obvious bias indicators detected."
  },
  "columnProfiles": [
    {
      "columnName": "age",
      "dataType": "NUMERIC",
      "totalCount": 1000,
      "nullCount": 50,
      "uniqueCount": 45,
      "nullPercentage": 5.0,
      "uniquePercentage": 4.7,
      "mean": 35.5,
      "median": 34.0,
      "stdDev": 12.3,
      "min": 18.0,
      "max": 85.0,
      "q1": 28.0,
      "q3": 45.0,
      "hasPII": false,
      "hasOutliers": true,
      "outlierValues": [85.0, 90.0],
      "qualityIssues": ["Moderate null percentage: 5.00%"]
    },
    {
      "columnName": "email",
      "dataType": "CATEGORICAL",
      "totalCount": 1000,
      "nullCount": 10,
      "uniqueCount": 990,
      "nullPercentage": 1.0,
      "uniquePercentage": 100.0,
      "topValues": ["john@example.com", "jane@example.com"],
      "valueCounts": {
        "john@example.com": 5,
        "jane@example.com": 3
      },
      "hasPII": true,
      "piiTypes": ["EMAIL", "COLUMN_NAME_MATCH"],
      "qualityIssues": []
    }
  ],
  "issues": [
    {
      "issueType": "DUPLICATES",
      "severity": "MEDIUM",
      "columnName": null,
      "description": "12.00% of rows are duplicates",
      "affectedRows": 120,
      "recommendation": "Remove duplicate records or investigate data collection process"
    },
    {
      "issueType": "PII_DETECTED",
      "severity": "HIGH",
      "columnName": null,
      "description": "PII detected in 1 column(s)",
      "affectedRows": null,
      "recommendation": "Implement data masking, encryption, or anonymization"
    }
  ],
  "recommendations": [
    "Overall Data Quality: GOOD (Score: 85.50/100)",
    "Good data quality. Address minor issues to achieve excellence.",
    "Remove or investigate duplicate records",
    "Address 2 identified issues (see issues list for details)"
  ],
  "piiFindings": {
    "piiDetected": true,
    "totalPIIColumns": 1,
    "piiByColumn": {
      "email": ["EMAIL", "COLUMN_NAME_MATCH"]
    },
    "recommendations": [
      "PII detected in 1 column(s). Consider the following:",
      "1. Encrypt or hash sensitive columns before storage",
      "2. Implement access controls and audit logging",
      "3. Consider anonymization or pseudonymization techniques",
      "4. Ensure compliance with GDPR, CCPA, or other privacy regulations",
      "5. Remove or mask PII if not necessary for analysis"
    ]
  },
  "duplicateAnalysis": {
    "totalDuplicates": 120,
    "duplicatePercentage": 12.0,
    "duplicateRowIndices": [10, 25, 37, 48],
    "duplicatesByColumn": {
      "name": 150,
      "email": 10
    },
    "hasExactDuplicates": true,
    "hasFuzzyDuplicates": false
  },
  "processingTimeMs": 1250
}
```

## Data Models

### Quality Levels

| Level | Score Range | Description |
|-------|-------------|-------------|
| EXCELLENT | 90-100 | Outstanding data quality |
| GOOD | 75-89 | Good quality with minor improvements needed |
| FAIR | 60-74 | Fair quality, several improvements needed |
| POOR | 40-59 | Poor quality, significant improvements required |
| CRITICAL | 0-39 | Critical issues, immediate action needed |

### Data Types

- `TABULAR` - Structured tabular data
- `TEXT` - Unstructured text data
- `IMAGE` - Image metadata
- `METADATA` - Metadata records

### Sensitivity Levels

- `PUBLIC` - No sensitive data
- `INTERNAL` - Internal use only
- `CONFIDENTIAL` - Confidential data
- `RESTRICTED` - Highly sensitive (PII, PHI, financial)

### Issue Severity

- `HIGH` - Critical issue requiring immediate attention
- `MEDIUM` - Important issue to address soon
- `LOW` - Minor issue for improvement

## Error Responses

All errors follow this format:

```json
{
  "timestamp": "2025-10-25T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid file format"
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input |
| 413 | Payload Too Large - File too large |
| 415 | Unsupported Media Type - Unsupported file format |
| 500 | Internal Server Error - Processing error |
| 503 | Service Unavailable - Service temporarily down |

## Rate Limiting

**Current:** No rate limiting (implement in production)

**Recommended:** 
- 100 requests per minute per IP
- 1000 requests per hour per API key

## Webhooks (Future)

For long-running analyses, webhooks can be configured to receive results:

```json
{
  "webhookUrl": "https://your-app.com/webhook",
  "analysisId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## SDKs and Client Libraries

### JavaScript/TypeScript
```typescript
import { DataQualityClient } from 'ai-data-quality-sdk';

const client = new DataQualityClient('http://localhost:8080');
const result = await client.analyzeFile(file);
```

### Python
```python
from ai_data_quality import Client

client = Client('http://localhost:8080')
result = client.analyze_file('data.csv')
```

## Best Practices

1. **File Size**: Keep files under 100MB for optimal performance
2. **Timeout**: Set client timeout to at least 60 seconds for large files
3. **Retry Logic**: Implement exponential backoff for failed requests
4. **Caching**: Cache results for identical files
5. **Async Processing**: Use webhooks for files > 10MB

## Support

For API questions:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/api-docs
- GitHub Issues: [link]

## Changelog

### Version 1.0.0 (2025-10-25)
- Initial release
- File upload analysis
- URL analysis
- Inline data analysis
- PII detection
- Bias detection
- Duplicate analysis
- Comprehensive reporting

