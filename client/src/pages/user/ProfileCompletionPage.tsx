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
    Paper,
    CircularProgress,
    Stepper,
    Step,
    StepLabel,
    Card,
    CardContent,
    Alert,
    IconButton,
    Chip,
} from '@mui/material';
import {
    Person,
    Phone,
    Email,
    Security,
    CheckCircle,
    ArrowForward,
    ArrowBack,
    Add,
    Delete,
} from '@mui/icons-material';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
import { UserCreationRequest, EmergencyContact, Preferences } from '../../types/user';
import axios from 'axios';

const steps = [
    'Basic Information',
    'Contact Details',
    'Emergency Contacts',
    'Safety Preferences',
    'Review & Complete'
];

const ProfileCompletionPage: React.FC = () => {
    const navigate = useNavigate();
    const [activeStep, setActiveStep] = useState(0);
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

    const handleNext = () => {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
        setError(null);
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
        setError(null);
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

    const isStepValid = (step: number) => {
        switch (step) {
            case 0:
                return displayName && gender && ageGroup;
            case 1:
                return phoneNr && preferredContactMethod;
            case 2:
                return emergencyContacts.every(contact => 
                    contact.name && contact.email && contact.phone
                );
            case 3:
                return true; // Preferences are pre-filled
            case 4:
                return true; // Review step is always valid
            default:
                return false;
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
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
            navigate('/profile');
        } catch (err) {
            console.error('Error creating user profile:', err);
            if (err instanceof Error) {
                if (err.message.includes('Authentication failed')) {
                    navigate('/login');
                    return;
                }
                setError(err.message);
            } else if (axios.isAxiosError(err)) {
                if (err.response?.status === 403) {
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

    const renderStepContent = (step: number) => {
        switch (step) {
            case 0:
                return (
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" mb={2}>
                                <Person sx={{ mr: 1, color: 'primary.main' }} />
                                <Typography variant="h6">Basic Information</Typography>
                            </Box>
                            <Grid container spacing={3}>
                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Display Name"
                                        value={displayName}
                                        onChange={(e) => setDisplayName(e.target.value)}
                                        required
                                        helperText="This is how you'll appear in the app"
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
                            </Grid>
                        </CardContent>
                    </Card>
                );

            case 1:
                return (
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" mb={2}>
                                <Phone sx={{ mr: 1, color: 'primary.main' }} />
                                <Typography variant="h6">Contact Details</Typography>
                            </Box>
                            <Grid container spacing={3}>
                                <Grid item xs={12} md={6}>
                                    <TextField
                                        fullWidth
                                        label="Phone Number"
                                        value={phoneNr}
                                        onChange={(e) => setPhoneNr(e.target.value)}
                                        required
                                        helperText="For emergency notifications"
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
                            </Grid>
                        </CardContent>
                    </Card>
                );

            case 2:
                return (
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" mb={2}>
                                <Security sx={{ mr: 1, color: 'primary.main' }} />
                                <Typography variant="h6">Emergency Contacts</Typography>
                            </Box>
                            <Typography variant="body2" color="text.secondary" mb={2}>
                                Add trusted contacts who will be notified in case of emergency
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
                                                <Delete />
                                            </IconButton>
                                        </Grid>
                                    </Grid>
                                </Box>
                            ))}
                            <Button
                                variant="outlined"
                                startIcon={<Add />}
                                onClick={handleAddContact}
                                sx={{ mt: 2 }}
                            >
                                Add Emergency Contact
                            </Button>
                        </CardContent>
                    </Card>
                );

            case 3:
                return (
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" mb={2}>
                                <Security sx={{ mr: 1, color: 'primary.main' }} />
                                <Typography variant="h6">Safety Preferences</Typography>
                            </Box>
                            <Typography variant="body2" color="text.secondary" mb={3}>
                                These settings help us provide better safety features
                            </Typography>
                            <Grid container spacing={2}>
                                <Grid item xs={12}>
                                    <Chip 
                                        label="Share location with emergency contacts" 
                                        color="primary" 
                                        variant="outlined"
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <Chip 
                                        label="Auto-notify contacts on delays" 
                                        color="primary" 
                                        variant="outlined"
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <Chip 
                                        label="Enable SOS feature" 
                                        color="primary" 
                                        variant="outlined"
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <Chip 
                                        label="15-minute check-in intervals" 
                                        color="primary" 
                                        variant="outlined"
                                    />
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                );

            case 4:
                return (
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Review Your Profile
                            </Typography>
                            <Grid container spacing={2}>
                                <Grid item xs={12} md={6}>
                                    <Typography variant="subtitle2" color="text.secondary">Name</Typography>
                                    <Typography variant="body1">{displayName}</Typography>
                                </Grid>
                                <Grid item xs={12} md={6}>
                                    <Typography variant="subtitle2" color="text.secondary">Gender</Typography>
                                    <Typography variant="body1">{gender}</Typography>
                                </Grid>
                                <Grid item xs={12} md={6}>
                                    <Typography variant="subtitle2" color="text.secondary">Age Group</Typography>
                                    <Typography variant="body1">{ageGroup}</Typography>
                                </Grid>
                                <Grid item xs={12} md={6}>
                                    <Typography variant="subtitle2" color="text.secondary">Phone</Typography>
                                    <Typography variant="body1">{phoneNr}</Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography variant="subtitle2" color="text.secondary">Emergency Contacts</Typography>
                                    {emergencyContacts.map((contact, index) => (
                                        <Typography key={index} variant="body2">
                                            {contact.name} - {contact.email}
                                        </Typography>
                                    ))}
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                );

            default:
                return null;
        }
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 8 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom align="center">
                    Welcome to GetHome! 🏠
                </Typography>
                <Typography variant="body1" color="text.secondary" paragraph align="center">
                    Let's set up your profile to keep you safe on your journeys
                </Typography>

                <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
                    {steps.map((label) => (
                        <Step key={label}>
                            <StepLabel>{label}</StepLabel>
                        </Step>
                    ))}
                </Stepper>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                {renderStepContent(activeStep)}

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
                    <Button
                        disabled={activeStep === 0}
                        onClick={handleBack}
                        startIcon={<ArrowBack />}
                    >
                        Back
                    </Button>
                    <Box>
                        {activeStep === steps.length - 1 ? (
                            <Button
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={loading || !isStepValid(activeStep)}
                                startIcon={loading ? <CircularProgress size={20} /> : <CheckCircle />}
                            >
                                {loading ? 'Creating Profile...' : 'Complete Setup'}
                            </Button>
                        ) : (
                            <Button
                                variant="contained"
                                onClick={handleNext}
                                disabled={!isStepValid(activeStep)}
                                endIcon={<ArrowForward />}
                            >
                                Next
                            </Button>
                        )}
                    </Box>
                </Box>
            </Paper>
        </Container>
    );
};

export default ProfileCompletionPage; 