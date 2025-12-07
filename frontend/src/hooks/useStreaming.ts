import { useState } from 'react';
import { chatService } from '@/services/chatService';
import { conversationService } from '@/services/conversationService';
import type { ChatRequest, Message } from '@/types';
import { ERROR_TIMEOUT } from '@/constants';

interface UseStreamingCallbacks {
  onConversationCreated?: (conversationId: number) => void;
  onMessageComplete?: (conversationId: number, messageId: number) => void;
  onError?: () => void;
}

export const useStreaming = (callbacks: UseStreamingCallbacks = {}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [isThinking, setIsThinking] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const sendMessageStream = async (
    request: ChatRequest,
    streamingMessageId: number,
    onChunk: (chunk: string) => void
  ) => {
    setIsLoading(true);
    setIsThinking(true);
    setErrorMessage(null);

    let firstChunkReceived = false;

    try {
      await chatService.sendMessageStream(
        request,
        (chunk: string) => {
          const cleanedChunk = chunk.replace(/<br\s*\/?>/gi, '\n');

          if (!firstChunkReceived && cleanedChunk.trim().length > 0) {
            setIsThinking(false);
            setIsLoading(false);
            firstChunkReceived = true;
          }

          onChunk(cleanedChunk);
        },
        (conversationId: number) => {
          callbacks.onConversationCreated?.(conversationId);
        },
        async (conversationId: number, messageId: number) => {
          callbacks.onMessageComplete?.(conversationId, messageId);
          setIsLoading(false);
          setIsThinking(false);
        },
        (error: string) => {
          console.error('Streaming error:', error);
          setIsThinking(false);
          setErrorMessage(error);
          setTimeout(() => setErrorMessage(null), ERROR_TIMEOUT);
        }
      );
    } catch (error) {
      console.error('Failed to send message:', error);
      setIsThinking(false);
      setIsLoading(false);
      const errorMsg =
        error instanceof Error
          ? error.message
          : 'Failed to send message. Please try again.';
      setErrorMessage(errorMsg);
      setTimeout(() => setErrorMessage(null), ERROR_TIMEOUT);
    }
  };

  const clearError = () => {
    setErrorMessage(null);
  };

  return {
    isLoading,
    isThinking,
    errorMessage,
    sendMessageStream,
    clearError,
  };
};
