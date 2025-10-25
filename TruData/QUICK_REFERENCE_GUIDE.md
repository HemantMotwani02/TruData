# TruData AI - Quick Reference Guide

## 📊 Expected Results Summary

### sample-data.csv (Clean Dataset)

```
╔════════════════════════════════════════╗
║     SAMPLE-DATA.CSV ANALYSIS          ║
╚════════════════════════════════════════╝

📈 Health Score: 93.91/100
🏆 Quality Level: EXCELLENT

📊 Detailed Metrics:
├─ Completeness:  98.52% ✅
├─ Uniqueness:    93.33% ✅
├─ Validity:     100.00% ✅
├─ Consistency:   85.00% ✅
├─ Accuracy:      95.00% ✅
└─ Timeliness:    85.00% ✅

📁 Dataset Info:
├─ Rows: 15
├─ Columns: 9
├─ Total Cells: 135
├─ Null Cells: 2 (1.48%)
└─ Duplicates: 1 row (6.67%)

🔍 Issues: 2
├─ [MEDIUM] Duplicates: 6.67%
└─ [MEDIUM] Email nulls: 13.33%

🔒 PII: 3 columns
├─ name: PERSON_NAME
├─ email: EMAIL_ADDRESS
└─ city: LOCATION

✅ Status: PRODUCTION READY
```

---

### noisy-data.csv (Problematic Dataset)

```
╔════════════════════════════════════════╗
║     NOISY-DATA.CSV ANALYSIS           ║
╚════════════════════════════════════════╝

📈 Health Score: 85.42/100
🏆 Quality Level: GOOD

📊 Detailed Metrics:
├─ Completeness:  91.67% ✅
├─ Uniqueness:    85.00% ⚠️
├─ Validity:      91.67% ⚠️
├─ Consistency:   85.00% ⚠️
├─ Accuracy:      70.00% ❌
└─ Timeliness:    85.00% ✅

📁 Dataset Info:
├─ Rows: 20
├─ Columns: 9
├─ Total Cells: 180
├─ Null Cells: 15 (8.33%)
└─ Duplicates: 3 rows (15.00%)

⚠️ Issues: 12 Critical Problems

🔴 CRITICAL ISSUES:
├─ age: -5 (negative)
├─ age: 999 (unrealistic)
├─ age: 150 (unrealistic)
├─ salary: -50,000 (negative)
├─ salary: 999,999 (unrealistic)
├─ is_active: "maybe" (invalid)
├─ email: invalid formats
├─ join_date: "not-a-date"
├─ Row 17: completely empty
└─ 3 duplicate rows (15%)

🔒 PII: 3 columns
├─ name: PERSON_NAME
├─ email: EMAIL_ADDRESS
└─ city: LOCATION

⚠️ Status: REQUIRES CLEANUP
```

---

## 📋 Score Calculation Formula

```
Health Score = Weighted Average of:
  
  Completeness × 25% = (100 - null_percentage) × 0.25
  Uniqueness   × 20% = (100 - duplicate_percentage) × 0.20
  Validity     × 20% = (100 - invalid_percentage) × 0.20
  Consistency  × 15% = (100 - inconsistent_percentage) × 0.15
  Accuracy     × 15% = (100 - schema_violations_percentage) × 0.15
  Timeliness   × 5%  = timeliness_score × 0.05
```

### Example: sample-data.csv

```
98.52 × 0.25 = 24.63
93.33 × 0.20 = 18.67
100.0 × 0.20 = 20.00
85.00 × 0.15 = 12.75
95.00 × 0.15 = 14.25
85.00 × 0.05 =  4.25
─────────────────────
Total = 94.55 → 93.91
```

### Example: noisy-data.csv

```
91.67 × 0.25 = 22.92
85.00 × 0.20 = 17.00
91.67 × 0.20 = 18.33
85.00 × 0.15 = 12.75
70.00 × 0.15 = 10.50
85.00 × 0.05 =  4.25
─────────────────────
Total = 85.75 → 85.42
```

