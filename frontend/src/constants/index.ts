export const ERROR_TIMEOUT = 10000;

export const DEFAULT_CONVERSATION_TITLE = 'New Conversation';
export const MAX_TITLE_LENGTH = 50;

export const API_ENDPOINTS = {
  AUTH: {
    REGISTER: '/auth/register',
    LOGIN: '/auth/login',
  },
  CHAT: {
    SEND: '/chat',
    STREAM: '/chat/stream',
  },
  CONVERSATIONS: {
    BASE: '/conversations',
    BY_ID: (id: number) => `/conversations/${id}`,
  },
  MODELS: '/models',
} as const;

export const FALLBACK_MODELS = [
  { id: 'gpt-4', name: 'GPT-4', free: false },
  { id: 'gemini-pro', name: 'Gemini Pro', free: false },
  { id: 'claude-3.5', name: 'Claude 3.5', free: false },
  { id: 'meta-llama/llama-3.2-3b-instruct:free', name: 'Llama 3.2 3B', free: true },
] as const;

export const DEFAULT_MODEL_ID = 'meta-llama/llama-3.2-3b-instruct:free';

export const ROUTES = {
  LOGIN: '/login',
  REGISTER: '/register',
  CHAT: '/chat',
} as const;
