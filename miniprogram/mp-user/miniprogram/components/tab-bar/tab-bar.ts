Component({
  properties: {
    currentTab: {
      type: String,
      value: 'home'
    }
  },

  data: {},

  methods: {
    switchToRules() {
      wx.switchTab({
        url: '/pages/rules/rules'
      })
    },

    switchToHome() {
      wx.switchTab({
        url: '/pages/home/home'
      })
    },

    switchToProfile() {
      wx.switchTab({
        url: '/pages/profile/profile'
      })
    }
  }
})
