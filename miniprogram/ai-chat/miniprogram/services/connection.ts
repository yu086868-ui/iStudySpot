const BASE_URL = 'http://localhost:3000/api';

type ConnectionStatus = 'unknown' | 'connected' | 'disconnected';

class ConnectionService {
  private status: ConnectionStatus = 'unknown';
  private checking: boolean = false;
  private listeners: Set<(status: ConnectionStatus) => void> = new Set();

  getStatus(): ConnectionStatus {
    return this.status;
  }

  isConnected(): boolean {
    return this.status === 'connected';
  }

  async checkConnection(): Promise<boolean> {
    if (this.checking) {
      return this.status === 'connected';
    }

    this.checking = true;

    try {
      await new Promise<void>((resolve, reject) => {
        const requestTask = wx.request({
          url: `${BASE_URL}/health`,
          method: 'GET',
          timeout: 3000,
          success: (res) => {
            if (res.statusCode >= 200 && res.statusCode < 300) {
              resolve();
            } else {
              reject(new Error('Server error'));
            }
          },
          fail: reject
        });
      });

      this.status = 'connected';
      this.notifyListeners();
      return true;
    } catch {
      this.status = 'disconnected';
      this.notifyListeners();
      return false;
    } finally {
      this.checking = false;
    }
  }

  addListener(callback: (status: ConnectionStatus) => void): () => void {
    this.listeners.add(callback);
    return () => {
      this.listeners.delete(callback);
    };
  }

  private notifyListeners(): void {
    this.listeners.forEach(callback => callback(this.status));
  }
}

const connectionService = new ConnectionService();

export default connectionService;
export type { ConnectionStatus };
