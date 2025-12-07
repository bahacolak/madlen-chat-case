import React from 'react';
import { ErrorIcon, CloseIcon } from '@/components/icons/IconComponents';

interface ErrorBannerProps {
  errorMessage: string | null;
  onDismiss: () => void;
}

export const ErrorBanner: React.FC<ErrorBannerProps> = ({ errorMessage, onDismiss }) => {
  if (!errorMessage) return null;

  const isRateLimit = errorMessage.includes('429') || errorMessage.includes('Too Many Requests');

  return (
    <div className="bg-red-900/50 border-b border-red-800 px-4 py-3 flex items-center justify-between flex-shrink-0">
      <div className="flex items-center gap-3 flex-1">
        <ErrorIcon className="w-5 h-5 text-red-400 flex-shrink-0" />
        <div className="flex-1">
          <p className="text-sm font-medium text-red-200">
            {isRateLimit ? 'Rate Limit Exceeded' : 'Error'}
          </p>
          <p className="text-xs text-red-300/80 mt-0.5">{errorMessage}</p>
        </div>
      </div>
      <button
        onClick={onDismiss}
        className="text-red-400 hover:text-red-300 transition-colors p-1"
        aria-label="Close error"
      >
        <CloseIcon className="w-5 h-5" />
      </button>
    </div>
  );
};
