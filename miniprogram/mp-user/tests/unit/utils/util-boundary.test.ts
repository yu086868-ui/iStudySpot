import { formatTime } from '../miniprogram/utils/util';

describe('util.ts - Boundary Tests', () => {
  describe('formatTime - Edge Cases', () => {
    it('should handle year boundary (December 31 to January 1)', () => {
      const date = new Date(2024, 11, 31, 23, 59, 59);
      expect(formatTime(date)).toBe('2024/12/31 23:59:59');
    });

    it('should handle leap year date (February 29)', () => {
      const date = new Date(2024, 1, 29, 12, 0, 0);
      expect(formatTime(date)).toBe('2024/02/29 12:00:00');
    });

    it('should handle first day of year', () => {
      const date = new Date(2024, 0, 1, 0, 0, 0);
      expect(formatTime(date)).toBe('2024/01/01 00:00:00');
    });

    it('should handle last second of day', () => {
      const date = new Date(2024, 5, 15, 23, 59, 59);
      expect(formatTime(date)).toBe('2024/06/15 23:59:59');
    });

    it('should handle single digit month and day', () => {
      const date = new Date(2024, 0, 5, 9, 5, 5);
      expect(formatTime(date)).toBe('2024/01/05 09:05:05');
    });

    it('should handle maximum time values', () => {
      const date = new Date(2024, 11, 31, 23, 59, 59);
      const result = formatTime(date);
      expect(result).toContain('23:59:59');
    });

    it('should handle minimum time values', () => {
      const date = new Date(2024, 0, 1, 0, 0, 0);
      const result = formatTime(date);
      expect(result).toContain('00:00:00');
    });

    it('should handle non-leap year February 28', () => {
      const date = new Date(2023, 1, 28, 12, 0, 0);
      expect(formatTime(date)).toBe('2023/02/28 12:00:00');
    });

    it('should handle midnight correctly', () => {
      const date = new Date(2024, 5, 15, 0, 0, 0);
      expect(formatTime(date)).toBe('2024/06/15 00:00:00');
    });

    it('should handle noon correctly', () => {
      const date = new Date(2024, 5, 15, 12, 0, 0);
      expect(formatTime(date)).toBe('2024/06/15 12:00:00');
    });
  });
});
