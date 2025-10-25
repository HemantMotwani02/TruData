export interface DataQualityResponse {
  analysisId: string;
  timestamp: string;
  healthScore: number;
  qualityLevel: QualityLevel;
  summary: DatasetSummary;
  qualityMetrics: QualityMetrics;
  columnProfiles: ColumnProfile[];
  issues: DataQualityIssue[];
  recommendations: string[];
  piiFindings?: PIIFindings;
  duplicateAnalysis: DuplicateAnalysis;
  processingTimeMs: number;
}

export interface DatasetSummary {
  fileFormat: string;
  dataType: string;
  rowCount: number;
  columnCount: number;
  totalCells: number;
  fileSizeBytes?: number;
  encoding?: string;
  hasHeader: boolean;
  columnNames: string[];
}

export interface QualityMetrics {
  completenessScore: number;
  totalCells: number;
  nullCells: number;
  nullPercentage: number;
  uniquenessScore: number;
  totalRows: number;
  duplicateRows: number;
  duplicatePercentage: number;
  validityScore: number;
  invalidValues: number;
  invalidPercentage: number;
  consistencyScore: number;
  inconsistentValues: number;
  inconsistentPercentage: number;
  accuracyScore: number;
  schemaViolations: number;
  timelinessScore: number;
  hasTemporalData: boolean;
  biasScore?: number;
  biasDetected?: boolean;
  biasDescription?: string;
}

export interface ColumnProfile {
  columnName: string;
  dataType: string;
  totalCount: number;
  nullCount: number;
  uniqueCount: number;
  nullPercentage: number;
  uniquePercentage: number;
  mean?: number;
  median?: number;
  stdDev?: number;
  min?: number;
  max?: number;
  q1?: number;
  q3?: number;
  valueCounts?: Record<string, number>;
  topValues?: string[];
  hasPII?: boolean;
  piiTypes?: string[];
  hasOutliers?: boolean;
  outlierValues?: any[];
  hasAnomalies?: boolean;
  qualityIssues?: string[];
}

export interface DataQualityIssue {
  issueType: string;
  severity: string;
  columnName?: string;
  description: string;
  affectedRows?: number;
  recommendation: string;
}

export interface PIIFindings {
  piiDetected: boolean;
  totalPIIColumns: number;
  piiByColumn: Record<string, string[]>;
  recommendations: string[];
}

export interface DuplicateAnalysis {
  totalDuplicates: number;
  duplicatePercentage: number;
  duplicateRowIndices: number[];
  duplicatesByColumn: Record<string, number>;
  hasExactDuplicates: boolean;
  hasFuzzyDuplicates: boolean;
}

export type QualityLevel = 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'CRITICAL';

