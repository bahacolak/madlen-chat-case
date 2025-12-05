import React, { useEffect, useRef } from 'react';
import type { Message } from '@/types';

interface MessageListProps {
  messages: Message[];
  isLoading?: boolean;
}

export const MessageList: React.FC<MessageListProps> = ({ messages, isLoading }) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  if (messages.length === 0 && !isLoading) {
    return (
      <div className="flex items-center justify-center h-full text-zinc-400">
        <div className="text-center">
          <svg
            className="w-16 h-16 mx-auto mb-4 text-zinc-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>
          <p className="text-lg font-medium">Henüz mesaj yok</p>
          <p className="text-sm mt-1">İlk mesajınızı göndererek başlayın!</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto px-4 py-6 space-y-6 bg-zinc-950">
      {messages.map((message) => {
        const isUser = message.role === 'user';

        return (
          <div
            key={message.id}
            className={`flex gap-4 ${isUser ? 'justify-end' : 'justify-start'}`}
          >
            {!isUser && (
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-zinc-800 flex items-center justify-center text-zinc-400">
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
                    d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                  />
                </svg>
              </div>
            )}

            <div
              className={`max-w-[85%] lg:max-w-[70%] rounded-2xl px-4 py-3 ${
                isUser
                  ? 'bg-zinc-800 text-zinc-100'
                  : 'bg-zinc-900 text-zinc-200 border border-zinc-800'
              }`}
            >
              {/* Code block detection */}
              {message.content.includes('```') ? (
                <div className="whitespace-pre-wrap break-words">
                  {message.content.split('```').map((part, index) => {
                    if (index % 2 === 1) {
                      // Code block
                      const lines = part.split('\n');
                      const language = lines[0] || '';
                      const code = lines.slice(1).join('\n');
                      return (
                        <pre
                          key={index}
                          className="bg-zinc-950 rounded-lg p-4 overflow-x-auto my-2 text-sm font-mono"
                        >
                          <code className="text-zinc-300">{code}</code>
                        </pre>
                      );
                    }
                    return <span key={index}>{part}</span>;
                  })}
                </div>
              ) : (
                <div className="whitespace-pre-wrap break-words text-sm leading-relaxed">
                  {message.content}
                </div>
              )}

              {message.imageUrl && (
                <img
                  src={message.imageUrl}
                  alt="Attached"
                  className="mt-3 max-w-full rounded-lg max-h-64 object-contain"
                />
              )}

              {message.model && (
                <div className="mt-2 text-xs text-zinc-500 flex items-center gap-1">
                  <svg
                    className="w-3 h-3"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                    />
                  </svg>
                  {message.model}
                </div>
              )}
            </div>

            {isUser && (
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-zinc-800 flex items-center justify-center text-zinc-400">
                {message.content.charAt(0).toUpperCase()}
              </div>
            )}
          </div>
        );
      })}

      {isLoading && (
        <div className="flex justify-start gap-4">
          <div className="flex-shrink-0 w-8 h-8 rounded-full bg-zinc-800 flex items-center justify-center text-zinc-400">
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
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
          </div>
          <div className="bg-zinc-900 border border-zinc-800 rounded-2xl px-4 py-3">
            <div className="flex items-center gap-2">
              <div className="flex gap-1">
                <div className="w-2 h-2 bg-zinc-500 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                <div className="w-2 h-2 bg-zinc-500 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                <div className="w-2 h-2 bg-zinc-500 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
              <span className="text-sm text-zinc-400">Yanıt bekleniyor...</span>
            </div>
          </div>
        </div>
      )}

      <div ref={messagesEndRef} />
    </div>
  );
};
