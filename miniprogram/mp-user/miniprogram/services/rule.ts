import type { ApiResponse, Rule, RuleListParams } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const ruleApi = {
  async getRules(params?: RuleListParams, forceRefresh = false): Promise<ApiResponse<Rule[]>> {
    if (!forceRefresh && !params) {
      const cachedRules = store.getRules();
      if (cachedRules.length > 0) {
        return {
          code: 200,
          message: 'success',
          data: cachedRules,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<Rule[]>({
        url: '/rules',
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.setRules(response.data);
      }
      return response;
    }

    const response = await request.get<Rule[]>('/rules', params);
    if (response.code === 200 && response.data) {
      store.setRules(response.data);
    }
    return response;
  },

  async getRuleDetail(id: string): Promise<ApiResponse<Rule>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Rule>({
        url: `/rules/${id}`,
        method: 'GET'
      });
    }

    return await request.get<Rule>(`/rules/${id}`);
  }
};
