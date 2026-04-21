import request from './request';
import type { Character } from '../typings/character';

export async function getCharacters(): Promise<Character[]> {
  return request.get<Character[]>('/characters');
}
