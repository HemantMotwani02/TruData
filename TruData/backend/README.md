# TruData Backend

Spring Boot backend for the TruData platform.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Security**: For API security
- **Spring Data JPA**: For data persistence
- **H2 Database**: In-memory database (dev)
- **Maven**: Dependency management
- **Lombok**: Reduce boilerplate code
- **Apache POI**: Excel file processing
- **OpenCSV**: CSV file processing
- **Jackson**: JSON processing
- **Springdoc OpenAPI**: API documentation

## Project Structure

```
backend/
├── src/main/java/com/aidataquality/
│   ├── AiDataQualityApplication.java    # Main application class
│   ├── config/                           # Configuration classes
│   │   ├── SecurityConfig.java          # Security configuration
│   │   └── AsyncConfig.java             # Async processing config
│   ├── controller/                       # REST API controllers
│   │   ├── DataQualityController.java   # Main API endpoints
│   │   └── HealthController.java        # Health check endpoint
│   ├── service/                          # Business logic services
│   │   ├── DataIngestionService.java    # File/URL data ingestion
│   │   ├── DataProfilingService.java    # Column profiling
│   │   ├── QualityMetricsService.java   # Quality computation
│   │   ├── HealthScoreService.java      # Score aggregation
│   │   ├── PIIDetectionService.java     # PII detection
│   │   ├── BiasDetectionService.java    # Bias detection
│   │   └── DataQualityOrchestrationService.java  # Main orchestrator
│   ├── model/                            # Data models
│   │   ├── dto/                         # Data Transfer Objects
│   │   ├── entity/                      # JPA entities
│   │   └── enums/                       # Enumerations
│   ├── exception/                        # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── DataQualityException.java
│   │   └── UnsupportedFileFormatException.java
│   └── util/                            # Utility classes
└── src/main/resources/
    ├── application.yml                   # Main configuration
    ├── application-dev.yml              # Development config
    └── application-prod.yml             # Production config
```

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.8 or higher

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Or with a specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run Tests
```bash
mvn test
```

## Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
app:
  data-quality:
    max-rows-to-process: 1000000
    max-file-size: 100MB
    supported-formats: csv,json,xlsx,parquet
    temp-storage-path: /tmp/data-quality
    
  security:
    jwt:
      secret-key: ${JWT_SECRET}
      expiration: 86400000
    cors:
      allowed-origins: http://localhost:3000
```

### Environment Variables

For production, set these environment variables:

- `JWT_SECRET`: Secret key for JWT tokens
- `ALLOWED_ORIGINS`: Comma-separated list of allowed CORS origins
- `TEMP_STORAGE`: Path for temporary file storage
- `SPRING_PROFILES_ACTIVE`: Active profile (dev/prod)

## API Endpoints

### Health Check
- **GET** `/api/health` - Service health status

### Data Quality Analysis
- **POST** `/api/v1/data-quality/analyze/file` - Analyze uploaded file
- **POST** `/api/v1/data-quality/analyze/url` - Analyze data from URL
- **POST** `/api/v1/data-quality/analyze/inline` - Analyze inline JSON data

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

## Services Overview

### DataIngestionService
Handles data ingestion from multiple sources:
- File uploads (CSV, JSON, XLSX)
- URL downloads
- Inline JSON payloads

### DataProfilingService
Profiles each column in the dataset:
- Data type inference
- Statistical analysis (mean, median, std dev, quartiles)
- Null count and unique values
- Outlier detection
- Top values for categorical data

### QualityMetricsService
Computes quality metrics:
- Completeness score
- Uniqueness score (duplicate detection)
- Validity score
- Consistency score
- Accuracy score (schema validation)
- Timeliness score

### HealthScoreService
Aggregates metrics into overall health score:
- Weighted scoring algorithm
- Quality level determination (Excellent/Good/Fair/Poor/Critical)
- Issue identification
- Recommendation generation

### PIIDetectionService
Detects personally identifiable information:
- Email addresses
- Phone numbers
- Social Security Numbers
- Credit card numbers
- IP addresses
- Common PII column names

### BiasDetectionService
Identifies potential bias in datasets:
- Sensitive attribute detection
- Distribution analysis
- Imbalance identification

## Security Features

### CORS Configuration
Configured to allow requests from frontend application with credentials support.

### Input Validation
- File size limits
- File type validation
- JSON schema validation
- SQL injection prevention

### Error Handling
Global exception handler provides consistent error responses without leaking sensitive information.

## Performance Considerations

### Async Processing
Large file processing uses async execution to prevent blocking.

### Streaming
For very large files, consider implementing streaming to reduce memory footprint.

### Caching
Results can be cached for repeated analyses of the same data.

## Monitoring

### Actuator Endpoints
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### Logging
Logs are written to:
- Console (development)
- `logs/ai-data-quality.log` (production)

Log levels can be configured per package in `application.yml`.

## Database

### Development
H2 in-memory database is used for development.
Console available at: http://localhost:8080/h2-console

### Production
For production, configure a persistent database:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dataquality
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
```

## Deployment

### Docker
Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/ai-data-quality-tool-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t ai-data-quality-backend .
docker run -p 8080:8080 ai-data-quality-backend
```

### Cloud Deployment
The application can be deployed to:
- AWS (Elastic Beanstalk, ECS, EKS)
- Azure (App Service, AKS)
- Google Cloud (App Engine, GKE)
- Heroku

## Troubleshooting

### Common Issues

1. **OutOfMemoryError**: Increase JVM heap size
```bash
java -Xmx2g -jar target/ai-data-quality-tool-1.0.0.jar
```

2. **File Upload Fails**: Check max file size configuration

3. **CORS Errors**: Verify allowed origins in configuration

## Contributing

When contributing to the backend:

1. Follow Java coding conventions
2. Add unit tests for new features
3. Update API documentation
4. Run `mvn checkstyle:check` before committing
5. Ensure all tests pass

## License

Proprietary software. All rights reserved.

