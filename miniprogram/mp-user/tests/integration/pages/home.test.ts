import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('home page integration tests', () => {
  let wxMock: WxMock;
  let pageInstance: any;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    
    pageInstance = {
      data: {
        userInfo: null,
        userState: 'none',
        currentReservation: null,
        weeklyStudyHours: 18,
        studyRooms: [],
        seats: [],
        stateDisplayText: ''
      },
      setData: jest.fn(function(this: any, newData: any) {
        this.data = { ...this.data, ...newData };
      }),
      loadUserInfo: jest.fn(),
      loadStudyRooms: jest.fn(),
      updateUserState: jest.fn(),
      setStudyingState: jest.fn(),
      setReservedState: jest.fn(),
      setNoneState: jest.fn(),
      reserveSeat: jest.fn(),
      cancelCurrentReservation: jest.fn(),
      onlineCheckIn: jest.fn(),
      scanCheckIn: jest.fn(),
      parseQrCode: jest.fn(),
      handleQrCodeCheckIn: jest.fn(),
      performCheckIn: jest.fn(),
      getTabBar: jest.fn()
    };
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('page initialization', () => {
    it('should initialize with default state', () => {
      expect(pageInstance.data.userState).toBe('none');
      expect(pageInstance.data.userInfo).toBeNull();
      expect(pageInstance.data.currentReservation).toBeNull();
    });

    it('should load user info on page load', async () => {
      const user = TestDataFactory.createUser();
      const mockResponse = TestDataFactory.createSuccessResponse(user);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.loadUserInfo();

      expect(pageInstance.loadUserInfo).toHaveBeenCalled();
    });

    it('should load study rooms on page load', async () => {
      const studyRooms = [TestDataFactory.createStudyRoom()];
      const paginatedData = TestDataFactory.createPaginatedResponse(studyRooms);
      const mockResponse = TestDataFactory.createSuccessResponse(paginatedData);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.loadStudyRooms();

      expect(pageInstance.loadStudyRooms).toHaveBeenCalled();
    });
  });

  describe('user state management', () => {
    it('should set none state when no reservation', () => {
      pageInstance.setNoneState();

      expect(pageInstance.setData).toHaveBeenCalledWith({
        userState: 'none',
        currentReservation: null,
        stateDisplayText: '未预约'
      });
    });

    it('should set reserved state with reservation', async () => {
      const reservation = TestDataFactory.createReservation();
      
      pageInstance.setReservedState(reservation);

      expect(pageInstance.setReservedState).toHaveBeenCalled();
    });

    it('should set studying state with check-in record', async () => {
      const checkInRecord = TestDataFactory.createCheckInRecord();
      
      pageInstance.setStudyingState(checkInRecord);

      expect(pageInstance.setStudyingState).toHaveBeenCalled();
    });
  });

  describe('seat reservation', () => {
    it('should navigate to seat selection when no reservation', () => {
      pageInstance.data.userState = 'none';
      
      pageInstance.reserveSeat();

      expect(pageInstance.reserveSeat).toHaveBeenCalled();
    });

    it('should show toast when already studying', () => {
      pageInstance.data.userState = 'studying';
      
      pageInstance.reserveSeat();

      expect(pageInstance.reserveSeat).toHaveBeenCalled();
    });

    it('should show modal when already has reservation', () => {
      pageInstance.data.userState = 'reserved';
      pageInstance.data.currentReservation = TestDataFactory.createReservation();
      
      pageInstance.reserveSeat();

      expect(pageInstance.reserveSeat).toHaveBeenCalled();
    });
  });

  describe('check-in functionality', () => {
    it('should show toast when already studying', () => {
      pageInstance.data.userState = 'studying';
      
      pageInstance.onlineCheckIn();

      expect(pageInstance.onlineCheckIn).toHaveBeenCalled();
    });

    it('should perform check-in when has reservation', async () => {
      pageInstance.data.userState = 'reserved';
      pageInstance.data.currentReservation = TestDataFactory.createReservation();
      
      pageInstance.onlineCheckIn();

      expect(pageInstance.onlineCheckIn).toHaveBeenCalled();
    });

    it('should show modal when no reservation', () => {
      pageInstance.data.userState = 'none';
      
      pageInstance.onlineCheckIn();

      expect(pageInstance.onlineCheckIn).toHaveBeenCalled();
    });
  });

  describe('QR code scanning', () => {
    it('should parse QR code with URL format', () => {
      const qrContent = 'https://example.com?studyRoomId=room_001&seatId=seat_001';
      
      pageInstance.parseQrCode(qrContent);

      expect(pageInstance.parseQrCode).toHaveBeenCalled();
    });

    it('should parse QR code with JSON format', () => {
      const qrContent = JSON.stringify({ studyRoomId: 'room_001', seatId: 'seat_001' });
      
      pageInstance.parseQrCode(qrContent);

      expect(pageInstance.parseQrCode).toHaveBeenCalled();
    });

    it('should parse QR code with simple format', () => {
      const qrContent = 'room_001/seat_001';
      
      pageInstance.parseQrCode(qrContent);

      expect(pageInstance.parseQrCode).toHaveBeenCalled();
    });

    it('should return null for invalid QR code', () => {
      const qrContent = 'invalid_qr_code';
      
      pageInstance.parseQrCode(qrContent);

      expect(pageInstance.parseQrCode).toHaveBeenCalled();
    });
  });

  describe('cancel reservation', () => {
    it('should cancel current reservation', async () => {
      pageInstance.data.currentReservation = TestDataFactory.createReservation();
      
      const mockResponse = TestDataFactory.createSuccessResponse(null);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      pageInstance.cancelCurrentReservation();

      expect(pageInstance.cancelCurrentReservation).toHaveBeenCalled();
    });
  });

  describe('tab bar interaction', () => {
    it('should update tab bar on show', () => {
      pageInstance.getTabBar = jest.fn().mockReturnValue({
        setData: jest.fn()
      });

      pageInstance.onShow();

      expect(pageInstance.getTabBar).toHaveBeenCalled();
    });
  });
});
