export type Gender = 'MALE' | 'FEMALE' | 'DIVERS' | 'NO_INFO';
export type AgeGroup = 'TEENAGER' | 'YOUNG_ADULT' | 'ADULT' | 'ELDERLY';
export type AiTone = 'FRIENDLY' | 'NEUTRAL' | 'PROFESSIONAL';
export type Talkativeness = 'TALKATIVE' | 'BALANCED' | 'LISTENING';
export type SocialDistance = 'INTERESTED' | 'NEUTRAL' | 'DISTANT';
export type PreferredContactMethod = 'EMAIL' | 'SMS';

export interface Preferences {
    checkInInterval: number;
    shareLocation: boolean;
    notifyOnDelay: boolean;
    autoNotifyContacts: boolean;
    enableSOS: boolean;
}

export interface EmergencyContact {
    name: string;
    email: string;
    phone: string;
    preferredMethod: 'EMAIL' | 'SMS';
}

export interface UserCreationRequest {
    id: string;
    email: string;
    alias: string;
    gender: string;
    ageGroup: string;
    phoneNr: string;
    preferredContactMethod: string;
    emergencyContacts: EmergencyContact[];
    preferences: Preferences;
}

export interface UserUpdateRequest {
    alias: string;
    gender: Gender;
    ageGroup: AgeGroup;
    phoneNr: string;
    preferredContactMethod: PreferredContactMethod;
    emergencyContacts: EmergencyContact[];
    preferences: Preferences;
}

export interface UserProfile {
    id?: string;
    email?: string;
    alias?: string;
    gender?: string;
    ageGroup?: string;
    phoneNr?: string;
    preferredContactMethod?: string;
    emergencyContacts?: EmergencyContact[];
    preferences?: Preferences;
    createdAt?: string;
    updatedAt?: string;
}

export interface UserSummary {
    id: string;
    alias: string;
    gender: Gender;
    ageGroup: AgeGroup;
    createdAt: string;
}

export interface EmergencyContactData {
    name: string;
    relationship: string;
    phoneNumber: string;
    email: string;
}

export interface UserSearchResponse {
    userId: string;
    email: string;
    alias: string;
    profilePictureUrl?: string;
} 