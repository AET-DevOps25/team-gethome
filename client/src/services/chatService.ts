import axios from 'axios';

const BASE_URL = process.env.REACT_APP_CHAT_SERVICE_URL || 'http://localhost:8085/api/chat';

class ChatService {
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
    const res = await axios.post(`${BASE_URL}/sessions`, {}, this.getAuthHeader());
    return res.data.session_id;
  }

  async sendMessage(sessionId: string, message: string): Promise<{ reply: string; timestamp: string }> {
    const res = await axios.post(
      `${BASE_URL}/sessions/${sessionId}/message`,
      { message },
      this.getAuthHeader()
    );
    return res.data;
  }

  async closeSession(sessionId: string): Promise<void> {
    await axios.post(`${BASE_URL}/sessions/${sessionId}`, {}, this.getAuthHeader());
  }
}

export const chatService = new ChatService();