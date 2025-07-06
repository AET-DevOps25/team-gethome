import React, { useEffect, useState } from 'react';
import BottomTabBar from '../../components/BottomTabBar';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
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

interface SafetyReport {
    totalJourneys: number;
    safeJourneys: number;
    emergencyTriggers: number;
    dangerZonesEncountered: number;
    averageJourneyTime: number;
    safetyScore: number;
    lastUpdated: Date;
}

interface JourneyHistory {
    id: string;
    startLocation: string;
    endLocation: string;
    startTime: Date;
    endTime: Date;
    safetyStatus: 'safe' | 'warning' | 'danger';
    duration: number;
    dangerZones: number;
}

const ReportsPage: React.FC = () => {
    const [user, setUser] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState(3);
    const [safetyReport, setSafetyReport] = useState<SafetyReport>({
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
                const data = await userManagementService.getUserProfile(currentUser.id);
                setUser(data as UserProfile);
                
                // Generate mock safety report (in real app, this would come from backend)
                generateSafetyReport();
                generateRecentJourneys();
                generateSafetyTips();
            } catch (err) {
                setError('Could not load user data.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const generateSafetyReport = () => {
        // Mock data - in real app this would come from backend analytics
        const mockReport: SafetyReport = {
            totalJourneys: 24,
            safeJourneys: 22,
            emergencyTriggers: 1,
            dangerZonesEncountered: 3,
            averageJourneyTime: 25,
            safetyScore: 92,
            lastUpdated: new Date()
        };
        setSafetyReport(mockReport);
    };

    const generateRecentJourneys = () => {
        // Mock journey history
        const mockJourneys: JourneyHistory[] = [
            {
                id: '1',
                startLocation: 'Home',
                endLocation: 'Work',
                startTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
                endTime: new Date(Date.now() - 1.5 * 60 * 60 * 1000),
                safetyStatus: 'safe',
                duration: 30,
                dangerZones: 0
            },
            {
                id: '2',
                startLocation: 'Work',
                endLocation: 'Gym',
                startTime: new Date(Date.now() - 5 * 60 * 60 * 1000),
                endTime: new Date(Date.now() - 4.5 * 60 * 60 * 1000),
                safetyStatus: 'warning',
                duration: 25,
                dangerZones: 1
            },
            {
                id: '3',
                startLocation: 'Gym',
                endLocation: 'Home',
                startTime: new Date(Date.now() - 8 * 60 * 60 * 1000),
                endTime: new Date(Date.now() - 7.5 * 60 * 60 * 1000),
                safetyStatus: 'safe',
                duration: 28,
                dangerZones: 0
            }
        ];
        setRecentJourneys(mockJourneys);
    };

    const generateSafetyTips = () => {
        const tips = [
            "Your safety score is excellent! Keep up the good habits.",
            "Consider avoiding the area around Main St after 10 PM.",
            "You've been consistent with your check-ins - great job!",
            "Your emergency contacts are properly configured.",
            "Consider using the AI companion more during late-night journeys."
        ];
        setSafetyTips(tips);
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
                <Typography variant="h6" align="center">
                    Safety Analytics & Reports
                </Typography>
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
                                    {Math.round((safetyReport.safeJourneys / safetyReport.totalJourneys) * 100)}%
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