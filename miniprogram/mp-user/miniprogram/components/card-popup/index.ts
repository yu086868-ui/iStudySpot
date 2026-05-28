import type { Card, CardRarity } from '../../typings/api'
import { render as renderMarkdown } from '../../utils/markdown-engine'

const RARITY_BORDER_COLOR: Record<CardRarity, string> = {
  'N': '#CCCCCC',
  'R': '#4CAF50',
  'SR': '#2196F3',
  'SSR': '#9C27B0',
  'UR': '#FFD700',
  'LR': '#F44336'
}

const THEME_LABEL: Record<string, string> = {
  'growth': '励志成长',
  'history': '名人与历史',
  'philosophy': '哲思感悟',
  'nature': '自然意象',
  'tech': '科技未来',
  'companion': '温柔陪伴',
  'hidden': '隐藏主题'
}

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    card: {
      type: Object,
      value: {} as Record<string, unknown>
    },
    showAction: {
      type: Boolean,
      value: false
    },
    actionText: {
      type: String,
      value: '收下卡片'
    }
  },

  data: {
    animating: false,
    html: '',
    rarity: 'N' as CardRarity,
    imageURL: '',
    createTime: '',
    studyDuration: 0,
    themeCategory: '',
    themeLabel: '',
    rarityBorderColor: RARITY_BORDER_COLOR
  },

  observers: {
    'card': function(card: Record<string, unknown>) {
      if (card && card.uuid) {
        var cardData = card as unknown as Card
        this.setData({
          html: renderMarkdown(cardData.markdown),
          rarity: cardData.rarity,
          imageURL: cardData.imageURL,
          createTime: cardData.createTime,
          studyDuration: cardData.studyDuration,
          themeCategory: cardData.themeCategory || '',
          themeLabel: THEME_LABEL[cardData.themeCategory] || ''
        })
      }
    },
    'visible': function(visible: boolean) {
      if (visible) {
        var card = this.properties.card as Record<string, unknown>
        if (card && card.uuid) {
          var cardData = card as unknown as Card
          this.setData({
            html: renderMarkdown(cardData.markdown),
            rarity: cardData.rarity,
            imageURL: cardData.imageURL,
            createTime: cardData.createTime,
            studyDuration: cardData.studyDuration,
            themeCategory: cardData.themeCategory || '',
            themeLabel: THEME_LABEL[cardData.themeCategory] || ''
          })
        }
        setTimeout(() => {
          this.setData({ animating: true })
        }, 50)
      } else {
        this.setData({ animating: false })
      }
    }
  },

  methods: {
    onMaskTap() {
      this.close()
    },

    onClose() {
      this.close()
    },

    onAction() {
      this.triggerEvent('action', { card: this.properties.card })
      this.close()
    },

    close() {
      this.setData({ animating: false })
      setTimeout(() => {
        this.triggerEvent('close')
      }, 300)
    }
  }
})
