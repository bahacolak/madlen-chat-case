import apiClient from '@/utils/apiClient';
import type { ModelInfo } from '@/types';

export const modelService = {
  async getAvailableModels(): Promise<ModelInfo[]> {
    try {
      const response = await apiClient.get<ModelInfo[]>('/models');
      return response.data;
    } catch (error) {
      // Fallback to free models if API fails
      return [
        { id: 'meta-llama/llama-3.2-3b-instruct:free', name: 'Meta Llama 3.2 3B (Free)', free: true },
        { id: 'qwen/qwen-2.5-7b-instruct:free', name: 'Qwen 2.5 7B (Free)', free: true },
        { id: 'google/gemini-flash-1.5', name: 'Google Gemini Flash 1.5', free: false },
      ];
    }
  },
};

