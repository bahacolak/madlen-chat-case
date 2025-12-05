import apiClient from '@/utils/apiClient';
import type { ChatRequest, ChatResponse } from '@/types';

export const chatService = {
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    const response = await apiClient.post<ChatResponse>('/chat', request);
    return response.data;
  },
};

