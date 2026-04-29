import { generateUUID } from '../../utils/uuid';

describe('generateUUID', () => {
  it('should generate a valid UUID string', () => {
    const uuid = generateUUID();
    expect(typeof uuid).toBe('string');
  });

  it('should generate UUID with correct format', () => {
    const uuid = generateUUID();
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;
    expect(uuid).toMatch(uuidRegex);
  });

  it('should generate unique UUIDs', () => {
    const uuids = new Set<string>();
    for (let i = 0; i < 100; i++) {
      uuids.add(generateUUID());
    }
    expect(uuids.size).toBe(100);
  });

  it('should have version 4 indicator at position 14', () => {
    const uuid = generateUUID();
    expect(uuid.charAt(14)).toBe('4');
  });

  it('should have valid variant at position 19', () => {
    const uuid = generateUUID();
    const variantChar = uuid.charAt(19);
    expect(['8', '9', 'a', 'b']).toContain(variantChar);
  });

  it('should have correct length', () => {
    const uuid = generateUUID();
    expect(uuid.length).toBe(36);
  });

  it('should have correct number of hyphens', () => {
    const uuid = generateUUID();
    const hyphenCount = uuid.split('-').length - 1;
    expect(hyphenCount).toBe(4);
  });
});
