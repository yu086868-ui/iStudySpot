import { getCharacters } from '../../services/character';
import wx from '../mocks/wx';

describe('Character Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getCharacters', () => {
    it('should return a promise', () => {
      const result = getCharacters();
      expect(result).toBeInstanceOf(Promise);
    });

    it('should return an array of characters', async () => {
      const characters = await getCharacters();
      expect(Array.isArray(characters)).toBe(true);
    });

    it('should return characters with required properties', async () => {
      const characters = await getCharacters();
      characters.forEach(character => {
        expect(character).toHaveProperty('id');
        expect(character).toHaveProperty('name');
        expect(character).toHaveProperty('persona');
        expect(character).toHaveProperty('speaking_style');
      });
    });

    it('should make GET request to characters endpoint', async () => {
      await getCharacters();
      const calls = (wx.request as jest.Mock).mock.calls;
      expect(calls.length).toBeGreaterThan(0);
      
      const charactersCall = calls.find((call: any[]) => call[0].url.includes('/characters'));
      expect(charactersCall).toBeDefined();
      expect(charactersCall[0].method).toBe('GET');
    });

    it('should return mock data when connection fails', async () => {
      (wx.request as jest.Mock).mockImplementationOnce((options: any) => {
        if (options.fail) {
          setTimeout(() => options.fail(new Error('Network error')), 10);
        }
        return { onChunkReceived: jest.fn(), abort: jest.fn() };
      });

      const characters = await getCharacters();
      expect(Array.isArray(characters)).toBe(true);
      expect(characters.length).toBeGreaterThan(0);
    });
  });
});
