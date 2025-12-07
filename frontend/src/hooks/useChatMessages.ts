import { useState, useEffect, useRef } from 'react';
import { conversationService } from '@/services/conversationService';
import type { Message } from '@/types';

export const useChatMessages = (selectedConversationId: number | null) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const isStreamingRef = useRef(false);

  useEffect(() => {
    if (isStreamingRef.current) {
      return;
    }

    if (selectedConversationId) {
      const loadMessages = async () => {
        try {
          const conversation = await conversationService.getConversationById(selectedConversationId);
          setMessages(conversation.messages || []);
        } catch (error) {
          console.error('Failed to load messages:', error);
        }
      };
      loadMessages();
    } else {
      setMessages([]);
    }
  }, [selectedConversationId]);

  const addMessage = (message: Message) => {
    setMessages((prev) => [...prev, message]);
  };

  const updateMessage = (id: number, updater: (msg: Message) => Message) => {
    setMessages((prev) =>
      prev.map((msg) => (msg.id === id ? updater(msg) : msg))
    );
  };

  const removeMessage = (id: number) => {
    setMessages((prev) => prev.filter((msg) => msg.id !== id));
  };

  const clearMessages = () => {
    setMessages([]);
  };

  return {
    messages,
    setMessages,
    addMessage,
    updateMessage,
    removeMessage,
    clearMessages,
    isStreamingRef,
  };
};