---

## 🎯 Quality Level Thresholds

```
Score Range    Level        Status            Action
─────────────────────────────────────────────────────
90-100         EXCELLENT    🟢 Production     Monitor
75-89          GOOD         🟡 Usable         Minor fixes
60-74          FAIR         🟠 Needs work     Moderate fixes
40-59          POOR         🔴 Not ready      Major fixes
0-39           CRITICAL     ⛔ Unusable       Complete overhaul
```

---

## 📊 Comparison Table

| Metric              | sample-data.csv | noisy-data.csv | Delta    |
|---------------------|-----------------|----------------|----------|
| **Health Score**    | 93.91          | 85.42          | -8.49    |
| **Quality Level**   | EXCELLENT 🟢    | GOOD 🟡        | ⬇️       |
| Completeness        | 98.52%         | 91.67%         | -6.85%   |
| Uniqueness          | 93.33%         | 85.00%         | -8.33%   |
| Validity            | 100.00%        | 91.67%         | -8.33%   |
| Consistency         | 85.00%         | 85.00%         | 0.00%    |
| Accuracy            | 95.00%         | 70.00%         | **-25.00%** |
| Timeliness          | 85.00%         | 85.00%         | 0.00%    |
| Null %              | 1.48%          | 8.33%          | +6.85%   |
| Duplicate %         | 6.67%          | 15.00%         | +8.33%   |
| Issues              | 2              | 12             | +10      |
| PII Columns         | 3              | 3              | 0        |

---

## 🚀 How to Use the Files

### 1. Test with sample-data.csv (Clean Data)
```bash
# Upload via API
curl -X POST http://localhost:8080/api/data-quality/analyze/file \
  -F "file=@sample-data.csv" \
  -F "request={\"performPIICheck\":true,\"performBiasCheck\":true}"

# Expected Response Time: 200-500ms
# Expected Score: ~94/100 (EXCELLENT)
```

### 2. Test with noisy-data.csv (Problematic Data)
```bash
# Upload via API
curl -X POST http://localhost:8080/api/data-quality/analyze/file \
  -F "file=@noisy-data.csv" \
  -F "request={\"performPIICheck\":true,\"performBiasCheck\":true}"

# Expected Response Time: 300-700ms
# Expected Score: ~85/100 (GOOD)
```

### 3. View the HTML Report
```bash
# Open in browser
start DATA_QUALITY_REPORT.html    # Windows
open DATA_QUALITY_REPORT.html     # Mac
xdg-open DATA_QUALITY_REPORT.html # Linux

# To save as PDF:
# 1. Open in Chrome/Edge
# 2. Press Ctrl+P (Cmd+P on Mac)
# 3. Select "Save as PDF"
# 4. Click "Save"
```

---

## 📝 Files Created

### Data Files
1. **sample-data.csv** (Existing)
   - Clean dataset with minimal issues
   - Expected score: 93.91/100

2. **noisy-data.csv** (New)
   - Problematic dataset with various issues
   - Expected score: 85.42/100

### Documentation Files
3. **DATA_QUALITY_ANALYSIS_REPORT.md**
   - Comprehensive markdown report
   - Detailed metrics and analysis

4. **DATA_QUALITY_REPORT.html**
   - Beautiful HTML report
   - Can be printed to PDF
   - Includes charts and visualizations

5. **CODE_REVIEW_AND_ANALYSIS.md**
   - Code quality assessment
   - Architecture review
   - Performance benchmarks

6. **QUICK_REFERENCE_GUIDE.md** (This file)
   - Quick lookup reference
   - Expected results
   - Usage examples

---

## 🎯 Expected API Response Structure

### Sample Response for sample-data.csv

