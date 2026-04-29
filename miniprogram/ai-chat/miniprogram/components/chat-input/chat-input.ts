Component({
  properties: {
    disabled: {
      type: Boolean,
      value: false
    },
    placeholder: {
      type: String,
      value: '输入消息...'
    }
  },

  data: {
    inputValue: '',
    focus: false
  },

  methods: {
    onInputChange(e: WechatMiniprogram.Input) {
      this.setData({
        inputValue: e.detail.value
      });
    },

    onFocus() {
      this.setData({
        focus: true
      });
    },

    onBlur() {
      this.setData({
        focus: false
      });
    },

    onSend() {
      const { inputValue } = this.data;
      if (!inputValue.trim()) {
        return;
      }

      this.triggerEvent('send', { content: inputValue.trim() });
      this.setData({
        inputValue: ''
      });
    },

    onConfirm(e: WechatMiniprogram.Input) {
      const value = e.detail.value;
      if (!value.trim()) {
        return;
      }

      this.triggerEvent('send', { content: value.trim() });
      this.setData({
        inputValue: ''
      });
    }
  }
});
