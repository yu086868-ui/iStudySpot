import cacheService from '../../services/cache';
import wx from '../mocks/wx';
import type { Message } from '../../typings/chat';

describe('Cache Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    wx.clearStorageSync();
  });

  describe('saveChatHistory', () => {
    it('should save chat history to storage', () => {
      const messages: Message[] = [
        { role: 'user', content: 'Hello' },
        { role: 'assistant', content: 'Hi there!' }
      ];
      cacheService.saveChatHistory('test-character', messages);
      expect(wx.setStorageSync).toHaveBeenCalled();
    });

    it('should handle empty messages array', () => {
      cacheService.saveChatHistory('test-character', []);
      expect(wx.setStorageSync).toHaveBeenCalled();
    });
  });

  describe('getChatHistory', () => {
    it('should return empty array when no history exists', () => {
      const result = cacheService.getChatHistory('non-existent');
      expect(Array.isArray(result)).toBe(true);
    });

    it('should retrieve saved chat history', () => {
      const messages: Message[] = [
        { role: 'user', content: 'Test message' }
      ];
      cacheService.saveChatHistory('test-character', messages);
      const result = cacheService.getChatHistory('test-character');
      expect(result).toEqual(messages);
    });
  });

  describe('getAllChatHistory', () => {
    it('should return array', () => {
      const result = cacheService.getAllChatHistory();
      expect(Array.isArray(result)).toBe(true);
    });
  });

  describe('clearChatHistory', () => {
    it('should clear specific character history', () => {
      const messages: Message[] = [
        { role: 'user', content: 'Test' }
      ];
      cacheService.saveChatHistory('char1', messages);
      cacheService.saveChatHistory('char2', messages);
      cacheService.clearChatHistory('char1');
      expect(wx.setStorageSync).toHaveBeenCalled();
    });

    it('should clear all history when no characterId provided', () => {
      cacheService.clearChatHistory();
      expect(wx.removeStorageSync).toHaveBeenCalled();
    });
  });

  describe('saveCurrentCharacter', () => {
    it('should save current character to storage', () => {
      cacheService.saveCurrentCharacter('einstein');
      expect(wx.setStorageSync).toHaveBeenCalled();
    });
  });

  describe('getCurrentCharacter', () => {
    it('should return null when no character saved', () => {
      const result = cacheService.getCurrentCharacter();
      expect(result).toBeNull();
    });

    it('should return saved character id', () => {
      cacheService.saveCurrentCharacter('einstein');
      const result = cacheService.getCurrentCharacter();
      expect(result).toBe('einstein');
    });
  });
});
