import { WxMock } from './tests/mocks/wx-mock';

declare global {
  var wx: WxMock;
}

export {};
