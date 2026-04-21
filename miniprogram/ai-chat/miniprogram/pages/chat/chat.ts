import { sendMessageStream } from '../../services';
import { generateUUID } from '../../utils/uuid';
import type { Message } from '../../typings/chat';

interface ChatData {
  characterId: string;
  characterName: string;
  characterAvatar: string;
  sessionId: string;
  messages: Message[];
  isStreaming: boolean;
  streamingContent: string;
  scrollIntoView: string;
}

Component({
  data: {
    characterId: '',
    characterName: '',
    characterAvatar: '',
    sessionId: '',
    messages: [] as Message[],
    isStreaming: false,
    streamingContent: '',
    scrollIntoView: ''
  } as ChatData,

  lifetimes: {
    attached() {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const options = currentPage.options || {};

      const characterId = options.characterId || '';
      const characterName = decodeURIComponent(options.characterName || '科学家');
      const characterAvatar = decodeURIComponent(options.characterAvatar || '');

      this.setData({
        characterId,
        characterName,
        characterAvatar,
        sessionId: generateUUID()
      });
    }
  },

  methods: {
    onSendMessage(e: WechatMiniprogram.CustomEvent) {
      const { content } = e.detail;
      if (!content.trim() || this.data.isStreaming) {
        return;
      }

      this.sendMessage(content);
    },

    sendMessage(content: string) {
      const { characterId, sessionId, messages } = this.data;

      const userMessage: Message = {
        role: 'user',
        content,
        timestamp: Date.now()
      };

      this.setData({
        messages: [...messages, userMessage],
        isStreaming: true,
        streamingContent: ''
      });

      this.scrollToBottom();

      sendMessageStream(
        {
          session_id: sessionId,
          character_id: characterId,
          message: content
        },
        {
          onStart: () => {
            this.setData({
              streamingContent: ''
            });
          },
          onDelta: (delta: string) => {
            this.setData({
              streamingContent: this.data.streamingContent + delta
            });
            this.scrollToBottom();
          },
          onEnd: () => {
            const assistantMessage: Message = {
              role: 'assistant',
              content: this.data.streamingContent,
              timestamp: Date.now()
            };

            this.setData({
              messages: [...this.data.messages, assistantMessage],
              isStreaming: false,
              streamingContent: ''
            });

            this.scrollToBottom();
          },
          onError: (message: string) => {
            wx.showToast({
              title: message,
              icon: 'none'
            });
            this.setData({
              isStreaming: false,
              streamingContent: ''
            });
          }
        }
      ).catch((error) => {
        console.error('Stream error:', error);
        wx.showToast({
          title: '发送失败',
          icon: 'none'
        });
        this.setData({
          isStreaming: false,
          streamingContent: ''
        });
      });
    },

    scrollToBottom() {
      this.setData({
        scrollIntoView: 'bottom-anchor'
      });
    }
  }
});
