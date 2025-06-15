import axios from 'axios';
import { userManagementService } from './userManagementService';

const API_URL = 'http://localhost:8081/api/v1';

// Configure axios defaults
axios.defaults.withCredentials = true;
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Add axios interceptor to handle token
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add response interceptor to handle 401 errors
axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

class AuthService {
    private getAuthHeader() {
        const token = localStorage.getItem('token');
        if (!token) {
            return {
                headers: {
                    'Content-Type': 'application/json'
                }
            };
        }
        return {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        };
    }

    isAuthenticated(): boolean {
        return !!localStorage.getItem('token');
    }

    async login(email: string, password: string): Promise<{ needsProfile: boolean }> {
        console.log('AuthService: Starting login process');
        try {
            console.log('AuthService: Sending login request to', `${API_URL}/auth/login`);
            const response = await axios.post(`${API_URL}/auth/login`, { email, password });
            console.log('AuthService: Login response received', response.data);
            
            const { token } = response.data;
            if (!token) {
                throw new Error('No token received from server');
            }
            
            localStorage.setItem('token', token);
            console.log('AuthService: Token stored in localStorage');
            
            // Get user info to check if profile exists
            try {
                const userInfo = await this.getCurrentUser();
                console.log('AuthService: User info received', userInfo);
                
                // Check if user profile exists
                try {
                    const profile = await userManagementService.getUserProfile(userInfo.id);
                    console.log('AuthService: Profile check result', profile);
                    
                    // Check if profile is complete
                    const isProfileComplete = profile && 
                        profile.alias && 
                        profile.gender && 
                        profile.ageGroup;
                    
                    return { needsProfile: !isProfileComplete };
                } catch (error) {
                    console.error('AuthService: Error checking user profile', error);
                    if (axios.isAxiosError(error)) {
                        if (error.response?.status === 404) {
                            return { needsProfile: true };
                        }
                        if (error.response?.status === 403) {
                            // If we get a 403, the token might be invalid
                            localStorage.removeItem('token');
                            throw new Error('Authentication failed. Please log in again.');
                        }
                    }
                    // For any other error, assume profile exists
                    return { needsProfile: false };
                }
            } catch (error) {
                console.error('AuthService: Error getting current user', error);
                throw error;
            }
        } catch (error) {
            console.error('AuthService: Login error', error);
            if (axios.isAxiosError(error)) {
                if (error.code === 'ERR_NETWORK') {
                    throw new Error('Unable to connect to the authentication service. Please check your internet connection.');
                }
                if (error.response?.status === 401) {
                    throw new Error('Invalid email or password');
                }
                if (error.response?.status === 403) {
                    throw new Error('Access denied. Please try logging in again.');
                }
                throw new Error(error.response?.data?.message || 'Login failed. Please try again.');
            }
            throw error;
        }
    }

    async register(email: string, password: string): Promise<void> {
        console.log('AuthService: Starting registration process');
        try {
            console.log('AuthService: Sending registration request to', `${API_URL}/auth/register`);
            const response = await axios.post(`${API_URL}/auth/register`, { email, password });
            console.log('AuthService: Registration response received', response.data);
            
            const { token } = response.data;
            if (!token) {
                throw new Error('No token received from server');
            }
            
            localStorage.setItem('token', token);
            console.log('AuthService: Token stored in localStorage');
        } catch (error) {
            console.error('AuthService: Registration error', error);
            if (axios.isAxiosError(error)) {
                if (error.code === 'ERR_NETWORK') {
                    throw new Error('Unable to connect to the authentication service. Please check your internet connection.');
                }
                if (error.response?.status === 409) {
                    throw new Error('An account with this email already exists');
                }
                throw new Error(error.response?.data?.message || 'Registration failed. Please try again.');
            }
            throw error;
        }
    }

    async logout(): Promise<void> {
        console.log('AuthService: Logging out');
        localStorage.removeItem('token');
        window.location.href = '/login';
    }

    async getCurrentUser(): Promise<any> {
        console.log('AuthService: Getting current user');
        try {
            const response = await axios.get(`${API_URL}/auth/me`, this.getAuthHeader());
            console.log('AuthService: Current user response', response.data);
            return response.data;
        } catch (error) {
            console.error('AuthService: Error getting current user', error);
            if (axios.isAxiosError(error) && error.response?.status === 401) {
                this.logout();
            }
            throw error;
        }
    }
}

export const authService = new AuthService(); 