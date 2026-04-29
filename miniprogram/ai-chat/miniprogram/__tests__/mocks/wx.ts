type RequestTask = {
  onChunkReceived: (callback: (res: { data: ArrayBuffer }) => void) => void;
  abort: () => void;
};

type StorageData = Record<string, string>;

const storage: StorageData = {};

const mockRequest = jest.fn().mockImplementation((options: {
  url: string;
  method?: string;
  data?: unknown;
  header?: Record<string, string>;
  timeout?: number;
  enableChunked?: boolean;
  success?: (res: { statusCode: number; data: unknown }) => void;
  fail?: (error: Error) => void;
}) => {
  const task: RequestTask = {
    onChunkReceived: jest.fn(),
    abort: jest.fn()
  };

  const url = options.url || '';

  if (url.includes('/health')) {
    if (options.success) {
      setTimeout(() => options.success!({ statusCode: 200, data: { status: 'ok' } }), 10);
    }
  } else if (url.includes('/characters')) {
    if (options.success) {
      setTimeout(() => options.success!({
        statusCode: 200,
        data: [{ id: 'test', name: 'Test Character', persona: 'Test', speaking_style: 'Test' }]
      }), 10);
    }
  } else if (url.includes('/chat/stream')) {
    if (options.success) {
      setTimeout(() => options.success!({
        statusCode: 200,
        data: { reply: 'Test stream reply' }
      }), 10);
    }
  } else if (url.includes('/chat')) {
    if (options.success) {
      setTimeout(() => options.success!({
        statusCode: 200,
        data: { reply: 'Test reply' }
      }), 10);
    }
  } else {
    if (options.success) {
      setTimeout(() => options.success!({ statusCode: 200, data: {} }), 10);
    }
  }

  return task;
});

const wx = {
  request: mockRequest,

  showToast: jest.fn(),

  setStorageSync: jest.fn().mockImplementation((key: string, value: string) => {
    storage[key] = value;
  }),

  getStorageSync: jest.fn().mockImplementation((key: string) => {
    return storage[key] || '';
  }),

  removeStorageSync: jest.fn().mockImplementation((key: string) => {
    delete storage[key];
  }),

  clearStorageSync: jest.fn().mockImplementation(() => {
    Object.keys(storage).forEach(key => delete storage[key]);
  })
};

Object.defineProperty(global, 'wx', {
  value: wx,
  writable: true
});

export default wx;
