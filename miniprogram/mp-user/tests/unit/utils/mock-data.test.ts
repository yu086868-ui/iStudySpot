import { rollRarity, rollThemeCategory, generateMarkdown, getImageURL, generateCard } from '../../../miniprogram/utils/mock-data';
import type { CardRarity, ThemeCategory } from '../../../miniprogram/typings/api';

const ALL_RARITIES: CardRarity[] = ['N', 'R', 'SR', 'SSR', 'UR', 'LR'];
const ALL_THEMES: ThemeCategory[] = ['growth', 'history', 'philosophy', 'nature', 'tech', 'companion', 'hidden'];

describe('rollRarity', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('returns N when duration < 10 regardless of random', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0.5);
    expect(rollRarity(5)).toBe('N');
    jest.restoreAllMocks();
    jest.spyOn(Math, 'random').mockReturnValue(0.99);
    expect(rollRarity(0)).toBe('N');
    jest.restoreAllMocks();
    jest.spyOn(Math, 'random').mockReturnValue(0);
    expect(rollRarity(9)).toBe('N');
  });

  it('returns only N, R, or SR for duration 10-30', () => {
    const seen = new Set<CardRarity>();
    for (let i = 0; i < 500; i++) {
      seen.add(rollRarity(20));
    }
    expect(seen).not.toContain('SSR');
    expect(seen).not.toContain('UR');
    expect(seen).not.toContain('LR');
    for (const r of seen) {
      expect(['N', 'R', 'SR']).toContain(r);
    }
  });

  it('never returns UR or LR for duration 30-60', () => {
    const seen = new Set<CardRarity>();
    for (let i = 0; i < 500; i++) {
      seen.add(rollRarity(45));
    }
    expect(seen).not.toContain('UR');
    expect(seen).not.toContain('LR');
  });

  it('never returns LR for duration 60-120', () => {
    const seen = new Set<CardRarity>();
    for (let i = 0; i < 500; i++) {
      seen.add(rollRarity(90));
    }
    expect(seen).not.toContain('LR');
  });

  it('can return any rarity for duration 120-240', () => {
    const seen = new Set<CardRarity>();
    for (let i = 0; i < 2000; i++) {
      seen.add(rollRarity(150));
    }
    for (const r of ALL_RARITIES) {
      expect(seen).toContain(r);
    }
  });

  it('can return any rarity for duration 240+', () => {
    const seen = new Set<CardRarity>();
    for (let i = 0; i < 2000; i++) {
      seen.add(rollRarity(300));
    }
    for (const r of ALL_RARITIES) {
      expect(seen).toContain(r);
    }
  });
});

describe('rollThemeCategory', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('never returns hidden for N/R/SR/SSR rarities', () => {
    const lowerRarities: CardRarity[] = ['N', 'R', 'SR', 'SSR'];
    for (const rarity of lowerRarities) {
      const seen = new Set<ThemeCategory>();
      for (let i = 0; i < 500; i++) {
        seen.add(rollThemeCategory(rarity));
      }
      expect(seen).not.toContain('hidden');
    }
  });

  it('hidden can appear for UR', () => {
    const seen = new Set<ThemeCategory>();
    for (let i = 0; i < 2000; i++) {
      seen.add(rollThemeCategory('UR'));
    }
    expect(seen).toContain('hidden');
  });

  it('hidden can appear for LR', () => {
    const seen = new Set<ThemeCategory>();
    for (let i = 0; i < 2000; i++) {
      seen.add(rollThemeCategory('LR'));
    }
    expect(seen).toContain('hidden');
  });

  it('returns a valid ThemeCategory', () => {
    for (const rarity of ALL_RARITIES) {
      for (let i = 0; i < 100; i++) {
        const result = rollThemeCategory(rarity);
        expect(ALL_THEMES).toContain(result);
      }
    }
  });
});

describe('generateMarkdown', () => {
  it('returns non-empty string for each ThemeCategory', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0);
    for (const theme of ALL_THEMES) {
      const result = generateMarkdown(theme);
      expect(typeof result).toBe('string');
      expect(result.length).toBeGreaterThan(0);
    }
  });
});

describe('getImageURL', () => {
  it('returns non-empty string for each ThemeCategory', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0);
    for (const theme of ALL_THEMES) {
      const result = getImageURL(theme);
      expect(typeof result).toBe('string');
      expect(result.length).toBeGreaterThan(0);
    }
  });
});

describe('generateCard', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('returns a Card with correct structure', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0.5);
    const card = generateCard('user_001', 60);
    expect(card).toHaveProperty('uuid');
    expect(card).toHaveProperty('userID');
    expect(card).toHaveProperty('cardID');
    expect(card).toHaveProperty('createTime');
    expect(card).toHaveProperty('studyDuration');
    expect(card).toHaveProperty('rarity');
    expect(card).toHaveProperty('borderTheme');
    expect(card).toHaveProperty('cardTheme');
    expect(card).toHaveProperty('themeCategory');
    expect(card).toHaveProperty('markdown');
    expect(card).toHaveProperty('imageURL');
  });

  it('preserves userID in the result', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0.5);
    const card = generateCard('user_999', 30);
    expect(card.userID).toBe('user_999');
  });

  it('preserves studyDuration in the result', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0.5);
    const card = generateCard('user_001', 42);
    expect(card.studyDuration).toBe(42);
  });

  it('sets rarity to a valid CardRarity', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0.5);
    const card = generateCard('user_001', 60);
    expect(ALL_RARITIES).toContain(card.rarity);
  });

  it('sets themeCategory to a valid ThemeCategory', () => {
    jest.spyOn(Math, 'random').mockReturnValue(0.5);
    const card = generateCard('user_001', 60);
    expect(ALL_THEMES).toContain(card.themeCategory);
  });
});
