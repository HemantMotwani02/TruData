import { useState } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { ColumnProfile } from '../types';
import NumericDistributionChart from './NumericDistributionChart';
import TopValuesChart from './TopValuesChart';

interface ColumnProfilesTableProps {
  columns: ColumnProfile[];
}

export default function ColumnProfilesTable({ columns }: ColumnProfilesTableProps) {
  const [expandedColumn, setExpandedColumn] = useState<string | null>(null);

  const toggleColumn = (columnName: string) => {
    setExpandedColumn(expandedColumn === columnName ? null : columnName);
  };

  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">
        Column Profiles ({columns.length})
      </h3>
      
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200">
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Column</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Type</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Null %</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Unique</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Issues</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700"></th>
            </tr>
          </thead>
          <tbody>
            {columns.map((column) => (
              <>
                <tr
                  key={column.columnName}
                  className="border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
                  onClick={() => toggleColumn(column.columnName)}
                >
                  <td className="py-3 px-4">
                    <div className="flex items-center space-x-2">
                      {expandedColumn === column.columnName ? (
                        <ChevronDown className="w-4 h-4 text-gray-400" />
                      ) : (
                        <ChevronRight className="w-4 h-4 text-gray-400" />
                      )}
                      <span className="font-medium text-gray-800">
                        {column.columnName}
                      </span>
                      {column.hasPII && (
                        <span className="px-2 py-0.5 text-xs bg-red-100 text-red-700 rounded">
                          PII
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="py-3 px-4">
                    <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs">
                      {column.dataType}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-gray-700">
                    {column.nullPercentage.toFixed(2)}%
                  </td>
                  <td className="py-3 px-4 text-gray-700">
                    {column.uniqueCount.toLocaleString()}
                  </td>
                  <td className="py-3 px-4">
                    {column.qualityIssues && column.qualityIssues.length > 0 ? (
                      <span className="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-xs">
                        {column.qualityIssues.length}
                      </span>
                    ) : (
                      <span className="text-green-600 text-xs">✓</span>
                    )}
                  </td>
                  <td className="py-3 px-4 text-right">
                    <span className="text-xs text-gray-500">Details</span>
                  </td>
                </tr>
                
                {expandedColumn === column.columnName && (
                  <tr>
                    <td colSpan={6} className="bg-gray-50 p-4">
                      <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-xs mb-4">
                        <div>
                          <p className="text-gray-600 mb-1">Total Count</p>
                          <p className="font-semibold">{column.totalCount.toLocaleString()}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 mb-1">Null Count</p>
                          <p className="font-semibold">{column.nullCount.toLocaleString()}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 mb-1">Unique %</p>
                          <p className="font-semibold">{column.uniquePercentage.toFixed(2)}%</p>
                        </div>
                        
                        {column.qualityIssues && column.qualityIssues.length > 0 && (
                          <div className="col-span-2 md:col-span-3">
                            <p className="text-gray-600 mb-1">Quality Issues</p>
                            <ul className="list-disc list-inside space-y-1 text-yellow-700">
                              {column.qualityIssues.map((issue, idx) => (
                                <li key={idx}>{issue}</li>
                              ))}
                            </ul>
                          </div>
                        )}
                        
                        {column.hasPII && column.piiTypes && (
                          <div className="col-span-2 md:col-span-3">
                            <p className="text-gray-600 mb-1">PII Types Detected</p>
                            <div className="flex flex-wrap gap-1">
                              {column.piiTypes.map((type, idx) => (
                                <span
                                  key={idx}
                                  className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs"
                                >
                                  {type}
                                </span>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>

                      {/* Charts for numeric columns */}
                      {column.mean !== undefined && (
                        <NumericDistributionChart column={column} />
                      )}

                      {/* Charts for categorical columns */}
                      {column.valueCounts && Object.keys(column.valueCounts).length > 0 && (
                        <TopValuesChart column={column} />
                      )}
                    </td>
                  </tr>
                )}
              </>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

