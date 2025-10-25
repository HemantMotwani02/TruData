import { CheckCircle, Copy, Shield, Target, Clock, TrendingUp } from 'lucide-react';
import { QualityMetrics } from '../types';

interface MetricsGridProps {
  metrics: QualityMetrics;
}

export default function MetricsGrid({ metrics }: MetricsGridProps) {
  const getScoreColor = (score: number) => {
    if (score >= 85) return 'text-green-600';
    if (score >= 70) return 'text-yellow-600';
    return 'text-red-600';
  };

  const metricCards = [
    {
      icon: CheckCircle,
      title: 'Completeness',
      score: metrics.completenessScore,
      details: `${metrics.nullPercentage.toFixed(2)}% null cells`,
      color: 'blue'
    },
    {
      icon: Copy,
      title: 'Uniqueness',
      score: metrics.uniquenessScore,
      details: `${metrics.duplicateRows} duplicate rows`,
      color: 'purple'
    },
    {
      icon: Shield,
      title: 'Validity',
      score: metrics.validityScore,
      details: `${metrics.invalidValues} invalid values`,
      color: 'green'
    },
    {
      icon: Target,
      title: 'Consistency',
      score: metrics.consistencyScore,
      details: `${metrics.inconsistentValues} inconsistent`,
      color: 'orange'
    },
    {
      icon: TrendingUp,
      title: 'Accuracy',
      score: metrics.accuracyScore,
      details: `${metrics.schemaViolations} violations`,
      color: 'red'
    },
    {
      icon: Clock,
      title: 'Timeliness',
      score: metrics.timelinessScore,
      details: metrics.hasTemporalData ? 'Temporal data found' : 'No temporal data',
      color: 'indigo'
    }
  ];

  return (
    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
      {metricCards.map((metric) => {
        const Icon = metric.icon;
        return (
          <div key={metric.title} className="card hover:shadow-lg transition-shadow">
            <div className="flex items-start justify-between mb-3">
              <div className={`p-2 rounded-lg bg-${metric.color}-100`}>
                <Icon className={`w-6 h-6 text-${metric.color}-600`} />
              </div>
              <div className={`text-2xl font-bold ${getScoreColor(metric.score)}`}>
                {metric.score.toFixed(1)}
              </div>
            </div>
            <h4 className="text-sm font-semibold text-gray-800 mb-1">
              {metric.title}
            </h4>
            <p className="text-xs text-gray-600">{metric.details}</p>
            
            {/* Progress bar */}
            <div className="mt-3 h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                className={`h-full bg-${metric.color}-500 transition-all duration-500`}
                style={{ width: `${metric.score}%` }}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
}

