import React, { useState, useRef, useEffect, useCallback } from 'react';

interface MessageInputProps {
  onSendMessage: (message: string, image?: string) => void;
  isLoading?: boolean;
  disabled?: boolean;
  selectedModelSupportsVision?: boolean;
  selectedModelGeneratesImages?: boolean;
}

export const MessageInput: React.FC<MessageInputProps> = ({
  onSendMessage,
  isLoading = false,
  disabled = false,
  selectedModelSupportsVision = false,
  selectedModelGeneratesImages = false,
}) => {
  const [message, setMessage] = useState('');
  const [image, setImage] = useState<string | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      const scrollHeight = textareaRef.current.scrollHeight;
      const maxHeight = 200; // Max height in pixels (about 8-9 lines)
      textareaRef.current.style.height = `${Math.min(scrollHeight, maxHeight)}px`;
    }
  }, [message]);

  const processFile = useCallback((file: File) => {
    if (!file.type.startsWith('image/')) return;

    const reader = new FileReader();
    reader.onloadend = () => {
      const base64String = reader.result as string;
      const base64 = base64String.includes(',')
        ? base64String.split(',')[1]
        : base64String;
      setImage(base64);
    };
    reader.readAsDataURL(file);
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isLoading && !disabled) {
      setIsDragOver(true);
    }
  }, [isLoading, disabled]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    if (isLoading || disabled) return;

    const file = e.dataTransfer.files?.[0];
    if (file) {
      processFile(file);
    }
  }, [isLoading, disabled, processFile]);

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
      processFile(file);
    }
  };

  return (
    <div
      className={`border-t border-zinc-800 bg-zinc-900 p-4 flex-shrink-0 transition-all ${isDragOver ? 'bg-zinc-800 border-2 border-dashed border-indigo-500' : ''
        }`}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {isDragOver && (
        <div className="mb-3 p-4 bg-indigo-900/30 border border-dashed border-indigo-500 rounded-lg flex items-center justify-center gap-2">
          <svg className="w-6 h-6 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
          <span className="text-indigo-300 font-medium">Drop image here</span>
        </div>
      )}

      {image && !selectedModelSupportsVision && !selectedModelGeneratesImages && (
        <div className="mb-3 p-3 bg-red-900/50 border border-red-700 rounded-lg flex items-center gap-2">
          <svg className="w-5 h-5 text-red-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <span className="text-sm text-red-200">
            Selected model doesn't support images. Choose a model with 👁️ icon for image analysis.
          </span>
        </div>
      )}

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
            placeholder="Type your message... (Enter to send, Shift+Enter for new line, or drag & drop image)"
            rows={1}
            disabled={isLoading || disabled}
            className="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-zinc-100 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-zinc-600 focus:border-transparent resize-none disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            style={{ minHeight: '44px', maxHeight: '200px' }}
          />
        </div>

        <div className="flex items-center gap-2">
          <label className="cursor-pointer bg-zinc-800 hover:bg-zinc-700 p-2.5 rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center border border-zinc-700" title="Upload image or drag & drop">
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
            disabled={!message.trim() || isLoading || disabled || (!!image && !selectedModelSupportsVision && !selectedModelGeneratesImages)}
            className="bg-zinc-100 hover:bg-white text-zinc-900 px-4 py-2.5 rounded-xl font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            {isLoading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-zinc-900"></div>
                <span>Sending...</span>
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
                <span>Send</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

