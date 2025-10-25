import { useState } from 'react';
import Header from './components/Header';
import FileUpload from './components/FileUpload';
import AnalysisResults from './components/AnalysisResults';
import LoadingSpinner from './components/LoadingSpinner';
import { DataQualityResponse } from './types';

function App() {
  const [analysisResult, setAnalysisResult] = useState<DataQualityResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleAnalysisComplete = (result: DataQualityResponse) => {
    setAnalysisResult(result);
    setIsLoading(false);
  };

  const handleAnalysisStart = () => {
    setIsLoading(true);
    setAnalysisResult(null);
  };

  const handleReset = () => {
    setAnalysisResult(null);
    setIsLoading(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      <Header />
      
      <main className="container mx-auto px-4 py-8 max-w-7xl">
        {!isLoading && !analysisResult && (
          <FileUpload 
            onAnalysisComplete={handleAnalysisComplete}
            onAnalysisStart={handleAnalysisStart}
          />
        )}
        
        {isLoading && <LoadingSpinner />}
        
        {analysisResult && (
          <AnalysisResults 
            result={analysisResult} 
            onReset={handleReset}
          />
        )}
      </main>
      
      <footer className="bg-gray-800 text-white py-6 mt-16">
        <div className="container mx-auto px-4 text-center">
          <p className="text-sm">
            <span className="font-semibold">TruData</span> v1.0.0 | Enterprise-grade data quality assurance platform
          </p>
          <p className="text-xs text-gray-400 mt-2">
            Powered by Spring Boot & React | © 2025 TruData. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}

export default App;

