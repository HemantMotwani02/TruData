# TruData

<div align="center">
  <img src="frontend/public/logo.svg" alt="TruData Logo" width="300"/>
  
  <h3>Data Quality Assurance Platform</h3>
  
  <p>Enterprise-grade data quality assessment and diagnostics platform that helps you identify issues, detect PII, find duplicates, and get actionable insights about your data.</p>
  
  <p><strong>Trust Your Data with TruData</strong></p>
</div>

## 🌟 Features

### Core Capabilities
- **Multi-Format Support**: CSV, JSON, XLSX, Parquet files
- **Flexible Input Methods**: File upload, URL links, or inline JSON data
- **Comprehensive Analysis**: 
  - Data profiling for each column
  - Quality metrics computation (Completeness, Uniqueness, Validity, Consistency, Accuracy, Timeliness)
  - Overall health score calculation
  - Duplicate detection
  - PII (Personally Identifiable Information) detection
  - Bias detection in datasets
  - Outlier identification
  - Data type inference

### Security & Privacy
- ✅ Automatic PII detection (emails, phone numbers, SSN, credit cards, etc.)
- ✅ Sensitivity level configuration
- ✅ Secure file handling with size limits
- ✅ CORS protection
- ✅ Input validation
- ✅ Security recommendations

### Analytics & Reporting
- 📊 Visual health score with quality levels
- 📈 Detailed metrics for 6 quality dimensions
- 🔍 Column-by-column profiling with statistics
- ⚠️ Comprehensive issue detection and recommendations
- 💾 Exportable JSON reports

## 🎨 Branding

**TruData** combines "True" and "Data" to represent our commitment to data accuracy and trustworthiness. See [BRANDING.md](BRANDING.md) for complete brand guidelines.

## 🏗️ Architecture

### Tech Stack

**Backend:**
- Java 17
- Spring Boot 3.2.0
- Spring Security
- Maven
- H2 Database (development)
- Apache POI (Excel processing)
- OpenCSV (CSV processing)
- Jackson (JSON processing)

**Frontend:**
- React 18.2
- TypeScript
- Vite
- Tailwind CSS
- Axios
- React Dropzone
- Lucide Icons

## 📁 Project Structure

```
TruData/
├── backend/                          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/aidataquality/
│   │   │   │   ├── config/          # Security, CORS, Async configs
│   │   │   │   ├── controller/      # REST API endpoints
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── model/           # DTOs, Entities, Enums
│   │   │   │   ├── exception/       # Custom exceptions
│   │   │   │   └── util/            # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   └── pom.xml
│
├── frontend/                         # React Frontend
│   ├── src/
│   │   ├── components/              # React components
│   │   ├── services/                # API services
│   │   ├── types/                   # TypeScript types
│   │   ├── App.tsx                  # Main app component
│   │   └── main.tsx                 # Entry point
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── tailwind.config.js
│
└── README.md                         # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Node.js 18** or higher
- **Maven 3.8** or higher
- **npm** or **yarn**

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### Configuration

Edit `backend/src/main/resources/application.yml` to customize:
- File upload limits
- PII detection patterns
- Quality thresholds
- Security settings

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:3000`

## 📖 API Documentation

Once the backend is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### Main Endpoints

#### 1. File Upload Analysis
```http
POST /api/v1/data-quality/analyze/file
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- performPIICheck: boolean (optional, default: true)
- performBiasCheck: boolean (optional, default: false)
```

#### 2. URL Analysis
```http
POST /api/v1/data-quality/analyze/url
Content-Type: application/json

Body:
{
  "dataUrl": "https://example.com/data.csv",
  "performPIICheck": true,
  "performBiasCheck": false
}
```

#### 3. Inline Data Analysis
```http
POST /api/v1/data-quality/analyze/inline
Content-Type: application/json

Body:
{
  "inlineData": "[{\"name\":\"John\",\"age\":30}]",
  "performPIICheck": true,
  "performBiasCheck": false
}
```

### Response Format

