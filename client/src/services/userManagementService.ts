import axios from 'axios';
import { 
    UserProfile, 
    UserCreationRequest, 
    UserUpdateRequest, 
    UserSummary, 
    EmergencyContactData
} from '../types/user';
import { authService } from './authService';

class UserManagementService {
    private baseUrl: string;

    constructor() {
        this.baseUrl = process.env.REACT_APP_USER_MANAGEMENT_SERVICE_URL || 'http://localhost:8082/api';
    }

    private getAuthHeader() {
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('No auth token found in localStorage');
            throw new Error('Authentication required');
        }
        console.log('Using token:', token.substring(0, 10) + '...'); // Log first 10 chars of token
        return {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        };
    }

    async getUserProfile(userId: string): Promise<UserCreationRequest | null> {
        try {
            const response = await axios.get(`${this.baseUrl}/users/${userId}/profile`, this.getAuthHeader());
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.error('Error response:', error.response?.data);
                if (error.response?.status === 404) {
                    return null;
                }
                if (error.response?.status === 403) {
                    throw new Error('Authentication failed. Please log in again.');
                }
            }
            console.error('Error fetching user profile:', error);
            throw error;
        }
    }

    async createUserProfile(data: UserCreationRequest): Promise<void> {
        try {
            console.log('Creating user profile with data:', data);
            const response = await axios.post(`${this.baseUrl}/users`, data, this.getAuthHeader());
            console.log('Profile creation response:', response.data);
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.error('Error response:', error.response?.data);
                if (error.response?.status === 403) {
                    throw new Error('Authentication failed. Please log in again.');
                }
            }
            console.error('Error creating user profile:', error);
            throw error;
        }
    }

    async updateUserProfile(data: UserUpdateRequest): Promise<void> {
        try {
            const currentUser = await authService.getCurrentUser();
            console.log('Updating profile for user:', currentUser.id);
            
            // Ensure emergency contacts are properly formatted
            const formattedData = {
                ...data,
                emergencyContacts: data.emergencyContacts.map(contact => ({
                    name: contact.name,
                    email: contact.email,
                    phone: contact.phone,
                    preferredMethod: contact.preferredMethod
                })),
                preferences: {
                    checkInInterval: data.preferences.checkInInterval,
                    shareLocation: data.preferences.shareLocation,
                    notifyOnDelay: data.preferences.notifyOnDelay,
                    autoNotifyContacts: data.preferences.autoNotifyContacts,
                    enableSOS: data.preferences.enableSOS
                }
            };

            console.log('Formatted update data:', JSON.stringify(formattedData, null, 2));
            
            const response = await axios.put(
                `${this.baseUrl}/users/${currentUser.id}`,
                formattedData,
                {
                    ...this.getAuthHeader(),
                    headers: {
                        ...this.getAuthHeader().headers,
                        'Content-Type': 'application/json'
                    }
                }
            );
            
            console.log('Update response:', response.data);

            // Verify the update was successful
            if (!response.data) {
                throw new Error('No response data received from server');
            }

            // Fetch the updated profile to verify changes
            const updatedProfile = await this.getUserProfile(currentUser.id);
            if (updatedProfile) {
                console.log('Updated profile:', updatedProfile);
                if (!updatedProfile.emergencyContacts || updatedProfile.emergencyContacts.length === 0) {
                    console.warn('Emergency contacts not saved in the response');
                }
            } else {
                console.warn('Could not fetch updated profile');
            }
        } catch (error) {
            console.error('Error updating user profile:', error);
            if (axios.isAxiosError(error)) {
                console.error('Response data:', error.response?.data);
                console.error('Response status:', error.response?.status);
                console.error('Request data:', error.config?.data);
            }
            throw error;
        }
    }

    async deleteUserProfile(userId: string): Promise<void> {
        await axios.delete(`${this.baseUrl}/users/${userId}`, this.getAuthHeader());
    }

    async addEmergencyContact(userId: string, contactUserCode: string): Promise<{ id: string; requesterId: string; contactUserId: string }> {
        const response = await axios.post(
            `${this.baseUrl}/users/${userId}/emergency-contacts?contactUserCode=${contactUserCode}`,
            {},
            this.getAuthHeader()
        );
        return response.data;
    }

    async getPendingEmergencyContacts(userId: string): Promise<EmergencyContactData[]> {
        const response = await axios.get(`${this.baseUrl}/users/${userId}/emergency-contacts/pending`, this.getAuthHeader());
        return response.data;
    }

    async respondToEmergencyContact(userId: string, requestId: string, accept: boolean): Promise<void> {
        await axios.put(
            `${this.baseUrl}/users/${userId}/emergency-contacts/${requestId}?accept=${accept}`,
            {},
            this.getAuthHeader()
        );
    }

    async getEmergencyContacts(userId: string): Promise<EmergencyContactData[]> {
        const response = await axios.get(`${this.baseUrl}/users/${userId}/emergency-contacts`, this.getAuthHeader());
        return response.data;
    }

    async getEmergencyContactsOf(userId: string): Promise<EmergencyContactData[]> {
        const response = await axios.get(`${this.baseUrl}/users/${userId}/emergency-contacts-of`, this.getAuthHeader());
        return response.data;
    }

    async removeEmergencyContact(userId: string, contactId: string): Promise<void> {
        await axios.delete(`${this.baseUrl}/users/${userId}/emergency-contacts/${contactId}`, this.getAuthHeader());
    }

    async removeEmergencyContactOf(userId: string, requesterId: string): Promise<void> {
        await axios.delete(`${this.baseUrl}/users/${userId}/emergency-contacts-of/${requesterId}`, this.getAuthHeader());
    }
}

export const userManagementService = new UserManagementService(); 