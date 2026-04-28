import { formatTime } from '../../miniprogram/utils/util';

describe('util.ts', () => {
  describe('formatTime', () => {
    it('should format date correctly', () => {
      const date = new Date(2024, 0, 15, 14, 30, 45);
      const result = formatTime(date);
      expect(result).toBe('2024/01/15 14:30:45');
    });

    it('should pad single digit numbers with zero', () => {
      const date = new Date(2024, 0, 5, 9, 5, 5);
      const result = formatTime(date);
      expect(result).toBe('2024/01/05 09:05:05');
    });

    it('should handle month correctly (0-indexed)', () => {
      const date = new Date(2024, 11, 15, 14, 30, 45);
      const result = formatTime(date);
      expect(result).toBe('2024/12/15 14:30:45');
    });

    it('should handle midnight correctly', () => {
      const date = new Date(2024, 5, 1, 0, 0, 0);
      const result = formatTime(date);
      expect(result).toBe('2024/06/01 00:00:00');
    });

    it('should handle end of day correctly', () => {
      const date = new Date(2024, 5, 1, 23, 59, 59);
      const result = formatTime(date);
      expect(result).toBe('2024/06/01 23:59:59');
    });
  });

  describe('formatNumber (internal)', () => {
    it('should pad single digit numbers', () => {
      const date = new Date(2024, 0, 5, 9, 5, 5);
      const result = formatTime(date);
      expect(result).toMatch(/09:05:05/);
    });

    it('should not pad double digit numbers', () => {
      const date = new Date(2024, 0, 15, 14, 30, 45);
      const result = formatTime(date);
      expect(result).toMatch(/14:30:45/);
    });
  });
});
