import axios, { AxiosInstance } from 'axios';

const BASE_URL = process.env.REACT_APP_CHAT_SERVICE_URL || 'http://localhost:8085/api/chat';

class ChatService {
  private chatAxios: AxiosInstance;

  constructor() {
    // Create a separate axios instance for chat that doesn't trigger global auth redirects
    this.chatAxios = axios.create({
      baseURL: BASE_URL,
      timeout: 10000,
    });
  }

  private getAuthHeader() {
    const token = localStorage.getItem('token');
    if (!token) throw new Error('Authentication required');
    return {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    };
  }

  async startSession(): Promise<string> {
    try {
      const res = await this.chatAxios.post('/sessions', {}, this.getAuthHeader());
      return res.data.session_id;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 401) {
          throw new Error('Authentication failed. Please refresh the page and try again.');
        }
        if (error.code === 'ERR_NETWORK') {
          throw new Error('Unable to connect to chat service. Please check your connection.');
        }
        throw new Error(error.response?.data?.detail || 'Failed to start chat session');
      }
      throw error;
    }
  }

  async sendMessage(sessionId: string, message: string): Promise<{ reply: string; timestamp: string }> {
    try {
      const res = await this.chatAxios.post(
        `/sessions/${sessionId}/message`,
        { message },
        this.getAuthHeader()
      );
      return res.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 401) {
          throw new Error('Session expired. Please refresh the page and try again.');
        }
        if (error.response?.status === 404) {
          throw new Error('Chat session not found. Please start a new session.');
        }
        if (error.code === 'ERR_NETWORK') {
          throw new Error('Unable to connect to chat service. Please check your connection.');
        }
        throw new Error(error.response?.data?.detail || 'Failed to send message');
      }
      throw error;
    }
  }

  async closeSession(sessionId: string): Promise<void> {
    try {
      await this.chatAxios.post(`/sessions/${sessionId}`, {}, this.getAuthHeader());
    } catch (error) {
      // Silently ignore errors when closing session
      console.warn('Failed to close chat session:', error);
    }
  }
}

export const chatService = new ChatService();