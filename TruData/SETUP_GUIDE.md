# TruData - Quick Setup Guide

Step-by-step guide to get TruData running on your system.

## ⚡ Quick Start (5 minutes)

### Step 1: Prerequisites Check

Ensure you have:
- ✅ Java 17+ (`java -version`)
- ✅ Maven 3.8+ (`mvn -version`)
- ✅ Node.js 18+ (`node -v`)
- ✅ npm 9+ (`npm -v`)

### Step 2: Backend Setup

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean install

# Run the backend (in a separate terminal)
mvn spring-boot:run
```

**Expected output:**
```
Started AiDataQualityApplication in X.XXX seconds
```

Backend will be available at: http://localhost:8080

### Step 3: Frontend Setup

```bash
# Open a new terminal
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm run dev
```

**Expected output:**
```
VITE v5.0.8  ready in XXX ms

➜  Local:   http://localhost:3000/
```

Frontend will be available at: http://localhost:3000

### Step 4: Verify Installation

1. Open browser to http://localhost:3000
2. You should see the TruData interface with the logo
3. Backend API docs at http://localhost:8080/swagger-ui.html

## 🧪 Test the Application

### Option 1: Use Sample Data

Create a test CSV file `sample-data.csv`:
```csv
name,email,age,city
John Doe,john@example.com,30,New York
Jane Smith,jane@example.com,25,Los Angeles
Bob Johnson,bob@example.com,35,Chicago
Alice Williams,,28,Houston
Charlie Brown,charlie@example.com,32,Phoenix
```

### Option 2: Test with Inline JSON

Use this sample JSON in the inline data mode:
```json
[
  {"name": "John", "age": 30, "email": "john@example.com"},
  {"name": "Jane", "age": 25, "email": "jane@example.com"},
  {"name": "Bob", "age": 35, "email": "bob@example.com"}
]
```

### Option 3: Test via API

```bash
curl -X POST http://localhost:8080/api/v1/data-quality/analyze/inline \
  -H "Content-Type: application/json" \
  -d '{
    "inlineData": "[{\"name\":\"John\",\"age\":30}]",
    "performPIICheck": true
  }'
```

## 🔧 Troubleshooting

### Backend Issues

**Problem:** Port 8080 already in use
```bash
# Solution 1: Change port in application.yml
server:
  port: 8081

# Solution 2: Kill process on port 8080
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux:
lsof -ti:8080 | xargs kill -9
```

**Problem:** Maven build fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

**Problem:** Java version mismatch
```bash
# Check Java version
java -version

# Set JAVA_HOME if needed
# Windows:
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Mac/Linux:
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Frontend Issues

**Problem:** Port 3000 already in use
```javascript
// Edit vite.config.ts
export default defineConfig({
  server: {
    port: 3001,  // Change port
  }
})
```

**Problem:** npm install fails
```bash
# Clear cache and reinstall
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

**Problem:** API connection error
- Verify backend is running on http://localhost:8080
- Check CORS configuration in backend
- Check browser console for errors

### Common Issues

**Problem:** "Cannot connect to backend"
- Ensure backend is running
- Check firewall settings
- Verify no proxy blocking localhost

**Problem:** File upload fails
- Check file size (< 100MB)
- Verify file format (CSV, JSON, XLSX)
- Check backend logs for errors

## 📚 Next Steps

1. **Read Documentation**
   - [Main README](README.md)
   - [Backend README](backend/README.md)
   - [Frontend README](frontend/README.md)
   - [API Documentation](API_DOCUMENTATION.md)

2. **Explore Features**
   - Try different file formats
   - Enable PII detection
   - Test bias detection
   - Export results

3. **Customize**
   - Modify quality thresholds
   - Add custom PII patterns
   - Customize UI theme
   - Add authentication

## 🚀 Production Deployment

### Backend

```bash
# Build production JAR
cd backend
mvn clean package -DskipTests

# Run production build
java -jar target/ai-data-quality-tool-1.0.0.jar --spring.profiles.active=prod
```

### Frontend

```bash
# Build production bundle
cd frontend
npm run build

# Serve with nginx, Apache, or your preferred web server
# The dist/ folder contains the production build
```

### Environment Variables

Create `.env` file for production:

**Backend:**
```bash
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your-secret-key-min-256-bits
ALLOWED_ORIGINS=https://yourdomain.com
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
```

**Frontend:**
```bash
VITE_API_BASE_URL=https://api.yourdomain.com
```

## 🐳 Docker Setup (Optional)

### Backend Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/ai-data-quality-tool-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

### Frontend Dockerfile

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

Run with:
```bash
docker-compose up -d
```

## 📞 Support

- **Documentation**: Check README files
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Issues**: Report on GitHub
- **Email**: support@example.com

## ✅ Checklist

- [ ] Java 17+ installed
- [ ] Maven 3.8+ installed
- [ ] Node.js 18+ installed
- [ ] Backend running on port 8080
- [ ] Frontend running on port 3000
- [ ] Can access web interface
- [ ] Tested file upload
- [ ] Reviewed API documentation
- [ ] Ready to analyze data!

---

---

<div align="center">
  <h2>🎉 Congratulations!</h2>
  <p><strong>TruData is ready to use!</strong></p>
  <p>Trust Your Data with TruData</p>
</div>

