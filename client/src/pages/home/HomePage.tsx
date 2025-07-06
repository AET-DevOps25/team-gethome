import React, { useEffect, useState } from 'react';
import BottomTabBar from '../../components/BottomTabBar';
import UserSearch from '../../components/UserSearch';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
import { UserProfile, EmergencyContact, UserSearchResponse } from '../../types/user';
import { 
    Card, 
    CardContent, 
    Typography, 
    Box, 
    Grid, 
    Chip, 
    Button, 
    Avatar, 
    Divider,
    List,
    ListItem,
    ListItemText,
    ListItemAvatar,
    IconButton,
    Alert,
    CircularProgress,
    TextField
} from '@mui/material';
import {
    Person,
    Phone,
    Email,
    LocationOn,
    Security,
    Settings,
    Edit,
    Add,
    CheckCircle,
    Warning,
    Info
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

type TabType = 'overview' | 'contacts' | 'preferences';

const HomePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<TabType>('overview');
  const [selectedContact, setSelectedContact] = useState<UserSearchResponse | null>(null);
  const [addContactStatus, setAddContactStatus] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const currentUser = await authService.getCurrentUser();
        if (!currentUser) {
          setError('No authenticated user found.');
          setUser(null);
          setLoading(false);
          return;
        }
        const data = await userManagementService.getUserProfile(currentUser.id);
        if (!data) {
          setError('User not found.');
          setUser(null);
        } else {
          setUser(data as UserProfile);
          setError(null);
        }
      } catch (err) {
        setError('Could not load profile.');
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  // Handler for adding emergency contact
  const handleAddContact = async () => {
    setAddContactStatus(null);
    if (!selectedContact) {
      setAddContactStatus('Please select a contact first.');
      return;
    }

    try {
      const currentUser = await authService.getCurrentUser();
      if (!currentUser) {
        setAddContactStatus('No authenticated user found.');
        return;
      }
      await userManagementService.addEmergencyContact(currentUser.id, selectedContact.userId);
      setAddContactStatus('Contact request sent!');
      setSelectedContact(null);
      // Refresh user data
      const data = await userManagementService.getUserProfile(currentUser.id);
      setUser(data as UserProfile);
    } catch (err: any) {
      setAddContactStatus('Failed to add contact: ' + (err?.message || 'Unknown error'));
    }
  };

  const getSafetyStatus = () => {
    if (!user) return 'unknown';
    const contactCount = user.emergencyContacts?.length || 0;
    if (contactCount >= 2) return 'excellent';
    if (contactCount >= 1) return 'good';
    return 'needs-attention';
  };

  const getSafetyStatusColor = (status: string) => {
    switch (status) {
      case 'excellent': return 'success';
      case 'good': return 'warning';
      case 'needs-attention': return 'error';
      default: return 'default';
    }
  };

  const getSafetyStatusText = (status: string) => {
    switch (status) {
      case 'excellent': return 'Excellent';
      case 'good': return 'Good';
      case 'needs-attention': return 'Needs Attention';
      default: return 'Unknown';
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (!user) {
    return (
      <Box p={3}>
        <Alert severity="warning">No user data available</Alert>
      </Box>
    );
  }

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      {/* Header with User Info */}
      <Box sx={{ bgcolor: 'primary.main', color: 'white', p: 3, pb: 4 }}>
        <Grid container alignItems="center" spacing={2}>
          <Grid item>
            <Avatar 
              sx={{ 
                width: 64, 
                height: 64, 
                bgcolor: 'primary.dark',
                fontSize: '1.5rem'
              }}
            >
              {user.alias?.charAt(0).toUpperCase() || user.email?.charAt(0).toUpperCase() || '?'}
            </Avatar>
          </Grid>
          <Grid item xs>
            <Typography variant="h5" fontWeight="bold">
              Welcome back, {user.alias || 'User'}!
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.8 }}>
              {user.email}
            </Typography>
          </Grid>
          <Grid item>
            <IconButton 
              color="inherit" 
              onClick={() => navigate('/profile')}
            >
              <Edit />
            </IconButton>
          </Grid>
        </Grid>
      </Box>

      {/* Safety Status Card */}
      <Box sx={{ px: 3, mt: -2, mb: 3 }}>
        <Card elevation={3}>
          <CardContent>
            <Box display="flex" alignItems="center" justifyContent="space-between">
              <Box display="flex" alignItems="center">
                <Security sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">Safety Status</Typography>
              </Box>
              <Chip 
                label={getSafetyStatusText(getSafetyStatus())}
                color={getSafetyStatusColor(getSafetyStatus()) as any}
                variant="filled"
              />
            </Box>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {user.emergencyContacts?.length || 0} emergency contacts configured
            </Typography>
          </CardContent>
        </Card>
      </Box>

      {/* Quick Actions */}
      <Box sx={{ px: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Quick Actions
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <Card 
              sx={{ 
                cursor: 'pointer',
                '&:hover': { bgcolor: 'action.hover' }
              }}
              onClick={() => navigate('/map')}
            >
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <LocationOn sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
                <Typography variant="body2">Plan Route</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6}>
            <Card 
              sx={{ 
                cursor: 'pointer',
                '&:hover': { bgcolor: 'action.hover' }
              }}
              onClick={() => navigate('/chat')}
            >
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Person sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
                <Typography variant="body2">AI Companion</Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>

      {/* Tabs */}
      <Box sx={{ px: 3, mb: 2 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Box sx={{ display: 'flex' }}>
            {[
              { key: 'overview', label: 'Overview', icon: Info },
              { key: 'contacts', label: 'Emergency Contacts', icon: Security },
              { key: 'preferences', label: 'Preferences', icon: Settings }
            ].map((tabItem) => (
              <Button
                key={tabItem.key}
                onClick={() => setTab(tabItem.key as TabType)}
                sx={{
                  color: tab === tabItem.key ? 'primary.main' : 'text.secondary',
                  borderBottom: tab === tabItem.key ? 2 : 0,
                  borderColor: 'primary.main',
                  borderRadius: 0,
                  textTransform: 'none',
                  px: 3
                }}
                startIcon={<tabItem.icon />}
              >
                {tabItem.label}
              </Button>
            ))}
          </Box>
        </Box>
      </Box>

      {/* Tab Content */}
      <Box sx={{ px: 3, pb: 8, flex: 1 }}>
        {tab === 'overview' && (
          <Box>
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Profile Information
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <Box display="flex" alignItems="center" mb={1}>
                      <Person sx={{ mr: 1, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">Display Name</Typography>
                    </Box>
                    <Typography variant="body1">{user.alias || 'Not set'}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Box display="flex" alignItems="center" mb={1}>
                      <Phone sx={{ mr: 1, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">Phone</Typography>
                    </Box>
                    <Typography variant="body1">{user.phoneNr || 'Not set'}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Box display="flex" alignItems="center" mb={1}>
                      <Email sx={{ mr: 1, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">Preferred Contact</Typography>
                    </Box>
                    <Typography variant="body1">{user.preferredContactMethod || 'Not set'}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Box display="flex" alignItems="center" mb={1}>
                      <Info sx={{ mr: 1, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">Age Group</Typography>
                    </Box>
                    <Typography variant="body1">{user.ageGroup || 'Not set'}</Typography>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Safety Features
                </Typography>
                <Grid container spacing={1}>
                  {user.preferences?.shareLocation && (
                    <Grid item xs={12}>
                      <Chip 
                        icon={<CheckCircle />} 
                        label="Location sharing enabled" 
                        color="success" 
                        variant="outlined"
                      />
                    </Grid>
                  )}
                  {user.preferences?.enableSOS && (
                    <Grid item xs={12}>
                      <Chip 
                        icon={<Security />} 
                        label="SOS feature enabled" 
                        color="success" 
                        variant="outlined"
                      />
                    </Grid>
                  )}
                  {user.preferences?.autoNotifyContacts && (
                    <Grid item xs={12}>
                      <Chip 
                        icon={<CheckCircle />} 
                        label="Auto-notify contacts" 
                        color="success" 
                        variant="outlined"
                      />
                    </Grid>
                  )}
                </Grid>
              </CardContent>
            </Card>
          </Box>
        )}

        {tab === 'contacts' && (
          <Box>
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Emergency Contacts
                </Typography>
                
                {/* Add new contact section */}
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Add Emergency Contact
                  </Typography>
                  <UserSearch 
                    onUserSelect={setSelectedContact}
                    placeholder="Search by email or name..."
                  />
                  {selectedContact && (
                    <Box sx={{ mt: 1, p: 1, bgcolor: 'grey.100', borderRadius: 1 }}>
                      <Typography variant="body2">
                        Selected: {selectedContact.alias || selectedContact.email}
                      </Typography>
                    </Box>
                  )}
                  <Button
                    variant="contained"
                    onClick={handleAddContact}
                    disabled={!selectedContact}
                    sx={{ mt: 1 }}
                  >
                    Add Contact
                  </Button>
                  {addContactStatus && (
                    <Alert severity={addContactStatus.includes('Failed') ? 'error' : 'success'} sx={{ mt: 1 }}>
                      {addContactStatus}
                    </Alert>
                  )}
                </Box>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Emergency Contacts ({user.emergencyContacts?.length || 0})
                </Typography>
                {user.emergencyContacts && user.emergencyContacts.length > 0 ? (
                  <List>
                    {user.emergencyContacts.map((contact: EmergencyContact, idx: number) => (
                      <React.Fragment key={idx}>
                        <ListItem>
                          <ListItemAvatar>
                            <Avatar>
                              <Person />
                            </Avatar>
                          </ListItemAvatar>
                          <ListItemText
                            primary={contact.name}
                            secondary={
                              <Box>
                                <Typography variant="body2">{contact.email}</Typography>
                                <Typography variant="body2">{contact.phone}</Typography>
                                <Chip 
                                  label={contact.preferredMethod} 
                                  size="small" 
                                  variant="outlined"
                                  sx={{ mt: 0.5 }}
                                />
                              </Box>
                            }
                          />
                        </ListItem>
                        {idx < user.emergencyContacts!.length - 1 && <Divider />}
                      </React.Fragment>
                    ))}
                  </List>
                ) : (
                  <Box textAlign="center" py={3}>
                    <Warning sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
                    <Typography color="text.secondary">
                      No emergency contacts added yet
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Add contacts to ensure help is available in emergencies
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Box>
        )}

        {tab === 'preferences' && (
          <Box>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Safety Preferences
                </Typography>
                <List>
                  <ListItem>
                    <ListItemText
                      primary="Share Location"
                      secondary="Allow emergency contacts to see your location"
                    />
                    <Chip 
                      label={user.preferences?.shareLocation ? 'Enabled' : 'Disabled'} 
                      color={user.preferences?.shareLocation ? 'success' : 'default'}
                      size="small"
                    />
                  </ListItem>
                  <Divider />
                  <ListItem>
                    <ListItemText
                      primary="Check-in Interval"
                      secondary="How often to check in during journeys"
                    />
                    <Chip 
                      label={`${user.preferences?.checkInInterval || 15} minutes`} 
                      size="small"
                    />
                  </ListItem>
                  <Divider />
                  <ListItem>
                    <ListItemText
                      primary="Auto Notify Contacts"
                      secondary="Automatically notify contacts on delays"
                    />
                    <Chip 
                      label={user.preferences?.autoNotifyContacts ? 'Enabled' : 'Disabled'} 
                      color={user.preferences?.autoNotifyContacts ? 'success' : 'default'}
                      size="small"
                    />
                  </ListItem>
                  <Divider />
                  <ListItem>
                    <ListItemText
                      primary="SOS Feature"
                      secondary="Enable emergency SOS button"
                    />
                    <Chip 
                      label={user.preferences?.enableSOS ? 'Enabled' : 'Disabled'} 
                      color={user.preferences?.enableSOS ? 'success' : 'default'}
                      size="small"
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Box>
        )}
      </Box>

      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default HomePage;