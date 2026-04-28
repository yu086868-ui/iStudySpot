import { getMockCharacters, getMockChatReply } from '../../utils/mock';

describe('mock utilities', () => {
  describe('getMockCharacters', () => {
    it('should return an array of characters', () => {
      const characters = getMockCharacters();
      expect(Array.isArray(characters)).toBe(true);
    });

    it('should return characters with required properties', () => {
      const characters = getMockCharacters();
      characters.forEach(character => {
        expect(character).toHaveProperty('id');
        expect(character).toHaveProperty('name');
        expect(character).toHaveProperty('persona');
        expect(character).toHaveProperty('speaking_style');
      });
    });

    it('should return Einstein character', () => {
      const characters = getMockCharacters();
      const einstein = characters.find(c => c.id === 'einstein');
      expect(einstein).toBeDefined();
      expect(einstein?.name).toBe('爱因斯坦');
    });

    it('should return same reference on multiple calls', () => {
      const characters1 = getMockCharacters();
      const characters2 = getMockCharacters();
      expect(characters1).toEqual(characters2);
    });
  });

  describe('getMockChatReply', () => {
    it('should return a string', () => {
      const reply = getMockChatReply();
      expect(typeof reply).toBe('string');
    });

    it('should return non-empty string', () => {
      const reply = getMockChatReply();
      expect(reply.length).toBeGreaterThan(0);
    });

    it('should return consistent reply', () => {
      const reply1 = getMockChatReply();
      const reply2 = getMockChatReply();
      expect(reply1).toBe(reply2);
    });

    it('should return expected mock message', () => {
      const reply = getMockChatReply();
      expect(reply).toBe('请检查链接。');
    });
  });
});
