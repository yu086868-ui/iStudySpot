import { formatTime } from '../../utils/util';

describe('formatTime', () => {
  it('should format date correctly', () => {
    const date = new Date(2024, 0, 15, 10, 30, 45);
    const result = formatTime(date);
    expect(result).toBe('2024/01/15 10:30:45');
  });

  it('should pad single digit numbers with zero', () => {
    const date = new Date(2024, 0, 5, 5, 5, 5);
    const result = formatTime(date);
    expect(result).toBe('2024/01/05 05:05:05');
  });

  it('should handle month correctly (0-indexed)', () => {
    const date = new Date(2024, 11, 25, 12, 0, 0);
    const result = formatTime(date);
    expect(result).toBe('2024/12/25 12:00:00');
  });

  it('should format current date without error', () => {
    const date = new Date();
    expect(() => formatTime(date)).not.toThrow();
  });

  it('should return string in correct format', () => {
    const date = new Date(2024, 5, 15, 14, 30, 0);
    const result = formatTime(date);
    expect(result).toMatch(/^\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}:\d{2}$/);
  });
});
