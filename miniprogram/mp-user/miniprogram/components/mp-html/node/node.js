/**
 * @fileoverview 递归子组件，用于显示节点树
 */
Component({
  data: {
    ctrl: {},
    nodes: [],
    isiOS: (wx.canIUse('getDeviceInfo') ? wx.getDeviceInfo() : wx.getSystemInfoSync()).system.includes('iOS')
  },
  properties: {
    childs: {
      type: Array,
      value: [],
      observer (nodes) {
        const data = {}
        function diff (a, b, path) {
          let alen = a.length
          if (alen === 0 || b.length === 0) {
            data[path] = b
            return
          }
          while (alen > b.length) {
            if (Object.keys(a[alen - 1]).length !== 0) {
              data[path + '[' + (alen - 1) + ']'] = {}
            }
            alen -= 1
          }
          while (alen < b.length) {
            data[path + '[' + alen + ']'] = b[alen]
            alen += 1
          }
          for (let i = 0; i < Math.min(a.length, b.length); i++) {
            const keys = new Set(Object.keys(a[i]).concat(Object.keys(b[i])))
            for (const key of keys) {
              if (a[i][key] === undefined || b[i][key] === undefined || (typeof b[i][key] !== 'object' && a[i][key] !== b[i][key])) {
                data[path + '[' + i + ']'] = b[i]
                break
              }
            }
            if (!((path + '[' + i + ']') in data)) {
              for (const key of keys) {
                if (Array.isArray(b[i][key])) {
                  diff(a[i][key], b[i][key], path + '[' + i + '].' + key)
                } else {
                  for (const subKey of Object.keys(a[i][key]).concat(Object.keys(b[i][key]))) {
                    if (a[i][key][subKey] !== b[i][key][subKey]) {
                      data[path + '[' + i + '].' + key] = b[i][key]
                      break
                    }
                  }
                }
              }
            }
          }
        }
        diff(this.data.nodes, nodes, 'nodes')
        if (Object.keys(data).length !== 0) {
          this.setData(data)
        }
      }
    },
    opts: Array
  },
  options: {
    addGlobalClass: true
  },
  attached () {
    this.triggerEvent('add', this, {
      bubbles: true,
      composed: true
    })
  },
  methods: {
    noop () { },
    getNode (path) {
      try {
        const nums = path.split('_')
        let node = this.properties.childs[nums[0]]
        for (let i = 1; i < nums.length; i++) {
          node = node.children[nums[i]]
        }
        return node
      } catch (e) {
        return {
          text: '',
          attrs: {},
          children: []
        }
      }
    },
    play (e) {
      const i = e.target.dataset.i
      const node = this.getNode(i)
      this.root.triggerEvent('play', {
        source: node.name,
        attrs: Object.assign({}, node.attrs, {
          src: node.src[this.data.ctrl[i] || 0]
        })
      })
      if (this.root.properties.pauseVideo) {
        let flag = false
        const id = e.target.id
        for (let i = this.root._videos.length; i--;) {
          if (this.root._videos[i].id === id) {
            flag = true
          } else {
            this.root._videos[i].pause()
          }
        }
        if (!flag) {
          const ctx = wx.createVideoContext(id, this)
          ctx.id = id
          if (this.root.playbackRate) {
            ctx.playbackRate(this.root.playbackRate)
          }
          this.root._videos.push(ctx)
        }
      }
    },
    mediaEvent (e) {
      const i = e.target.dataset.i
      const node = this.getNode(i)
      this.root.triggerEvent(e.type, Object.assign({}, e.detail, {
        source: node.name,
        attrs: Object.assign({}, node.attrs, {
          src: node.src[this.data.ctrl[i] || 0]
        })
      }))
    },
    imgTap (e) {
      const node = this.getNode(e.target.dataset.i)
      if (node.a) return this.linkTap(node.a)
      if (node.attrs.ignore) return
      this.root.triggerEvent('imgtap', node.attrs)
      if (this.root.properties.previewImg) {
        const current = this.root.imgList[node.i]
        wx.previewImage({
          showmenu: this.root.properties.showImgMenu,
          current,
          urls: this.root.imgList
        })
      }
    },
    imgLoad (e) {
      const i = e.target.dataset.i
      const node = this.getNode(i)
      let val
      if (!node.w) {
        val = e.detail.width
      } else if ((this.properties.opts[1] && !this.data.ctrl[i]) || this.data.ctrl[i] === -1) {
        val = 1
      }
      if (val) {
        this.setData({
          ['ctrl.' + i]: val
        })
      }
      this.checkReady()
    },
    checkReady () {
      if (!this.root.properties.lazyLoad) {
        this.root.imgList._unloadimgs -= 1
        if (!this.root.imgList._unloadimgs) {
          setTimeout(() => {
            this.root.getRect().then(rect => {
              this.root.triggerEvent('ready', rect)
            }).catch(() => {
              this.root.triggerEvent('ready', {})
            })
          }, 350)
        }
      }
    },
    linkTap (e) {
      const node = e.currentTarget ? this.getNode(e.currentTarget.dataset.i) : {}
      const attrs = node.attrs || e
      const href = attrs.href
      this.root.triggerEvent('linktap', Object.assign({
        innerText: this.root.getText(node.children || [])
      }, attrs))
      if (href) {
        if (href[0] === '#') {
          this.root.navigateTo(href.substring(1)).catch(() => { })
        } else if (href.split('?')[0].includes('://')) {
          if (this.root.properties.copyLink) {
            wx.setClipboardData({
              data: href,
              success: () =>
                wx.showToast({
                  title: '链接已复制'
                })
            })
          }
        } else {
          wx.navigateTo({
            url: href,
            fail () {
              wx.switchTab({
                url: href,
                fail () { }
              })
            }
          })
        }
      }
    },
    mediaError (e) {
      const i = e.target.dataset.i
      const node = this.getNode(i)
      if (node.name === 'video' || node.name === 'audio') {
        let index = (this.data.ctrl[i] || 0) + 1
        if (index > node.src.length) {
          index = 0
        }
        if (index < node.src.length) {
          return this.setData({
            ['ctrl.' + i]: index
          })
        }
      } else if (node.name === 'img') {
        if (this.properties.opts[2]) {
          this.setData({
            ['ctrl.' + i]: -1
          })
        }
        this.checkReady()
      }
      if (this.root) {
        this.root.triggerEvent('error', {
          source: node.name,
          attrs: node.attrs,
          errMsg: e.detail.errMsg
        })
      }
    }
  }
})
