import apiClient from '@/utils/apiClient';
import type { Conversation } from '@/types';

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const conversationService = {
  async createConversation(): Promise<Conversation> {
    const response = await apiClient.post<Conversation>('/conversations');
    return response.data;
  },

  async getUserConversations(page = 0, size = 50): Promise<Conversation[]> {
    const response = await apiClient.get<PageResponse<Conversation>>(`/conversations?page=${page}&size=${size}`);
    return response.data.content;
  },

  async getConversationById(id: number): Promise<Conversation> {
    const response = await apiClient.get<Conversation>(`/conversations/${id}`);
    return response.data;
  },

  async deleteConversation(id: number): Promise<void> {
    await apiClient.delete(`/conversations/${id}`);
  },
};
