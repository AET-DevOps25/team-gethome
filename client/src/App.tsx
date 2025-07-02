import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from './theme';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ProfileCompletionPage from './pages/user/ProfileCompletionPage';
import HomePage from './pages/home/HomePage';
import ProfileSettingsPage from './pages/profile/ProfileSettingsPage';
import { authService } from './services/authService';
import { userManagementService } from './services/userManagementService';
import { UserProfile } from './types/user';
import MapPage from './pages/map/MapPage';
import ChatPage from './pages/chat/ChatPage';
import ReportsPage from './pages/reports/ReportsPage';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const isAuthenticated = authService.isAuthenticated();
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

const ProfileCheckRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [loading, setLoading] = useState(true);
    const [hasProfile, setHasProfile] = useState(false);

    useEffect(() => {
        const checkProfile = async () => {
            try {
                const currentUser = await authService.getCurrentUser();
                const profile = await userManagementService.getUserProfile(currentUser.id);
                setHasProfile(!!profile && !!profile.alias && !!profile.gender && !!profile.ageGroup);
            } catch (error) {
                console.error('Error checking profile:', error);
                setHasProfile(false);
            } finally {
                setLoading(false);
            }
        };

        if (authService.isAuthenticated()) {
            checkProfile();
        }
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    return hasProfile ? <>{children}</> : <Navigate to="/profile-completion" />;
};

const App: React.FC = () => {
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Router>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route
                        path="/profile-completion"
                        element={
                            <PrivateRoute>
                                <ProfileCompletionPage />
                            </PrivateRoute>
                        }
                    />
                   
                    <Route
                        path="/home"
                        element={
                            <ProfileCheckRoute>
                                <HomePage />
                            </ProfileCheckRoute>
                        }
                    />
                    
                    <Route
                    path="/profile"
                    element={
                        <ProfileCheckRoute>
                        <HomePage />
                        </ProfileCheckRoute>
                    }
                    />

                    <Route
                    path="/map"
                    element={
                        <ProfileCheckRoute>
                        <MapPage />
                        </ProfileCheckRoute>
                    }
                    />

                    <Route
                    path="/chat"
                    element={
                        <ProfileCheckRoute>
                        <ChatPage />
                        </ProfileCheckRoute>
                    }
                    />
                    
                    <Route
                    path="/reports"
                    element={
                        <ProfileCheckRoute>
                        <ReportsPage />
                        </ProfileCheckRoute>
                    }
                    />

                    <Route path="/" element={
                        <ProfileCheckRoute>
                            <Navigate to="/profile" replace />
                        </ProfileCheckRoute>
                    } 
                    />
                    <Route path="*" element={
                        <ProfileCheckRoute>
                            <Navigate to="/profile" replace />
                        </ProfileCheckRoute>
                    } />

                </Routes>
            </Router>
        </ThemeProvider>
    );
};

export default App; 