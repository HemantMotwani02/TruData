import { RefreshCw, Download, AlertCircle, CheckCircle, XCircle } from 'lucide-react';
import { DataQualityResponse } from '../types';
import HealthScoreCard from './HealthScoreCard';
import MetricsGrid from './MetricsGrid';
import IssuesList from './IssuesList';
import ColumnProfilesTable from './ColumnProfilesTable';
import RecommendationsList from './RecommendationsList';
import ChartsSection from './ChartsSection';
import DuplicateAnalysisChart from './DuplicateAnalysisChart';

interface AnalysisResultsProps {
  result: DataQualityResponse;
  onReset: () => void;
}

export default function AnalysisResults({ result, onReset }: AnalysisResultsProps) {
  const handleDownload = () => {
    const dataStr = JSON.stringify(result, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `data-quality-report-${result.analysisId}.json`;
    link.click();
    URL.revokeObjectURL(url);
  };

  const getQualityIcon = () => {
    switch (result.qualityLevel) {
      case 'EXCELLENT':
      case 'GOOD':
        return <CheckCircle className="w-6 h-6 text-green-500" />;
      case 'FAIR':
        return <AlertCircle className="w-6 h-6 text-yellow-500" />;
      default:
        return <XCircle className="w-6 h-6 text-red-500" />;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="card">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center space-x-3 mb-2">
              {getQualityIcon()}
              <h2 className="text-2xl font-bold text-gray-800">
                Data Quality Report
              </h2>
            </div>
            <p className="text-sm text-gray-600">
              Analysis ID: {result.analysisId} | 
              Processed in {result.processingTimeMs}ms
            </p>
          </div>
          
          <div className="flex space-x-3">
            <button
              onClick={handleDownload}
              className="btn btn-secondary flex items-center space-x-2"
            >
              <Download className="w-4 h-4" />
              <span>Export</span>
            </button>
            
            <button
              onClick={onReset}
              className="btn btn-primary flex items-center space-x-2"
            >
              <RefreshCw className="w-4 h-4" />
              <span>New Analysis</span>
            </button>
          </div>
        </div>
      </div>

      {/* Health Score */}
      <HealthScoreCard result={result} />

      {/* Dataset Summary */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-800 mb-4">Dataset Summary</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <p className="text-sm text-gray-600">Format</p>
            <p className="text-lg font-semibold text-gray-800">
              {result.summary.fileFormat}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Rows</p>
            <p className="text-lg font-semibold text-gray-800">
              {result.summary.rowCount.toLocaleString()}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Columns</p>
            <p className="text-lg font-semibold text-gray-800">
              {result.summary.columnCount}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Total Cells</p>
            <p className="text-lg font-semibold text-gray-800">
              {result.summary.totalCells.toLocaleString()}
            </p>
          </div>
        </div>
      </div>

      {/* Quality Metrics */}
      <MetricsGrid metrics={result.qualityMetrics} />

      {/* Visual Analytics Charts */}
      <ChartsSection metrics={result.qualityMetrics} columnProfiles={result.columnProfiles} />

      {/* Issues */}
      {result.issues.length > 0 && (
        <IssuesList issues={result.issues} />
      )}

      {/* PII Findings */}
      {result.piiFindings?.piiDetected && (
        <div className="card border-l-4 border-l-red-500">
          <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
            <AlertCircle className="w-5 h-5 text-red-500 mr-2" />
            PII Detected
          </h3>
          <p className="text-sm text-gray-700 mb-4">
            Found PII in {result.piiFindings.totalPIIColumns} column(s). 
            Please review and implement appropriate security measures.
          </p>
          <div className="space-y-2">
            {Object.entries(result.piiFindings.piiByColumn).map(([column, types]) => (
              <div key={column} className="bg-red-50 p-3 rounded-lg">
                <p className="font-medium text-sm text-gray-800">{column}</p>
                <p className="text-xs text-gray-600">
                  Types: {types.join(', ')}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Duplicate Analysis with Chart */}
      {result.duplicateAnalysis.hasExactDuplicates && (
        <DuplicateAnalysisChart analysis={result.duplicateAnalysis} />
      )}

      {/* Column Profiles */}
      <ColumnProfilesTable columns={result.columnProfiles} />

      {/* Recommendations */}
      <RecommendationsList recommendations={result.recommendations} />
    </div>
  );
}

