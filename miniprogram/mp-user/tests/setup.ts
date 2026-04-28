import { WxMock } from './mocks/wx-mock';

declare global {
  namespace NodeJS {
    interface Global {
      wx: WxMock;
    }
  }
}

(global as any).wx = new WxMock();

beforeEach(() => {
  (global as any).wx.clearAllMocks();
});

afterEach(() => {
  jest.clearAllMocks();
});

expect.extend({
  toBeSuccessfulResponse(received: any) {
    const pass = received && received.code === 200;
    return {
      pass,
      message: () => pass
        ? `expected ${received} not to be a successful response`
        : `expected ${received} to be a successful response with code 200`
    };
  },
  toBeErrorResponse(received: any, expectedCode?: number) {
    const pass = received && received.code !== 200 && 
      (expectedCode ? received.code === expectedCode : true);
    return {
      pass,
      message: () => pass
        ? `expected ${received} not to be an error response`
        : `expected ${received} to be an error response${expectedCode ? ` with code ${expectedCode}` : ''}`
    };
  }
});
