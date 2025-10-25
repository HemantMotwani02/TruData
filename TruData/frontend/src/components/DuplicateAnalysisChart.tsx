import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { DuplicateAnalysis } from '../types';

interface DuplicateAnalysisChartProps {
  analysis: DuplicateAnalysis;
}

const COLORS = ['#ef4444', '#10b981'];

export default function DuplicateAnalysisChart({ analysis }: DuplicateAnalysisChartProps) {
  if (!analysis.hasExactDuplicates) {
    return null;
  }

  // Duplicate vs Unique data
  const duplicateData = [
    { name: 'Duplicates', value: analysis.totalDuplicates, fill: '#ef4444' },
    { name: 'Unique', value: 100 - analysis.duplicatePercentage, fill: '#10b981' },
  ];

  // Duplicates by column
  const columnDuplicatesData = Object.entries(analysis.duplicatesByColumn || {})
    .sort(([, a], [, b]) => b - a)
    .slice(0, 10)
    .map(([column, count]) => ({
      name: column.length > 15 ? column.substring(0, 15) + '...' : column,
      duplicates: count,
    }));

  return (
    <div className="card border-l-4 border-l-orange-500">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">
        Duplicate Analysis
      </h3>
      
      <div className="mb-4">
        <p className="text-sm text-gray-700">
          Found <strong>{analysis.totalDuplicates}</strong> duplicate rows 
          (<strong>{analysis.duplicatePercentage.toFixed(2)}%</strong> of total)
        </p>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        {/* Pie Chart */}
        <div>
          <h4 className="text-sm font-semibold text-gray-700 mb-2">
            Duplicate vs Unique Rows
          </h4>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={[
                  { name: 'Unique', value: 100 - analysis.duplicatePercentage },
                  { name: 'Duplicates', value: analysis.duplicatePercentage },
                ]}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(1)}%`}
                outerRadius={70}
                dataKey="value"
              >
                {duplicateData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index]} />
                ))}
              </Pie>
              <Tooltip 
                formatter={(value: number) => [`${value.toFixed(2)}%`, 'Percentage']}
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Bar Chart - Duplicates by Column */}
        {columnDuplicatesData.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-2">
              Duplicates by Column
            </h4>
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={columnDuplicatesData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" angle={-45} textAnchor="end" height={60} />
                <YAxis />
                <Tooltip 
                  formatter={(value: number) => [value.toLocaleString(), 'Duplicates']}
                  contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
                />
                <Bar dataKey="duplicates" fill="#f59e0b" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {analysis.duplicateRowIndices.length > 0 && (
        <div className="mt-4 p-3 bg-orange-50 rounded">
          <p className="text-xs text-gray-700">
            <strong>Sample duplicate row indices:</strong>{' '}
            {analysis.duplicateRowIndices.slice(0, 20).join(', ')}
            {analysis.duplicateRowIndices.length > 20 && ` ... and ${analysis.duplicateRowIndices.length - 20} more`}
          </p>
        </div>
      )}
    </div>
  );
}

