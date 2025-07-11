import React, { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    TextField,
    Button,
    Box,
    Grid,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    IconButton,
    Paper,
    CircularProgress,
    Alert,
    Divider,
    SelectChangeEvent,
    Switch,
} from '@mui/material';
import { Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
import { UserProfile, EmergencyContact, UserUpdateRequest, Gender, AgeGroup, PreferredContactMethod, Preferences } from '../../types/user';

const ProfileSettingsPage: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
    const [formData, setFormData] = useState({
        alias: '',
        gender: '',
        ageGroup: '',
        phoneNr: '',
        preferredContactMethod: '',
    });
    const [emergencyContacts, setEmergencyContacts] = useState<EmergencyContact[]>([]);
    const [safetyPreferences, setSafetyPreferences] = useState<Preferences>({
        checkInInterval: 15,
        shareLocation: false,
        notifyOnDelay: false,
        autoNotifyContacts: false,
        enableSOS: true
    });

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const currentUser = await authService.getCurrentUser();
                const profile = await userManagementService.getUserProfile(currentUser.id);
                if (profile) {
                    setUserProfile(profile as UserProfile);
                    setFormData({
                        alias: profile.alias || '',
                        gender: profile.gender || '',
                        ageGroup: profile.ageGroup || '',
                        phoneNr: profile.phoneNr || '',
                        preferredContactMethod: profile.preferredContactMethod || '',
                    });
                    setEmergencyContacts(profile.emergencyContacts || []);
                    setSafetyPreferences(profile.preferences || {
                        checkInInterval: 15,
                        shareLocation: false,
                        notifyOnDelay: false,
                        autoNotifyContacts: false,
                        enableSOS: true
                    });
                }
            } catch (err) {
                console.error('Error fetching profile:', err);
                setError('Failed to load profile');
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSelectChange = (e: SelectChangeEvent<string>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleAddContact = () => {
        const newContact: EmergencyContact = {
            name: '',
            email: '',
            phone: '',
            preferredMethod: 'EMAIL'
        };
        setEmergencyContacts([...emergencyContacts, newContact]);
        console.log('Added new emergency contact slot');
    };

    const handleRemoveContact = (index: number) => {
        if (emergencyContacts.length <= 1) {
            setError('At least one emergency contact is required');
            return;
        }
        const updatedContacts = emergencyContacts.filter((_, i) => i !== index);
        setEmergencyContacts(updatedContacts);
        console.log('Removed emergency contact at index:', index);
    };

    const handleContactChange = (index: number, field: keyof EmergencyContact, value: string) => {
        const newContacts = [...emergencyContacts];
        newContacts[index] = { ...newContacts[index], [field]: value };
        setEmergencyContacts(newContacts);
        console.log(`Updated emergency contact ${index} field ${field}:`, value);
        
        // Clear any previous errors when user starts typing
        if (error && (error.includes('emergency contact') || error.includes('required'))) {
            setError(null);
        }
    };

    const handleSafetyPreferenceChange = (field: keyof Preferences, value: boolean | number) => {
        setSafetyPreferences(prev => ({
            ...prev,
            [field]: value
        }));
        console.log(`Updated safety preference ${field}:`, value);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        try {
            // Enhanced validation for emergency contacts
            console.log('Validating emergency contacts:', emergencyContacts);
            
            const validEmergencyContacts = emergencyContacts.filter(contact => {
                const isValid = contact.name?.trim() && 
                               contact.email?.trim() && 
                               contact.phone?.trim() && 
                               contact.preferredMethod;
                console.log(`Contact ${contact.name} valid:`, isValid);
                return isValid;
            });

            console.log('Valid emergency contacts:', validEmergencyContacts);

            if (validEmergencyContacts.length === 0) {
                setError('At least one complete emergency contact is required. Please fill in all fields (name, email, phone, and preferred method).');
                setSaving(false);
                return;
            }

            // Validate email format
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            for (const contact of validEmergencyContacts) {
                if (!emailRegex.test(contact.email)) {
                    setError(`Invalid email format for contact "${contact.name}". Please check the email address.`);
                    setSaving(false);
                    return;
                }
            }

            // Validate phone format (basic check)
            const phoneRegex = /^[\d\s\-\+\(\)]+$/;
            for (const contact of validEmergencyContacts) {
                if (!phoneRegex.test(contact.phone)) {
                    setError(`Invalid phone format for contact "${contact.name}". Please use only numbers, spaces, and basic punctuation.`);
                    setSaving(false);
                    return;
                }
            }

            // Create the update request
            const updateData: UserUpdateRequest = {
                alias: formData.alias.trim(),
                gender: formData.gender as Gender,
                ageGroup: formData.ageGroup as AgeGroup,
                phoneNr: formData.phoneNr.trim(),
                preferredContactMethod: formData.preferredContactMethod as PreferredContactMethod,
                emergencyContacts: validEmergencyContacts.map(contact => ({
                    name: contact.name.trim(),
                    email: contact.email.trim().toLowerCase(),
                    phone: contact.phone.trim(),
                    preferredMethod: contact.preferredMethod
                })),
                preferences: safetyPreferences // Use the current safety preferences
            };

            console.log('Sending update data:', JSON.stringify(updateData, null, 2));
            
            // Update the profile
            await userManagementService.updateUserProfile(updateData);
            
            // Fetch the updated profile to verify changes
            const currentUser = await authService.getCurrentUser();
            const updatedProfile = await userManagementService.getUserProfile(currentUser.id);
            
            if (updatedProfile) {
                console.log('Profile updated successfully:', updatedProfile);
                if (!updatedProfile.emergencyContacts || updatedProfile.emergencyContacts.length === 0) {
                    setError('Warning: Emergency contacts may not have been saved properly. Please refresh the page and check your contacts.');
                } else {
                    setSuccess(`Profile updated successfully! ${updatedProfile.emergencyContacts.length} emergency contact(s) saved.`);
                    // Update the local state with the new data
                    setUserProfile(updatedProfile);
                    setEmergencyContacts(updatedProfile.emergencyContacts);
                }
            } else {
                setError('Profile update completed, but could not verify changes. Please refresh the page to see if your changes were saved.');
            }
        } catch (err: any) {
            console.error('Error updating profile:', err);
            const errorMessage = err?.response?.data?.message || err?.message || 'Failed to update profile';
            setError(`Update failed: ${errorMessage}. Please try again.`);
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom>
                    Profile Settings
                </Typography>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                {success && (
                    <Alert severity="success" sx={{ mb: 2 }}>
                        {success}
                    </Alert>
                )}

                <form onSubmit={handleSubmit}>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Display Name"
                                name="alias"
                                value={formData.alias}
                                onChange={handleInputChange}
                                required
                            />
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth required>
                                <InputLabel>Gender</InputLabel>
                                <Select
                                    name="gender"
                                    value={formData.gender}
                                    label="Gender"
                                    onChange={handleSelectChange}
                                >
                                    <MenuItem value="MALE">Male</MenuItem>
                                    <MenuItem value="FEMALE">Female</MenuItem>
                                    <MenuItem value="DIVERS">Other</MenuItem>
                                    <MenuItem value="NO_INFO">Prefer not to say</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth required>
                                <InputLabel>Age Group</InputLabel>
                                <Select
                                    name="ageGroup"
                                    value={formData.ageGroup}
                                    label="Age Group"
                                    onChange={handleSelectChange}
                                >
                                    <MenuItem value="TEENAGER">Under 18</MenuItem>
                                    <MenuItem value="YOUNG_ADULT">18-24</MenuItem>
                                    <MenuItem value="ADULT">25-64</MenuItem>
                                    <MenuItem value="ELDERLY">65+</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <TextField
                                fullWidth
                                label="Phone Number"
                                name="phoneNr"
                                value={formData.phoneNr}
                                onChange={handleInputChange}
                                required
                            />
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth required>
                                <InputLabel>Preferred Contact Method</InputLabel>
                                <Select
                                    name="preferredContactMethod"
                                    value={formData.preferredContactMethod}
                                    label="Preferred Contact Method"
                                    onChange={handleSelectChange}
                                >
                                    <MenuItem value="EMAIL">Email</MenuItem>
                                    <MenuItem value="SMS">SMS</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>

                        <Grid item xs={12}>
                            <Divider sx={{ my: 2 }} />
                            <Box sx={{ mb: 2 }}>
                                <Typography variant="h6" gutterBottom>
                                    Emergency Contacts
                                </Typography>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                    Add people who should be notified in case of an emergency. All fields are required for each contact.
                                </Typography>
                            </Box>
                            
                            {emergencyContacts.length === 0 && (
                                <Alert severity="info" sx={{ mb: 2 }}>
                                    No emergency contacts added yet. Click "Add Emergency Contact" to get started.
                                </Alert>
                            )}
                            
                            {emergencyContacts.map((contact, index) => (
                                <Box key={index} sx={{ 
                                    mb: 2, 
                                    p: 2, 
                                    border: '1px solid #e0e0e0', 
                                    borderRadius: 1,
                                    backgroundColor: '#fafafa'
                                }}>
                                    <Typography variant="subtitle2" sx={{ mb: 1, color: 'primary.main' }}>
                                        Emergency Contact {index + 1}
                                    </Typography>
                                    <Grid container spacing={2} alignItems="center">
                                        <Grid item xs={12} md={3}>
                                            <TextField
                                                fullWidth
                                                label="Full Name"
                                                value={contact.name}
                                                onChange={(e) => handleContactChange(index, 'name', e.target.value)}
                                                required
                                                placeholder="e.g., John Doe"
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={3}>
                                            <TextField
                                                fullWidth
                                                label="Email Address"
                                                type="email"
                                                value={contact.email}
                                                onChange={(e) => handleContactChange(index, 'email', e.target.value)}
                                                required
                                                placeholder="e.g., john@example.com"
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={3}>
                                            <TextField
                                                fullWidth
                                                label="Phone Number"
                                                value={contact.phone}
                                                onChange={(e) => handleContactChange(index, 'phone', e.target.value)}
                                                required
                                                placeholder="e.g., +1234567890"
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={2}>
                                            <FormControl fullWidth required>
                                                <InputLabel>Contact Method</InputLabel>
                                                <Select
                                                    value={contact.preferredMethod}
                                                    label="Contact Method"
                                                    onChange={(e) => handleContactChange(index, 'preferredMethod', e.target.value)}
                                                >
                                                    <MenuItem value="EMAIL">Email</MenuItem>
                                                    <MenuItem value="SMS">SMS</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Grid>
                                        <Grid item xs={12} md={1}>
                                            <IconButton
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    e.stopPropagation();
                                                    handleRemoveContact(index);
                                                }}
                                                disabled={emergencyContacts.length === 1}
                                                color="error"
                                                aria-label={`Remove contact ${index + 1}`}
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        </Grid>
                                    </Grid>
                                </Box>
                            ))}
                            <Button
                                variant="outlined"
                                startIcon={<AddIcon />}
                                onClick={handleAddContact}
                                sx={{ mt: 2 }}
                            >
                                Add Emergency Contact
                            </Button>
                        </Grid>

                        {/* Safety Preferences Section */}
                        <Grid item xs={12}>
                            <Divider sx={{ my: 2 }} />
                            <Box sx={{ mb: 2 }}>
                                <Typography variant="h6" gutterBottom>
                                    Safety Preferences
                                </Typography>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                    Configure your safety and emergency notification settings.
                                </Typography>
                            </Box>
                            
                            <Grid container spacing={3}>
                                <Grid item xs={12} md={6}>
                                    <FormControl fullWidth>
                                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                            <Box>
                                                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                                                    Share Location
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Allow emergency contacts to see your location
                                                </Typography>
                                            </Box>
                                            <Switch
                                                checked={safetyPreferences.shareLocation}
                                                onChange={(e) => handleSafetyPreferenceChange('shareLocation', e.target.checked)}
                                                color="primary"
                                            />
                                        </Box>
                                    </FormControl>
                                </Grid>

                                <Grid item xs={12} md={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>Check-in Interval</InputLabel>
                                        <Select
                                            value={safetyPreferences.checkInInterval}
                                            label="Check-in Interval"
                                            onChange={(e) => handleSafetyPreferenceChange('checkInInterval', Number(e.target.value))}
                                        >
                                            <MenuItem value={5}>5 minutes</MenuItem>
                                            <MenuItem value={10}>10 minutes</MenuItem>
                                            <MenuItem value={15}>15 minutes</MenuItem>
                                            <MenuItem value={30}>30 minutes</MenuItem>
                                            <MenuItem value={60}>1 hour</MenuItem>
                                        </Select>
                                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                            How often to check in during journeys
                                        </Typography>
                                    </FormControl>
                                </Grid>

                                <Grid item xs={12} md={6}>
                                    <FormControl fullWidth>
                                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                            <Box>
                                                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                                                    Auto Notify Contacts
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Automatically notify contacts on delays
                                                </Typography>
                                            </Box>
                                            <Switch
                                                checked={safetyPreferences.autoNotifyContacts}
                                                onChange={(e) => handleSafetyPreferenceChange('autoNotifyContacts', e.target.checked)}
                                                color="primary"
                                            />
                                        </Box>
                                    </FormControl>
                                </Grid>

                                <Grid item xs={12} md={6}>
                                    <FormControl fullWidth>
                                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                            <Box>
                                                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                                                    SOS Feature
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Enable emergency SOS button
                                                </Typography>
                                            </Box>
                                            <Switch
                                                checked={safetyPreferences.enableSOS}
                                                onChange={(e) => handleSafetyPreferenceChange('enableSOS', e.target.checked)}
                                                color="primary"
                                            />
                                        </Box>
                                    </FormControl>
                                </Grid>

                                <Grid item xs={12}>
                                    <FormControl fullWidth>
                                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                            <Box>
                                                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                                                    Delay Notifications
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Notify contacts when journey takes longer than expected
                                                </Typography>
                                            </Box>
                                            <Switch
                                                checked={safetyPreferences.notifyOnDelay}
                                                onChange={(e) => handleSafetyPreferenceChange('notifyOnDelay', e.target.checked)}
                                                color="primary"
                                            />
                                        </Box>
                                    </FormControl>
                                </Grid>
                            </Grid>
                        </Grid>

                        <Grid item xs={12}>
                            <Button
                                type="submit"
                                variant="contained"
                                fullWidth
                                disabled={saving}
                            >
                                {saving ? <CircularProgress size={24} color="inherit" /> : 'Save Settings'}
                            </Button>
                        </Grid>
                    </Grid>
                </form>
            </Paper>
        </Container>
    );
};

export default ProfileSettingsPage;