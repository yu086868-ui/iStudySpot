import request from '../../services/request';
import wx from '../mocks/wx';

describe('Request Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('get method', () => {
    it('should make a GET request', async () => {
      await request.get('/characters');
      expect(wx.request).toHaveBeenCalled();
    });

    it('should pass correct URL', async () => {
      await request.get('/characters');
      const calls = (wx.request as jest.Mock).mock.calls;
      expect(calls.length).toBeGreaterThan(0);
      const callArgs = calls[0][0];
      expect(callArgs.url).toContain('/characters');
    });

    it('should use GET method', async () => {
      await request.get('/characters');
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.method).toBe('GET');
    });

    it('should include data parameter when provided', async () => {
      await request.get('/characters', { id: 1 });
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.data).toEqual({ id: 1 });
    });
  });

  describe('post method', () => {
    it('should make a POST request', async () => {
      await request.post('/chat', { name: 'test' });
      expect(wx.request).toHaveBeenCalled();
    });

    it('should use POST method', async () => {
      await request.post('/chat');
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.method).toBe('POST');
    });

    it('should include data in request body', async () => {
      const testData = { message: 'hello' };
      await request.post('/chat', testData);
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.data).toEqual(testData);
    });
  });

  describe('request method', () => {
    it('should set Content-Type header by default', async () => {
      await request.get('/characters');
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.header['Content-Type']).toBe('application/json');
    });

    it('should merge custom headers', async () => {
      await request.request({
        url: '/characters',
        method: 'GET',
        header: { 'X-Custom': 'value' }
      });
      const calls = (wx.request as jest.Mock).mock.calls;
      const callArgs = calls[0][0];
      expect(callArgs.header['X-Custom']).toBe('value');
    });

    it('should handle successful response', async () => {
      const result = await request.get('/characters');
      expect(result).toBeDefined();
    });
  });
});
