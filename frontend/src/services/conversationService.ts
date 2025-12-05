import apiClient from '@/utils/apiClient';
import type { Conversation } from '@/types';

export const conversationService = {
  async createConversation(): Promise<Conversation> {
    const response = await apiClient.post<Conversation>('/conversations');
    return response.data;
  },

  async getUserConversations(): Promise<Conversation[]> {
    const response = await apiClient.get<Conversation[]>('/conversations');
    return response.data;
  },

  async getConversationById(id: number): Promise<Conversation> {
    const response = await apiClient.get<Conversation>(`/conversations/${id}`);
    return response.data;
  },

  async deleteConversation(id: number): Promise<void> {
    await apiClient.delete(`/conversations/${id}`);
  },
};

