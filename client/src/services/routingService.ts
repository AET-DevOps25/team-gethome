import axios from 'axios';

const BASE_URL = process.env.REACT_APP_ROUTING_SERVICE_URL || 'http://localhost:8084/api';

class RoutingService {
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

  // Route Planning
  async planRoute(
    startLat: number,
    startLng: number,
    endLat: number,
    endLng: number,
    avoidDangerZones: boolean = true
  ): Promise<any> {
    const res = await axios.post(
      `${BASE_URL}/routes/plan`,
      {
        startLatitude: startLat,
        startLongitude: startLng,
        endLatitude: endLat,
        endLongitude: endLng,
        avoidDangerZones: avoidDangerZones,
        transportMode: 'WALKING'
      },
      this.getAuthHeader()
    );
    return res.data;
  }

  async getRoutes(): Promise<any[]> {
    const res = await axios.get(`${BASE_URL}/routes`, this.getAuthHeader());
    return res.data;
  }

  async getRoute(routeId: string): Promise<any> {
    const res = await axios.get(`${BASE_URL}/routes/${routeId}`, this.getAuthHeader());
    return res.data;
  }

  async completeRoute(routeId: string): Promise<void> {
    await axios.post(`${BASE_URL}/routes/${routeId}/complete`, {}, this.getAuthHeader());
  }

  // Danger Zones
  async reportDangerZone(
    latitude: number,
    longitude: number,
    description: string,
    category: string,
    dangerLevel: string
  ): Promise<any> {
    const res = await axios.post(
      `${BASE_URL}/danger-zones/report`,
      {
        latitude,
        longitude,
        description,
        category,
        dangerLevel
      },
      this.getAuthHeader()
    );
    return res.data;
  }

  async getNearbyDangerZones(latitude: number, longitude: number, radius: number = 500): Promise<any[]> {
    const res = await axios.get(
      `${BASE_URL}/danger-zones/nearby?latitude=${latitude}&longitude=${longitude}&radius=${radius}`,
      this.getAuthHeader()
    );
    return res.data;
  }

  async getDangerZones(): Promise<any[]> {
    const res = await axios.get(`${BASE_URL}/danger-zones`, this.getAuthHeader());
    return res.data;
  }

  // Emergency Triggers
  async triggerEmergency(
    latitude: number,
    longitude: number,
    location: string,
    reason: string,
    audioSnippet?: string
  ): Promise<any> {
    const res = await axios.post(
      `${BASE_URL}/emergency/trigger`,
      {
        latitude,
        longitude,
        location,
        reason,
        audioSnippet: audioSnippet || null
      },
      this.getAuthHeader()
    );
    return res.data;
  }

  async triggerAudioEmergency(
    latitude: number,
    longitude: number,
    location: string,
    audioSnippet: string
  ): Promise<any> {
    const res = await axios.post(
      `${BASE_URL}/emergency/trigger-audio`,
      null,
      {
        ...this.getAuthHeader(),
        params: {
          latitude,
          longitude,
          location,
          audioSnippet
        }
      }
    );
    return res.data;
  }

  // Health Check
  async healthCheck(): Promise<string> {
    const res = await axios.get(`${BASE_URL}/emergency/health`);
    return res.data;
  }
}

export const routingService = new RoutingService(); 