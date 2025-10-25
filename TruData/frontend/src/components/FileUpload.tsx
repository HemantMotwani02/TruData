import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, FileText, Link as LinkIcon, Code } from 'lucide-react';
import { uploadFile, analyzeFromUrl, analyzeFromInline } from '../services/api';
import { DataQualityResponse } from '../types';

interface FileUploadProps {
  onAnalysisComplete: (result: DataQualityResponse) => void;
  onAnalysisStart: () => void;
}

type UploadMode = 'file' | 'url' | 'inline';

export default function FileUpload({ onAnalysisComplete, onAnalysisStart }: FileUploadProps) {
  const [mode, setMode] = useState<UploadMode>('file');
  const [url, setUrl] = useState('');
  const [jsonData, setJsonData] = useState('');
  const [performPIICheck, setPerformPIICheck] = useState(true);
  const [performBiasCheck, setPerformBiasCheck] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    if (acceptedFiles.length === 0) return;
    
    const file = acceptedFiles[0];
    setError(null);
    onAnalysisStart();
    
    try {
      const result = await uploadFile(file, performPIICheck, performBiasCheck);
      onAnalysisComplete(result);
    } catch (err: any) {
      setError(err.message || 'Failed to analyze file');
      onAnalysisComplete(null as any);
    }
  }, [performPIICheck, performBiasCheck, onAnalysisStart, onAnalysisComplete]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/csv': ['.csv'],
      'application/json': ['.json'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
      'application/vnd.ms-excel': ['.xls']
    },
    maxFiles: 1,
    multiple: false
  });

  const handleUrlSubmit = async () => {
    if (!url.trim()) {
      setError('Please enter a valid URL');
      return;
    }
    
    setError(null);
    onAnalysisStart();
    
    try {
      const result = await analyzeFromUrl(url, performPIICheck, performBiasCheck);
      onAnalysisComplete(result);
    } catch (err: any) {
      setError(err.message || 'Failed to analyze data from URL');
      onAnalysisComplete(null as any);
    }
  };

  const handleInlineSubmit = async () => {
    if (!jsonData.trim()) {
      setError('Please enter valid JSON data');
      return;
    }
    
    try {
      JSON.parse(jsonData); // Validate JSON
    } catch {
      setError('Invalid JSON format');
      return;
    }
    
    setError(null);
    onAnalysisStart();
    
    try {
      const result = await analyzeFromInline(jsonData, performPIICheck, performBiasCheck);
      onAnalysisComplete(result);
    } catch (err: any) {
      setError(err.message || 'Failed to analyze inline data');
      onAnalysisComplete(null as any);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      {/* Mode Selection */}
      <div className="card mb-6">
        <h2 className="text-2xl font-bold mb-4 text-gray-800">Upload Your Data</h2>
        
        <div className="flex space-x-2 mb-6">
          <button
            onClick={() => setMode('file')}
            className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
              mode === 'file'
                ? 'bg-primary-600 text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <Upload className="w-5 h-5 inline mr-2" />
            File Upload
          </button>
          
          <button
            onClick={() => setMode('url')}
            className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
              mode === 'url'
                ? 'bg-primary-600 text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <LinkIcon className="w-5 h-5 inline mr-2" />
            URL Link
          </button>
          
          <button
            onClick={() => setMode('inline')}
            className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
              mode === 'inline'
                ? 'bg-primary-600 text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <Code className="w-5 h-5 inline mr-2" />
            Inline JSON
          </button>
        </div>

        {/* File Upload Mode */}
        {mode === 'file' && (
          <div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-xl p-12 text-center cursor-pointer transition-all ${
              isDragActive
                ? 'border-primary-500 bg-primary-50'
                : 'border-gray-300 hover:border-primary-400 hover:bg-gray-50'
            }`}
          >
            <input {...getInputProps()} />
            <FileText className="w-16 h-16 mx-auto mb-4 text-gray-400" />
            {isDragActive ? (
              <p className="text-lg text-primary-600 font-medium">Drop the file here...</p>
            ) : (
              <>
                <p className="text-lg font-medium text-gray-700 mb-2">
                  Drag & drop your data file here
                </p>
                <p className="text-sm text-gray-500 mb-4">or click to browse</p>
                <p className="text-xs text-gray-400">
                  Supported formats: CSV, JSON, XLSX (Max 100MB)
                </p>
              </>
            )}
          </div>
        )}

        {/* URL Mode */}
        {mode === 'url' && (
          <div>
            <input
              type="url"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://example.com/data.csv"
              className="input mb-4"
            />
            <button
              onClick={handleUrlSubmit}
              className="btn btn-primary w-full"
            >
              Analyze from URL
            </button>
          </div>
        )}

        {/* Inline JSON Mode */}
        {mode === 'inline' && (
          <div>
            <textarea
              value={jsonData}
              onChange={(e) => setJsonData(e.target.value)}
              placeholder='[{"name": "John", "age": 30}, {"name": "Jane", "age": 25}]'
              className="input min-h-[200px] font-mono text-sm mb-4"
            />
            <button
              onClick={handleInlineSubmit}
              className="btn btn-primary w-full"
            >
              Analyze JSON Data
            </button>
          </div>
        )}

        {/* Options */}
        <div className="mt-6 pt-6 border-t border-gray-200">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">Analysis Options</h3>
          <div className="space-y-2">
            <label className="flex items-center space-x-3">
              <input
                type="checkbox"
                checked={performPIICheck}
                onChange={(e) => setPerformPIICheck(e.target.checked)}
                className="w-4 h-4 text-primary-600 rounded"
              />
              <span className="text-sm text-gray-700">
                Perform PII Detection (Recommended)
              </span>
            </label>
            
            <label className="flex items-center space-x-3">
              <input
                type="checkbox"
                checked={performBiasCheck}
                onChange={(e) => setPerformBiasCheck(e.target.checked)}
                className="w-4 h-4 text-primary-600 rounded"
              />
              <span className="text-sm text-gray-700">
                Perform Bias Detection
              </span>
            </label>
          </div>
        </div>

        {error && (
          <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}
      </div>

      {/* Features Info */}
      <div className="grid md:grid-cols-3 gap-4">
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-2">🎯 Comprehensive Analysis</h3>
          <p className="text-sm text-gray-600">
            Get detailed insights on completeness, uniqueness, validity, and more
          </p>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-2">🔒 Privacy First</h3>
          <p className="text-sm text-gray-600">
            Automatic PII detection and security recommendations
          </p>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-2">📊 Visual Reports</h3>
          <p className="text-sm text-gray-600">
            Beautiful charts and detailed diagnostics for every column
          </p>
        </div>
      </div>
    </div>
  );
}

