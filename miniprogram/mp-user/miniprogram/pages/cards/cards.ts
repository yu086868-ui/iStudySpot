import { cardApi, store, StoreEvent } from '../../services/index'
import type { Card, CardRarity } from '../../typings/api'

type SortMode = 'id' | 'rarity' | 'time'

const RARITY_ORDER: CardRarity[] = ['N', 'R', 'SR', 'SSR', 'UR', 'LR']

const RARITY_LABEL: Record<CardRarity, string> = {
  'N': '白',
  'R': '绿',
  'SR': '蓝',
  'SSR': '紫',
  'UR': '金',
  'LR': '红'
}

const RARITY_BORDER_COLOR: Record<CardRarity, string> = {
  'N': '#CCCCCC',
  'R': '#4CAF50',
  'SR': '#2196F3',
  'SSR': '#9C27B0',
  'UR': '#FFD700',
  'LR': '#F44336'
}

const SORT_OPTIONS: { key: SortMode; label: string }[] = [
  { key: 'id', label: '默认' },
  { key: 'rarity', label: '稀有度' },
  { key: 'time', label: '获取时间' }
]

function sortCards(cards: Card[], mode: SortMode): Card[] {
  const sorted = [...cards]
  switch (mode) {
    case 'id':
      sorted.sort((a, b) => a.uuid.localeCompare(b.uuid))
      break
    case 'rarity':
      sorted.sort((a, b) => {
        const ia = RARITY_ORDER.indexOf(a.rarity)
        const ib = RARITY_ORDER.indexOf(b.rarity)
        return ib - ia
      })
      break
    case 'time':
      sorted.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
      break
  }
  return sorted
}

Page({
  data: {
    cards: [] as Card[],
    sortedCards: [] as Card[],
    sortMode: 'id' as SortMode,
    sortOptions: SORT_OPTIONS,
    showSortPicker: false,
    loading: false,
    rarityLabel: RARITY_LABEL,
    rarityBorderColor: RARITY_BORDER_COLOR
  },

  unsubscribeCards: null as (() => void) | null,

  onLoad() {
    this.loadCards()
    this.subscribeStoreEvents()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'cards'
      })
    }
  },

  onUnload() {
    if (this.unsubscribeCards) {
      this.unsubscribeCards()
    }
  },

  subscribeStoreEvents() {
    this.unsubscribeCards = store.on(StoreEvent.CARDS_CHANGED, () => {
      const cards = store.getCards()
      this.setData({ cards })
      this.applySort()
    })
  },

  async loadCards() {
    const cachedCards = store.getCards()
    if (cachedCards.length > 0) {
      this.setData({ cards: cachedCards })
      this.applySort()
      return
    }

    this.setData({ loading: true })
    try {
      const user = store.getUser()
      const res = await cardApi.getCardList({ userID: user ? user.id : 'user_001' })
      if (res.code === 200 && res.data) {
        this.setData({ cards: res.data })
        this.applySort()
      }
    } catch (error) {
      console.error('加载卡片列表失败', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  applySort() {
    const { cards, sortMode } = this.data
    const sortedCards = sortCards(cards, sortMode)
    this.setData({ sortedCards })
  },

  onSortTap() {
    this.setData({ showSortPicker: true })
  },

  onSortPickerClose() {
    this.setData({ showSortPicker: false })
  },

  onSortSelect(e: WechatMiniprogram.TouchEvent) {
    const mode = e.currentTarget.dataset.mode as SortMode
    this.setData({
      sortMode: mode,
      showSortPicker: false
    })
    this.applySort()
  },

  onCardTap(e: WechatMiniprogram.TouchEvent) {
    const uuid = e.currentTarget.dataset.uuid as string
    wx.navigateTo({
      url: `/pages/card-detail/card-detail?uuid=${uuid}`
    })
  },

  async onRefresh() {
    this.setData({ loading: true })
    try {
      const user = store.getUser()
      const res = await cardApi.getCardList({ userID: user ? user.id : 'user_001' }, true)
      if (res.code === 200 && res.data) {
        this.setData({ cards: res.data })
        this.applySort()
      }
    } catch (error) {
      console.error('刷新卡片列表失败', error)
    } finally {
      this.setData({ loading: false })
    }
  }
})
