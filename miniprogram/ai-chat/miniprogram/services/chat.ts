import type { ChatRequest, ChatResponse, SSEEvent } from '../typings/chat';

const BASE_URL = 'http://localhost:3000/api';

export async function sendMessage(data: ChatRequest): Promise<ChatResponse> {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}/chat`,
      method: 'POST',
      data,
      header: {
        'Content-Type': 'application/json'
      },
      success: (res: WechatMiniprogram.RequestSuccessCallbackResult) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data as ChatResponse);
        } else {
          reject(new Error(`Request failed with status ${res.statusCode}`));
        }
      },
      fail: (error: WechatMiniprogram.GeneralCallbackResult) => {
        reject(error);
      }
    });
  });
}

export interface StreamCallbacks {
  onStart: () => void;
  onDelta: (content: string) => void;
  onEnd: () => void;
  onError: (message: string) => void;
}

export function sendMessageStream(
  data: ChatRequest,
  callbacks: StreamCallbacks
): Promise<void> {
  return new Promise((resolve, reject) => {
    const requestTask = wx.request({
      url: `${BASE_URL}/chat/stream`,
      method: 'POST',
      data,
      header: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'Cache-Control': 'no-cache'
      },
      enableChunked: true,
      success: () => {
        resolve();
      },
      fail: (error: WechatMiniprogram.GeneralCallbackResult) => {
        callbacks.onError('网络请求失败');
        reject(error);
      }
    });

    requestTask.onChunkReceived((response: WechatMiniprogram.OnChunkReceivedListenerResult) => {
      const arrayBuffer = response.data;
      const text = arrayBufferToString(arrayBuffer);
      parseSSE(text, callbacks);
    });
  });
}

function arrayBufferToString(buffer: ArrayBuffer): string {
  const uint8Array = new Uint8Array(buffer);
  let result = '';
  for (let i = 0; i < uint8Array.length; i++) {
    result += String.fromCharCode(uint8Array[i]);
  }
  return decodeURIComponent(escape(result));
}

function parseSSE(text: string, callbacks: StreamCallbacks): void {
  const lines = text.split('\n');
  let currentData = '';

  for (const line of lines) {
    if (line.startsWith('data:')) {
      currentData = line.substring(5).trim();
    } else if (line === '' && currentData) {
      try {
        const event: SSEEvent = JSON.parse(currentData);
        handleSSEEvent(event, callbacks);
      } catch (e) {
        console.error('Failed to parse SSE event:', currentData, e);
      }
      currentData = '';
    }
  }
}

function handleSSEEvent(event: SSEEvent, callbacks: StreamCallbacks): void {
  switch (event.type) {
    case 'start':
      callbacks.onStart();
      break;
    case 'delta':
      if (event.content) {
        callbacks.onDelta(event.content);
      }
      break;
    case 'end':
      callbacks.onEnd();
      break;
    case 'error':
      callbacks.onError(event.message || '未知错误');
      break;
  }
}
