import { Loader2 } from 'lucide-react';

export default function LoadingSpinner() {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <Loader2 className="w-16 h-16 text-primary-600 animate-spin mb-4" />
      <h3 className="text-xl font-semibold text-gray-700 mb-2">
        Analyzing Your Data...
      </h3>
      <p className="text-gray-500 text-center max-w-md">
        We're computing quality metrics, detecting issues, and generating insights.
        This may take a moment for large datasets.
      </p>
      
      <div className="mt-8 space-y-2 text-sm text-gray-600">
        <div className="flex items-center space-x-2">
          <div className="w-2 h-2 bg-primary-600 rounded-full animate-pulse"></div>
          <span>Profiling columns and data types...</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-2 h-2 bg-primary-600 rounded-full animate-pulse delay-100"></div>
          <span>Computing quality metrics...</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-2 h-2 bg-primary-600 rounded-full animate-pulse delay-200"></div>
          <span>Detecting anomalies and PII...</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-2 h-2 bg-primary-600 rounded-full animate-pulse delay-300"></div>
          <span>Generating health score...</span>
        </div>
      </div>
    </div>
  );
}

