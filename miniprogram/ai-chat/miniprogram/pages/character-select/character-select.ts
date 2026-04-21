import { getCharacters } from '../../services/index';
import type { Character } from '../../typings/character';

Component({
  data: {
    characters: [] as Character[],
    loading: true,
    defaultAvatar: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'
  },

  lifetimes: {
    attached() {
      this.loadCharacters();
    }
  },

  methods: {
    async loadCharacters() {
      try {
        const characters = await getCharacters();
        this.setData({
          characters,
          loading: false
        });
      } catch (error) {
        console.error('Failed to load characters:', error);
        this.setData({
          loading: false
        });
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
    },

    onSelectCharacter(e: WechatMiniprogram.TouchEvent) {
      const { character } = e.currentTarget.dataset;
      wx.navigateTo({
        url: `/pages/chat/chat?characterId=${character.id}&characterName=${encodeURIComponent(character.name)}&characterAvatar=${encodeURIComponent(character.avatar || '')}`
      });
    }
  }
});
