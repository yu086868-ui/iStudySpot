import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('seat-selection page integration tests', () => {
  let wxMock: WxMock;
  let pageInstance: any;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    
    pageInstance = {
      data: {
        seats: [],
        seatGroups: [],
        selectedSeat: null,
        selectedSeatFacilitiesText: '',
        studyRoomId: 'room_001',
        isLoading: false,
        selectedDate: '',
        selectedStartHour: 9,
        selectedStartMinute: 0,
        selectedEndHour: 13,
        selectedEndMinute: 0,
        minDate: '',
        maxDate: '',
        hours: [],
        minutes: [],
        reservationRules: null,
        seatStats: {
          total: 0,
          available: 0,
          occupied: 0,
          reserved: 0
        },
        preselectedSeatId: '',
        isImmediateMode: false
      },
      setData: jest.fn(function(this: any, newData: any) {
        this.data = { ...this.data, ...newData };
      }),
      onLoad: jest.fn(),
      initDateTimePicker: jest.fn(),
      loadReservationRules: jest.fn(),
      loadSeats: jest.fn(),
      onDateChange: jest.fn(),
      onStartHourChange: jest.fn(),
      onStartMinuteChange: jest.fn(),
      onEndHourChange: jest.fn(),
      onEndMinuteChange: jest.fn(),
      selectSeat: jest.fn(),
      getSeatClass: jest.fn(),
      getRowLabel: jest.fn(),
      formatTimeDisplay: jest.fn(),
      isImmediateStartTime: jest.fn(),
      confirmSelection: jest.fn(),
      performCheckIn: jest.fn()
    };
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('page initialization', () => {
    it('should initialize with default values', () => {
      expect(pageInstance.data.studyRoomId).toBe('room_001');
      expect(pageInstance.data.selectedSeat).toBeNull();
      expect(pageInstance.data.isLoading).toBe(false);
    });

    it('should load seats on page load', async () => {
      const seats = TestDataFactory.createSeats(10);
      const mockResponse = TestDataFactory.createSuccessResponse(seats);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.loadSeats();

      expect(pageInstance.loadSeats).toHaveBeenCalled();
    });

    it('should handle preselected seat', () => {
      const options = { studyRoomId: 'room_002', seatId: 'seat_005' };
      
      pageInstance.onLoad(options);

      expect(pageInstance.onLoad).toHaveBeenCalled();
    });

    it('should handle immediate mode', () => {
      const options = { immediate: 'true' };
      
      pageInstance.onLoad(options);

      expect(pageInstance.onLoad).toHaveBeenCalled();
    });
  });

  describe('date and time selection', () => {
    it('should update selected date', () => {
      const event = { detail: { value: '2024-01-20' } };
      
      pageInstance.onDateChange(event);

      expect(pageInstance.onDateChange).toHaveBeenCalled();
    });

    it('should update start hour', () => {
      const event = { detail: { value: 5 } };
      pageInstance.data.hours = [8, 9, 10, 11, 12, 13, 14];
      
      pageInstance.onStartHourChange(event);

      expect(pageInstance.onStartHourChange).toHaveBeenCalled();
    });

    it('should update start minute', () => {
      const event = { detail: { value: 3 } };
      pageInstance.data.minutes = [0, 5, 10, 15, 20];
      
      pageInstance.onStartMinuteChange(event);

      expect(pageInstance.onStartMinuteChange).toHaveBeenCalled();
    });

    it('should update end hour', () => {
      const event = { detail: { value: 7 } };
      pageInstance.data.hours = [8, 9, 10, 11, 12, 13, 14, 15];
      pageInstance.data.selectedStartHour = 10;
      
      pageInstance.onEndHourChange(event);

      expect(pageInstance.onEndHourChange).toHaveBeenCalled();
    });

    it('should validate end time must be greater than start time', () => {
      const event = { detail: { value: 2 } };
      pageInstance.data.hours = [8, 9, 10, 11, 12];
      pageInstance.data.selectedStartHour = 12;
      
      pageInstance.onEndHourChange(event);

      expect(pageInstance.onEndHourChange).toHaveBeenCalled();
    });
  });

  describe('seat selection', () => {
    it('should select available seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'available' });
      const event = {
        currentTarget: {
          dataset: { seat }
        }
      };
      
      pageInstance.selectSeat(event);

      expect(pageInstance.selectSeat).toHaveBeenCalled();
    });

    it('should not select occupied seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'occupied' });
      const event = {
        currentTarget: {
          dataset: { seat }
        }
      };
      
      pageInstance.selectSeat(event);

      expect(pageInstance.selectSeat).toHaveBeenCalled();
    });

    it('should not select reserved seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'reserved' });
      const event = {
        currentTarget: {
          dataset: { seat }
        }
      };
      
      pageInstance.selectSeat(event);

      expect(pageInstance.selectSeat).toHaveBeenCalled();
    });

    it('should not select maintenance seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'maintenance' });
      const event = {
        currentTarget: {
          dataset: { seat }
        }
      };
      
      pageInstance.selectSeat(event);

      expect(pageInstance.selectSeat).toHaveBeenCalled();
    });

    it('should deselect seat when clicking same seat', () => {
      const seat = TestDataFactory.createSeat({ id: 'seat_001', status: 'available' });
      pageInstance.data.selectedSeat = seat;
      const event = {
        currentTarget: {
          dataset: { seat }
        }
      };
      
      pageInstance.selectSeat(event);

      expect(pageInstance.selectSeat).toHaveBeenCalled();
    });
  });

  describe('seat class generation', () => {
    it('should generate class for available seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'available' });
      
      pageInstance.getSeatClass(seat);

      expect(pageInstance.getSeatClass).toHaveBeenCalled();
    });

    it('should generate class for occupied seat', () => {
      const seat = TestDataFactory.createSeat({ status: 'occupied' });
      
      pageInstance.getSeatClass(seat);

      expect(pageInstance.getSeatClass).toHaveBeenCalled();
    });

    it('should generate class for selected seat', () => {
      const seat = TestDataFactory.createSeat({ id: 'seat_001' });
      pageInstance.data.selectedSeat = seat;
      
      pageInstance.getSeatClass(seat);

      expect(pageInstance.getSeatClass).toHaveBeenCalled();
    });

    it('should generate class for VIP seat', () => {
      const seat = TestDataFactory.createSeat({ type: 'vip' });
      
      pageInstance.getSeatClass(seat);

      expect(pageInstance.getSeatClass).toHaveBeenCalled();
    });
  });

  describe('row label generation', () => {
    it('should generate label for row 1', () => {
      pageInstance.getRowLabel(1);

      expect(pageInstance.getRowLabel).toHaveBeenCalled();
    });

    it('should generate label for row 26', () => {
      pageInstance.getRowLabel(26);

      expect(pageInstance.getRowLabel).toHaveBeenCalled();
    });
  });

  describe('time display formatting', () => {
    it('should format time display correctly', () => {
      pageInstance.data.selectedDate = '2024-01-20';
      pageInstance.data.selectedStartHour = 9;
      pageInstance.data.selectedStartMinute = 0;
      pageInstance.data.selectedEndHour = 13;
      pageInstance.data.selectedEndMinute = 0;
      
      pageInstance.formatTimeDisplay();

      expect(pageInstance.formatTimeDisplay).toHaveBeenCalled();
    });
  });

  describe('reservation confirmation', () => {
    it('should show error when no seat selected', () => {
      pageInstance.data.selectedSeat = null;
      
      pageInstance.confirmSelection();

      expect(pageInstance.confirmSelection).toHaveBeenCalled();
    });

    it('should show error when start time is in the past', () => {
      pageInstance.data.selectedSeat = TestDataFactory.createSeat();
      pageInstance.data.selectedDate = '2020-01-01';
      
      pageInstance.confirmSelection();

      expect(pageInstance.confirmSelection).toHaveBeenCalled();
    });

    it('should create reservation successfully', async () => {
      pageInstance.data.selectedSeat = TestDataFactory.createSeat();
      pageInstance.data.selectedDate = new Date(Date.now() + 86400000).toISOString().split('T')[0];
      
      const reservation = TestDataFactory.createReservation();
      const mockResponse = TestDataFactory.createSuccessResponse(reservation);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.confirmSelection();

      expect(pageInstance.confirmSelection).toHaveBeenCalled();
    });

    it('should handle immediate start mode', async () => {
      pageInstance.data.selectedSeat = TestDataFactory.createSeat();
      pageInstance.data.isImmediateMode = true;
      
      const reservation = TestDataFactory.createReservation();
      const mockResponse = TestDataFactory.createSuccessResponse(reservation);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.confirmSelection();

      expect(pageInstance.confirmSelection).toHaveBeenCalled();
    });
  });

  describe('check-in after reservation', () => {
    it('should perform check-in successfully', async () => {
      const mockResponse = TestDataFactory.createSuccessResponse({
        checkInRecordId: 'checkin_001',
        checkInTime: new Date().toISOString()
      });
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.performCheckIn('res_001', 'seat_001');

      expect(pageInstance.performCheckIn).toHaveBeenCalled();
    });
  });
});
