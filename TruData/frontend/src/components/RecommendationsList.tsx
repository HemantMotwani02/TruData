import { Lightbulb } from 'lucide-react';

interface RecommendationsListProps {
  recommendations: string[];
}

export default function RecommendationsList({ recommendations }: RecommendationsListProps) {
  return (
    <div className="card bg-gradient-to-br from-blue-50 to-purple-50 border-blue-200">
      <div className="flex items-center space-x-2 mb-4">
        <Lightbulb className="w-6 h-6 text-blue-600" />
        <h3 className="text-lg font-semibold text-gray-800">
          Recommendations
        </h3>
      </div>
      
      <ul className="space-y-3">
        {recommendations.map((recommendation, index) => (
          <li key={index} className="flex items-start space-x-3">
            <span className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-semibold">
              {index + 1}
            </span>
            <span className="text-sm text-gray-700 pt-0.5">
              {recommendation}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

