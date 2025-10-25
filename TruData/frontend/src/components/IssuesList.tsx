import { AlertTriangle, AlertCircle, Info } from 'lucide-react';
import { DataQualityIssue } from '../types';

interface IssuesListProps {
  issues: DataQualityIssue[];
}

export default function IssuesList({ issues }: IssuesListProps) {
  const getSeverityIcon = (severity: string) => {
    switch (severity.toUpperCase()) {
      case 'HIGH':
        return <AlertTriangle className="w-5 h-5 text-red-500" />;
      case 'MEDIUM':
        return <AlertCircle className="w-5 h-5 text-yellow-500" />;
      default:
        return <Info className="w-5 h-5 text-blue-500" />;
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toUpperCase()) {
      case 'HIGH':
        return 'border-red-200 bg-red-50';
      case 'MEDIUM':
        return 'border-yellow-200 bg-yellow-50';
      default:
        return 'border-blue-200 bg-blue-50';
    }
  };

  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">
        Data Quality Issues ({issues.length})
      </h3>
      
      <div className="space-y-3">
        {issues.map((issue, index) => (
          <div
            key={index}
            className={`p-4 rounded-lg border ${getSeverityColor(issue.severity)}`}
          >
            <div className="flex items-start space-x-3">
              <div className="flex-shrink-0 mt-0.5">
                {getSeverityIcon(issue.severity)}
              </div>
              
              <div className="flex-grow">
                <div className="flex items-center space-x-2 mb-2">
                  <span className="text-xs font-semibold px-2 py-1 bg-white rounded">
                    {issue.issueType}
                  </span>
                  {issue.columnName && (
                    <span className="text-xs text-gray-600">
                      Column: {issue.columnName}
                    </span>
                  )}
                  {issue.affectedRows && (
                    <span className="text-xs text-gray-600">
                      Affected: {issue.affectedRows} rows
                    </span>
                  )}
                </div>
                
                <p className="text-sm text-gray-800 mb-2">
                  {issue.description}
                </p>
                
                <p className="text-xs text-gray-600 italic">
                  💡 {issue.recommendation}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

