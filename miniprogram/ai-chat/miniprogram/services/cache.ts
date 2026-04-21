import type { Message } from '../typings/chat';

const STORAGE_KEYS = {
  CHAT_HISTORY: 'ai_chat_history',
  CURRENT_CHARACTER: 'ai_current_character'
};

interface ChatHistoryItem {
  characterId: string;
  messages: Message[];
  updatedAt: number;
}

class CacheService {
  saveChatHistory(characterId: string, messages: Message[]): void {
    try {
      const history = this.getAllChatHistory();
      const existingIndex = history.findIndex(item => item.characterId === characterId);
      const newItem: ChatHistoryItem = {
        characterId,
        messages,
        updatedAt: Date.now()
      };

      if (existingIndex >= 0) {
        history[existingIndex] = newItem;
      } else {
        history.push(newItem);
      }

      wx.setStorageSync(STORAGE_KEYS.CHAT_HISTORY, JSON.stringify(history));
    } catch (e) {
      console.error('Failed to save chat history:', e);
    }
  }

  getChatHistory(characterId: string): Message[] {
    try {
      const historyStr = wx.getStorageSync(STORAGE_KEYS.CHAT_HISTORY);
      if (!historyStr) return [];

      const history: ChatHistoryItem[] = JSON.parse(historyStr);
      const item = history.find(h => h.characterId === characterId);
      return item ? item.messages : [];
    } catch (e) {
      console.error('Failed to get chat history:', e);
      return [];
    }
  }

  getAllChatHistory(): ChatHistoryItem[] {
    try {
      const historyStr = wx.getStorageSync(STORAGE_KEYS.CHAT_HISTORY);
      return historyStr ? JSON.parse(historyStr) : [];
    } catch (e) {
      console.error('Failed to get all chat history:', e);
      return [];
    }
  }

  clearChatHistory(characterId?: string): void {
    try {
      if (characterId) {
        const history = this.getAllChatHistory();
        const filtered = history.filter(item => item.characterId !== characterId);
        wx.setStorageSync(STORAGE_KEYS.CHAT_HISTORY, JSON.stringify(filtered));
      } else {
        wx.removeStorageSync(STORAGE_KEYS.CHAT_HISTORY);
      }
    } catch (e) {
      console.error('Failed to clear chat history:', e);
    }
  }

  saveCurrentCharacter(characterId: string): void {
    try {
      wx.setStorageSync(STORAGE_KEYS.CURRENT_CHARACTER, characterId);
    } catch (e) {
      console.error('Failed to save current character:', e);
    }
  }

  getCurrentCharacter(): string | null {
    try {
      return wx.getStorageSync(STORAGE_KEYS.CURRENT_CHARACTER) || null;
    } catch (e) {
      console.error('Failed to get current character:', e);
      return null;
    }
  }
}

const cacheService = new CacheService();

export default cacheService;
