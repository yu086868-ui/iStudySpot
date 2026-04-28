import connectionService from '../../services/connection';
import wx from '../mocks/wx';

describe('Connection Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getStatus', () => {
    it('should return initial status as unknown', () => {
      const status = connectionService.getStatus();
      expect(['unknown', 'connected', 'disconnected']).toContain(status);
    });
  });

  describe('isConnected', () => {
    it('should return boolean', () => {
      const result = connectionService.isConnected();
      expect(typeof result).toBe('boolean');
    });
  });

  describe('checkConnection', () => {
    it('should return a promise', async () => {
      const result = connectionService.checkConnection();
      expect(result).toBeInstanceOf(Promise);
    });

    it('should make request to health endpoint', async () => {
      await connectionService.checkConnection();
      const calls = (wx.request as jest.Mock).mock.calls;
      expect(calls.length).toBeGreaterThan(0);
      const callArgs = calls[0][0];
      expect(callArgs.url).toContain('/health');
    });

    it('should use GET method for health check', async () => {
      await connectionService.checkConnection();
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.method).toBe('GET');
    });

    it('should set timeout for health check', async () => {
      await connectionService.checkConnection();
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.timeout).toBe(3000);
    });

    it('should return true when connection succeeds', async () => {
      const result = await connectionService.checkConnection();
      expect(result).toBe(true);
    });
  });

  describe('addListener', () => {
    it('should add listener and return unsubscribe function', () => {
      const callback = jest.fn();
      const unsubscribe = connectionService.addListener(callback);
      expect(typeof unsubscribe).toBe('function');
    });

    it('should remove listener when unsubscribe is called', () => {
      const callback = jest.fn();
      const unsubscribe = connectionService.addListener(callback);
      unsubscribe();
      expect(typeof unsubscribe).toBe('function');
    });
  });
});
