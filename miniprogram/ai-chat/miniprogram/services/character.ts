import request from './request';
import type { Character } from '../typings/character';
import { getMockCharacters } from '../utils/mock';

const USE_MOCK = true;

export async function getCharacters(): Promise<Character[]> {
  if (USE_MOCK) {
    return getMockCharacters();
  }
  return request.get<Character[]>('/characters');
}
