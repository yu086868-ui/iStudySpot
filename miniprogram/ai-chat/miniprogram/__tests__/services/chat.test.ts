import { sendMessage, sendMessageStream } from '../../services/chat';
import wx from '../mocks/wx';
import type { ChatRequest } from '../../typings/chat';

describe('Chat Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('sendMessage', () => {
    it('should return a promise', () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      const result = sendMessage(request);
      expect(result).toBeInstanceOf(Promise);
    });

    it('should make POST request after connection check', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessage(request);
      const calls = (wx.request as jest.Mock).mock.calls;
      expect(calls.length).toBeGreaterThan(0);
      
      const chatCall = calls.find((call: any[]) => call[0].url.includes('/chat'));
      expect(chatCall).toBeDefined();
      expect(chatCall[0].method).toBe('POST');
    });

    it('should send correct URL', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessage(request);
      const calls = (wx.request as jest.Mock).mock.calls;
      const chatCall = calls.find((call: any[]) => call[0].url.includes('/chat'));
      expect(chatCall[0].url).toContain('/chat');
    });

    it('should include request data', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessage(request);
      const calls = (wx.request as jest.Mock).mock.calls;
      const chatCall = calls.find((call: any[]) => call[0].url.includes('/chat'));
      expect(chatCall[0].data).toEqual(request);
    });

    it('should set correct headers', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessage(request);
      const calls = (wx.request as jest.Mock).mock.calls;
      const chatCall = calls.find((call: any[]) => call[0].url.includes('/chat'));
      expect(chatCall[0].header['Content-Type']).toBe('application/json');
    });
  });

  describe('sendMessageStream', () => {
    const callbacks = {
      onStart: jest.fn(),
      onDelta: jest.fn(),
      onEnd: jest.fn(),
      onError: jest.fn()
    };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should return a promise', () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      const result = sendMessageStream(request, callbacks);
      expect(result).toBeInstanceOf(Promise);
    });

    it('should make POST request to stream endpoint', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessageStream(request, callbacks);
      const calls = (wx.request as jest.Mock).mock.calls;
      expect(calls.length).toBeGreaterThan(0);
      
      const streamCall = calls.find((call: any[]) => call[0].url.includes('/chat/stream'));
      expect(streamCall).toBeDefined();
      expect(streamCall[0].method).toBe('POST');
    });

    it('should set stream headers', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessageStream(request, callbacks);
      const calls = (wx.request as jest.Mock).mock.calls;
      const streamCall = calls.find((call: any[]) => call[0].url.includes('/chat/stream'));
      expect(streamCall[0].header['Accept']).toBe('text/event-stream');
      expect(streamCall[0].header['Cache-Control']).toBe('no-cache');
    });

    it('should enable chunked transfer', async () => {
      const request: ChatRequest = {
        session_id: 'test-session',
        character_id: 'einstein',
        message: 'Hello'
      };
      await sendMessageStream(request, callbacks);
      const calls = (wx.request as jest.Mock).mock.calls;
      const streamCall = calls.find((call: any[]) => call[0].url.includes('/chat/stream'));
      expect(streamCall[0].enableChunked).toBe(true);
    });
  });
});
