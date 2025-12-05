import React, { useState, useEffect } from 'react';
import type { Message, ModelInfo, Conversation } from '@/types';
import { chatService } from '@/services/chatService';
import { modelService } from '@/services/modelService';
import { conversationService } from '@/services/conversationService';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { ModelSelector } from './ModelSelector';
import { Sidebar } from './Sidebar';
import { useAuth } from '@/contexts/AuthContext';



export const ChatInterface: React.FC = () => {
  const { logout, user } = useAuth();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversationId, setSelectedConversationId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [models, setModels] = useState<ModelInfo[]>([]);
  const [selectedModel, setSelectedModel] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [isThinking, setIsThinking] = useState(false);
  const [isLoadingModels, setIsLoadingModels] = useState(true);
  const [isLoadingConversations, setIsLoadingConversations] = useState(true);
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const loadModels = async () => {
      try {
        const availableModels = await modelService.getAvailableModels();
        setModels(availableModels);
        if (availableModels.length > 0) {
          const freeModel = availableModels.find((m) => m.free);
          setSelectedModel(freeModel?.id || availableModels[0].id);
        }
      } catch (error) {
        console.error('Failed to load models:', error);
        // Use mock models if API fails
        setModels([
          { id: 'gpt-4', name: 'GPT-4', free: false },
          { id: 'gemini-pro', name: 'Gemini Pro', free: false },
          { id: 'claude-3.5', name: 'Claude 3.5', free: false },
          { id: 'meta-llama/llama-3.2-3b-instruct:free', name: 'Llama 3.2 3B', free: true },
        ]);
        setSelectedModel('meta-llama/llama-3.2-3b-instruct:free');
      } finally {
        setIsLoadingModels(false);
      }
    };
    loadModels();
  }, []);

  useEffect(() => {
    const loadConversations = async () => {
      try {
        const userConversations = await conversationService.getUserConversations();
        setConversations(userConversations);
        if (userConversations.length > 0 && !selectedConversationId) {
          const mostRecent = userConversations.sort(
            (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
          )[0];
          setSelectedConversationId(mostRecent.id);
        }
      } catch (error) {
        console.error('Failed to load conversations:', error);
      } finally {
        setIsLoadingConversations(false);
      }
    };
    loadConversations();
  }, []);

  useEffect(() => {
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

  const handleSendMessage = async (messageText: string, image?: string) => {
    if (!selectedModel) {
      alert('Lütfen bir model seçin');
      return;
    }

    const userMessage: Message = {
      id: Date.now(),
      role: 'user',
      content: messageText,
      createdAt: new Date().toISOString(),
      imageUrl: image ? `data:image/jpeg;base64,${image}` : undefined,
    };

    const streamingMessageId = Date.now() + 1;
    const assistantMessage: Message = {
      id: streamingMessageId,
      role: 'assistant',
      content: '',
      model: selectedModel,
      createdAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMessage, assistantMessage]);
    setIsLoading(true);
    setIsThinking(true);
    setErrorMessage(null);

    let firstChunkReceived = false;

    try {
      await chatService.sendMessageStream(
        {
          message: messageText,
          model: selectedModel,
          conversationId: selectedConversationId || undefined,
          image,
        },
        (chunk: string) => {
          if (!firstChunkReceived && chunk.trim().length > 0) {
            setIsThinking(false);
            setIsLoading(false);
            firstChunkReceived = true;
          }

          setMessages((prev) =>
            prev.map((msg) =>
              msg.id === streamingMessageId
                ? { ...msg, content: msg.content + chunk }
                : msg
            )
          );
        },
        (conversationId: number) => {
          if (!selectedConversationId) {
            setSelectedConversationId(conversationId);
          }
        },
        async (_conversationId: number, messageId: number) => {
          setMessages((prev) =>
            prev.map((msg) =>
              msg.id === streamingMessageId
                ? { ...msg, id: messageId }
                : msg
            )
          );

          const updatedConversations = await conversationService.getUserConversations();
          setConversations(updatedConversations);

          setIsLoading(false);
          setIsThinking(false);
        },
        (error: string) => {
          console.error('Streaming error:', error);
          setIsThinking(false);
          setMessages((prev) => prev.filter((msg) => msg.id !== streamingMessageId));
          setErrorMessage(error);
          setTimeout(() => setErrorMessage(null), 10000);
          setTimeout(() => setErrorMessage(null), 10000);
        }
      );
    } catch (error: any) {
      console.error('Failed to send message:', error);
      setIsThinking(false);
      setIsLoading(false);
      const errorMsg = error.response?.data?.message || error.message || 'Mesaj gönderilemedi. Lütfen tekrar deneyin.';
      setErrorMessage(errorMsg);
      setTimeout(() => setErrorMessage(null), 10000);
    }
  };

  const handleSelectConversation = (id: number) => {
    setSelectedConversationId(id);
  };

  const handleDeleteConversation = async (id: number) => {
    try {
      await conversationService.deleteConversation(id);
      const updatedConversations = await conversationService.getUserConversations();
      setConversations(updatedConversations);
      if (selectedConversationId === id) {
        setSelectedConversationId(null);
        setMessages([]);
      }
    } catch (error) {
      console.error('Failed to delete conversation:', error);
      alert('Konuşma silinemedi. Lütfen tekrar deneyin.');
    }
  };

  const handleCreateNewConversation = () => {
    setSelectedConversationId(null);
    setMessages([]);
  };

  return (
    <div className="flex h-screen bg-zinc-950 text-zinc-100 overflow-hidden">
      <Sidebar
        conversations={conversations}
        selectedConversationId={selectedConversationId}
        onSelectConversation={handleSelectConversation}
        onDeleteConversation={handleDeleteConversation}
        onCreateNew={handleCreateNewConversation}
        isLoading={isLoadingConversations}
        user={user}
        onLogout={logout}
        isMobileOpen={isMobileSidebarOpen}
        onMobileToggle={() => setIsMobileSidebarOpen(!isMobileSidebarOpen)}
      />

      <div className="flex-1 flex flex-col min-w-0 lg:ml-0">
        <header className="bg-zinc-900 border-b border-zinc-800 px-4 py-3 flex items-center justify-between flex-shrink-0">
          <div className="flex items-center gap-4 flex-1">
            <button
              onClick={() => setIsMobileSidebarOpen(!isMobileSidebarOpen)}
              className="lg:hidden p-2 rounded-lg hover:bg-zinc-800 transition-colors"
            >
              <svg
                className="w-6 h-6 text-zinc-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>

            <ModelSelector
              models={models}
              selectedModel={selectedModel}
              onModelChange={setSelectedModel}
              isLoading={isLoadingModels}
            />
          </div>


        </header>

        {errorMessage && (
          <div className="bg-red-900/50 border-b border-red-800 px-4 py-3 flex items-center justify-between flex-shrink-0">
            <div className="flex items-center gap-3 flex-1">
              <svg
                className="w-5 h-5 text-red-400 flex-shrink-0"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div className="flex-1">
                <p className="text-sm font-medium text-red-200">
                  {errorMessage.includes('429') || errorMessage.includes('Too Many Requests')
                    ? 'Rate Limit Aşıldı'
                    : 'Hata'}
                </p>
                <p className="text-xs text-red-300/80 mt-0.5">{errorMessage}</p>
              </div>
            </div>
            <button
              onClick={() => setErrorMessage(null)}
              className="text-red-400 hover:text-red-300 transition-colors p-1"
              aria-label="Close error"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        )}

        <div className="flex-1 flex flex-col overflow-hidden min-h-0">
          <MessageList messages={messages} isLoading={isLoading} isThinking={isThinking} />
          <MessageInput
            onSendMessage={handleSendMessage}
            isLoading={isLoading}
            disabled={!selectedModel}
          />
        </div>
      </div>
    </div>
  );
};