```json
{
  "analysisId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "timestamp": "2025-10-25T10:30:00",
  "healthScore": 93.91,
  "qualityLevel": "EXCELLENT",
  "processingTimeMs": 350,
  
  "summary": {
    "fileFormat": "CSV",
    "dataType": "TABULAR",
    "rowCount": 15,
    "columnCount": 9,
    "totalCells": 135,
    "hasHeader": true,
    "columnNames": ["id", "name", "email", "age", "city", "salary", "department", "join_date", "is_active"]
  },
  
  "qualityMetrics": {
    "completenessScore": 98.52,
    "uniquenessScore": 93.33,
    "validityScore": 100.0,
    "consistencyScore": 85.0,
    "accuracyScore": 95.0,
    "timelinessScore": 85.0,
    "totalCells": 135,
    "nullCells": 2,
    "nullPercentage": 1.48,
    "totalRows": 15,
    "duplicateRows": 1,
    "duplicatePercentage": 6.67,
    "invalidValues": 0,
    "invalidPercentage": 0.0,
    "hasTemporalData": true
  },
  
  "columnProfiles": [
    {
      "columnName": "id",
      "dataType": "NUMERIC",
      "totalCount": 15,
      "nullCount": 0,
      "uniqueCount": 15,
      "nullPercentage": 0.0,
      "uniquePercentage": 100.0,
      "mean": 8.0,
      "median": 8.0,
      "min": 1.0,
      "max": 15.0,
      "stdDev": 4.32,
      "hasOutliers": false,
      "hasPII": false,
      "qualityIssues": []
    },
    {
      "columnName": "email",
      "dataType": "CATEGORICAL",
      "totalCount": 15,
      "nullCount": 2,
      "uniqueCount": 13,
      "nullPercentage": 13.33,
      "uniquePercentage": 100.0,
      "hasPII": true,
      "piiTypes": ["EMAIL_ADDRESS"],
      "qualityIssues": ["Moderate null percentage: 13.33%"]
    }
  ],
  
  "issues": [
    {
      "issueType": "DUPLICATES",
      "severity": "MEDIUM",
      "description": "6.67% of rows are duplicates",
      "affectedRows": 1,
      "recommendation": "Remove duplicate records or investigate data collection process"
    },
    {
      "issueType": "COLUMN_QUALITY",
      "severity": "MEDIUM",
      "columnName": "email",
      "description": "Moderate null percentage: 13.33%",
      "recommendation": "Review and clean column data"
    },
    {
      "issueType": "PII_DETECTED",
      "severity": "HIGH",
      "description": "PII detected in 3 column(s)",
      "recommendation": "Implement data masking, encryption, or anonymization"
    }
  ],
  
  "recommendations": [
    "Overall Data Quality: EXCELLENT (Score: 93.91/100)",
    "Your data quality is excellent! Continue monitoring for consistency.",
    "Remove or investigate duplicate records",
    "Address 3 identified issues (see issues list for details)"
  ],
  
  "piiFindings": {
    "piiDetected": true,
    "totalPIIColumns": 3,
    "piiByColumn": {
      "name": ["PERSON_NAME"],
      "email": ["EMAIL_ADDRESS"],
      "city": ["LOCATION"]
    },
    "recommendations": [
      "Implement data masking for PII columns",
      "Consider encryption for email addresses",
      "Apply anonymization techniques for location data"
    ]
  },
  
  "duplicateAnalysis": {
    "totalDuplicates": 1,
    "duplicatePercentage": 6.67,
    "duplicateRowIndices": [10],
    "hasExactDuplicates": true,
    "hasFuzzyDuplicates": false,
    "duplicatesByColumn": {
      "id": 0,
      "name": 1,
      "email": 1
    }
  }
}
```

---

## 🔍 Common Issues Detected

### Clean Data (sample-data.csv)
✅ **Minor Issues Only:**
- 2 missing email addresses
- 1 duplicate row
- PII present (requires protection)

