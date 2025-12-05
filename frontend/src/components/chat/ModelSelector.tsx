import React from 'react';
import type { ModelInfo } from '@/types';

interface ModelSelectorProps {
  models: ModelInfo[];
  selectedModel: string;
  onModelChange: (modelId: string) => void;
  isLoading?: boolean;
}

export const ModelSelector: React.FC<ModelSelectorProps> = ({
  models,
  selectedModel,
  onModelChange,
  isLoading = false,
}) => {
  return (
    <div className="flex items-center gap-3">
      <label htmlFor="model-select" className="text-sm font-medium text-zinc-400 whitespace-nowrap">
        Model:
      </label>
      <select
        id="model-select"
        value={selectedModel}
        onChange={(e) => onModelChange(e.target.value)}
        disabled={isLoading}
        className="flex-1 max-w-xs px-4 py-2 bg-zinc-900 border border-zinc-800 rounded-lg text-zinc-200 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-700 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed transition-all"
      >
        {models.map((model) => (
          <option key={model.id} value={model.id} className="bg-zinc-900">
            {model.name} {model.free && '(Free)'}
          </option>
        ))}
      </select>
    </div>
  );
};
