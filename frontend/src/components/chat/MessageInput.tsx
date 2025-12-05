import React, { useState, useRef } from 'react';

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
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (message.trim() && !isLoading && !disabled) {
      onSendMessage(message.trim(), image || undefined);
      setMessage('');
      setImage(null);
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
        // Remove data URL prefix if present
        const base64 = base64String.includes(',') 
          ? base64String.split(',')[1] 
          : base64String;
        setImage(base64);
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="border-t border-gray-200 p-4 bg-white flex-shrink-0">
      {image && (
        <div className="mb-2 relative inline-block">
          <img
            src={`data:image/jpeg;base64,${image}`}
            alt="Preview"
            className="max-w-xs max-h-32 rounded shadow-sm"
          />
          <button
            type="button"
            onClick={() => setImage(null)}
            className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600 shadow-md"
          >
            ×
          </button>
        </div>
      )}
      <div className="flex items-end space-x-2">
        <div className="flex-1">
          <textarea
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
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
            style={{ minHeight: '44px', maxHeight: '120px' }}
          />
        </div>
        <div className="flex space-x-2">
          <label className="cursor-pointer bg-gray-100 hover:bg-gray-200 px-3 py-2 rounded-lg transition-colors disabled:opacity-50 flex items-center justify-center">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              disabled={isLoading || disabled}
              className="hidden"
            />
            <span className="text-gray-700 text-lg">📷</span>
          </label>
          <button
            type="submit"
            disabled={!message.trim() || isLoading || disabled}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium shadow-sm"
          >
            {isLoading ? 'Gönderiliyor...' : 'Gönder'}
          </button>
        </div>
      </div>
    </form>
  );
};

