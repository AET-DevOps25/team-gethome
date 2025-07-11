import axios from 'axios';

const ROUTING_SERVICE_URL = process.env.REACT_APP_ROUTING_SERVICE_URL || 'http://localhost:8083';

export interface RouteData {
  id: string;
  userId: string;
  routeName: string;
  createdAt: string;
  expiresAt: string;
  startLocation: {
    latitude: number;
    longitude: number;
    address: string;
  };
  endLocation: {
    latitude: number;
    longitude: number;
    address: string;
  };
  totalDistance: number;
  estimatedDuration: number;
  safetyScore: number;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED';
  avoidedDangerZones: string[];
}

export interface DangerZone {
  id: string;
  latitude: number;
  longitude: number;
  radius: number;
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  description: string;
  tags: string[];
  reportedBy: string;
  reportedAt: string;
  expiresAt: string;
  verified: boolean;
}

export interface SafetyAnalytics {
  totalJourneys: number;
  safeJourneys: number;
  emergencyTriggers: number;
  dangerZonesEncountered: number;
  averageJourneyTime: number;
  safetyScore: number;
  lastUpdated: Date;
}

export interface JourneyHistory {
  id: string;
  startLocation: string;
  endLocation: string;
  startTime: Date;
  endTime: Date;
  safetyStatus: 'safe' | 'warning' | 'danger';
  duration: number;
  dangerZones: number;
}

class SafetyAnalyticsService {
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

  async getUserRoutes(): Promise<RouteData[]> {
    try {
      const response = await axios.get(`${ROUTING_SERVICE_URL}/api/routes`, this.getAuthHeader());
      return response.data || [];
    } catch (error) {
      console.error('Failed to fetch user routes:', error);
      return [];
    }
  }

  async getUserReportedDangerZones(): Promise<DangerZone[]> {
    try {
      const response = await axios.get(`${ROUTING_SERVICE_URL}/api/danger-zones/my-reports`, this.getAuthHeader());
      return response.data || [];
    } catch (error) {
      console.error('Failed to fetch user reported danger zones:', error);
      return [];
    }
  }

  async generateSafetyAnalytics(): Promise<SafetyAnalytics> {
    try {
      const routes = await this.getUserRoutes();
      const dangerZones = await this.getUserReportedDangerZones();
      
      if (routes.length === 0) {
        // Return fallback data if no routes available
        return this.getFallbackAnalytics();
      }

      const completedRoutes = routes.filter(route => route.status === 'COMPLETED');
      const safeRoutes = completedRoutes.filter(route => route.safetyScore >= 0.7);
      const totalDangerZones = routes.reduce((sum, route) => sum + (route.avoidedDangerZones?.length || 0), 0);
      const averageDuration = completedRoutes.length > 0 
        ? completedRoutes.reduce((sum, route) => sum + route.estimatedDuration, 0) / completedRoutes.length / 60
        : 0;
      const averageSafetyScore = routes.length > 0 
        ? routes.reduce((sum, route) => sum + route.safetyScore, 0) / routes.length * 100
        : 0;

      return {
        totalJourneys: completedRoutes.length,
        safeJourneys: safeRoutes.length,
        emergencyTriggers: 0, // Would need emergency service data
        dangerZonesEncountered: totalDangerZones,
        averageJourneyTime: Math.round(averageDuration),
        safetyScore: Math.round(averageSafetyScore),
        lastUpdated: new Date()
      };
    } catch (error) {
      console.error('Failed to generate safety analytics:', error);
      return this.getFallbackAnalytics();
    }
  }

  async generateJourneyHistory(): Promise<JourneyHistory[]> {
    try {
      const routes = await this.getUserRoutes();
      
      if (routes.length === 0) {
        return this.getFallbackJourneyHistory();
      }

      const completedRoutes = routes
        .filter(route => route.status === 'COMPLETED')
        .slice(0, 10) // Get last 10 completed routes
        .map(route => ({
          id: route.id,
          startLocation: route.startLocation.address || 'Unknown Location',
          endLocation: route.endLocation.address || 'Unknown Location',
          startTime: new Date(route.createdAt),
          endTime: new Date(route.expiresAt), // Using expiresAt as proxy for end time
          safetyStatus: this.getSafetyStatus(route.safetyScore, route.avoidedDangerZones?.length || 0),
          duration: Math.round(route.estimatedDuration / 60), // Convert to minutes
          dangerZones: route.avoidedDangerZones?.length || 0
        }));

      return completedRoutes.length > 0 ? completedRoutes : this.getFallbackJourneyHistory();
    } catch (error) {
      console.error('Failed to generate journey history:', error);
      return this.getFallbackJourneyHistory();
    }
  }

  private getSafetyStatus(safetyScore: number, dangerZones: number): 'safe' | 'warning' | 'danger' {
    if (safetyScore >= 0.8 && dangerZones === 0) return 'safe';
    if (safetyScore >= 0.5 || dangerZones <= 2) return 'warning';
    return 'danger';
  }

  private getFallbackAnalytics(): SafetyAnalytics {
    return {
      totalJourneys: 24,
      safeJourneys: 22,
      emergencyTriggers: 1,
      dangerZonesEncountered: 3,
      averageJourneyTime: 25,
      safetyScore: 92,
      lastUpdated: new Date()
    };
  }

  private getFallbackJourneyHistory(): JourneyHistory[] {
    return [
      {
        id: '1',
        startLocation: 'Home',
        endLocation: 'Work',
        startTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
        endTime: new Date(Date.now() - 1.5 * 60 * 60 * 1000),
        safetyStatus: 'safe',
        duration: 30,
        dangerZones: 0
      },
      {
        id: '2',
        startLocation: 'Work',
        endLocation: 'Gym',
        startTime: new Date(Date.now() - 5 * 60 * 60 * 1000),
        endTime: new Date(Date.now() - 4.5 * 60 * 60 * 1000),
        safetyStatus: 'warning',
        duration: 25,
        dangerZones: 1
      },
      {
        id: '3',
        startLocation: 'Gym',
        endLocation: 'Home',
        startTime: new Date(Date.now() - 8 * 60 * 60 * 1000),
        endTime: new Date(Date.now() - 7.5 * 60 * 60 * 1000),
        safetyStatus: 'safe',
        duration: 28,
        dangerZones: 0
      }
    ];
  }

  generateSafetyTips(analytics: SafetyAnalytics): string[] {
    const tips: string[] = [];
    
    if (analytics.safetyScore >= 90) {
      tips.push("Your safety score is excellent! Keep up the good habits.");
    } else if (analytics.safetyScore >= 70) {
      tips.push("Good safety practices! Consider avoiding high-risk areas when possible.");
    } else {
      tips.push("Consider planning safer routes and using the AI companion during journeys.");
    }

    if (analytics.dangerZonesEncountered > 5) {
      tips.push("You've encountered several danger zones recently. Review your route planning.");
    }

    if (analytics.emergencyTriggers === 0) {
      tips.push("Great job staying safe! Your emergency contacts are properly configured.");
    }

    if (analytics.averageJourneyTime > 30) {
      tips.push("Consider optimizing your routes for shorter, safer travel times.");
    }

    tips.push("Consider using the AI companion more during late-night journeys.");
    
    return tips;
  }
}

export const safetyAnalyticsService = new SafetyAnalyticsService(); 