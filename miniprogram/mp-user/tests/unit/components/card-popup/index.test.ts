let componentDef: any;

global.Component = (def: any) => {
  componentDef = def;
};

jest.mock('../../../../miniprogram/utils/markdown-engine', () => ({
  render: jest.fn().mockReturnValue('<p>test</p>')
}));

require('../../../../miniprogram/components/card-popup/index');

function createInstance() {
  const setData = jest.fn();
  const triggerEvent = jest.fn();
  const instance = {
    setData,
    triggerEvent,
    properties: { card: {} as Record<string, unknown> },
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
  });

  it('has correct data defaults', () => {
    expect(componentDef.data.animating).toBe(false);
    expect(componentDef.data.html).toBe('');
    expect(componentDef.data.rarity).toBe('N');
    expect(componentDef.data.imageURL).toBe('');
    expect(componentDef.data.createTime).toBe('');
    expect(componentDef.data.studyDuration).toBe(0);
    expect(componentDef.data.themeCategory).toBe('');
    expect(componentDef.data.themeLabel).toBe('');
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
    expect(lastCall.themeLabel).toBe(expectedLabels.growth);
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
      html: '<p>test</p>',
      rarity: 'SSR',
      imageURL: 'https://example.com/img.png',
      createTime: '2025-01-01',
      studyDuration: 3600,
      themeCategory: 'philosophy',
      themeLabel: '哲思感悟'
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
        themeCategory: '',
        themeLabel: ''
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

  it('visible observer renders card data when visible is true and card has uuid', () => {
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
      html: '<p>test</p>',
      rarity: 'UR',
      imageURL: 'https://img.com/a.png',
      createTime: '2025-06-01',
      studyDuration: 7200,
      themeCategory: 'nature',
      themeLabel: '自然意象'
    });
    jest.advanceTimersByTime(50);
    expect(instance.setData).toHaveBeenCalledWith({ animating: true });
    jest.useRealTimers();
  });
});
