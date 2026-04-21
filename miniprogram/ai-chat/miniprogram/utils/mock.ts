import type { Character } from '../typings/character';

const MOCK_CHARACTERS: Character[] = [
  {
    id: 'einstein',
    name: '爱因斯坦',
    persona: '伟大的物理学家，相对论创立者，诺贝尔物理学奖获得者',
    speaking_style: '思维深邃，善于用简单的比喻解释复杂的物理概念'
  }
];

export function getMockCharacters(): Character[] {
  return MOCK_CHARACTERS;
}

export function getMockChatReply(): string {
  return '请检查链接。';
}
