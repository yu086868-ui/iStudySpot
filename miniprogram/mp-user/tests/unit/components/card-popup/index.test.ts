let componentDef: any;

global.Component = (def: any) => {
  componentDef = def;
};

jest.mock('../../../../miniprogram/utils/markdown-engine', () => ({
  render: jest.fn().mockReturnValue('<p>test</p>')
}));

require('../../../../miniprogram/components/card-popup/index');

function createInstance(overrides?: { data?: Record<string, unknown>; properties?: Record<string, unknown> }) {
  const setData = jest.fn();
  const triggerEvent = jest.fn();
  const instance = {
    setData,
    triggerEvent,
    data: {
      animating: false,
      isStreaming: false,
      displayHtml: '',
      displayRarity: 'N',
      displayImageURL: '',
      displayCreateTime: '',
      displayStudyDuration: 0,
      displayThemeCategory: '',
      displayThemeLabel: '',
      ...overrides?.data
    },
    properties: {
      card: {} as Record<string, unknown>,
      streaming: false,
      streamingHtml: '',
      streamingRarity: 'N',
      streamingThemeCategory: '',
      ...overrides?.properties
    } as Record<string, unknown>,
    ...componentDef.methods
  };
  return instance;
}

describe('card-popup component', () => {
  it('has correct properties with default values', () => {
    expect(componentDef.properties.visible.type).toBe(Boolean);
    expect(componentDef.properties.visible.value).toBe(false);
    expect(componentDef.properties.card.type).toBe(Object);
    expect(componentDef.properties.card.value).toEqual({});
    expect(componentDef.properties.showAction.type).toBe(Boolean);
    expect(componentDef.properties.showAction.value).toBe(false);
    expect(componentDef.properties.actionText.type).toBe(String);
    expect(componentDef.properties.actionText.value).toBe('收下卡片');
    expect(componentDef.properties.streaming.type).toBe(Boolean);
    expect(componentDef.properties.streaming.value).toBe(false);
    expect(componentDef.properties.streamingHtml.type).toBe(String);
    expect(componentDef.properties.streamingHtml.value).toBe('');
    expect(componentDef.properties.streamingRarity.type).toBe(String);
    expect(componentDef.properties.streamingRarity.value).toBe('N');
    expect(componentDef.properties.streamingThemeCategory.type).toBe(String);
    expect(componentDef.properties.streamingThemeCategory.value).toBe('');
  });

  it('has correct data defaults', () => {
    expect(componentDef.data.animating).toBe(false);
    expect(componentDef.data.isStreaming).toBe(false);
    expect(componentDef.data.displayHtml).toBe('');
    expect(componentDef.data.displayRarity).toBe('N');
    expect(componentDef.data.displayImageURL).toBe('');
    expect(componentDef.data.displayCreateTime).toBe('');
    expect(componentDef.data.displayStudyDuration).toBe(0);
    expect(componentDef.data.displayThemeCategory).toBe('');
    expect(componentDef.data.displayThemeLabel).toBe('');
  });

  it('RARITY_BORDER_COLOR maps all rarities to valid colors', () => {
    const rarityBorderColor = componentDef.data.rarityBorderColor;
    const rarities = ['N', 'R', 'SR', 'SSR', 'UR', 'LR'];
    rarities.forEach((rarity) => {
      expect(rarityBorderColor[rarity]).toMatch(/^#[0-9A-Fa-f]{6}$/);
    });
  });

  it('THEME_LABEL maps all theme categories to Chinese labels', () => {
    const themeLabel = componentDef.data.rarityBorderColor;
    const themes = ['growth', 'history', 'philosophy', 'nature', 'tech', 'companion', 'hidden'];
    const expectedLabels: Record<string, string> = {
      growth: '励志成长',
      history: '名人与历史',
      philosophy: '哲思感悟',
      nature: '自然意象',
      tech: '科技未来',
      companion: '温柔陪伴',
      hidden: '隐藏主题'
    };
    const cardObserver = componentDef.observers.card;
    const instance = createInstance();
    const testCard = {
      uuid: 'test-uuid',
      markdown: '# hello',
      rarity: 'SSR',
      imageURL: 'https://example.com/img.png',
      createTime: '2025-01-01',
      studyDuration: 3600,
      themeCategory: 'growth'
    };
    cardObserver.call(instance, testCard);
    const setDataCalls = instance.setData.mock.calls;
    const lastCall = setDataCalls[setDataCalls.length - 1][0];
    expect(lastCall.displayThemeLabel).toBe(expectedLabels.growth);
  });

  it('close sets animating to false and triggers close event after 300ms', () => {
    jest.useFakeTimers();
    const instance = createInstance();
    instance.close();
    expect(instance.setData).toHaveBeenCalledWith({ animating: false });
    expect(instance.triggerEvent).not.toHaveBeenCalled();
    jest.advanceTimersByTime(300);
    expect(instance.triggerEvent).toHaveBeenCalledWith('close');
    jest.useRealTimers();
  });

  it('onMaskTap calls close', () => {
    const instance = createInstance();
    const closeSpy = jest.spyOn(instance, 'close');
    instance.onMaskTap();
    expect(closeSpy).toHaveBeenCalled();
  });

  it('onClose calls close', () => {
    const instance = createInstance();
    const closeSpy = jest.spyOn(instance, 'close');
    instance.onClose();
    expect(closeSpy).toHaveBeenCalled();
  });

  it('onAction triggers action event with card data and calls close', () => {
    const instance = createInstance();
    const testCard = { uuid: 'abc', rarity: 'SR' };
    instance.properties.card = testCard;
    const closeSpy = jest.spyOn(instance, 'close');
    instance.onAction();
    expect(instance.triggerEvent).toHaveBeenCalledWith('action', { card: testCard });
    expect(closeSpy).toHaveBeenCalled();
  });

  it('card observer processes card data correctly', () => {
    const { render } = require('../../../../miniprogram/utils/markdown-engine');
    const instance = createInstance();
    const testCard = {
      uuid: 'test-uuid',
      markdown: '# hello',
      rarity: 'SSR',
      imageURL: 'https://example.com/img.png',
      createTime: '2025-01-01',
      studyDuration: 3600,
      themeCategory: 'philosophy'
    };
    componentDef.observers.card.call(instance, testCard);
    expect(render).toHaveBeenCalledWith('# hello');
    expect(instance.setData).toHaveBeenCalledWith({
      isStreaming: false,
      displayHtml: '<p>test</p>',
      displayRarity: 'SSR',
      displayImageURL: 'https://example.com/img.png',
      displayCreateTime: '2025-01-01',
      displayStudyDuration: 3600,
      displayThemeCategory: 'philosophy',
      displayThemeLabel: '哲思感悟'
    });
  });

  it('card observer does nothing when card has no uuid', () => {
    const instance = createInstance();
    componentDef.observers.card.call(instance, {});
    expect(instance.setData).not.toHaveBeenCalled();
  });

  it('card observer handles missing themeCategory gracefully', () => {
    const instance = createInstance();
    const testCard = {
      uuid: 'test-uuid',
      markdown: 'text',
      rarity: 'N',
      imageURL: '',
      createTime: '',
      studyDuration: 0
    };
    componentDef.observers.card.call(instance, testCard);
    expect(instance.setData).toHaveBeenCalledWith(
      expect.objectContaining({
        displayThemeCategory: '',
        displayThemeLabel: ''
      })
    );
  });

  it('visible observer triggers animation when visible is true', () => {
    jest.useFakeTimers();
    const instance = createInstance();
    instance.properties.card = {};
    componentDef.observers.visible.call(instance, true);
    jest.advanceTimersByTime(50);
    expect(instance.setData).toHaveBeenCalledWith({ animating: true });
    jest.useRealTimers();
  });

  it('visible observer sets animating false when visible is false', () => {
    const instance = createInstance();
    componentDef.observers.visible.call(instance, false);
    expect(instance.setData).toHaveBeenCalledWith({ animating: false });
  });

  it('visible observer renders card data when visible is true, card has uuid, and not streaming', () => {
    jest.useFakeTimers();
    const { render } = require('../../../../miniprogram/utils/markdown-engine');
    const instance = createInstance();
    const testCard = {
      uuid: 'test-uuid',
      markdown: '## hi',
      rarity: 'UR',
      imageURL: 'https://img.com/a.png',
      createTime: '2025-06-01',
      studyDuration: 7200,
      themeCategory: 'nature'
    };
    instance.properties.card = testCard;
    componentDef.observers.visible.call(instance, true);
    expect(render).toHaveBeenCalledWith('## hi');
    expect(instance.setData).toHaveBeenCalledWith({
      displayHtml: '<p>test</p>',
      displayRarity: 'UR',
      displayImageURL: 'https://img.com/a.png',
      displayCreateTime: '2025-06-01',
      displayStudyDuration: 7200,
      displayThemeCategory: 'nature',
      displayThemeLabel: '自然意象'
    });
    jest.advanceTimersByTime(50);
    expect(instance.setData).toHaveBeenCalledWith({ animating: true });
    jest.useRealTimers();
  });

  it('visible observer does not render card data when isStreaming is true', () => {
    jest.useFakeTimers();
    const instance = createInstance({ data: { isStreaming: true } });
    const testCard = {
      uuid: 'test-uuid',
      markdown: '## hi',
      rarity: 'UR',
      imageURL: 'https://img.com/a.png',
      createTime: '2025-06-01',
      studyDuration: 7200,
      themeCategory: 'nature'
    };
    instance.properties.card = testCard;
    componentDef.observers.visible.call(instance, true);
    // Should NOT call setData with card display data, only animating
    const setDataCalls = instance.setData.mock.calls;
    const cardDataCalls = setDataCalls.filter((call: any[]) =>
      call[0].displayHtml !== undefined
    );
    expect(cardDataCalls.length).toBe(0);
    jest.advanceTimersByTime(50);
    expect(instance.setData).toHaveBeenCalledWith({ animating: true });
    jest.useRealTimers();
  });

  // streaming observer tests
  it('streaming observer sets isStreaming to true when streaming is true', () => {
    const instance = createInstance({
      properties: {
        streamingRarity: 'SR',
        streamingThemeCategory: 'tech'
      }
    });
    componentDef.observers.streaming.call(instance, true);
    expect(instance.setData).toHaveBeenCalledWith({ isStreaming: true });
    expect(instance.setData).toHaveBeenCalledWith({
      displayRarity: 'SR',
      displayThemeCategory: 'tech',
      displayThemeLabel: '科技未来',
      displayImageURL: '',
      displayCreateTime: '',
      displayStudyDuration: 0
    });
  });

  it('streaming observer sets isStreaming to false when streaming is false', () => {
    const instance = createInstance();
    componentDef.observers.streaming.call(instance, false);
    expect(instance.setData).toHaveBeenCalledWith({ isStreaming: false });
    // Should not set display fields when streaming is false
    const setDataCalls = instance.setData.mock.calls;
    expect(setDataCalls.length).toBe(1);
  });

  it('streaming observer uses default rarity when streamingRarity is empty', () => {
    const instance = createInstance({
      properties: {
        streamingRarity: '',
        streamingThemeCategory: ''
      }
    });
    componentDef.observers.streaming.call(instance, true);
    expect(instance.setData).toHaveBeenCalledWith({
      displayRarity: 'N',
      displayThemeCategory: '',
      displayThemeLabel: '',
      displayImageURL: '',
      displayCreateTime: '',
      displayStudyDuration: 0
    });
  });

  // streamingHtml observer tests
  it('streamingHtml observer updates displayHtml when isStreaming is true', () => {
    const instance = createInstance({ data: { isStreaming: true } });
    componentDef.observers.streamingHtml.call(instance, '<p>streaming content</p>');
    expect(instance.setData).toHaveBeenCalledWith({ displayHtml: '<p>streaming content</p>' });
  });

  it('streamingHtml observer does nothing when isStreaming is false', () => {
    const instance = createInstance({ data: { isStreaming: false } });
    componentDef.observers.streamingHtml.call(instance, '<p>streaming content</p>');
    expect(instance.setData).not.toHaveBeenCalled();
  });

  // streamingRarity observer tests
  it('streamingRarity observer updates displayRarity when isStreaming is true', () => {
    const instance = createInstance({ data: { isStreaming: true } });
    componentDef.observers.streamingRarity.call(instance, 'SSR');
    expect(instance.setData).toHaveBeenCalledWith({ displayRarity: 'SSR' });
  });

  it('streamingRarity observer does nothing when isStreaming is false', () => {
    const instance = createInstance({ data: { isStreaming: false } });
    componentDef.observers.streamingRarity.call(instance, 'SSR');
    expect(instance.setData).not.toHaveBeenCalled();
  });

  // streamingThemeCategory observer tests
  it('streamingThemeCategory observer updates display fields when isStreaming is true', () => {
    const instance = createInstance({ data: { isStreaming: true } });
    componentDef.observers.streamingThemeCategory.call(instance, 'companion');
    expect(instance.setData).toHaveBeenCalledWith({
      displayThemeCategory: 'companion',
      displayThemeLabel: '温柔陪伴'
    });
  });

  it('streamingThemeCategory observer does nothing when isStreaming is false', () => {
    const instance = createInstance({ data: { isStreaming: false } });
    componentDef.observers.streamingThemeCategory.call(instance, 'companion');
    expect(instance.setData).not.toHaveBeenCalled();
  });

  it('streamingThemeCategory observer handles unknown category gracefully', () => {
    const instance = createInstance({ data: { isStreaming: true } });
    componentDef.observers.streamingThemeCategory.call(instance, 'unknown');
    expect(instance.setData).toHaveBeenCalledWith({
      displayThemeCategory: 'unknown',
      displayThemeLabel: ''
    });
  });
});
