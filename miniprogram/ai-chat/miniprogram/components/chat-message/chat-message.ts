import type { MessageRole } from '../../typings/chat';

Component({
  properties: {
    role: {
      type: String as () => MessageRole,
      value: 'assistant'
    },
    content: {
      type: String,
      value: ''
    },
    avatar: {
      type: String,
      value: ''
    },
    name: {
      type: String,
      value: ''
    },
    isStreaming: {
      type: Boolean,
      value: false
    }
  },

  data: {
    defaultUserAvatar: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
    defaultAiAvatar: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'
  },

  methods: {}
});
