import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';
import { ColumnProfile } from '../types';

interface NumericDistributionChartProps {
  column: ColumnProfile;
}

export default function NumericDistributionChart({ column }: NumericDistributionChartProps) {
  if (column.mean === undefined || column.median === undefined) {
    return null;
  }

  // Create box plot style visualization
  const data = [
    { metric: 'Min', value: column.min || 0 },
    { metric: 'Q1', value: column.q1 || 0 },
    { metric: 'Median', value: column.median || 0 },
    { metric: 'Mean', value: column.mean || 0 },
    { metric: 'Q3', value: column.q3 || 0 },
    { metric: 'Max', value: column.max || 0 },
  ];

  const statistics = [
    { label: 'Mean', value: column.mean.toFixed(2) },
    { label: 'Median', value: column.median.toFixed(2) },
    { label: 'Std Dev', value: column.stdDev?.toFixed(2) || 'N/A' },
    { label: 'Min', value: column.min?.toFixed(2) || 'N/A' },
    { label: 'Max', value: column.max?.toFixed(2) || 'N/A' },
  ];

  return (
    <div className="mt-4">
      <h5 className="text-sm font-semibold text-gray-700 mb-2">
        Statistical Distribution
      </h5>
      
      <div className="grid grid-cols-5 gap-2 mb-4">
        {statistics.map(stat => (
          <div key={stat.label} className="bg-blue-50 p-2 rounded text-center">
            <p className="text-xs text-gray-600">{stat.label}</p>
            <p className="text-sm font-semibold text-gray-800">{stat.value}</p>
          </div>
        ))}
      </div>

      <ResponsiveContainer width="100%" height={200}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="metric" />
          <YAxis />
          <Tooltip 
            formatter={(value: number) => [value.toFixed(2), 'Value']}
            contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
          />
          <ReferenceLine 
            y={column.mean} 
            stroke="#ef4444" 
            strokeDasharray="3 3"
            label={{ value: 'Mean', position: 'right' }}
          />
          <Bar dataKey="value" fill="#0ea5e9" radius={[8, 8, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>

      {column.hasOutliers && column.outlierValues && column.outlierValues.length > 0 && (
        <div className="mt-3 p-2 bg-yellow-50 rounded">
          <p className="text-xs text-yellow-800">
            <strong>Outliers detected:</strong> {column.outlierValues.slice(0, 5).join(', ')}
            {column.outlierValues.length > 5 && ` and ${column.outlierValues.length - 5} more`}
          </p>
        </div>
      )}
    </div>
  );
}

