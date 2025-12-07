import React, { useState, useEffect } from 'react';
import type { Message, ModelInfo } from '@/types';
import { modelService } from '@/services/modelService';
import { isImageGenerationModel } from '@/config/models';
import { FALLBACK_MODELS, DEFAULT_MODEL_ID } from '@/constants';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { Sidebar } from './Sidebar';
import { ErrorBanner } from './ErrorBanner';
import { ChatHeader } from './ChatHeader';
import { useAuth } from '@/contexts/AuthContext';
import { useConversations } from '@/hooks/useConversations';
import { useChatMessages } from '@/hooks/useChatMessages';
import { useStreaming } from '@/hooks/useStreaming';

export const ChatInterface: React.FC = () => {
  const { logout, user } = useAuth();
  const [selectedConversationId, setSelectedConversationId] = useState<number | null>(null);
  const [models, setModels] = useState<ModelInfo[]>([]);
  const [selectedModel, setSelectedModel] = useState<string>('');
  const [isLoadingModels, setIsLoadingModels] = useState(true);
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

  const { conversations, isLoading: isLoadingConversations, refreshConversations, deleteConversation } = useConversations();
  const { messages, addMessage, updateMessage, removeMessage, clearMessages, isStreamingRef } = useChatMessages(selectedConversationId);
  
  const [streamingMessageId, setStreamingMessageId] = useState<number>(0);

  const { isLoading, isThinking, errorMessage, sendMessageStream, clearError } = useStreaming({
    onConversationCreated: (conversationId: number) => {
      if (!selectedConversationId) {
        setSelectedConversationId(conversationId);
      }
    },
    onMessageComplete: async (_conversationId: number, messageId: number) => {
      updateMessage(streamingMessageId, (msg) => ({ ...msg, id: messageId, isStreaming: false }));
      await refreshConversations();
      isStreamingRef.current = false;
    },
    onError: () => {
      removeMessage(streamingMessageId);
      isStreamingRef.current = false;
    },
  });

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
        setModels([...FALLBACK_MODELS]);
        setSelectedModel(DEFAULT_MODEL_ID);
      } finally {
        setIsLoadingModels(false);
      }
    };
    loadModels();
  }, []);

  useEffect(() => {
    if (conversations.length > 0 && !selectedConversationId) {
      const mostRecent = conversations.sort(
        (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      )[0];
      setSelectedConversationId(mostRecent.id);
    }
  }, [conversations, selectedConversationId]);

  const handleSendMessage = async (messageText: string, image?: string) => {
    if (!selectedModel) {
      return;
    }

    const userMessage: Message = {
      id: Date.now(),
      role: 'user',
      content: messageText,
      createdAt: new Date().toISOString(),
      imageUrl: image ? `data:image/jpeg;base64,${image}` : undefined,
    };

    const newStreamingMessageId = Date.now() + 1;
    setStreamingMessageId(newStreamingMessageId);
    
    const assistantMessage: Message = {
      id: newStreamingMessageId,
      role: 'assistant',
      content: '',
      model: selectedModel,
      createdAt: new Date().toISOString(),
      isStreaming: true,
    };

    addMessage(userMessage);
    addMessage(assistantMessage);
    isStreamingRef.current = true;

    const currentStreamingId = newStreamingMessageId;
    await sendMessageStream(
      {
        message: messageText,
        model: selectedModel,
        conversationId: selectedConversationId || undefined,
        image,
      },
      currentStreamingId,
      (chunk: string) => {
        updateMessage(currentStreamingId, (msg) => ({
          ...msg,
          content: msg.content + chunk,
        }));
      }
    );
  };

  const handleSelectConversation = (id: number) => {
    setSelectedConversationId(id);
  };

  const handleDeleteConversation = async (id: number) => {
    const success = await deleteConversation(id);
    if (success) {
      if (selectedConversationId === id) {
        setSelectedConversationId(null);
        clearMessages();
      }
    }
  };

  const handleCreateNewConversation = () => {
    setSelectedConversationId(null);
    clearMessages();
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
        <ChatHeader
          models={models}
          selectedModel={selectedModel}
          onModelChange={setSelectedModel}
          isLoadingModels={isLoadingModels}
          onToggleMobileSidebar={() => setIsMobileSidebarOpen(!isMobileSidebarOpen)}
        />

        <ErrorBanner errorMessage={errorMessage} onDismiss={clearError} />

        <div className="flex-1 flex flex-col overflow-hidden min-h-0">
          <MessageList messages={messages} isLoading={isLoading} isThinking={isThinking} />
          <MessageInput
            onSendMessage={handleSendMessage}
            isLoading={isLoading}
            disabled={!selectedModel}
            selectedModelSupportsVision={models.find(m => m.id === selectedModel)?.supportsVision ?? false}
            selectedModelGeneratesImages={isImageGenerationModel(selectedModel)}
          />
        </div>
      </div>
    </div>
  );
};
