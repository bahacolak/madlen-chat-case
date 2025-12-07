import apiClient from '@/utils/apiClient';
import type { ModelInfo } from '@/types';

export const modelService = {
  async getAvailableModels(): Promise<ModelInfo[]> {
    try {
      const response = await apiClient.get<ModelInfo[]>('/models');
      return response.data;
    } catch (error) {
      return [
        { id: 'meta-llama/llama-3.2-3b-instruct:free', name: 'Meta Llama 3.2 3B (Free)', free: true, supportsVision: false },
        { id: 'amazon/nova-2-lite-v1:free', name: 'Amazon Nova 2 Lite (Free)', free: true, supportsVision: true },
        { id: 'google/gemma-3-4b-it:free', name: 'Google Gemma 3 4B (ImageFree)', free: true, supportsVision: false, generatesImages: true },
        { id: 'openai/gpt-oss-20b:free', name: 'OpenAI GPT-OSS 20B (Free)', free: true, supportsVision: false },
      ];
    }
  },
};
