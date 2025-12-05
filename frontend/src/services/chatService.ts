import apiClient from '@/utils/apiClient';
import type { ChatRequest, ChatResponse } from '@/types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

interface StreamCallbacks {
  onChunk: (chunk: string) => void;
  onInit: (conversationId: number) => void;
  onComplete: (conversationId: number, messageId: number) => void;
  onError: (error: string) => void;
}



const readStream = async (reader: ReadableStreamDefaultReader<Uint8Array>, callbacks: StreamCallbacks) => {
  const decoder = new TextDecoder();
  let buffer = '';
  let currentEvent = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() || '';

    for (const line of lines) {
      if (line.startsWith('event:')) {
        currentEvent = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        const data = line.slice(5).trim();
        if (!data) continue;

        if (currentEvent === 'error') {
          callbacks.onError(data);
          currentEvent = '';
          continue;
        }

        // Try parsing as JSON for init/complete events
        if (data.startsWith('{')) {
          try {
            const json = JSON.parse(data);
            if (json.messageId) {
              callbacks.onComplete(json.conversationId, json.messageId);
            } else if (json.conversationId) {
              callbacks.onInit(json.conversationId);
            }
            currentEvent = '';
            continue;
          } catch {
            // Not valid JSON, treat as content
          }
        }

        callbacks.onChunk(data);
        currentEvent = '';
      }
    }
  }
};

export const chatService = {
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    const response = await apiClient.post<ChatResponse>('/chat', request);
    return response.data;
  },

  async sendMessageStream(
    request: ChatRequest,
    onChunk: (chunk: string) => void,
    onInit: (conversationId: number) => void,
    onComplete: (conversationId: number, messageId: number) => void,
    onError: (error: string) => void
  ): Promise<void> {
    const token = localStorage.getItem('token');
    const callbacks: StreamCallbacks = { onChunk, onInit, onComplete, onError };

    if (!token) {
      onError('Oturum bulunamadı. Lütfen tekrar giriş yapın.');
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          onError('Oturum süresi dolmuş veya geçersiz. Lütfen tekrar giriş yapın.');
          return;
        }
        const errorText = await response.text().catch(() => '');
        throw new Error(errorText || `HTTP ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) throw new Error('No response body');

      await readStream(reader, callbacks);
    } catch (error) {
      onError(error instanceof Error ? error.message : 'Streaming failed');
    }
  },
};
