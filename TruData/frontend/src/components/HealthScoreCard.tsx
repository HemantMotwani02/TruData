import { DataQualityResponse } from '../types';

interface HealthScoreCardProps {
  result: DataQualityResponse;
}

export default function HealthScoreCard({ result }: HealthScoreCardProps) {
  const getScoreColor = (score: number) => {
    if (score >= 90) return 'text-green-600';
    if (score >= 75) return 'text-blue-600';
    if (score >= 60) return 'text-yellow-600';
    if (score >= 40) return 'text-orange-600';
    return 'text-red-600';
  };

  const getScoreBgColor = (score: number) => {
    if (score >= 90) return 'bg-green-500';
    if (score >= 75) return 'bg-blue-500';
    if (score >= 60) return 'bg-yellow-500';
    if (score >= 40) return 'bg-orange-500';
    return 'bg-red-500';
  };

  const getLevelBadgeColor = (level: string) => {
    switch (level) {
      case 'EXCELLENT': return 'bg-green-100 text-green-800 border-green-200';
      case 'GOOD': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'FAIR': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'POOR': return 'bg-orange-100 text-orange-800 border-orange-200';
      default: return 'bg-red-100 text-red-800 border-red-200';
    }
  };

  return (
    <div className="card bg-gradient-to-br from-white to-gray-50">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-800">Overall Health Score</h3>
        <span className={`px-4 py-2 rounded-full text-sm font-semibold border ${getLevelBadgeColor(result.qualityLevel)}`}>
          {result.qualityLevel}
        </span>
      </div>

      <div className="flex items-center justify-center mb-6">
        <div className="relative">
          <svg className="w-48 h-48 transform -rotate-90">
            <circle
              cx="96"
              cy="96"
              r="88"
              stroke="currentColor"
              strokeWidth="12"
              fill="transparent"
              className="text-gray-200"
            />
            <circle
              cx="96"
              cy="96"
              r="88"
              stroke="currentColor"
              strokeWidth="12"
              fill="transparent"
              strokeDasharray={`${2 * Math.PI * 88}`}
              strokeDashoffset={`${2 * Math.PI * 88 * (1 - result.healthScore / 100)}`}
              className={`${getScoreBgColor(result.healthScore)} transition-all duration-1000`}
              strokeLinecap="round"
            />
          </svg>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="text-center">
              <div className={`text-5xl font-bold ${getScoreColor(result.healthScore)}`}>
                {result.healthScore.toFixed(1)}
              </div>
              <div className="text-sm text-gray-600">out of 100</div>
            </div>
          </div>
        </div>
      </div>

      <div className="text-center text-sm text-gray-600">
        {result.healthScore >= 90 && 'Excellent! Your data quality is outstanding.'}
        {result.healthScore >= 75 && result.healthScore < 90 && 'Good quality with minor improvements needed.'}
        {result.healthScore >= 60 && result.healthScore < 75 && 'Fair quality. Several issues should be addressed.'}
        {result.healthScore >= 40 && result.healthScore < 60 && 'Poor quality. Significant improvements required.'}
        {result.healthScore < 40 && 'Critical issues detected. Immediate action needed.'}
      </div>
    </div>
  );
}

