import React, { useState, useEffect } from 'react';
import { Container, Typography, Paper, Grid, Box, CircularProgress, Alert, Button, AppBar, Toolbar } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { userManagementService } from '../../services/userManagementService';
import { UserProfile } from '../../types/user';
import { authService } from '../../services/authService';

const DashboardPage: React.FC = () => {
    const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUserProfile = async () => {
            try {
                const currentUser = await authService.getCurrentUser();
                const profile = await userManagementService.getUserProfile(currentUser.id);
                setUserProfile(profile as UserProfile);
            } catch (err) {
                console.error('Error fetching user profile:', err);
                setError('Failed to load user profile');
            } finally {
                setLoading(false);
            }
        };
        fetchUserProfile();
    }, [navigate]);

    const handleLogout = () => {
        authService.logout();
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress />
            </Box>
        );
    }

    if (error || !userProfile) {
        return (
            <Container>
                <Typography color="error" variant="h6">
                    {error || 'User profile not found'}
                </Typography>
                <Button 
                    variant="contained" 
                    color="primary" 
                    onClick={() => navigate('/profile-completion')}
                    sx={{ mt: 2 }}
                >
                    Complete Profile
                </Button>
            </Container>
        );
    }

    // Helper to check if profile is incomplete
    const isProfileIncomplete =
        !userProfile.alias || !userProfile.gender || !userProfile.ageGroup;

    return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static" color="default" elevation={1}>
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        GetHome
                    </Typography>
                    <Button color="inherit" onClick={() => navigate('/home')}>
                        Home
                    </Button>
                    <Button color="inherit" onClick={() => navigate('/profile-settings')}>
                        Profile Settings
                    </Button>
                    <Button color="inherit" onClick={() => navigate('/safety-settings')}>
                        Safety Settings
                    </Button>
                    <Button color="inherit" onClick={handleLogout}>
                        Logout
                    </Button>
                </Toolbar>
            </AppBar>

            <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
                {isProfileIncomplete && (
                    <Alert severity="warning" sx={{ mb: 2 }}>
                        Your profile is incomplete.{' '}
                        <Button color="inherit" size="small" onClick={() => navigate('/profile-completion')}>
                            Complete Now
                        </Button>
                    </Alert>
                )}
                <Grid container spacing={3}>
                    <Grid item xs={12}>
                        <Paper sx={{ p: 3 }}>
                            <Typography variant="h4" gutterBottom>
                                Welcome, {userProfile.alias || userProfile.email || 'User'}!
                            </Typography>
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="h6" gutterBottom>
                                    Profile Information
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid item xs={12} md={6}>
                                        <Typography>
                                            <strong>Email:</strong> {userProfile.email || ''}
                                        </Typography>
                                        <Typography>
                                            <strong>Gender:</strong> {userProfile.gender || 'Not specified'}
                                        </Typography>
                                        <Typography>
                                            <strong>Age Group:</strong> {userProfile.ageGroup || 'Not specified'}
                                        </Typography>
                                        <Typography>
                                            <strong>Phone:</strong> {userProfile.phoneNr || ''}
                                        </Typography>
                                        <Typography>
                                            <strong>Preferred Contact:</strong> {userProfile.preferredContactMethod || ''}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Box>

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h6" gutterBottom>
                                    Emergency Contacts
                                </Typography>
                                <Grid container spacing={2}>
                                    {userProfile.emergencyContacts?.map((contact, index) => (
                                        <Grid item xs={12} md={4} key={index}>
                                            <Box sx={{ p: 2, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                                                <Typography variant="subtitle1">
                                                    {contact.name}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    {contact.email}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    {contact.phone}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Preferred Contact: {contact.preferredMethod}
                                                </Typography>
                                            </Box>
                                        </Grid>
                                    ))}
                                    {(!userProfile.emergencyContacts || userProfile.emergencyContacts.length === 0) && (
                                        <Grid item xs={12}>
                                            <Typography color="text.secondary">
                                                No emergency contacts added yet.
                                            </Typography>
                                        </Grid>
                                    )}
                                </Grid>
                            </Box>

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h6" gutterBottom>
                                    Safety Preferences
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid item xs={12} md={6}>
                                        <Typography>
                                            <strong>Check-in Interval:</strong> {userProfile.preferences?.checkInInterval ?? 'Not set'} minutes
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={12} md={6}>
                                        <Typography variant="subtitle1" gutterBottom>
                                            <strong>Safety Features</strong>
                                        </Typography>
                                        <Typography>
                                            <strong>Share Location:</strong> {userProfile.preferences?.shareLocation ? 'Enabled' : 'Disabled'}
                                        </Typography>
                                        <Typography>
                                            <strong>Notify on Delay:</strong> {userProfile.preferences?.notifyOnDelay ? 'Enabled' : 'Disabled'}
                                        </Typography>
                                        <Typography>
                                            <strong>Auto Notify Contacts:</strong> {userProfile.preferences?.autoNotifyContacts ? 'Enabled' : 'Disabled'}
                                        </Typography>
                                        <Typography>
                                            <strong>SOS Feature:</strong> {userProfile.preferences?.enableSOS ? 'Enabled' : 'Disabled'}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Box>

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Profile created: {userProfile.createdAt ? new Date(userProfile.createdAt).toLocaleDateString() : 'N/A'}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Last updated: {userProfile.updatedAt ? new Date(userProfile.updatedAt).toLocaleDateString() : 'N/A'}
                                </Typography>
                            </Box>
                        </Paper>
                    </Grid>
                </Grid>
            </Container>
        </Box>
    );
};

export default DashboardPage; 