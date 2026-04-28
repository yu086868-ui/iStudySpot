import { SeatLayoutUtil } from '../miniprogram/utils/seat-layout';

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
      const seats = [{
        id: 'seat_001',
        studyRoomId: 'room_001',
        row: 1,
        col: 1,
        seatNumber: 'A1',
        type: 'normal' as const,
        status: 'available' as const,
        facilities: ['插座']
      }];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.totalRows).toBe(1);
      expect(result.totalCols).toBe(1);
      expect(result.rows).toHaveLength(1);
      expect(result.rows[0].rowNumber).toBe(1);
      expect(result.rows[0].seats).toHaveLength(1);
    });

    it('should create layout with multiple rows and columns', () => {
      const seats = [];
      for (let i = 1; i <= 12; i++) {
        const row = Math.ceil(i / 6);
        const col = ((i - 1) % 6) + 1;
        seats.push({
          id: `seat_${i}`,
          studyRoomId: 'room_001',
          row,
          col,
          seatNumber: `${String.fromCharCode(64 + row)}${col}`,
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        });
      }
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.totalRows).toBe(2);
      expect(result.totalCols).toBe(6);
      expect(result.rows).toHaveLength(2);
    });

    it('should sort rows by row number', () => {
      const seats = [
        { id: 'seat_1', studyRoomId: 'room_001', row: 3, col: 1, seatNumber: 'C1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: 'seat_2', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: 'seat_3', studyRoomId: 'room_001', row: 2, col: 1, seatNumber: 'B1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] }
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.rows[0].rowNumber).toBe(1);
      expect(result.rows[1].rowNumber).toBe(2);
      expect(result.rows[2].rowNumber).toBe(3);
    });

    it('should sort seats by column number within row', () => {
      const seats = [
        { id: 'seat_1', studyRoomId: 'room_001', row: 1, col: 3, seatNumber: 'A3', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: 'seat_2', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: 'seat_3', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] }
      ];
      const result = SeatLayoutUtil.createSeatLayout(seats);
      
      expect(result.rows[0].seats[0].col).toBe(1);
      expect(result.rows[0].seats[1].col).toBe(2);
      expect(result.rows[0].seats[2].col).toBe(3);
    });
  });

  describe('splitIntoGroups', () => {
    it('should split seats into groups based on config', () => {
      const seats = [];
      for (let i = 1; i <= 12; i++) {
        const row = Math.ceil(i / 6);
        const col = ((i - 1) % 6) + 1;
        seats.push({
          id: `seat_${i}`,
          studyRoomId: 'room_001',
          row,
          col,
          seatNumber: `${String.fromCharCode(64 + row)}${col}`,
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        });
      }
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
      const seats = [];
      for (let i = 1; i <= 6; i++) {
        seats.push({
          id: `seat_${i}`,
          studyRoomId: 'room_001',
          row: 1,
          col: i,
          seatNumber: `A${i}`,
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        });
      }
      const groupConfig = [
        { startCol: 1, endCol: 4, name: 'group1' },
        { startCol: 3, endCol: 6, name: 'group2' }
      ];
      
      const result = SeatLayoutUtil.splitIntoGroups(seats, groupConfig);
      
      expect(result).toHaveLength(2);
      expect(result[0].rows.length).toBeGreaterThan(0);
      expect(result[1].rows.length).toBeGreaterThan(0);
    });
  });

  describe('getSeatStatus', () => {
    it('should return empty for undefined seat', () => {
      expect(SeatLayoutUtil.getSeatStatus(undefined)).toBe('empty');
    });

    it('should return seat status', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.getSeatStatus(seat)).toBe('available');
    });

    it('should return occupied status', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'occupied' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.getSeatStatus(seat)).toBe('occupied');
    });
  });

  describe('isSeatSelectable', () => {
    it('should return false for undefined seat', () => {
      expect(SeatLayoutUtil.isSeatSelectable(undefined)).toBe(false);
    });

    it('should return true for available seat', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(true);
    });

    it('should return false for occupied seat', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'occupied' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(false);
    });

    it('should return false for reserved seat', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'reserved' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(false);
    });

    it('should return false for maintenance seat', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'maintenance' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.isSeatSelectable(seat)).toBe(false);
    });
  });

  describe('getSeatType', () => {
    it('should return none for undefined seat', () => {
      expect(SeatLayoutUtil.getSeatType(undefined)).toBe('none');
    });

    it('should return seat type', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'vip' as const, status: 'available' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.getSeatType(seat)).toBe('vip');
    });

    it('should return normal type', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] };
      expect(SeatLayoutUtil.getSeatType(seat)).toBe('normal');
    });

    it('should return quiet type', () => {
      const seat = { id: 'seat_001', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'quiet' as const, status: 'available' as const, facilities: ['插座'] };
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
        { id: '1', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '2', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'normal' as const, status: 'occupied' as const, facilities: ['插座'] },
        { id: '3', studyRoomId: 'room_001', row: 1, col: 3, seatNumber: 'A3', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] }
      ];
      
      const result = SeatLayoutUtil.getAvailableSeats(seats);
      
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('1');
      expect(result[1].id).toBe('3');
    });

    it('should return empty array if no available seats', () => {
      const seats = [
        { id: '1', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'occupied' as const, facilities: ['插座'] },
        { id: '2', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'normal' as const, status: 'reserved' as const, facilities: ['插座'] }
      ];
      
      const result = SeatLayoutUtil.getAvailableSeats(seats);
      
      expect(result).toHaveLength(0);
    });
  });

  describe('getSeatsByType', () => {
    it('should filter seats by type', () => {
      const seats = [
        { id: '1', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '2', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'vip' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '3', studyRoomId: 'room_001', row: 1, col: 3, seatNumber: 'A3', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] }
      ];
      
      const result = SeatLayoutUtil.getSeatsByType(seats, 'normal');
      
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('1');
      expect(result[1].id).toBe('3');
    });

    it('should return empty array if no matching type', () => {
      const seats = [
        { id: '1', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '2', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] }
      ];
      
      const result = SeatLayoutUtil.getSeatsByType(seats, 'vip');
      
      expect(result).toHaveLength(0);
    });
  });

  describe('getSeatsByStatus', () => {
    it('should filter seats by status', () => {
      const seats = [
        { id: '1', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '2', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'normal' as const, status: 'occupied' as const, facilities: ['插座'] },
        { id: '3', studyRoomId: 'room_001', row: 1, col: 3, seatNumber: 'A3', type: 'normal' as const, status: 'reserved' as const, facilities: ['插座'] }
      ];
      
      const result = SeatLayoutUtil.getSeatsByStatus(seats, 'occupied');
      
      expect(result).toHaveLength(1);
      expect(result[0].id).toBe('2');
    });
  });

  describe('calculateSeatStats', () => {
    it('should calculate correct statistics', () => {
      const seats = [
        { id: '1', studyRoomId: 'room_001', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '2', studyRoomId: 'room_001', row: 1, col: 2, seatNumber: 'A2', type: 'normal' as const, status: 'available' as const, facilities: ['插座'] },
        { id: '3', studyRoomId: 'room_001', row: 1, col: 3, seatNumber: 'A3', type: 'normal' as const, status: 'occupied' as const, facilities: ['插座'] },
        { id: '4', studyRoomId: 'room_001', row: 1, col: 4, seatNumber: 'A4', type: 'normal' as const, status: 'reserved' as const, facilities: ['插座'] },
        { id: '5', studyRoomId: 'room_001', row: 1, col: 5, seatNumber: 'A5', type: 'normal' as const, status: 'maintenance' as const, facilities: ['插座'] }
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
