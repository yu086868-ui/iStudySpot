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
    },
    streaming: {
      type: Boolean,
      value: false
    },
    streamingHtml: {
      type: String,
      value: ''
    },
    streamingRarity: {
      type: String,
      value: 'N'
    },
    streamingThemeCategory: {
      type: String,
      value: ''
    }
  },

  data: {
    animating: false,
    isStreaming: false,
    displayHtml: '',
    displayRarity: 'N' as CardRarity,
    displayImageURL: '',
    displayCreateTime: '',
    displayStudyDuration: 0,
    displayThemeCategory: '',
    displayThemeLabel: '',
    rarityBorderColor: RARITY_BORDER_COLOR
  },

  observers: {
    'streaming': function (streaming: boolean) {
      this.setData({ isStreaming: streaming })
      if (streaming) {
        this.setData({
          displayRarity: (this.properties.streamingRarity as CardRarity) || 'N',
          displayThemeCategory: this.properties.streamingThemeCategory || '',
          displayThemeLabel: THEME_LABEL[this.properties.streamingThemeCategory || ''] || '',
          displayImageURL: '',
          displayCreateTime: '',
          displayStudyDuration: 0
        })
      }
    },
    'streamingHtml': function (html: string) {
      if (this.data.isStreaming) {
        this.setData({ displayHtml: html })
      }
    },
    'streamingRarity': function (rarity: string) {
      if (this.data.isStreaming) {
        this.setData({ displayRarity: rarity as CardRarity })
      }
    },
    'streamingThemeCategory': function (category: string) {
      if (this.data.isStreaming) {
        this.setData({
          displayThemeCategory: category,
          displayThemeLabel: THEME_LABEL[category] || ''
        })
      }
    },
    'card': function (card: Record<string, unknown>) {
      if (card && card.uuid) {
        var cardData = card as unknown as Card
        this.setData({
          isStreaming: false,
          displayHtml: renderMarkdown(cardData.markdown),
          displayRarity: cardData.rarity,
          displayImageURL: cardData.imageURL,
          displayCreateTime: cardData.createTime,
          displayStudyDuration: cardData.studyDuration,
          displayThemeCategory: cardData.themeCategory || '',
          displayThemeLabel: THEME_LABEL[cardData.themeCategory] || ''
        })
      }
    },
    'visible': function (visible: boolean) {
      if (visible) {
        if (!this.data.isStreaming) {
          var card = this.properties.card as Record<string, unknown>
          if (card && card.uuid) {
            var cardData = card as unknown as Card
            this.setData({
              displayHtml: renderMarkdown(cardData.markdown),
              displayRarity: cardData.rarity,
              displayImageURL: cardData.imageURL,
              displayCreateTime: cardData.createTime,
              displayStudyDuration: cardData.studyDuration,
              displayThemeCategory: cardData.themeCategory || '',
              displayThemeLabel: THEME_LABEL[cardData.themeCategory] || ''
            })
          }
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
