const wxMock = {
  setStorageSync: jest.fn(),
  getStorageSync: jest.fn().mockReturnValue(''),
  removeStorageSync: jest.fn(),
  getStorageInfoSync: jest.fn().mockReturnValue({ keys: [] }),
  request: jest.fn(),
  showToast: jest.fn(),
  reLaunch: jest.fn(),
  navigateTo: jest.fn(),
  redirectTo: jest.fn(),
  switchTab: jest.fn(),
  navigateBack: jest.fn()
};

global.wx = wxMock as any;
global.getCurrentPages = jest.fn().mockReturnValue([]);
