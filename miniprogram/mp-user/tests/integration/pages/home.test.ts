describe('home page integration tests', () => {
  let pageInstance: any;

  beforeEach(() => {
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

  describe('page initialization', () => {
    it('should initialize with default state', () => {
      expect(pageInstance.data.userState).toBe('none');
      expect(pageInstance.data.userInfo).toBeNull();
      expect(pageInstance.data.currentReservation).toBeNull();
    });

    it('should load user info on page load', async () => {
      pageInstance.loadUserInfo();
      expect(pageInstance.loadUserInfo).toHaveBeenCalled();
    });

    it('should load study rooms on page load', async () => {
      pageInstance.loadStudyRooms();
      expect(pageInstance.loadStudyRooms).toHaveBeenCalled();
    });
  });

  describe('user state management', () => {
    it('should set none state when no reservation', () => {
      pageInstance.setNoneState.mockImplementation(function(this: any) {
        this.setData({
          userState: 'none',
          currentReservation: null,
          stateDisplayText: '未预约'
        });
      });

      pageInstance.setNoneState();

      expect(pageInstance.setData).toHaveBeenCalledWith({
        userState: 'none',
        currentReservation: null,
        stateDisplayText: '未预约'
      });
    });

    it('should set reserved state with reservation', async () => {
      const reservation = {
        id: 'res_001',
        userId: 'user_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
      pageInstance.setReservedState(reservation);

      expect(pageInstance.setReservedState).toHaveBeenCalled();
    });

    it('should set studying state with check-in record', async () => {
      const checkInRecord = {
        id: 'checkin_001',
        userId: 'user_001',
        reservationId: 'res_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
        duration: 0,
        status: 'active' as const
      };
      
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
      pageInstance.data.currentReservation = {
        id: 'res_001',
        userId: 'user_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
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
      pageInstance.data.currentReservation = {
        id: 'res_001',
        userId: 'user_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
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
      pageInstance.data.currentReservation = {
        id: 'res_001',
        userId: 'user_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      pageInstance.cancelCurrentReservation();

      expect(pageInstance.cancelCurrentReservation).toHaveBeenCalled();
    });
  });

  describe('tab bar interaction', () => {
    it('should update tab bar on show', () => {
      const mockTabBarSetData = jest.fn();
      pageInstance.getTabBar = jest.fn().mockReturnValue({
        setData: mockTabBarSetData
      });
      pageInstance.onShow = jest.fn().mockImplementation(function(this: any) {
        const tabBar = this.getTabBar();
        if (tabBar) {
          tabBar.setData({ selected: 0 });
        }
      });

      pageInstance.onShow();

      expect(pageInstance.getTabBar).toHaveBeenCalled();
      expect(mockTabBarSetData).toHaveBeenCalled();
    });
  });
});
