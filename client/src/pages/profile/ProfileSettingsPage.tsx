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
        setEmergencyContacts([
            ...emergencyContacts,
            { name: '', email: '', phone: '', preferredMethod: 'EMAIL' }
        ]);
    };

    const handleRemoveContact = (index: number) => {
        setEmergencyContacts(emergencyContacts.filter((_, i) => i !== index));
    };

    const handleContactChange = (index: number, field: keyof EmergencyContact, value: string) => {
        const newContacts = [...emergencyContacts];
        newContacts[index] = { ...newContacts[index], [field]: value };
        setEmergencyContacts(newContacts);
        console.log('Updated emergency contacts:', JSON.stringify(newContacts, null, 2)); // Debug log
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        try {
            // Validate emergency contacts
            const validEmergencyContacts = emergencyContacts.filter(contact => 
                contact.name && contact.email && contact.phone && contact.preferredMethod
            );

            if (validEmergencyContacts.length === 0) {
                setError('At least one emergency contact is required');
                setSaving(false);
                return;
            }

            // Create a new preferences object
            const preferences: Preferences = {
                checkInInterval: 15,
                shareLocation: true,
                notifyOnDelay: true,
                autoNotifyContacts: true,
                enableSOS: true
            };

            // Create the update request
            const updateData: UserUpdateRequest = {
                alias: formData.alias,
                gender: formData.gender as Gender,
                ageGroup: formData.ageGroup as AgeGroup,
                phoneNr: formData.phoneNr,
                preferredContactMethod: formData.preferredContactMethod as PreferredContactMethod,
                emergencyContacts: validEmergencyContacts.map(contact => ({
                    name: contact.name,
                    email: contact.email,
                    phone: contact.phone,
                    preferredMethod: contact.preferredMethod
                })),
                preferences: preferences
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
                    setError('Emergency contacts were not saved. Please try again.');
                } else {
                    setSuccess('Profile updated successfully');
                    // Update the local state with the new data
                    setUserProfile(updatedProfile);
                    setEmergencyContacts(updatedProfile.emergencyContacts);
                }
            } else {
                setError('Failed to verify profile update. Please check if your changes were saved.');
            }
        } catch (err) {
            console.error('Error updating profile:', err);
            setError('Failed to update profile. Please try again.');
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
                            <Typography variant="h6" gutterBottom>
                                Emergency Contacts
                            </Typography>
                            {emergencyContacts.map((contact, index) => (
                                <Box key={index} sx={{ mb: 2, p: 2, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                                    <Grid container spacing={2} alignItems="center">
                                        <Grid item xs={12} md={3}>
                                            <TextField
                                                fullWidth
                                                label="Name"
                                                value={contact.name}
                                                onChange={(e) => handleContactChange(index, 'name', e.target.value)}
                                                required
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={3}>
                                            <TextField
                                                fullWidth
                                                label="Email"
                                                type="email"
                                                value={contact.email}
                                                onChange={(e) => handleContactChange(index, 'email', e.target.value)}
                                                required
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={3}>
                                            <TextField
                                                fullWidth
                                                label="Phone"
                                                value={contact.phone}
                                                onChange={(e) => handleContactChange(index, 'phone', e.target.value)}
                                                required
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={2}>
                                            <FormControl fullWidth required>
                                                <InputLabel>Preferred Method</InputLabel>
                                                <Select
                                                    value={contact.preferredMethod}
                                                    label="Preferred Method"
                                                    onChange={(e) => handleContactChange(index, 'preferredMethod', e.target.value)}
                                                >
                                                    <MenuItem value="EMAIL">Email</MenuItem>
                                                    <MenuItem value="SMS">SMS</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Grid>
                                        <Grid item xs={12} md={1}>
                                            <IconButton
                                                onClick={() => handleRemoveContact(index)}
                                                disabled={emergencyContacts.length === 1}
                                                color="error"
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

                        <Grid item xs={12}>
                            <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    color="primary"
                                    disabled={saving}
                                >
                                    {saving ? <CircularProgress size={24} /> : 'Save Changes'}
                                </Button>
                                <Button
                                    variant="outlined"
                                    onClick={() => navigate('/home')}
                                >
                                    Cancel
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                </form>
            </Paper>
        </Container>
    );
};

export default ProfileSettingsPage; 