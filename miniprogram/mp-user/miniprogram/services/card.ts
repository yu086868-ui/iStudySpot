import type { ApiResponse, Card, GenerateCardParams, CardListParams } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const cardApi = {
  async generateCard(params: GenerateCardParams): Promise<ApiResponse<Card>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<Card>({
        url: '/card/generate',
        method: 'POST',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.addCard(response.data);
      }
      return response;
    }

    const response = await request.post<Card>('/card/generate', params);
    if (response.code === 200 && response.data) {
      store.addCard(response.data);
    }
    return response;
  },

  async getCardDetail(uuid: string, forceRefresh = false): Promise<ApiResponse<Card>> {
    if (!forceRefresh) {
      const cachedCard = store.getCardById(uuid);
      if (cachedCard) {
        return {
          code: 200,
          message: 'success',
          data: cachedCard,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<Card>({
        url: `/card/detail?id=${uuid}`,
        method: 'GET'
      });
      return response;
    }

    const response = await request.get<Card>('/card/detail', { id: uuid });
    return response;
  },

  async getCardList(params: CardListParams, forceRefresh = false): Promise<ApiResponse<Card[]>> {
    if (!forceRefresh) {
      const cachedCards = store.getCards();
      if (cachedCards.length > 0) {
        return {
          code: 200,
          message: 'success',
          data: cachedCards,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<Card[]>({
        url: '/card/list',
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.setCards(response.data);
      }
      return response;
    }

    const response = await request.get<Card[]>('/card/list', params);
    if (response.code === 200 && response.data) {
      store.setCards(response.data);
    }
    return response;
  }
};
