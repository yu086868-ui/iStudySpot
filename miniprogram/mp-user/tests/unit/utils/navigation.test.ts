import navigationManager from '../../../miniprogram/utils/navigation';

beforeEach(() => {
  jest.clearAllMocks();
  (getCurrentPages as jest.Mock).mockReturnValue([]);
});

describe('NavigationManager', () => {
  describe('navigateTo', () => {
    it('tab 页面使用 switchTab', () => {
      navigationManager.navigateTo('home');

      expect(wx.switchTab).toHaveBeenCalledWith({ url: '/pages/home/home' });
      expect(wx.navigateTo).not.toHaveBeenCalled();
    });

    it('非 tab 页面使用 navigateTo', () => {
      navigationManager.navigateTo('reservation');

      expect(wx.navigateTo).toHaveBeenCalledWith({
        url: '/pages/seat-selection/seat-selection',
        fail: expect.any(Function)
      });
      expect(wx.switchTab).not.toHaveBeenCalled();
    });

    it('clearStack 选项使用 reLaunch', () => {
      navigationManager.navigateTo('reservation', { clearStack: true });

      expect(wx.reLaunch).toHaveBeenCalledWith({ url: '/pages/seat-selection/seat-selection' });
      expect(wx.navigateTo).not.toHaveBeenCalled();
    });

    it('replaceCurrent 选项使用 redirectTo', () => {
      navigationManager.navigateTo('reservation', { replaceCurrent: true });

      expect(wx.redirectTo).toHaveBeenCalledWith({ url: '/pages/seat-selection/seat-selection' });
      expect(wx.navigateTo).not.toHaveBeenCalled();
    });

    it('navigateTo 失败时降级为 redirectTo', () => {
      (wx.navigateTo as jest.Mock).mockImplementation(({ fail }: any) => {
        fail({ errMsg: 'navigateTo:fail' });
      });

      navigationManager.navigateTo('reservation');

      expect(wx.navigateTo).toHaveBeenCalled();
      expect(wx.redirectTo).toHaveBeenCalledWith({ url: '/pages/seat-selection/seat-selection' });
    });
  });

  describe('navigateFromReservationToStudy', () => {
    it('使用 redirectTo', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/seat-selection/seat-selection' }
      ]);

      navigationManager.navigateFromReservationToStudy();

      expect(wx.redirectTo).toHaveBeenCalledWith({
        url: '/pages/study-status/study-status',
        fail: expect.any(Function)
      });
    });

    it('带参数时正确拼接 URL', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/seat-selection/seat-selection' }
      ]);

      navigationManager.navigateFromReservationToStudy({ seatId: 'A1' });

      expect(wx.redirectTo).toHaveBeenCalledWith({
        url: '/pages/study-status/study-status?seatId=A1',
        fail: expect.any(Function)
      });
    });
  });

  describe('navigateFromReservationToHome', () => {
    it('使用 switchTab', () => {
      navigationManager.navigateFromReservationToHome();

      expect(wx.switchTab).toHaveBeenCalledWith({ url: '/pages/home/home' });
    });
  });

  describe('navigateFromStudyToHome', () => {
    it('使用 switchTab', () => {
      navigationManager.navigateFromStudyToHome();

      expect(wx.switchTab).toHaveBeenCalledWith({ url: '/pages/home/home' });
    });
  });

  describe('goBackToHome', () => {
    it('页面栈中有首页时 navigateBack', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/home/home' },
        { route: 'pages/seat-selection/seat-selection' },
        { route: 'pages/study-status/study-status' }
      ]);

      navigationManager.goBackToHome();

      expect(wx.navigateBack).toHaveBeenCalledWith({ delta: 2 });
    });

    it('页面栈中无首页时 switchTab', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/seat-selection/seat-selection' },
        { route: 'pages/study-status/study-status' }
      ]);

      navigationManager.goBackToHome();

      expect(wx.switchTab).toHaveBeenCalledWith({ url: '/pages/home/home' });
    });

    it('只有一个页面时 switchTab', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/study-status/study-status' }
      ]);

      navigationManager.goBackToHome();

      expect(wx.switchTab).toHaveBeenCalledWith({ url: '/pages/home/home' });
    });
  });

  describe('getCurrentPageRoute', () => {
    it('返回当前路由', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/home/home' }
      ]);

      expect(navigationManager.getCurrentPageRoute()).toBe('pages/home/home');
    });

    it('无页面时返回 null', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([]);

      expect(navigationManager.getCurrentPageRoute()).toBeNull();
    });
  });

  describe('isOnReservationPage', () => {
    it('在预约页时返回 true', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/seat-selection/seat-selection' }
      ]);

      expect(navigationManager.isOnReservationPage()).toBe(true);
    });

    it('不在预约页时返回 false', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/home/home' }
      ]);

      expect(navigationManager.isOnReservationPage()).toBe(false);
    });
  });

  describe('isOnStudyPage', () => {
    it('在学习状态页时返回 true', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/study-status/study-status' }
      ]);

      expect(navigationManager.isOnStudyPage()).toBe(true);
    });

    it('不在学习状态页时返回 false', () => {
      (getCurrentPages as jest.Mock).mockReturnValue([
        { route: 'pages/home/home' }
      ]);

      expect(navigationManager.isOnStudyPage()).toBe(false);
    });
  });

  describe('buildPath', () => {
    it('无参数时返回基础路径', () => {
      const path = (navigationManager as any).buildPath('home');
      expect(path).toBe('/pages/home/home');
    });

    it('正确拼接查询参数', () => {
      const path = (navigationManager as any).buildPath('study', { seatId: 'A1', floor: 2 });
      expect(path).toBe('/pages/study-status/study-status?seatId=A1&floor=2');
    });

    it('对特殊字符进行编码', () => {
      const path = (navigationManager as any).buildPath('study', { name: 'test room' });
      expect(path).toBe('/pages/study-status/study-status?name=test%20room');
    });

    it('布尔值参数正确转换', () => {
      const path = (navigationManager as any).buildPath('study', { active: true });
      expect(path).toBe('/pages/study-status/study-status?active=true');
    });
  });
});
