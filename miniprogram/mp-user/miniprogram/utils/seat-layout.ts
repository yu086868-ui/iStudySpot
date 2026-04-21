import type { Seat } from '../typings/api';

export interface SeatLayout {
  rows: SeatRow[];
  totalRows: number;
  totalCols: number;
}

export interface SeatRow {
  rowNumber: number;
  seats: Seat[];
}

export interface SeatGroup {
  name: string;
  startCol: number;
  endCol: number;
  rows: SeatRow[];
}

export class SeatLayoutUtil {
  static createSeatLayout(seats: Seat[]): SeatLayout {
    if (!seats || seats.length === 0) {
      return { rows: [], totalRows: 0, totalCols: 0 };
    }

    const seatMap = new Map<string, Seat>();
    seats.forEach(seat => {
      seatMap.set(`${seat.row}-${seat.col}`, seat);
    });

    const maxRow = Math.max(...seats.map(s => s.row));
    const maxCol = Math.max(...seats.map(s => s.col));

    const rows: SeatRow[] = [];
    for (let row = 1; row <= maxRow; row++) {
      const rowSeats: Seat[] = [];
      for (let col = 1; col <= maxCol; col++) {
        const seat = seatMap.get(`${row}-${col}`);
        if (seat) {
          rowSeats.push(seat);
        }
      }
      if (rowSeats.length > 0) {
        rows.push({
          rowNumber: row,
          seats: rowSeats.sort((a, b) => a.col - b.col)
        });
      }
    }

    return {
      rows: rows.sort((a, b) => a.rowNumber - b.rowNumber),
      totalRows: maxRow,
      totalCols: maxCol
    };
  }

  static splitIntoGroups(seats: Seat[], groupConfig: { startCol: number; endCol: number; name: string }[]): SeatGroup[] {
    const layout = this.createSeatLayout(seats);
    
    return groupConfig.map(config => {
      const groupRows: SeatRow[] = layout.rows.map(row => ({
        rowNumber: row.rowNumber,
        seats: row.seats.filter(seat => 
          seat.col >= config.startCol && seat.col <= config.endCol
        )
      })).filter(row => row.seats.length > 0);

      return {
        name: config.name,
        startCol: config.startCol,
        endCol: config.endCol,
        rows: groupRows
      };
    });
  }

  static getSeatStatus(seat: Seat | undefined): 'available' | 'occupied' | 'reserved' | 'maintenance' | 'empty' {
    if (!seat) return 'empty';
    return seat.status;
  }

  static isSeatSelectable(seat: Seat | undefined): boolean {
    if (!seat) return false;
    return seat.status === 'available';
  }

  static getSeatType(seat: Seat | undefined): 'normal' | 'vip' | 'quiet' | 'none' {
    if (!seat) return 'none';
    return seat.type;
  }

  static generateSeatNumber(row: number, col: number): string {
    const rowLetter = String.fromCharCode(64 + row);
    return `${rowLetter}${col}`;
  }

  static getAvailableSeats(seats: Seat[]): Seat[] {
    return seats.filter(seat => seat.status === 'available');
  }

  static getSeatsByType(seats: Seat[], type: 'normal' | 'vip' | 'quiet'): Seat[] {
    return seats.filter(seat => seat.type === type);
  }

  static getSeatsByStatus(seats: Seat[], status: 'available' | 'occupied' | 'reserved' | 'maintenance'): Seat[] {
    return seats.filter(seat => seat.status === status);
  }

  static calculateSeatStats(seats: Seat[]): {
    total: number;
    available: number;
    occupied: number;
    reserved: number;
    maintenance: number;
  } {
    if (!Array.isArray(seats)) {
      console.error('calculateSeatStats: seats is not an array', seats);
      return {
        total: 0,
        available: 0,
        occupied: 0,
        reserved: 0,
        maintenance: 0
      };
    }
    
    return {
      total: seats.length,
      available: seats.filter(s => s.status === 'available').length,
      occupied: seats.filter(s => s.status === 'occupied').length,
      reserved: seats.filter(s => s.status === 'reserved').length,
      maintenance: seats.filter(s => s.status === 'maintenance').length
    };
  }

  static createDefaultGroupConfig(totalCols: number): { startCol: number; endCol: number; name: string }[] {
    if (totalCols <= 6) {
      return [{ startCol: 1, endCol: totalCols, name: 'main' }];
    }

    const midCol = Math.floor(totalCols / 2);
    return [
      { startCol: 1, endCol: midCol, name: 'left' },
      { startCol: midCol + 1, endCol: totalCols, name: 'right' }
    ];
  }
}
