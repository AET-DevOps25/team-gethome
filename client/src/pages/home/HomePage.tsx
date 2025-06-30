import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    UserIcon,
    MapIcon,
    ChatBubbleLeftRightIcon,
    FlagIcon,
} from '@heroicons/react/24/outline';
import { authService } from '../../services/authService';
import BottomTabBar from '../../components/BottomTabBar';

const HomePage: React.FC = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState<{ email?: string; name?: string } | null>(null);
    const [activeTab, setActiveTab] = useState(0);

    useEffect(() => {
        const loadUser = async () => {
            try {
                const userData = await authService.getCurrentUser();
                setUser(userData);
            } catch (error) {
                setUser(null);
            }
        };
        loadUser();
    }, []);

    return (
        <div className="flex flex-col min-h-screen bg-gray-50">
            {/* User Info */}
            <div className="flex flex-col items-center justify-center py-8 bg-white shadow">
                <div className="h-16 w-16 rounded-full bg-indigo-600 flex items-center justify-center text-2xl text-white font-bold mb-2">
                    {user?.name?.charAt(0).toUpperCase() || user?.email?.charAt(0).toUpperCase() || '?'}
                </div>
                <div className="text-lg font-semibold">{user?.name || 'User'}</div>
                <div className="text-gray-500 text-sm">{user?.email}</div>
            </div>

            {/* Main Content */}
            <div className="flex-1 flex flex-col items-center justify-center px-4">
                <h1 className="text-xl font-bold mb-2">Welcome to GetHome!</h1>
                <p className="text-gray-600 text-center">
                    Use the tabs below to navigate between Profile, Map, Chat, and Flags.
                </p>
            </div>

            {/* Bottom Tab Bar */}
            <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
        </div>
    );
};

export default HomePage;