import { SeatLayoutUtil } from '../../../miniprogram/utils/seat-layout';
import type { Seat } from '../../../miniprogram/typings/api';

function createSeat(overrides: Partial<Seat> = {}): Seat {
  return {
    id: 'seat-1',
    studyRoomId: 'room-1',
    row: 1,
    col: 1,
    seatNumber: 'A1',
    type: 'normal',
    status: 'available',
    facilities: [],
    lastUsedAt: '',
    ...overrides,
  };
}

describe('SeatLayoutUtil', () => {
  describe('createSeatLayout', () => {
    it('returns empty layout for empty array', () => {
      const result = SeatLayoutUtil.createSeatLayout([]);
      expect(result).toEqual({ rows: [], totalRows: 0, totalCols: 0 });
    });

    it('returns empty layout for undefined input', () => {
      const result = SeatLayoutUtil.createSeatLayout(undefined as any);
      expect(result).toEqual({ rows: [], totalRows: 0, totalCols: 0 });
    });

    it('creates layout from flat seat array grouped by row', () => {
      const seats = [
        createSeat({ id: '1', row: 2, col: 2, seatNumber: 'B2' }),
        createSeat({ id: '2', row: 1, col: 1, seatNumber: 'A1' }),
        createSeat({ id: '3', row: 1, col: 2, seatNumber: 'A2' }),
        createSeat({ id: '4', row: 2, col: 1, seatNumber: 'B1' }),
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      expect(result.totalRows).toBe(2);
      expect(result.totalCols).toBe(2);
      expect(result.rows).toHaveLength(2);
      expect(result.rows[0].rowNumber).toBe(1);
      expect(result.rows[0].seats.map(s => s.id)).toEqual(['2', '3']);
      expect(result.rows[1].rowNumber).toBe(2);
      expect(result.rows[1].seats.map(s => s.id)).toEqual(['4', '1']);
    });

    it('sorts seats within each row by col', () => {
      const seats = [
        createSeat({ id: 'a', row: 1, col: 3 }),
        createSeat({ id: 'b', row: 1, col: 1 }),
        createSeat({ id: 'c', row: 1, col: 2 }),
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      expect(result.rows[0].seats.map(s => s.id)).toEqual(['b', 'c', 'a']);
    });

    it('sorts rows by rowNumber', () => {
      const seats = [
        createSeat({ id: 'x', row: 3, col: 1 }),
        createSeat({ id: 'y', row: 1, col: 1 }),
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      expect(result.rows[0].rowNumber).toBe(1);
      expect(result.rows[1].rowNumber).toBe(3);
    });

    it('skips empty rows when seats have gaps in row numbers', () => {
      const seats = [
        createSeat({ id: '1', row: 1, col: 1 }),
        createSeat({ id: '2', row: 3, col: 1 }),
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      expect(result.rows).toHaveLength(2);
      expect(result.totalRows).toBe(3);
    });

    it('handles single seat', () => {
      const seats = [createSeat({ row: 1, col: 1 })];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      expect(result.rows).toHaveLength(1);
      expect(result.totalRows).toBe(1);
      expect(result.totalCols).toBe(1);
    });
  });

  describe('splitIntoGroups', () => {
    it('splits layout into named column groups', () => {
      const seats = [
        createSeat({ id: '1', row: 1, col: 1 }),
        createSeat({ id: '2', row: 1, col: 2 }),
        createSeat({ id: '3', row: 1, col: 3 }),
        createSeat({ id: '4', row: 1, col: 4 }),
      ];
      const config = [
        { startCol: 1, endCol: 2, name: 'left' },
        { startCol: 3, endCol: 4, name: 'right' },
      ];
      const result = SeatLayoutUtil.splitIntoGroups(seats, config);
      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('left');
      expect(result[0].startCol).toBe(1);
      expect(result[0].endCol).toBe(2);
      expect(result[0].rows[0].seats.map(s => s.id)).toEqual(['1', '2']);
      expect(result[1].name).toBe('right');
      expect(result[1].rows[0].seats.map(s => s.id)).toEqual(['3', '4']);
    });

    it('returns empty rows for groups with no matching seats', () => {
      const seats = [createSeat({ row: 1, col: 1 })];
      const config = [
        { startCol: 1, endCol: 1, name: 'left' },
        { startCol: 2, endCol: 3, name: 'right' },
      ];
      const result = SeatLayoutUtil.splitIntoGroups(seats, config);
      expect(result[0].rows).toHaveLength(1);
      expect(result[1].rows).toHaveLength(0);
    });

    it('handles empty seats array', () => {
      const config = [{ startCol: 1, endCol: 2, name: 'main' }];
      const result = SeatLayoutUtil.splitIntoGroups([], config);
      expect(result).toHaveLength(1);
      expect(result[0].rows).toHaveLength(0);
    });
  });

  describe('getSeatStatus', () => {
    it('returns seat status for a valid seat', () => {
      const seat = createSeat({ status: 'occupied' });
      expect(SeatLayoutUtil.getSeatStatus(seat)).toBe('occupied');
    });

    it('returns "empty" for undefined seat', () => {
      expect(SeatLayoutUtil.getSeatStatus(undefined)).toBe('empty');
    });

    it('returns "empty" for null seat', () => {
      expect(SeatLayoutUtil.getSeatStatus(null as any)).toBe('empty');
    });

    it('returns correct status for each possible status', () => {
      expect(SeatLayoutUtil.getSeatStatus(createSeat({ status: 'available' }))).toBe('available');
      expect(SeatLayoutUtil.getSeatStatus(createSeat({ status: 'occupied' }))).toBe('occupied');
      expect(SeatLayoutUtil.getSeatStatus(createSeat({ status: 'reserved' }))).toBe('reserved');
      expect(SeatLayoutUtil.getSeatStatus(createSeat({ status: 'maintenance' }))).toBe('maintenance');
    });
  });

  describe('isSeatSelectable', () => {
    it('returns true when status is "available"', () => {
      expect(SeatLayoutUtil.isSeatSelectable(createSeat({ status: 'available' }))).toBe(true);
    });

    it('returns false for other statuses', () => {
      expect(SeatLayoutUtil.isSeatSelectable(createSeat({ status: 'occupied' }))).toBe(false);
      expect(SeatLayoutUtil.isSeatSelectable(createSeat({ status: 'reserved' }))).toBe(false);
      expect(SeatLayoutUtil.isSeatSelectable(createSeat({ status: 'maintenance' }))).toBe(false);
    });

    it('returns false for undefined seat', () => {
      expect(SeatLayoutUtil.isSeatSelectable(undefined)).toBe(false);
    });

    it('returns false for null seat', () => {
      expect(SeatLayoutUtil.isSeatSelectable(null as any)).toBe(false);
    });
  });

  describe('getSeatType', () => {
    it('returns seat type for a valid seat', () => {
      expect(SeatLayoutUtil.getSeatType(createSeat({ type: 'vip' }))).toBe('vip');
    });

    it('returns "none" for undefined seat', () => {
      expect(SeatLayoutUtil.getSeatType(undefined)).toBe('none');
    });

    it('returns "none" for null seat', () => {
      expect(SeatLayoutUtil.getSeatType(null as any)).toBe('none');
    });

    it('returns correct type for each possible type', () => {
      expect(SeatLayoutUtil.getSeatType(createSeat({ type: 'normal' }))).toBe('normal');
      expect(SeatLayoutUtil.getSeatType(createSeat({ type: 'vip' }))).toBe('vip');
      expect(SeatLayoutUtil.getSeatType(createSeat({ type: 'quiet' }))).toBe('quiet');
    });
  });

  describe('generateSeatNumber', () => {
    it('generates "A1" for row 1, col 1', () => {
      expect(SeatLayoutUtil.generateSeatNumber(1, 1)).toBe('A1');
    });

    it('generates "B3" for row 2, col 3', () => {
      expect(SeatLayoutUtil.generateSeatNumber(2, 3)).toBe('B3');
    });

    it('generates "Z10" for row 26, col 10', () => {
      expect(SeatLayoutUtil.generateSeatNumber(26, 10)).toBe('Z10');
    });

    it('generates "C1" for row 3, col 1', () => {
      expect(SeatLayoutUtil.generateSeatNumber(3, 1)).toBe('C1');
    });
  });

  describe('getAvailableSeats', () => {
    it('filters only available seats', () => {
      const seats = [
        createSeat({ id: '1', status: 'available' }),
        createSeat({ id: '2', status: 'occupied' }),
        createSeat({ id: '3', status: 'available' }),
      ];
      const result = SeatLayoutUtil.getAvailableSeats(seats);
      expect(result).toHaveLength(2);
      expect(result.map(s => s.id)).toEqual(['1', '3']);
    });

    it('returns empty array when no seats are available', () => {
      const seats = [
        createSeat({ status: 'occupied' }),
        createSeat({ status: 'reserved' }),
      ];
      expect(SeatLayoutUtil.getAvailableSeats(seats)).toHaveLength(0);
    });

    it('returns empty array for empty input', () => {
      expect(SeatLayoutUtil.getAvailableSeats([])).toEqual([]);
    });
  });

  describe('getSeatsByType', () => {
    it('filters seats by type', () => {
      const seats = [
        createSeat({ id: '1', type: 'normal' }),
        createSeat({ id: '2', type: 'vip' }),
        createSeat({ id: '3', type: 'normal' }),
      ];
      const result = SeatLayoutUtil.getSeatsByType(seats, 'normal');
      expect(result).toHaveLength(2);
      expect(result.map(s => s.id)).toEqual(['1', '3']);
    });

    it('returns empty array when no seats match type', () => {
      const seats = [createSeat({ type: 'normal' })];
      expect(SeatLayoutUtil.getSeatsByType(seats, 'vip')).toHaveLength(0);
    });

    it('returns empty array for empty input', () => {
      expect(SeatLayoutUtil.getSeatsByType([], 'normal')).toEqual([]);
    });
  });

  describe('getSeatsByStatus', () => {
    it('filters seats by status', () => {
      const seats = [
        createSeat({ id: '1', status: 'reserved' }),
        createSeat({ id: '2', status: 'occupied' }),
        createSeat({ id: '3', status: 'reserved' }),
      ];
      const result = SeatLayoutUtil.getSeatsByStatus(seats, 'reserved');
      expect(result).toHaveLength(2);
      expect(result.map(s => s.id)).toEqual(['1', '3']);
    });

    it('returns empty array when no seats match status', () => {
      const seats = [createSeat({ status: 'available' })];
      expect(SeatLayoutUtil.getSeatsByStatus(seats, 'maintenance')).toHaveLength(0);
    });

    it('returns empty array for empty input', () => {
      expect(SeatLayoutUtil.getSeatsByStatus([], 'available')).toEqual([]);
    });
  });

  describe('calculateSeatStats', () => {
    it('calculates stats for mixed seat statuses', () => {
      const seats = [
        createSeat({ status: 'available' }),
        createSeat({ status: 'available' }),
        createSeat({ status: 'occupied' }),
        createSeat({ status: 'reserved' }),
        createSeat({ status: 'maintenance' }),
      ];
      const result = SeatLayoutUtil.calculateSeatStats(seats);
      expect(result).toEqual({
        total: 5,
        available: 2,
        occupied: 1,
        reserved: 1,
        maintenance: 1,
      });
    });

    it('returns all zeros for empty array', () => {
      const result = SeatLayoutUtil.calculateSeatStats([]);
      expect(result).toEqual({
        total: 0,
        available: 0,
        occupied: 0,
        reserved: 0,
        maintenance: 0,
      });
    });

    it('returns all zeros for non-array input', () => {
      const result = SeatLayoutUtil.calculateSeatStats(null as any);
      expect(result).toEqual({
        total: 0,
        available: 0,
        occupied: 0,
        reserved: 0,
        maintenance: 0,
      });
    });

    it('returns all zeros for undefined input', () => {
      const result = SeatLayoutUtil.calculateSeatStats(undefined as any);
      expect(result).toEqual({
        total: 0,
        available: 0,
        occupied: 0,
        reserved: 0,
        maintenance: 0,
      });
    });

    it('returns all zeros for a string input', () => {
      const result = SeatLayoutUtil.calculateSeatStats('not an array' as any);
      expect(result).toEqual({
        total: 0,
        available: 0,
        occupied: 0,
        reserved: 0,
        maintenance: 0,
      });
    });
  });

  describe('createDefaultGroupConfig', () => {
    it('returns single "main" group for totalCols <= 6', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(6);
      expect(result).toEqual([{ startCol: 1, endCol: 6, name: 'main' }]);
    });

    it('returns single "main" group for totalCols = 1', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(1);
      expect(result).toEqual([{ startCol: 1, endCol: 1, name: 'main' }]);
    });

    it('returns left/right split for totalCols > 6', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(10);
      expect(result).toEqual([
        { startCol: 1, endCol: 5, name: 'left' },
        { startCol: 6, endCol: 10, name: 'right' },
      ]);
    });

    it('returns left/right split for totalCols = 7', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(7);
      expect(result).toEqual([
        { startCol: 1, endCol: 3, name: 'left' },
        { startCol: 4, endCol: 7, name: 'right' },
      ]);
    });

    it('returns left/right split for even totalCols > 6', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(8);
      expect(result).toEqual([
        { startCol: 1, endCol: 4, name: 'left' },
        { startCol: 5, endCol: 8, name: 'right' },
      ]);
    });
  });
});
