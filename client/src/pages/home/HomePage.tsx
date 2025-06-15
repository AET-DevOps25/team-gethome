import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import {
    HomeIcon,
    UserIcon,
    ShieldCheckIcon,
    BellIcon,
    Bars3Icon,
    XMarkIcon,
    ArrowRightOnRectangleIcon,
} from '@heroicons/react/24/outline';

const HomePage: React.FC = () => {
    const navigate = useNavigate();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);
    const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
    const [userInitial, setUserInitial] = useState<string>('');

    useEffect(() => {
        const loadUser = async () => {
            try {
                const user = await authService.getCurrentUser();
                setUserInitial(user?.email?.charAt(0).toUpperCase() || '');
            } catch (error) {
                console.error('Error loading user:', error);
            }
        };
        loadUser();
    }, []);

    const menuItems = [
        { text: 'Dashboard', icon: HomeIcon, path: '/dashboard' },
        { text: 'Profile', icon: UserIcon, path: '/profile' },
        { text: 'Safety Settings', icon: ShieldCheckIcon, path: '/safety-settings' },
        { text: 'Notifications', icon: BellIcon, path: '/notifications' },
    ];

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-100">
            {/* Sidebar for mobile */}
            <div className={`fixed inset-0 z-40 lg:hidden ${isSidebarOpen ? 'block' : 'hidden'}`}>
                <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setIsSidebarOpen(false)}></div>
                <div className="fixed inset-y-0 left-0 flex w-64 flex-col bg-white">
                    <div className="flex h-16 items-center justify-between px-4">
                        <span className="text-xl font-semibold text-gray-800">GetHome</span>
                        <button
                            onClick={() => setIsSidebarOpen(false)}
                            className="rounded-md p-2 text-gray-500 hover:bg-gray-100"
                        >
                            <XMarkIcon className="h-6 w-6" />
                        </button>
                    </div>
                    <div className="flex-1 overflow-y-auto">
                        <nav className="mt-5 px-2">
                            {menuItems.map((item) => (
                                <button
                                    key={item.text}
                                    onClick={() => {
                                        navigate(item.path);
                                        setIsSidebarOpen(false);
                                    }}
                                    className="group flex w-full items-center rounded-md px-2 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                                >
                                    <item.icon className="mr-3 h-6 w-6" />
                                    {item.text}
                                </button>
                            ))}
                        </nav>
                    </div>
                </div>
            </div>

            {/* Static sidebar for desktop */}
            <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
                <div className="flex min-h-0 flex-1 flex-col border-r border-gray-200 bg-white">
                    <div className="flex h-16 items-center px-4">
                        <span className="text-xl font-semibold text-gray-800">GetHome</span>
                    </div>
                    <div className="flex flex-1 flex-col overflow-y-auto">
                        <nav className="flex-1 px-2 py-4">
                            {menuItems.map((item) => (
                                <button
                                    key={item.text}
                                    onClick={() => navigate(item.path)}
                                    className="group flex w-full items-center rounded-md px-2 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                                >
                                    <item.icon className="mr-3 h-6 w-6" />
                                    {item.text}
                                </button>
                            ))}
                        </nav>
                    </div>
                </div>
            </div>

            {/* Main content */}
            <div className="lg:pl-64">
                <div className="sticky top-0 z-10 flex h-16 flex-shrink-0 bg-white shadow">
                    <button
                        type="button"
                        className="border-r border-gray-200 px-4 text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500 lg:hidden"
                        onClick={() => setIsSidebarOpen(true)}
                    >
                        <Bars3Icon className="h-6 w-6" />
                    </button>
                    <div className="flex flex-1 justify-between px-4">
                        <div className="flex flex-1"></div>
                        <div className="ml-4 flex items-center md:ml-6">
                            {/* Profile dropdown */}
                            <div className="relative">
                                <button
                                    type="button"
                                    className="flex max-w-xs items-center rounded-full bg-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                                    onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
                                >
                                    <span className="sr-only">Open user menu</span>
                                    <div className="h-8 w-8 rounded-full bg-indigo-600 flex items-center justify-center text-white">
                                        {userInitial}
                                    </div>
                                </button>

                                {isProfileMenuOpen && (
                                    <div className="absolute right-0 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                                        <button
                                            onClick={() => {
                                                navigate('/profile');
                                                setIsProfileMenuOpen(false);
                                            }}
                                            className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                                        >
                                            Profile
                                        </button>
                                        <button
                                            onClick={() => {
                                                navigate('/profile');
                                                setIsProfileMenuOpen(false);
                                            }}
                                            className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                                        >
                                            Settings
                                        </button>
                                        <div className="border-t border-gray-100"></div>
                                        <button
                                            onClick={handleLogout}
                                            className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                                        >
                                            Logout
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                <main className="py-6">
                    <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                        <div className="rounded-lg bg-white p-6 shadow">
                            <h1 className="text-2xl font-semibold text-gray-900">Welcome to GetHome!</h1>
                            <p className="mt-4 text-gray-600">
                                This is your home page where you can access all the features of the application.
                            </p>
                            <p className="mt-2 text-gray-600">
                                Use the navigation menu on the left to access different sections of the application.
                            </p>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default HomePage; 