```json
{
  "analysisId": "uuid",
  "timestamp": "2025-10-25T10:30:00",
  "healthScore": 85.5,
  "qualityLevel": "GOOD",
  "summary": {
    "fileFormat": "CSV",
    "rowCount": 1000,
    "columnCount": 10,
    "totalCells": 10000
  },
  "qualityMetrics": {
    "completenessScore": 92.5,
    "uniquenessScore": 88.0,
    "validityScore": 95.0,
    "consistencyScore": 87.5,
    "accuracyScore": 90.0,
    "timelinessScore": 100.0
  },
  "columnProfiles": [...],
  "issues": [...],
  "recommendations": [...],
  "piiFindings": {...},
  "duplicateAnalysis": {...},
  "processingTimeMs": 1250
}
```

## 🎯 Usage Examples

### Web Interface

1. Open the frontend at `http://localhost:3000`
2. Choose your input method:
   - **File Upload**: Drag & drop or click to browse
   - **URL Link**: Enter a direct link to your data file
   - **Inline JSON**: Paste JSON data directly
3. Configure analysis options (PII check, Bias check)
4. Click analyze and review the comprehensive report
5. Export results as JSON for further processing

### Sample Data

Test with the included sample datasets in the `samples/` directory (create this if testing):

```bash
# Sample CSV
curl -X POST http://localhost:8080/api/v1/data-quality/analyze/file \
  -F "file=@sample-data.csv" \
  -F "performPIICheck=true"
```

## 🔒 Security Best Practices

This tool implements several security measures:

1. **File Upload Security**:
   - Size limits (100MB default)
   - Type validation
   - Virus scanning (implement in production)

2. **Data Privacy**:
   - No data persistence (optional)
   - PII detection and flagging
   - Secure temporary file handling

3. **API Security**:
   - CORS configuration
   - Rate limiting (implement in production)
   - Input validation
   - Error handling without information leakage

4. **Production Recommendations**:
   - Enable HTTPS
   - Add authentication/authorization
   - Implement audit logging
   - Use production-grade database
   - Add monitoring and alerting

## 📊 Quality Metrics Explained

### Completeness (25% weight)
Measures the percentage of non-null values in the dataset.

### Uniqueness (20% weight)
Identifies duplicate records and calculates unique value ratios.

### Validity (20% weight)
Checks if data conforms to expected formats and business rules.

### Consistency (15% weight)
Evaluates uniformity of data types and formats across the dataset.

### Accuracy (15% weight)
Validates data against schema definitions and type constraints.

### Timeliness (5% weight)
Assesses the freshness of temporal data (if present).

## 🎨 UI Features

- **Modern Design**: Clean, professional interface with Tailwind CSS
- **Responsive**: Works on desktop, tablet, and mobile
- **Interactive**: Expandable column details, downloadable reports
- **Visual**: Charts, progress bars, and color-coded metrics
- **Intuitive**: Clear navigation and helpful tooltips

## 🛠️ Development

### Running Tests

Backend:
```bash
cd backend
mvn test
```

Frontend:
```bash
cd frontend
npm test
```

### Building for Production

Backend:
```bash
cd backend
mvn clean package
java -jar target/ai-data-quality-tool-1.0.0.jar
```

Frontend:
```bash
cd frontend
npm run build
# Serve the dist/ folder with your preferred web server
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is proprietary software. All rights reserved.

## 🙋 Support

For questions, issues, or feature requests:
- Open an issue on GitHub
- Contact the development team
- Check the documentation at `/api-docs`

## 🗺️ Roadmap

- [ ] Support for more file formats (Avro, Protobuf)
- [ ] Machine learning-based anomaly detection
- [ ] Real-time data quality monitoring
- [ ] Data lineage tracking
- [ ] Integration with cloud storage (S3, Azure Blob)
- [ ] Custom rule engine
- [ ] Scheduled quality checks
- [ ] Team collaboration features
- [ ] Advanced visualization dashboards
- [ ] Export to PDF reports

## ⚡ Performance

- Handles datasets up to 1M rows (configurable)
- Average processing time: <2 seconds for 10K rows
- Memory-efficient streaming for large files
- Async processing for better responsiveness

## 🌐 Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

---

---

<div align="center">
  <strong>Built with ❤️ using Spring Boot & React</strong>
  
  Version: 1.0.0 | Last Updated: October 2025
  
  <p>© 2025 TruData. All rights reserved.</p>
</div>

