import request from './request';
import type { Character } from '../typings/character';
import { getMockCharacters } from '../utils/mock';
import connectionService from './connection';

export async function getCharacters(): Promise<Character[]> {
  const isConnected = await connectionService.checkConnection();
  
  if (!isConnected) {
    return getMockCharacters();
  }
  
  try {
    return await request.get<Character[]>('/characters');
  } catch (error) {
    console.error('Failed to fetch characters, using mock data:', error);
    return getMockCharacters();
  }
}
