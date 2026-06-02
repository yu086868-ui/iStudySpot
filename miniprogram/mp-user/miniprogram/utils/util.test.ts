import { formatTime } from './util'

describe('formatTime', () => {
  it('formats a normal date correctly', () => {
    const date = new Date(2024, 0, 15, 9, 5, 3)
    expect(formatTime(date)).toBe('2024/01/15 09:05:03')
  })

  it('zero-pads single-digit month, day, hour, minute, and second', () => {
    const date = new Date(2024, 2, 5, 3, 7, 1)
    expect(formatTime(date)).toBe('2024/03/05 03:07:01')
  })

  it('does not zero-pad double-digit values', () => {
    const date = new Date(2024, 10, 12, 11, 30, 45)
    expect(formatTime(date)).toBe('2024/11/12 11:30:45')
  })

  it('handles midnight correctly', () => {
    const date = new Date(2024, 5, 1, 0, 0, 0)
    expect(formatTime(date)).toBe('2024/06/01 00:00:00')
  })

  it('handles end of year correctly', () => {
    const date = new Date(2024, 11, 31, 23, 59, 59)
    expect(formatTime(date)).toBe('2024/12/31 23:59:59')
  })

  it('handles leap year date (Feb 29) correctly', () => {
    const date = new Date(2024, 1, 29, 12, 0, 0)
    expect(formatTime(date)).toBe('2024/02/29 12:00:00')
  })
})
