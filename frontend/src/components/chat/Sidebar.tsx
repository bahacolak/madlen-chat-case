import React, { useState } from 'react';
import type { Conversation } from '@/types';

interface SidebarProps {
  conversations: Conversation[];
  selectedConversationId: number | null;
  onSelectConversation: (id: number) => void;
  onDeleteConversation: (id: number) => void;
  onCreateNew: () => void;
  isLoading?: boolean;
  user?: { username: string; email: string } | null;
  onLogout: () => void;
  isMobileOpen: boolean;
  onMobileToggle: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  conversations,
  selectedConversationId,
  onSelectConversation,
  onDeleteConversation,
  onCreateNew,
  isLoading = false,
  user,
  onLogout,
  isMobileOpen,
  onMobileToggle,
}) => {
  const [showSettings, setShowSettings] = useState(false);

  const groupConversationsByDate = (convs: Conversation[]) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const weekAgo = new Date(today);
    weekAgo.setDate(weekAgo.getDate() - 7);

    const groups: {
      today: Conversation[];
      yesterday: Conversation[];
      week: Conversation[];
      older: Conversation[];
    } = {
      today: [],
      yesterday: [],
      week: [],
      older: [],
    };

    convs.forEach((conv) => {
      const convDate = new Date(conv.updatedAt);
      if (convDate >= today) {
        groups.today.push(conv);
      } else if (convDate >= yesterday) {
        groups.yesterday.push(conv);
      } else if (convDate >= weekAgo) {
        groups.week.push(conv);
      } else {
        groups.older.push(conv);
      }
    });

    return groups;
  };

  const groupedConversations = groupConversationsByDate(conversations);

  const renderConversationGroup = (title: string, convs: Conversation[]) => {
    if (convs.length === 0) return null;

    return (
      <div className="mb-6">
        <h3 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider px-3 mb-2">
          {title}
        </h3>
        <div className="space-y-1">
          {convs.map((conversation) => (
            <div
              key={conversation.id}
              className={`group relative px-3 py-2 rounded-lg cursor-pointer transition-colors ${
                selectedConversationId === conversation.id
                  ? 'bg-zinc-800 text-white'
                  : 'text-zinc-300 hover:bg-zinc-800/50'
              }`}
              onClick={() => {
                onSelectConversation(conversation.id);
                onMobileToggle();
              }}
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">
                    {conversation.title || 'New Conversation'}
                  </p>
                  <p className="text-xs text-zinc-500 mt-0.5">
                    {new Date(conversation.updatedAt).toLocaleTimeString('en-US', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    if (window.confirm('Are you sure you want to delete this conversation?')) {
                      onDeleteConversation(conversation.id);
                    }
                  }}
                  className="opacity-0 group-hover:opacity-100 text-zinc-400 hover:text-red-400 transition-all p-1 rounded"
                  title="Sil"
                >
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  return (
    <>
      {/* Mobile overlay */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={onMobileToggle}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed lg:static inset-y-0 left-0 z-50 w-64 bg-zinc-900 border-r border-zinc-800 flex flex-col transition-transform duration-300 ease-in-out ${
          isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Header */}
        <div className="p-4 border-b border-zinc-800 flex-shrink-0">
          <button
            onClick={() => {
              onCreateNew();
              onMobileToggle();
            }}
            className="w-full bg-zinc-800 hover:bg-zinc-700 text-white py-2.5 px-4 rounded-lg transition-colors font-medium text-sm flex items-center justify-center gap-2"
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
                d="M12 4v16m8-8H4"
              />
            </svg>
            New Chat
          </button>
        </div>

        {/* Conversations List */}
        <div className="flex-1 overflow-y-auto min-h-0 py-4">
          {isLoading ? (
            <div className="p-4 text-center text-zinc-400">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-zinc-400 mx-auto"></div>
              <p className="mt-2 text-sm">Loading...</p>
            </div>
          ) : conversations.length === 0 ? (
            <div className="p-4 text-center text-zinc-500 text-sm">
              No conversations yet
            </div>
          ) : (
            <div className="px-2">
              {renderConversationGroup('Today', groupedConversations.today)}
              {renderConversationGroup('Yesterday', groupedConversations.yesterday)}
              {renderConversationGroup('Previous 7 Days', groupedConversations.week)}
              {renderConversationGroup('Older', groupedConversations.older)}
            </div>
          )}
        </div>

        {/* User Profile & Settings */}
        <div className="p-4 border-t border-zinc-800 flex-shrink-0">
          <div className="relative">
            <button
              onClick={() => setShowSettings(!showSettings)}
              className="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-zinc-800 transition-colors text-left"
            >
              <div className="w-8 h-8 rounded-full bg-zinc-700 flex items-center justify-center text-zinc-300 text-sm font-medium flex-shrink-0">
                {user?.username?.charAt(0).toUpperCase() || 'U'}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-zinc-200 truncate">
                  {user?.username || 'User'}
                </p>
                <p className="text-xs text-zinc-500 truncate">
                  {user?.email || ''}
                </p>
              </div>
              <svg
                className={`w-5 h-5 text-zinc-400 transition-transform ${
                  showSettings ? 'rotate-180' : ''
                }`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 9l-7 7-7-7"
                />
              </svg>
            </button>

            {showSettings && (
              <div className="absolute bottom-full left-0 right-0 mb-2 bg-zinc-800 rounded-lg shadow-lg border border-zinc-700 overflow-hidden">
                <button
                  onClick={() => {
                    onLogout();
                    setShowSettings(false);
                  }}
                  className="w-full px-4 py-2 text-left text-sm text-zinc-300 hover:bg-zinc-700 transition-colors flex items-center gap-2"
                >
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                    />
                  </svg>
                  Sign Out
                </button>
              </div>
            )}
          </div>
        </div>
      </aside>
    </>
  );
};

