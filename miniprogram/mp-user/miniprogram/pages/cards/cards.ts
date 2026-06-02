import { cardApi, store, StoreEvent } from '../../services/index'
import type { Card, CardRarity } from '../../typings/api'
import { render as renderMarkdown } from '../../utils/markdown-engine'

type SortMode = 'id' | 'rarity' | 'time'

interface DisplayCard extends Card {
  html: string
}

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

const RARITY_DOC_MD = `# 卡片稀有度说明

每一张卡片，都会拥有属于自己的稀有度。

稀有度并不代表学习价值的高低。

它更像是一种——

> 对一次真实学习投入的记录方式。

------

## 稀有度如何产生？

卡片稀有度由系统自动计算。

依据为：

- 本次有效学习时长

系统会根据学习记录进入对应概率池，并生成最终稀有度。

AI不会决定稀有度。

稀有度由系统规则独立计算。

------

## 稀有度概率

不同学习时长，将进入不同概率池。

### 短时学习

适用于：

- 30分钟以内学习

概率：

- 普通（Common）：70%
- 稀有（Rare）：25%
- 史诗（Epic）：5%
- 传说（Legend）：0%

------

### 中等学习

适用于：

- 30~120分钟学习

概率：

- 普通（Common）：50%
- 稀有（Rare）：35%
- 史诗（Epic）：13%
- 传说（Legend）：2%

------

### 深度学习

适用于：

- 120分钟以上学习

概率：

- 普通（Common）：35%
- 稀有（Rare）：40%
- 史诗（Epic）：20%
- 传说（Legend）：5%

------

> 以上概率为当前版本规则，后续如有调整，将在此公示。

------

## 关于生成机制

卡片采用概率生成机制。

这意味着：

- 稀有卡始终存在，但数量较少
- 高等级卡片不会固定出现
- 不存在保底机制
- 不支持重抽或刷新结果

每一次学习记录，只会对应一次生成结果。

------

## 关于高稀有度

高稀有度卡片始终保持稀缺。

它不是签到奖励。

也不是连续生成后的必然结果。

稀有度的存在，并不是为了制造焦虑，而是为了保留收藏中的偶然性与惊喜感。

------

## 最后

卡片系统的核心不是抽卡。

而是：

> 用收藏的方式，记录你认真投入过的时间。`

function sortCards(cards: Card[], mode: SortMode): DisplayCard[] {
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
  return sorted.map(function(card) {
    return Object.assign({}, card, { html: renderMarkdown(card.markdown) })
  })
}

Page({
  data: {
    cards: [] as Card[],
    sortedCards: [] as DisplayCard[],
    sortMode: 'id' as SortMode,
    sortOptions: SORT_OPTIONS,
    showSortPicker: false,
    loading: false,
    rarityLabel: RARITY_LABEL,
    rarityBorderColor: RARITY_BORDER_COLOR,
    showCardPopup: false,
    selectedCard: null as Card | null,
    showDocPopup: false,
    docAnimating: false,
    rarityDocHtml: ''
  },

  unsubscribeCards: null as (() => void) | null,

  onLoad() {
    this.setData({
      rarityDocHtml: renderMarkdown(RARITY_DOC_MD)
    })
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
    const card = this.data.cards.find(function(c) { return c.uuid === uuid })
    if (card) {
      this.setData({
        showCardPopup: true,
        selectedCard: card
      })
    }
  },

  onCardPopupClose() {
    this.setData({ showCardPopup: false, selectedCard: null })
  },

  onInfoTap() {
    this.setData({ showDocPopup: true })
    setTimeout(() => {
      this.setData({ docAnimating: true })
    }, 50)
  },

  onDocPopupClose() {
    this.setData({ docAnimating: false })
    setTimeout(() => {
      this.setData({ showDocPopup: false })
    }, 300)
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
    } finally {
      this.setData({ loading: false })
    }
  }
})
