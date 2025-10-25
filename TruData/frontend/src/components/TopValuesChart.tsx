import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { ColumnProfile } from '../types';

interface TopValuesChartProps {
  column: ColumnProfile;
}

export default function TopValuesChart({ column }: TopValuesChartProps) {
  if (!column.valueCounts || Object.keys(column.valueCounts).length === 0) {
    return null;
  }

  const data = Object.entries(column.valueCounts)
    .sort(([, a], [, b]) => b - a)
    .slice(0, 10)
    .map(([value, count]) => ({
      value: value.length > 20 ? value.substring(0, 20) + '...' : value,
      count: count,
    }));

  return (
    <div className="mt-4">
      <h5 className="text-sm font-semibold text-gray-700 mb-2">
        Top Values Distribution
      </h5>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="value" 
            angle={-45}
            textAnchor="end"
            height={80}
          />
          <YAxis />
          <Tooltip 
            formatter={(value: number) => [value.toLocaleString(), 'Count']}
            contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
          />
          <Bar dataKey="count" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

