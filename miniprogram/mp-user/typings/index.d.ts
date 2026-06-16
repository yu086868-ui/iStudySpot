/// <reference path="./types/index.d.ts" />

interface IAppOption {
  globalData: {
    userInfo?: WechatMiniprogram.UserInfo,
  }
  userInfoReadyCallback?: WechatMiniprogram.GetUserInfoSuccessCallback,
  onError?: (err: string) => void,
  onUnhandledRejection?: (rejection: WechatMiniprogram.OnUnhandledRejectionCallbackResult) => void,
  onPageNotFound?: () => void,
}