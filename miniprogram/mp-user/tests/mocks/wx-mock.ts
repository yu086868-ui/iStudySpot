type MockFunction = jest.Mock;

interface MockStorage {
  [key: string]: any;
}

export class WxMock {
  private storage: MockStorage = {};
  private mockFunctions: { [key: string]: MockFunction } = {};

  constructor() {
    this.initMockFunctions();
  }

  private initMockFunctions() {
    this.mockFunctions = {
      request: jest.fn(),
      showToast: jest.fn(),
      showLoading: jest.fn(),
      hideLoading: jest.fn(),
      showModal: jest.fn(),
      navigateTo: jest.fn(),
      redirectTo: jest.fn(),
      reLaunch: jest.fn(),
      switchTab: jest.fn(),
      scanCode: jest.fn(),
      getStorageSync: jest.fn((key: string) => this.storage[key]),
      setStorageSync: jest.fn((key: string, value: any) => {
        this.storage[key] = value;
      }),
      removeStorageSync: jest.fn((key: string) => {
        delete this.storage[key];
      }),
      clearStorageSync: jest.fn(() => {
        this.storage = {};
      }),
      login: jest.fn(),
      getStorageSync_original: jest.fn((key: string) => this.storage[key]),
    };
  }

  clearAllMocks() {
    Object.values(this.mockFunctions).forEach(mock => {
      if (mock && typeof mock.mockClear === 'function') {
        mock.mockClear();
      }
    });
    this.storage = {};
  }

  getStorageSync(key: string): any {
    return this.storage[key];
  }

  setStorageSync(key: string, value: any): void {
    this.storage[key] = value;
  }

  removeStorageSync(key: string): void {
    delete this.storage[key];
  }

  clearStorageSync(): void {
    this.storage = {};
  }

  request(options: any): any {
    return this.mockFunctions.request(options);
  }

  showToast(options: any): void {
    return this.mockFunctions.showToast(options);
  }

  showLoading(options: any): void {
    return this.mockFunctions.showLoading(options);
  }

  hideLoading(): void {
    return this.mockFunctions.hideLoading();
  }

  showModal(options: any): void {
    return this.mockFunctions.showModal(options);
  }

  navigateTo(options: any): void {
    return this.mockFunctions.navigateTo(options);
  }

  redirectTo(options: any): void {
    return this.mockFunctions.redirectTo(options);
  }

  reLaunch(options: any): void {
    return this.mockFunctions.reLaunch(options);
  }

  switchTab(options: any): void {
    return this.mockFunctions.switchTab(options);
  }

  scanCode(options: any): void {
    return this.mockFunctions.scanCode(options);
  }

  login(options: any): void {
    return this.mockFunctions.login(options);
  }

  getMockFunction(name: string): MockFunction {
    return this.mockFunctions[name];
  }

  mockRequestImplementation(implementation: (options: any) => any) {
    this.mockFunctions.request.mockImplementation(implementation);
  }

  mockRequestSuccess(data: any, code = 200, message = 'success') {
    this.mockFunctions.request.mockImplementation((options: any) => {
      if (options.success) {
        options.success({
          data: {
            code,
            message,
            data,
            timestamp: Date.now()
          },
          statusCode: 200,
          header: {}
        });
      }
    });
  }

  mockRequestError(error: any) {
    this.mockFunctions.request.mockImplementation((options: any) => {
      if (options.fail) {
        options.fail(error);
      }
    });
  }

  mockLoginSuccess(code = 'mock_code') {
    this.mockFunctions.login.mockImplementation((options: any) => {
      if (options.success) {
        options.success({ code });
      }
    });
  }

  mockScanCodeSuccess(result: string) {
    this.mockFunctions.scanCode.mockImplementation((options: any) => {
      if (options.success) {
        options.success({ result });
      }
    });
  }

  mockShowModalConfirm() {
    this.mockFunctions.showModal.mockImplementation((options: any) => {
      if (options.success) {
        options.success({ confirm: true, cancel: false });
      }
    });
  }

  mockShowModalCancel() {
    this.mockFunctions.showModal.mockImplementation((options: any) => {
      if (options.success) {
        options.success({ confirm: false, cancel: true });
      }
    });
  }
}
