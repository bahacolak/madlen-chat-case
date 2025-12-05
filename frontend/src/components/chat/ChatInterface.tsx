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

// Mock data for testing
const mockMessages: Message[] = [
  {
    id: 1,
    role: 'user',
    content: 'Merhaba! React ile nasıl bir state yönetimi yapabilirim?',
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    role: 'assistant',
    content: `React'te state yönetimi için birkaç yaklaşım var:

1. **useState Hook**: Basit component state'i için
\`\`\`javascript
const [count, setCount] = useState(0);
\`\`\`

2. **useReducer**: Karmaşık state logic için
\`\`\`javascript
const [state, dispatch] = useReducer(reducer, initialState);
\`\`\`

3. **Context API**: Global state için
\`\`\`javascript
const ThemeContext = createContext();
\`\`\`

4. **External Libraries**: Redux, Zustand, Jotai gibi

Hangi yaklaşımı kullanmak istediğinize bağlı olarak değişir.`,
    model: 'meta-llama/llama-3.2-3b-instruct:free',
    createdAt: new Date().toISOString(),
  },
  {
    id: 3,
    role: 'user',
    content: 'Teşekkürler! Context API ile bir örnek verebilir misin?',
    createdAt: new Date().toISOString(),
  },
  {
    id: 4,
    role: 'assistant',
    content: `Tabii! İşte Context API ile basit bir örnek:

\`\`\`javascript
// ThemeContext.js
import { createContext, useContext, useState } from 'react';

const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const [theme, setTheme] = useState('dark');
  
  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
};

// Component'te kullanım
const MyComponent = () => {
  const { theme, setTheme } = useTheme();
  
  return (
    <div className={theme === 'dark' ? 'bg-black' : 'bg-white'}>
      <button onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>
        Toggle Theme
      </button>
    </div>
  );
};
\`\`\`

Bu şekilde theme state'ini tüm component'lerde kullanabilirsiniz!`,
    model: 'meta-llama/llama-3.2-3b-instruct:free',
    createdAt: new Date().toISOString(),
  },
];

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
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);
  const [useMockData, setUseMockData] = useState(false);

  // Load models on mount
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

  // Load conversations on mount
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

  // Load messages when conversation is selected
  useEffect(() => {
    if (selectedConversationId && !useMockData) {
      const loadMessages = async () => {
        try {
          const conversation = await conversationService.getConversationById(selectedConversationId);
          setMessages(conversation.messages || []);
        } catch (error) {
          console.error('Failed to load messages:', error);
        }
      };
      loadMessages();
    } else if (useMockData) {
      setMessages(mockMessages);
    } else {
      setMessages([]);
    }
  }, [selectedConversationId, useMockData]);

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

      const userMessage: Message = {
        id: Date.now(),
        role: 'user',
        content: messageText,
        createdAt: new Date().toISOString(),
        imageUrl: image ? `data:image/jpeg;base64,${image}` : undefined,
      };

      const assistantMessage: Message = {
        id: response.messageId,
        role: 'assistant',
        content: response.response,
        model: selectedModel,
        createdAt: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, userMessage, assistantMessage]);

      if (!selectedConversationId) {
        setSelectedConversationId(response.conversationId);
      }

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
    setUseMockData(false);
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
    setUseMockData(false);
  };

  const toggleMockData = () => {
    setUseMockData(!useMockData);
    if (!useMockData) {
      setMessages(mockMessages);
    } else {
      setMessages([]);
    }
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
        {/* Header with Model Selector */}
        <header className="bg-zinc-900 border-b border-zinc-800 px-4 py-3 flex items-center justify-between flex-shrink-0">
          <div className="flex items-center gap-4 flex-1">
            {/* Mobile menu button */}
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

          {/* Mock data toggle (for testing) */}
          <button
            onClick={toggleMockData}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              useMockData
                ? 'bg-zinc-800 text-zinc-300'
                : 'bg-zinc-800/50 text-zinc-500 hover:bg-zinc-800'
            }`}
            title="Toggle mock data"
          >
            {useMockData ? 'Mock: ON' : 'Mock: OFF'}
          </button>
        </header>

        {/* Main Chat Area */}
        <div className="flex-1 flex flex-col overflow-hidden min-h-0">
          <MessageList messages={messages} isLoading={isLoading} />
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
