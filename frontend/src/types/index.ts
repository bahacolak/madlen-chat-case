// API Response Types
export interface AuthResponse {
  token: string;
  user: User;
}

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface ChatRequest {
  message: string;
  model: string;
  conversationId?: number;
  image?: string;
}

export interface ChatResponse {
  response: string;
  conversationId: number;
  messageId: number;
}

export interface Conversation {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: Message[];
}

export interface Message {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  model?: string;
  imageUrl?: string;
  createdAt: string;
  isStreaming?: boolean;
}

export interface ModelInfo {
  id: string;
  name: string;
  free: boolean;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

