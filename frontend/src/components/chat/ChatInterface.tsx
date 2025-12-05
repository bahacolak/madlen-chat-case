import React, { useState, useEffect } from 'react';
import type { Message, ModelInfo, Conversation } from '@/types';
import { chatService } from '@/services/chatService';
import { modelService } from '@/services/modelService';
import { conversationService } from '@/services/conversationService';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { ModelSelector } from './ModelSelector';
import { ConversationList } from './ConversationList';
import { useAuth } from '@/contexts/AuthContext';

export const ChatInterface: React.FC = () => {
  const { logout, user } = useAuth();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversationId, setSelectedConversationId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [models, setModels] = useState<ModelInfo[]>([]);
  const [selectedModel, setSelectedModel] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingModels, setIsLoadingModels] = useState(true);
  const [isLoadingConversations, setIsLoadingConversations] = useState(true);

  // Load models on mount
  useEffect(() => {
    const loadModels = async () => {
      try {
        const availableModels = await modelService.getAvailableModels();
        setModels(availableModels);
        if (availableModels.length > 0) {
          // Prefer free models
          const freeModel = availableModels.find((m) => m.free);
          setSelectedModel(freeModel?.id || availableModels[0].id);
        }
      } catch (error) {
        console.error('Failed to load models:', error);
      } finally {
        setIsLoadingModels(false);
      }
    };
    loadModels();
  }, []);

  // Load conversations on mount
  useEffect(() => {
    const loadConversations = async () => {
      try {
        const userConversations = await conversationService.getUserConversations();
        setConversations(userConversations);
        if (userConversations.length > 0 && !selectedConversationId) {
          // Select the most recent conversation
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

  // Load messages when conversation is selected
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

    setIsLoading(true);
    try {
      const response = await chatService.sendMessage({
        message: messageText,
        model: selectedModel,
        conversationId: selectedConversationId || undefined,
        image,
      });

      // Add user message
      const userMessage: Message = {
        id: Date.now(),
        role: 'user',
        content: messageText,
        createdAt: new Date().toISOString(),
        imageUrl: image ? `data:image/jpeg;base64,${image}` : undefined,
      };

      // Add assistant response
      const assistantMessage: Message = {
        id: response.messageId,
        role: 'assistant',
        content: response.response,
        model: selectedModel,
        createdAt: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, userMessage, assistantMessage]);

      // Update conversation ID if it was a new conversation
      if (!selectedConversationId) {
        setSelectedConversationId(response.conversationId);
      }

      // Reload conversations to get updated list
      const updatedConversations = await conversationService.getUserConversations();
      setConversations(updatedConversations);
    } catch (error: any) {
      console.error('Failed to send message:', error);
      alert(error.response?.data?.message || 'Mesaj gönderilemedi. Lütfen tekrar deneyin.');
    } finally {
      setIsLoading(false);
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
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      <ConversationList
        conversations={conversations}
        selectedConversationId={selectedConversationId}
        onSelectConversation={handleSelectConversation}
        onDeleteConversation={handleDeleteConversation}
        onCreateNew={handleCreateNewConversation}
        isLoading={isLoadingConversations}
      />
      <div className="flex-1 flex flex-col min-w-0">
        <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between flex-shrink-0">
          <h1 className="text-xl font-semibold text-gray-800">Chat</h1>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-600">{user?.username}</span>
            <button
              onClick={logout}
              className="text-sm text-red-600 hover:text-red-800 transition-colors px-3 py-1 rounded hover:bg-red-50"
            >
              Çıkış Yap
            </button>
          </div>
        </div>
        <div className="flex-1 flex flex-col overflow-hidden min-h-0">
          <div className="p-4 border-b border-gray-200 bg-white flex-shrink-0">
            <ModelSelector
              models={models}
              selectedModel={selectedModel}
              onModelChange={setSelectedModel}
              isLoading={isLoadingModels}
            />
          </div>
          <div className="flex-1 overflow-hidden min-h-0">
            <MessageList messages={messages} isLoading={isLoading} />
          </div>
          <div className="flex-shrink-0">
            <MessageInput
              onSendMessage={handleSendMessage}
              isLoading={isLoading}
              disabled={!selectedModel}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

