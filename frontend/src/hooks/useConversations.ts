import { useState, useEffect } from 'react';
import { conversationService } from '@/services/conversationService';
import type { Conversation } from '@/types';

export const useConversations = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const loadConversations = async () => {
    try {
      const userConversations = await conversationService.getUserConversations();
      setConversations(userConversations);
    } catch (error) {
      console.error('Failed to load conversations:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadConversations();
  }, []);

  const refreshConversations = async () => {
    const updatedConversations = await conversationService.getUserConversations();
    setConversations(updatedConversations);
  };

  const deleteConversation = async (id: number) => {
    try {
      await conversationService.deleteConversation(id);
      await refreshConversations();
      return true;
    } catch (error) {
      console.error('Failed to delete conversation:', error);
      return false;
    }
  };

  return {
    conversations,
    isLoading,
    refreshConversations,
    deleteConversation,
  };
};
