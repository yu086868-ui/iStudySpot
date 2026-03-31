import type { ApiResponse, Rule, RuleListParams } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const ruleApi = {
  async getRules(params?: RuleListParams): Promise<ApiResponse<Rule[]>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Rule[]>({
        url: '/rules',
        method: 'GET',
        data: params
      });
    }

    return await request.get<Rule[]>('/rules', params);
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
