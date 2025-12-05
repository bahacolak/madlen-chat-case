import React, { useState, useRef, useEffect } from 'react';

interface MessageInputProps {
  onSendMessage: (message: string, image?: string) => void;
  isLoading?: boolean;
  disabled?: boolean;
}

export const MessageInput: React.FC<MessageInputProps> = ({
  onSendMessage,
  isLoading = false,
  disabled = false,
}) => {
  const [message, setMessage] = useState('');
  const [image, setImage] = useState<string | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      const scrollHeight = textareaRef.current.scrollHeight;
      const maxHeight = 200; // Max height in pixels (about 8-9 lines)
      textareaRef.current.style.height = `${Math.min(scrollHeight, maxHeight)}px`;
    }
  }, [message]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (message.trim() && !isLoading && !disabled) {
      onSendMessage(message.trim(), image || undefined);
      setMessage('');
      setImage(null);
      if (textareaRef.current) {
        textareaRef.current.style.height = 'auto';
      }
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64String = reader.result as string;
        const base64 = base64String.includes(',')
          ? base64String.split(',')[1]
          : base64String;
        setImage(base64);
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <div className="border-t border-zinc-800 bg-zinc-900 p-4 flex-shrink-0">
      {image && (
        <div className="mb-3 relative inline-block">
          <img
            src={`data:image/jpeg;base64,${image}`}
            alt="Preview"
            className="max-w-xs max-h-32 rounded-lg shadow-lg border border-zinc-700"
          />
          <button
            type="button"
            onClick={() => setImage(null)}
            className="absolute top-1 right-1 bg-red-600 hover:bg-red-700 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs transition-colors shadow-md"
          >
            ×
          </button>
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex items-end gap-3">
        <div className="flex-1 relative">
          <textarea
            ref={textareaRef}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSubmit(e);
              }
            }}
            placeholder="Mesajınızı yazın... (Enter ile gönder, Shift+Enter ile satır)"
            rows={1}
            disabled={isLoading || disabled}
            className="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-zinc-100 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-zinc-600 focus:border-transparent resize-none disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            style={{ minHeight: '44px', maxHeight: '200px' }}
          />
        </div>

        <div className="flex items-center gap-2">
          <label className="cursor-pointer bg-zinc-800 hover:bg-zinc-700 p-2.5 rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center border border-zinc-700">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              disabled={isLoading || disabled}
              className="hidden"
            />
            <svg
              className="w-5 h-5 text-zinc-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </label>

          <button
            type="submit"
            disabled={!message.trim() || isLoading || disabled}
            className="bg-zinc-100 hover:bg-white text-zinc-900 px-4 py-2.5 rounded-xl font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            {isLoading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-zinc-900"></div>
                <span>Gönderiliyor...</span>
              </>
            ) : (
              <>
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
                    d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                  />
                </svg>
                <span>Gönder</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
