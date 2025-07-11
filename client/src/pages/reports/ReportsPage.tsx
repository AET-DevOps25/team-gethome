import React, { useEffect, useState } from 'react';
import BottomTabBar from '../../components/BottomTabBar';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
import { safetyAnalyticsService, SafetyAnalytics, JourneyHistory } from '../../services/safetyAnalyticsService';
import { UserProfile } from '../../types/user';
import {
    Box,
    Card,
    CardContent,
    Typography,
    Grid,
    Chip,
    CircularProgress,
    Alert,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Divider,
    Button,
    LinearProgress,
    Paper
} from '@mui/material';
import {
    TrendingUp,
    TrendingDown,
    Security,
    LocationOn,
    Warning,
    CheckCircle,
    Info,
    Flag,
    BarChart,
    Timeline,
    Shield,
    Speed
} from '@mui/icons-material';

const ReportsPage: React.FC = () => {
    const [user, setUser] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState(3);
    const [safetyReport, setSafetyReport] = useState<SafetyAnalytics>({
        totalJourneys: 0,
        safeJourneys: 0,
        emergencyTriggers: 0,
        dangerZonesEncountered: 0,
        averageJourneyTime: 0,
        safetyScore: 0,
        lastUpdated: new Date()
    });
    const [recentJourneys, setRecentJourneys] = useState<JourneyHistory[]>([]);
    const [safetyTips, setSafetyTips] = useState<string[]>([]);
    const [dataSource, setDataSource] = useState<'real' | 'fallback'>('fallback');

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                const currentUser = await authService.getCurrentUser();
                if (!currentUser) {
                    setError('No authenticated user found.');
                    setLoading(false);
                    return;
                }
                const userData = await userManagementService.getUserProfile(currentUser.id);
                setUser(userData as UserProfile);
                
                // Fetch real safety analytics and journey data
                await loadSafetyData();
                
            } catch (err) {
                console.error('Error loading user data:', err);
                setError('Could not load user data.');
                // Load fallback data even if user data fails
                await loadFallbackData();
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const loadSafetyData = async () => {
        try {
            console.log('Loading real safety analytics...');
            
            // Load safety analytics
            const analytics = await safetyAnalyticsService.generateSafetyAnalytics();
            setSafetyReport(analytics);
            
            // Load journey history
            const journeys = await safetyAnalyticsService.generateJourneyHistory();
            setRecentJourneys(journeys);
            
            // Generate personalized safety tips
            const tips = safetyAnalyticsService.generateSafetyTips(analytics);
            setSafetyTips(tips);
            
            // Check if we got real data or fallback data
            const routes = await safetyAnalyticsService.getUserRoutes();
            setDataSource(routes.length > 0 ? 'real' : 'fallback');
            
            console.log('Safety analytics loaded successfully:', { analytics, journeys: journeys.length, dataSource: routes.length > 0 ? 'real' : 'fallback' });
            
        } catch (error) {
            console.error('Error loading safety data:', error);
            await loadFallbackData();
        }
    };

    const loadFallbackData = async () => {
        console.log('Loading fallback data...');
        try {
            // Use service fallback methods
            const analytics = await safetyAnalyticsService.generateSafetyAnalytics();
            const journeys = await safetyAnalyticsService.generateJourneyHistory();
            const tips = safetyAnalyticsService.generateSafetyTips(analytics);
            
            setSafetyReport(analytics);
            setRecentJourneys(journeys);
            setSafetyTips(tips);
            setDataSource('fallback');
            
            console.log('Fallback data loaded successfully');
        } catch (error) {
            console.error('Error loading fallback data:', error);
            setError('Failed to load safety data.');
        }
    };

    const getSafetyStatusColor = (status: string) => {
        switch (status) {
            case 'safe': return 'success';
            case 'warning': return 'warning';
            case 'danger': return 'error';
            default: return 'default';
        }
    };

    const getSafetyStatusIcon = (status: string) => {
        switch (status) {
            case 'safe': return <CheckCircle />;
            case 'warning': return <Warning />;
            case 'danger': return <Warning />;
            default: return <Info />;
        }
    };

    const formatDuration = (minutes: number) => {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
    };

    const formatTime = (date: Date) => {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
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
            {/* Header */}
            <Box sx={{ bgcolor: 'primary.main', color: 'white', p: 2 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="h6">
                        Safety Analytics & Reports
                    </Typography>
                    <Chip 
                        label={dataSource === 'real' ? 'Live Data' : 'Sample Data'}
                        color={dataSource === 'real' ? 'success' : 'warning'}
                        size="small"
                        variant="filled"
                        sx={{ 
                            bgcolor: dataSource === 'real' ? 'rgba(46, 125, 50, 0.8)' : 'rgba(237, 108, 2, 0.8)',
                            color: 'white'
                        }}
                    />
                </Box>
                {dataSource === 'fallback' && (
                    <Typography variant="body2" sx={{ mt: 1, opacity: 0.9 }}>
                        Showing sample data. Complete some journeys to see your real analytics.
                    </Typography>
                )}
            </Box>

            {/* Safety Score Overview */}
            <Box sx={{ px: 2, py: 1 }}>
                <Card>
                    <CardContent>
                        <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
                            <Box display="flex" alignItems="center">
                                <Shield sx={{ mr: 1, color: 'primary.main' }} />
                                <Typography variant="h6">Overall Safety Score</Typography>
                            </Box>
                            <Chip 
                                label={`${safetyReport.safetyScore}%`}
                                color="success"
                                variant="filled"
                            />
                        </Box>
                        <LinearProgress 
                            variant="determinate" 
                            value={safetyReport.safetyScore} 
                            sx={{ height: 8, borderRadius: 4 }}
                        />
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            Excellent safety record! Keep up the good work.
                        </Typography>
                    </CardContent>
                </Card>
            </Box>

            {/* Key Metrics */}
            <Box sx={{ px: 2, py: 1 }}>
                <Grid container spacing={2}>
                    <Grid item xs={6}>
                        <Card>
                            <CardContent sx={{ textAlign: 'center', py: 2 }}>
                                <TrendingUp sx={{ fontSize: 40, color: 'success.main', mb: 1 }} />
                                <Typography variant="h4" color="success.main">
                                    {safetyReport.safeJourneys}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Safe Journeys
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                    <Grid item xs={6}>
                        <Card>
                            <CardContent sx={{ textAlign: 'center', py: 2 }}>
                                <Speed sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
                                <Typography variant="h4" color="primary.main">
                                    {formatDuration(safetyReport.averageJourneyTime)}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Avg Journey Time
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Box>

            {/* Detailed Statistics */}
            <Box sx={{ px: 2, py: 1 }}>
                <Card>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Journey Statistics
                        </Typography>
                        <Grid container spacing={2}>
                            <Grid item xs={6}>
                                <Box display="flex" alignItems="center" mb={1}>
                                    <BarChart sx={{ mr: 1, color: 'text.secondary' }} />
                                    <Typography variant="body2" color="text.secondary">Total Journeys</Typography>
                                </Box>
                                <Typography variant="h6">{safetyReport.totalJourneys}</Typography>
                            </Grid>
                            <Grid item xs={6}>
                                <Box display="flex" alignItems="center" mb={1}>
                                    <Warning sx={{ mr: 1, color: 'text.secondary' }} />
                                    <Typography variant="body2" color="text.secondary">Danger Zones</Typography>
                                </Box>
                                <Typography variant="h6">{safetyReport.dangerZonesEncountered}</Typography>
                            </Grid>
                            <Grid item xs={6}>
                                <Box display="flex" alignItems="center" mb={1}>
                                    <Flag sx={{ mr: 1, color: 'text.secondary' }} />
                                    <Typography variant="body2" color="text.secondary">Emergency Triggers</Typography>
                                </Box>
                                <Typography variant="h6">{safetyReport.emergencyTriggers}</Typography>
                            </Grid>
                            <Grid item xs={6}>
                                <Box display="flex" alignItems="center" mb={1}>
                                    <CheckCircle sx={{ mr: 1, color: 'text.secondary' }} />
                                    <Typography variant="body2" color="text.secondary">Safety Rate</Typography>
                                </Box>
                                <Typography variant="h6">
                                    {safetyReport.totalJourneys > 0 
                                        ? `${Math.round((safetyReport.safeJourneys / safetyReport.totalJourneys) * 100)}%`
                                        : 'N/A'
                                    }
                                </Typography>
                            </Grid>
                        </Grid>
                    </CardContent>
                </Card>
            </Box>

            {/* Recent Journeys */}
            <Box sx={{ px: 2, py: 1 }}>
                <Card>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Recent Journeys
                        </Typography>
                        <List>
                            {recentJourneys.map((journey, index) => (
                                <React.Fragment key={journey.id}>
                                    <ListItem>
                                        <ListItemIcon>
                                            <Box
                                                sx={{
                                                    color: `${getSafetyStatusColor(journey.safetyStatus)}.main`
                                                }}
                                            >
                                                {getSafetyStatusIcon(journey.safetyStatus)}
                                            </Box>
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={
                                                <Box display="flex" justifyContent="space-between" alignItems="center">
                                                    <Typography variant="body1">
                                                        {journey.startLocation} → {journey.endLocation}
                                                    </Typography>
                                                    <Chip 
                                                        label={journey.safetyStatus.toUpperCase()}
                                                        color={getSafetyStatusColor(journey.safetyStatus) as any}
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                </Box>
                                            }
                                            secondary={
                                                <Box>
                                                    <Typography variant="body2" color="text.secondary">
                                                        {formatTime(journey.startTime)} - {formatTime(journey.endTime)} 
                                                        ({formatDuration(journey.duration)})
                                                    </Typography>
                                                    {journey.dangerZones > 0 && (
                                                        <Typography variant="body2" color="warning.main">
                                                            {journey.dangerZones} danger zone{journey.dangerZones > 1 ? 's' : ''} encountered
                                                        </Typography>
                                                    )}
                                                </Box>
                                            }
                                        />
                                    </ListItem>
                                    {index < recentJourneys.length - 1 && <Divider />}
                                </React.Fragment>
                            ))}
                        </List>
                    </CardContent>
                </Card>
            </Box>

            {/* Safety Tips */}
            <Box sx={{ px: 2, py: 1 }}>
                <Card>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Personalized Safety Tips
                        </Typography>
                        <List dense>
                            {safetyTips.map((tip, index) => (
                                <ListItem key={index} sx={{ py: 0.5 }}>
                                    <ListItemIcon sx={{ minWidth: 32 }}>
                                        <CheckCircle color="success" fontSize="small" />
                                    </ListItemIcon>
                                    <ListItemText primary={tip} />
                                </ListItem>
                            ))}
                        </List>
                    </CardContent>
                </Card>
            </Box>

            {/* Action Buttons */}
            <Box sx={{ px: 2, py: 1, pb: 8 }}>
                <Grid container spacing={2}>
                    <Grid item xs={6}>
                        <Button
                            fullWidth
                            variant="outlined"
                            startIcon={<LocationOn />}
                            onClick={() => window.location.href = '/map'}
                        >
                            Plan Route
                        </Button>
                    </Grid>
                    <Grid item xs={6}>
                        <Button
                            fullWidth
                            variant="outlined"
                            startIcon={<Security />}
                            onClick={() => window.location.href = '/profile'}
                        >
                            Safety Settings
                        </Button>
                    </Grid>
                </Grid>
            </Box>

      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default ReportsPage;