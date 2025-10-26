import { BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { QualityMetrics, ColumnProfile } from '../types';

interface ChartsSectionProps {
  metrics: QualityMetrics;
  columnProfiles: ColumnProfile[];
}

const COLORS = ['#0ea5e9', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#6366f1'];

export default function ChartsSection({ metrics, columnProfiles }: ChartsSectionProps) {
  // Quality Metrics Data
  const qualityMetricsData = [
    { name: 'Completeness', score: metrics.completenessScore, fill: COLORS[0] },
    { name: 'Uniqueness', score: metrics.uniquenessScore, fill: COLORS[1] },
    { name: 'Validity', score: metrics.validityScore, fill: COLORS[2] },
    { name: 'Consistency', score: metrics.consistencyScore, fill: COLORS[3] },
    { name: 'Accuracy', score: metrics.accuracyScore, fill: COLORS[4] },
    { name: 'Timeliness', score: metrics.timelinessScore, fill: COLORS[5] },
  ];

  // Null Percentage by Column
  const nullPercentageData = columnProfiles
    .filter(col => col.nullPercentage > 0)
    .sort((a, b) => b.nullPercentage - a.nullPercentage)
    .slice(0, 10)
    .map(col => ({
      name: col.columnName.length > 15 ? col.columnName.substring(0, 15) + '...' : col.columnName,
      percentage: parseFloat(col.nullPercentage.toFixed(2)),
    }));

  // Data Type Distribution
  const dataTypeDistribution = columnProfiles.reduce((acc, col) => {
    const type = col.dataType;
    acc[type] = (acc[type] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  const dataTypeData = Object.entries(dataTypeDistribution).map(([type, count]) => ({
    name: type,
    value: count,
  }));

  // Uniqueness by Column
  const uniquenessData = columnProfiles
    .slice(0, 10)
    .map(col => ({
      name: col.columnName.length > 15 ? col.columnName.substring(0, 15) + '...' : col.columnName,
      unique: parseFloat(col.uniquePercentage.toFixed(2)),
    }));

  return (
    <div className="space-y-6">
      <h3 className="text-xl font-bold text-gray-800">Visual Analytics</h3>
      
      {/* Quality Metrics Bar Chart */}
      <div className="card">
        <h4 className="text-lg font-semibold text-gray-800 mb-4">Quality Dimensions Comparison</h4>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={qualityMetricsData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis domain={[0, 100]} />
            <Tooltip 
              formatter={(value: number) => [`${value.toFixed(2)}%`, 'Score']}
              contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
            />
            <Legend />
            <Bar dataKey="score" radius={[8, 8, 0, 0]}>
              {qualityMetricsData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.fill} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        {/* Data Type Distribution Pie Chart */}
        <div className="card">
          <h4 className="text-lg font-semibold text-gray-800 mb-4">Data Type Distribution</h4>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={dataTypeData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {dataTypeData.map((_, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip 
                formatter={(value: number) => [value, 'Columns']}
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Data Quality Overview Pie Chart */}
        <div className="card">
          <h4 className="text-lg font-semibold text-gray-800 mb-4">Data Completeness Overview</h4>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={[
                  { name: 'Complete', value: metrics.totalCells - metrics.nullCells },
                  { name: 'Null', value: metrics.nullCells },
                ]}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(1)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                <Cell fill="#10b981" />
                <Cell fill="#ef4444" />
              </Pie>
              <Tooltip 
                formatter={(value: number) => [value.toLocaleString(), 'Cells']}
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Null Percentage by Column */}
      {nullPercentageData.length > 0 && (
        <div className="card">
          <h4 className="text-lg font-semibold text-gray-800 mb-4">
            Null Percentage by Column (Top 10)
          </h4>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={nullPercentageData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" domain={[0, 100]} />
              <YAxis dataKey="name" type="category" width={120} />
              <Tooltip 
                formatter={(value: number) => [`${value.toFixed(2)}%`, 'Null %']}
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
              />
              <Bar dataKey="percentage" fill="#ef4444" radius={[0, 8, 8, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Uniqueness by Column */}
      {uniquenessData.length > 0 && (
        <div className="card">
          <h4 className="text-lg font-semibold text-gray-800 mb-4">
            Uniqueness by Column
          </h4>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={uniquenessData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis domain={[0, 100]} />
              <Tooltip 
                formatter={(value: number) => [`${value.toFixed(2)}%`, 'Unique %']}
                contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
              />
              <Legend />
              <Line 
                type="monotone" 
                dataKey="unique" 
                stroke="#8b5cf6" 
                strokeWidth={2}
                dot={{ fill: '#8b5cf6', r: 4 }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Numeric Column Statistics */}
      {columnProfiles.filter(col => col.mean !== undefined).length > 0 && (
        <NumericColumnsChart columns={columnProfiles.filter(col => col.mean !== undefined).slice(0, 5)} />
      )}
    </div>
  );
}

// Separate component for numeric column statistics
function NumericColumnsChart({ columns }: { columns: ColumnProfile[] }) {
  const data = columns.flatMap(col => [
    { column: col.columnName, metric: 'Min', value: col.min },
    { column: col.columnName, metric: 'Q1', value: col.q1 },
    { column: col.columnName, metric: 'Median', value: col.median },
    { column: col.columnName, metric: 'Q3', value: col.q3 },
    { column: col.columnName, metric: 'Max', value: col.max },
  ].filter(d => d.value !== undefined));

  if (data.length === 0) return null;

  return (
    <div className="card">
      <h4 className="text-lg font-semibold text-gray-800 mb-4">
        Numeric Column Statistics (Top 5)
      </h4>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="column" />
          <YAxis />
          <Tooltip 
            contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
          />
          <Legend />
          <Bar dataKey="value" fill="#0ea5e9" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

