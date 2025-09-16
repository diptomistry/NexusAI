// Types for conversation API
export interface ConversationMessage {
  id?: number;
  role: 'user' | 'assistant';
  content: string;
  images?: string;
  createdAt?: string;
}

export interface Conversation {
  id?: number;
  userId: string;
  assistantId: string;
  title: string;
  createdAt?: string;
  updatedAt?: string;
  messages?: ConversationMessage[];
}

export interface CreateConversationRequest {
  userId: string;
  assistantId: string;
  title: string;
}

export interface AddMessageRequest {
  conversationId: number;
  role: 'user' | 'assistant';
  content: string;
  images?: string;
}