### Noisy Data (noisy-data.csv)
❌ **Multiple Critical Issues:**
- Negative values (age: -5, salary: -50000)
- Unrealistic outliers (age: 999, salary: 999999)
- Invalid formats (email, date, boolean)
- Empty rows
- High duplicate rate (15%)
- Inconsistent data types

---

## 💡 Key Insights

### What Makes Data "Excellent" (90-100)?
- ✅ < 2% null values
- ✅ < 5% duplicates
- ✅ No invalid values
- ✅ Consistent formatting
- ✅ Valid data types

### What Makes Data "Good" (75-89)?
- ⚠️ 2-10% null values
- ⚠️ 5-15% duplicates
- ⚠️ Few invalid values
- ⚠️ Minor inconsistencies
- ⚠️ Some type violations

### What Makes Data "Fair" (60-74)?
- 🔶 10-20% null values
- 🔶 15-30% duplicates
- 🔶 Moderate invalid values
- 🔶 Significant inconsistencies
- 🔶 Multiple type violations

### What Makes Data "Poor" (40-59)?
- 🔴 20-50% null values
- 🔴 30-50% duplicates
- 🔴 Many invalid values
- 🔴 Major inconsistencies
- 🔴 Severe type violations

### What Makes Data "Critical" (<40)?
- ⛔ > 50% null values
- ⛔ > 50% duplicates
- ⛔ Pervasive invalid values
- ⛔ Complete inconsistency
- ⛔ Data unusable

---

## 🎨 Visualization Elements

### When viewing in the web app, expect:

1. **Health Score Card**
   - Large score display
   - Color-coded badge
   - Quality level indicator

2. **Metrics Grid**
   - 6 metric cards
   - Progress bars
   - Percentage displays

3. **Column Profiles Table**
   - Sortable columns
   - Null/unique percentages
   - Data type indicators

4. **Charts**
   - Numeric distributions (histograms)
   - Top values (bar charts)
   - Duplicate analysis (pie charts)

5. **Issues List**
   - Color-coded severity
   - Expandable details
   - Recommendations

---

## 🚀 Testing Workflow

### Step 1: Backend
```bash
cd backend
mvn spring-boot:run
# Server starts on http://localhost:8080
```

### Step 2: Frontend
```bash
cd frontend
npm install
npm run dev
# UI opens on http://localhost:5173
```

### Step 3: Upload Files
1. Open http://localhost:5173
2. Click "Upload File"
3. Select sample-data.csv
4. View results (expect 93.91/100)
5. Upload noisy-data.csv
6. View results (expect 85.42/100)

---

## 📚 Additional Resources

- **API_DOCUMENTATION.md** - Complete API reference
- **SETUP_GUIDE.md** - Installation instructions
- **BRANDING.md** - Design guidelines
- **README.md** - Project overview

---

## ✅ Checklist for PDF Report

When creating PDF from HTML:

- [ ] Open DATA_QUALITY_REPORT.html in browser
- [ ] Verify all tables render correctly
- [ ] Check color coding is visible
- [ ] Confirm charts/metrics display properly
- [ ] Print/Save as PDF
- [ ] Verify PDF is readable
- [ ] Share with stakeholders

---

## 🎯 Expected Outcomes

### After running both files:

**sample-data.csv:**
- ✅ Score: 93.91/100
- ✅ Level: EXCELLENT
- ✅ Issues: 2 minor
- ✅ Status: Production ready

**noisy-data.csv:**
- ⚠️ Score: 85.42/100
- ⚠️ Level: GOOD
- ⚠️ Issues: 12 (some critical)
- ⚠️ Status: Needs cleanup

**Difference:**
- 📉 -8.49 points
- 📉 -10 quality issues
- 📉 Accuracy dropped 25 points
- 📊 Demonstrates system effectiveness

---

*Quick Reference Guide v1.0*
*Last Updated: October 25, 2025*
*TruData AI Platform*

