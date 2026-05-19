type NavigationTarget = 'home' | 'rules' | 'profile' | 'reservation' | 'study'

interface NavigationOptions {
  clearStack?: boolean
  replaceCurrent?: boolean
  params?: Record<string, string | number | boolean>
}

const PAGE_PATHS: Record<NavigationTarget, string> = {
  home: '/pages/home/home',
  rules: '/pages/rules/rules',
  profile: '/pages/profile/profile',
  reservation: '/pages/seat-selection/seat-selection',
  study: '/pages/study-status/study-status'
}

const TAB_PAGES = ['home', 'rules', 'profile']

class NavigationManager {
  private lastReservationPageId: string | null

  constructor() {
    this.lastReservationPageId = null
  }

  navigateTo(target: NavigationTarget, options: NavigationOptions = {}): void {
    const path = this.buildPath(target, options.params)

    if (TAB_PAGES.includes(target)) {
      this.switchTab(target)
      return
    }

    if (options.clearStack) {
      this.navigateWithClearStack(path)
      return
    }

    if (options.replaceCurrent) {
      this.redirectTo(path)
      return
    }

    wx.navigateTo({
      url: path,
      fail: (err) => {
        console.error('[导航] navigateTo 失败', err)
        this.redirectTo(path)
      }
    })
  }

  navigateFromReservationToStudy(params?: Record<string, string | number | boolean>): void {
    console.log('[导航] 从预约页面跳转到学习状态页面')
    
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    
    if (currentPage) {
      this.lastReservationPageId = currentPage.route || null
    }

    const studyPath = this.buildPath('study', params)
    
    wx.redirectTo({
      url: studyPath,
      fail: (err) => {
        console.error('[导航] redirectTo 失败，尝试 navigateTo', err)
        wx.navigateTo({
          url: studyPath
        })
      }
    })
  }

  navigateFromReservationToHome(): void {
    console.log('[导航] 从预约页面返回首页')
    wx.switchTab({
      url: PAGE_PATHS.home
    })
  }

  navigateFromStudyToHome(): void {
    console.log('[导航] 从学习状态页面返回首页')
    wx.switchTab({
      url: PAGE_PATHS.home
    })
  }

  goBackToHome(): void {
    const pages = getCurrentPages()
    
    if (pages.length <= 1) {
      this.switchTab('home')
      return
    }

    const homePageIndex = pages.findIndex(
      page => page.route === 'pages/home/home'
    )

    if (homePageIndex >= 0) {
      const delta = pages.length - 1 - homePageIndex
      wx.navigateBack({ delta })
    } else {
      this.switchTab('home')
    }
  }

  private switchTab(target: NavigationTarget): void {
    wx.switchTab({
      url: PAGE_PATHS[target]
    })
  }

  private redirectTo(path: string): void {
    wx.redirectTo({
      url: path,
      fail: (err) => {
        console.error('[导航] redirectTo 失败', err)
      }
    })
  }

  private navigateWithClearStack(path: string): void {
    wx.reLaunch({
      url: path
    })
  }

  private buildPath(target: NavigationTarget, params?: Record<string, string | number | boolean>): string {
    let path = PAGE_PATHS[target]
    
    if (params && Object.keys(params).length > 0) {
      const queryString = Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
        .join('&')
      path = `${path}?${queryString}`
    }
    
    return path
  }

  getCurrentPageRoute(): string | null {
    const pages = getCurrentPages()
    if (pages.length > 0) {
      return pages[pages.length - 1].route || null
    }
    return null
  }

  isOnReservationPage(): boolean {
    const currentRoute = this.getCurrentPageRoute()
    return currentRoute === 'pages/seat-selection/seat-selection'
  }

  isOnStudyPage(): boolean {
    const currentRoute = this.getCurrentPageRoute()
    return currentRoute === 'pages/study-status/study-status'
  }
}

const navigationManager = new NavigationManager()

export { navigationManager }
export type { NavigationTarget, NavigationOptions }
export default navigationManager
