export function createWxMock() {
  const storage: Record<string, string> = {};

  return {
    setStorageSync: jest.fn((key: string, value: unknown) => {
      storage[key] = JSON.stringify(value);
    }),
    getStorageSync: jest.fn((key: string) => {
      return storage[key] || '';
    }),
    removeStorageSync: jest.fn((key: string) => {
      delete storage[key];
    }),
    getStorageInfoSync: jest.fn(() => ({
      keys: Object.keys(storage)
    })),
    request: jest.fn(),
    showToast: jest.fn(),
    reLaunch: jest.fn(),
    navigateTo: jest.fn(),
    redirectTo: jest.fn(),
    switchTab: jest.fn(),
    navigateBack: jest.fn(),
    _storage: storage
  };
}
