import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
} from '@mui/material';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
import { UserCreationRequest, EmergencyContact, Preferences } from '../../types/user';
import axios from 'axios';

const ProfileCompletionPage: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [displayName, setDisplayName] = useState('');
    const [gender, setGender] = useState('');
    const [ageGroup, setAgeGroup] = useState('');
    const [phoneNr, setPhoneNr] = useState('');
    const [preferredContactMethod, setPreferredContactMethod] = useState('');
    const [emergencyContacts, setEmergencyContacts] = useState<EmergencyContact[]>([
        { name: '', email: '', phone: '', preferredMethod: 'EMAIL' }
    ]);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const currentUser = await authService.getCurrentUser();
                if (!currentUser) {
                    navigate('/login');
                }
            } catch (err) {
                console.error('Auth check failed:', err);
                navigate('/login');
            }
        };
        checkAuth();
    }, [navigate]);

    // Default preferences
    const defaultPreferences: Preferences = {
        shareLocation: true,
        notifyOnDelay: true,
        autoNotifyContacts: true,
        checkInInterval: 15,
        enableSOS: true
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
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            // First check if we have a valid token
            const currentUser = await authService.getCurrentUser();
            if (!currentUser) {
                throw new Error('No authenticated user found');
            }

            const formData: UserCreationRequest = {
                id: currentUser.id,
                email: currentUser.email,
                alias: displayName,
                gender,
                ageGroup,
                phoneNr,
                preferredContactMethod,
                emergencyContacts,
                preferences: defaultPreferences
            };

            await userManagementService.createUserProfile(formData);
            navigate('/dashboard');
        } catch (err) {
            console.error('Error creating user profile:', err);
            if (err instanceof Error) {
                if (err.message.includes('Authentication failed')) {
                    // If auth failed, redirect to login
                    navigate('/login');
                    return;
                }
                setError(err.message);
            } else if (axios.isAxiosError(err)) {
                if (err.response?.status === 403) {
                    // If forbidden, redirect to login
                    navigate('/login');
                    return;
                }
                setError(err.response?.data?.message || 'Failed to create user profile. Please try again.');
            } else {
                setError('An unexpected error occurred. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom>
                    Complete Your Profile
                </Typography>
                <Typography variant="body1" color="text.secondary" paragraph>
                    Please provide the following information to complete your profile.
                </Typography>

                <form onSubmit={handleSubmit}>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Display Name"
                                value={displayName}
                                onChange={(e) => setDisplayName(e.target.value)}
                                required
                            />
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth required>
                                <InputLabel>Gender</InputLabel>
                                <Select
                                    value={gender}
                                    label="Gender"
                                    onChange={(e) => setGender(e.target.value)}
                                >
                                    <MenuItem value="MALE">Male</MenuItem>
                                    <MenuItem value="FEMALE">Female</MenuItem>
                                    <MenuItem value="OTHER">Other</MenuItem>
                                    <MenuItem value="PREFER_NOT_TO_SAY">Prefer not to say</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth required>
                                <InputLabel>Age Group</InputLabel>
                                <Select
                                    value={ageGroup}
                                    label="Age Group"
                                    onChange={(e) => setAgeGroup(e.target.value)}
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
                                value={phoneNr}
                                onChange={(e) => setPhoneNr(e.target.value)}
                                required
                            />
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth required>
                                <InputLabel>Preferred Contact Method</InputLabel>
                                <Select
                                    value={preferredContactMethod}
                                    label="Preferred Contact Method"
                                    onChange={(e) => setPreferredContactMethod(e.target.value)}
                                >
                                    <MenuItem value="EMAIL">Email</MenuItem>
                                    <MenuItem value="SMS">SMS</MenuItem>
                                    <MenuItem value="WHATSAPP">WhatsApp</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>

                        <Grid item xs={12}>
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
                                            <Button
                                                onClick={() => handleRemoveContact(index)}
                                                disabled={emergencyContacts.length === 1}
                                                color="error"
                                                size="small"
                                            >
                                                Remove
                                            </Button>
                                        </Grid>
                                    </Grid>
                                </Box>
                            ))}
                            <Button
                                variant="outlined"
                                onClick={handleAddContact}
                                sx={{ mt: 2 }}
                            >
                                Add Emergency Contact
                            </Button>
                        </Grid>

                        {error && (
                            <Grid item xs={12}>
                                <Typography color="error">{error}</Typography>
                            </Grid>
                        )}

                        <Grid item xs={12}>
                            <Button
                                type="submit"
                                variant="contained"
                                color="primary"
                                fullWidth
                                disabled={loading}
                            >
                                {loading ? <CircularProgress size={24} /> : 'Complete Profile'}
                            </Button>
                        </Grid>
                    </Grid>
                </form>
            </Paper>
        </Container>
    );
};

export default ProfileCompletionPage; 