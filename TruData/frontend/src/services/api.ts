import axios from 'axios';
import { DataQualityResponse } from '../types';


// Use environment variable (falls back to local or Render URL)
const API_BASE_URL =
  import.meta.env.VITE_API_URL
    ? `${import.meta.env.VITE_API_URL}/api/v1/data-quality`
    : '/api/v1/data-quality';
// const API_BASE_URL = '/api/v1/data-quality';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const uploadFile = async (
  file: File,
  performPIICheck: boolean = true,
  performBiasCheck: boolean = false
): Promise<DataQualityResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('performPIICheck', performPIICheck.toString());
  formData.append('performBiasCheck', performBiasCheck.toString());

  const response = await api.post<DataQualityResponse>('/analyze/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

export const analyzeFromUrl = async (
  dataUrl: string,
  performPIICheck: boolean = true,
  performBiasCheck: boolean = false
): Promise<DataQualityResponse> => {
  const response = await api.post<DataQualityResponse>('/analyze/url', {
    dataUrl,
    performPIICheck,
    performBiasCheck,
  });

  return response.data;
};

export const analyzeFromInline = async (
  inlineData: string,
  performPIICheck: boolean = true,
  performBiasCheck: boolean = false
): Promise<DataQualityResponse> => {
  const response = await api.post<DataQualityResponse>('/analyze/inline', {
    inlineData,
    performPIICheck,
    performBiasCheck,
  });

  return response.data;
};

export default api;

