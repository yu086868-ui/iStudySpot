export type MessageRole = 'user' | 'assistant';

export interface Message {
  role: MessageRole;
  content: string;
  timestamp?: number;
}

export interface Session {
  session_id: string;
  character_id: string;
  messages: Message[];
}

export interface ChatRequest {
  session_id: string;
  character_id: string;
  message: string;
}

export interface ChatResponse {
  reply: string;
}

export type SSEEventType = 'start' | 'delta' | 'end' | 'error';

export interface SSEEvent {
  type: SSEEventType;
  content?: string;
  message?: string;
}
