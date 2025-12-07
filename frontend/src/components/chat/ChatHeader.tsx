import React from 'react';
import { ModelSelector } from './ModelSelector';
import { MenuIcon } from '@/components/icons/IconComponents';
import type { ModelInfo } from '@/types';

interface ChatHeaderProps {
  models: ModelInfo[];
  selectedModel: string;
  onModelChange: (modelId: string) => void;
  isLoadingModels: boolean;
  onToggleMobileSidebar: () => void;
}

export const ChatHeader: React.FC<ChatHeaderProps> = ({
  models,
  selectedModel,
  onModelChange,
  isLoadingModels,
  onToggleMobileSidebar,
}) => {
  return (
    <header className="bg-zinc-900 border-b border-zinc-800 px-4 py-3 flex items-center justify-between flex-shrink-0">
      <div className="flex items-center gap-4 flex-1">
        <button
          onClick={onToggleMobileSidebar}
          className="lg:hidden p-2 rounded-lg hover:bg-zinc-800 transition-colors"
        >
          <MenuIcon className="w-6 h-6 text-zinc-400" />
        </button>

        <ModelSelector
          models={models}
          selectedModel={selectedModel}
          onModelChange={onModelChange}
          isLoading={isLoadingModels}
        />
      </div>
    </header>
  );
};
