import type { ApiResponse, Card, GenerateCardParams, CardListParams, StreamCallbacks, SSEDataEvent, SSEInitEvent, SSETextEvent, SSECompleteEvent, SSEErrorEvent } from '../typings/api';
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
  },

  generateCardStream(
    params: GenerateCardParams,
    callbacks: StreamCallbacks
  ): () => void {
    if (mockManager.isEnabled()) {
      return mockManager.requestStream('/card/generate/stream', 'POST', params, callbacks);
    }

    var requestTask = request.requestSSE({
      url: '/card/generate/stream',
      data: params,
      onEvent: function (eventName: string, eventData: unknown) {
        if (eventName === 'data') {
          var data = eventData as SSEDataEvent;
          if (data.type === 'init') {
            callbacks.onInit(data as SSEInitEvent);
          } else if (data.type === 'text') {
            callbacks.onText((data as SSETextEvent).content);
          }
        } else if (eventName === 'complete') {
          var completeData = eventData as SSECompleteEvent;
          if (completeData.success && completeData.card) {
            store.addCard(completeData.card);
            callbacks.onComplete(completeData.card);
          } else {
            callbacks.onError(completeData.message || '生成失败');
          }
        } else if (eventName === 'error') {
          var errorData = eventData as SSEErrorEvent;
          callbacks.onError(errorData.message || '生成失败');
        }
      },
      onError: function (error: string) {
        callbacks.onError(error);
      },
      onComplete: function () {
        // SSE 连接关闭
      }
    });

    return function () {
      requestTask.abort();
    };
  }
};
