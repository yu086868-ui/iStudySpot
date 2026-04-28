import { SeatLayoutUtil } from '../miniprogram/utils/seat-layout';
import { TestDataFactory } from '../tests/utils/test-data-factory';

describe('SeatLayoutUtil', () => {
  describe('createSeatLayout', () => {
    it('should create layout from empty seats', () => {
      const result = SeatLayoutUtil.createSeatLayout([]);
      expect(result).toEqual({
        rows: [],
        totalRows: 0,
        totalCols: 0
      });
    });

    it('should create layout from single seat', () => {
      const seats = [TestDataFactory.createSeat({ row: 1, col: 1 })];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.totalRows).toBe(1);
      expect(result.totalCols).toBe(1);
      expect(result.rows).toHaveLength(1);
      expect(result.rows[0].rowNumber).toBe(1);
      expect(result.rows[0].seats).toHaveLength(1);
    });

    it('should create layout with multiple rows and columns', () => {
      const seats = TestDataFactory.createSeats(12);
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.totalRows).toBe(2);
      expect(result.totalCols).toBe(6);
      expect(result.rows).toHaveLength(2);
    });

    it('should sort rows by row number', () => {
      const seats = [
        TestDataFactory.createSeat({ id: 'seat_1', row: 3, col: 1 }),
        TestDataFactory.createSeat({ id: 'seat_2', row: 1, col: 1 }),
        TestDataFactory.createSeat({ id: 'seat_3', row: 2, col: 1 })
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.rows[0].rowNumber).toBe(1);
      expect(result.rows[1].rowNumber).toBe(2);
      expect(result.rows[2].rowNumber).toBe(3);
    });

    it('should sort seats by column number within row', () => {
      const seats = [
        TestDataFactory.createSeat({ id: 'seat_1', row: 1, col: 3 }),
        TestDataFactory.createSeat({ id: 'seat_2', row: 1, col: 1 }),
        TestDataFactory.createSeat({ id: 'seat_3', row: 1, col: 2 })
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.rows[0].seats[0].col).toBe(1);
      expect(result.rows[0].seats[1].col).toBe(2);
      expect(result.rows[0].seats[2].col).toBe(3);
    });
  });

  describe('splitIntoGroups', () => {
    it('should split seats into groups based on config', () => {
      const seats = TestDataFactory.createSeats(12);
      const groupConfig = [
        { startCol: 1, endCol: 3, name: 'left' },
        { startCol: 4, endCol: 6, name: 'right' }
      ];
      
      const result = SeatLayoutUtil.splitIntoGroups(seats, groupConfig);
      
      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('left');
      expect(result[1].name).toBe('right');
    });

    it('should handle overlapping groups', () => {
      const seats = TestDataFactory.createSeats(6);
      const groupConfig = [
        { startCol: 1, endCol: 4, name: 'group1' },
        { startCol: 3, endCol: 6, name: 'group2' }
      ];
      
      const result = SeatLayoutUtil.splitIntoGroups(seats, groupConfig);
      
      expect(result).toHaveLength(2);
      expect(result[0].seats.length).toBeGreaterThan(0);
      expect(result[1].seats.length).toBeGreaterThan(0);
    });
  });

  describe('getSeatStatus', () => {
    it('should return empty for undefined seat', () => {
      expect(SeatLayoutUtil.getSeatStatus(undefined)).toBe('empty');
    });

    it('should return seat status', () => {
      const seat = TestDataFactory.createSeat({ status: 'available' });
      expect(SeatLayoutUtil.getSeatStatus(seat)).toBe('available');
    });

    it('should return occupied status', () => {
      const seat = TestDataFactory.createSeat({ status: 'occupied' });
      expect(SeatLayoutUtil.getSeatStatus(seat)).toBe('occupied');
    });
  });

  describe('isSeatSelectable', () => {
    it('should return false for undefined seat', () => {
      expect(SeatLayoutUtil.isSeatSelectable(undefined)).toBe(false);
    });

    it('should return true for available seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'available' });
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(true);
    });

    it('should return false for occupied seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'occupied' });
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(false);
    });

    it('should return false for reserved seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'reserved' });
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(false);
    });

    it('should return false for maintenance seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'maintenance' });
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(false);
    });
  });

  describe('getSeatType', () => {
    it('should return none for undefined seat', () => {
      expect(SeatLayoutUtil.getSeatType(undefined)).toBe('none');
    });

    it('should return seat type', () => {
      const seat = TestDataFactory.createSeat({ type: 'vip' });
      expect(SeatLayoutUtil.getSeatType(seat)).toBe('vip');
    });

    it('should return normal type', () => {
      const seat = TestDataFactory.createSeat({ type: 'normal' });
      expect(SeatLayoutUtil.getSeatType(seat)).toBe('normal');
    });

    it('should return quiet type', () => {
      const seat = TestDataFactory.createSeat({ type: 'quiet' });
      expect(SeatLayoutUtil.getSeatType(seat)).toBe('quiet');
    });
  });

  describe('generateSeatNumber', () => {
    it('should generate seat number for row 1', () => {
      expect(SeatLayoutUtil.generateSeatNumber(1, 5)).toBe('A5');
    });

    it('should generate seat number for row 26', () => {
      expect(SeatLayoutUtil.generateSeatNumber(26, 10)).toBe('Z10');
    });

    it('should generate seat number for row 2', () => {
      expect(SeatLayoutUtil.generateSeatNumber(2, 3)).toBe('B3');
    });
  });

  describe('getAvailableSeats', () => {
    it('should filter available seats', () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', status: 'available' }),
        TestDataFactory.createSeat({ id: '2', status: 'occupied' }),
        TestDataFactory.createSeat({ id: '3', status: 'available' })
      ];
      
      const result = SeatLayoutUtil.getAvailableSeats(seats);
      
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('1');
      expect(result[1].id).toBe('3');
    });

    it('should return empty array if no available seats', () => {
      const seats = [
        TestDataFactory.createSeat({ status: 'occupied' }),
        TestDataFactory.createSeat({ status: 'reserved' })
      ];
      
      const result = SeatLayoutUtil.getAvailableSeats(seats);
      
      expect(result).toHaveLength(0);
    });
  });

  describe('getSeatsByType', () => {
    it('should filter seats by type', () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', type: 'normal' }),
        TestDataFactory.createSeat({ id: '2', type: 'vip' }),
        TestDataFactory.createSeat({ id: '3', type: 'normal' })
      ];
      
      const result = SeatLayoutUtil.getSeatsByType(seats, 'normal');
      
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('1');
      expect(result[1].id).toBe('3');
    });

    it('should return empty array if no matching type', () => {
      const seats = [
        TestDataFactory.createSeat({ type: 'normal' }),
        TestDataFactory.createSeat({ type: 'normal' })
      ];
      
      const result = SeatLayoutUtil.getSeatsByType(seats, 'vip');
      
      expect(result).toHaveLength(0);
    });
  });

  describe('getSeatsByStatus', () => {
    it('should filter seats by status', () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', status: 'available' }),
        TestDataFactory.createSeat({ id: '2', status: 'occupied' }),
        TestDataFactory.createSeat({ id: '3', status: 'reserved' })
      ];
      
      const result = SeatLayoutUtil.getSeatsByStatus(seats, 'occupied');
      
      expect(result).toHaveLength(1);
      expect(result[0].id).toBe('2');
    });
  });

  describe('calculateSeatStats', () => {
    it('should calculate correct statistics', () => {
      const seats = [
        TestDataFactory.createSeat({ status: 'available' }),
        TestDataFactory.createSeat({ status: 'available' }),
        TestDataFactory.createSeat({ status: 'occupied' }),
        TestDataFactory.createSeat({ status: 'reserved' }),
        TestDataFactory.createSeat({ status: 'maintenance' })
      ];
      
      const result = SeatLayoutUtil.calculateSeatStats(seats);
      
      expect(result.total).toBe(5);
      expect(result.available).toBe(2);
      expect(result.occupied).toBe(1);
      expect(result.reserved).toBe(1);
      expect(result.maintenance).toBe(1);
    });

    it('should handle empty array', () => {
      const result = SeatLayoutUtil.calculateSeatStats([]);
      
      expect(result.total).toBe(0);
      expect(result.available).toBe(0);
      expect(result.occupied).toBe(0);
      expect(result.reserved).toBe(0);
      expect(result.maintenance).toBe(0);
    });

    it('should handle non-array input', () => {
      const result = SeatLayoutUtil.calculateSeatStats(null as any);
      
      expect(result.total).toBe(0);
      expect(result.available).toBe(0);
    });
  });

  describe('createDefaultGroupConfig', () => {
    it('should create single group for small total columns', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(6);
      
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('main');
      expect(result[0].startCol).toBe(1);
      expect(result[0].endCol).toBe(6);
    });

    it('should create two groups for large total columns', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(10);
      
      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('left');
      expect(result[1].name).toBe('right');
    });

    it('should split columns evenly', () => {
      const result = SeatLayoutUtil.createDefaultGroupConfig(10);
      
      expect(result[0].endCol).toBe(5);
      expect(result[1].startCol).toBe(6);
    });
  });
});
