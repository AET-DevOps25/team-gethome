import React from 'react';
import {
    UserIcon,
    MapIcon,
    ChatBubbleLeftRightIcon,
    FlagIcon,
} from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';

const tabs = [
    { name: 'Profile', icon: UserIcon, path: '/profile' },
    { name: 'Map', icon: MapIcon, path: '/map' },
    { name: 'Chat', icon: ChatBubbleLeftRightIcon, path: '/chat' },
    { name: 'Flags', icon: FlagIcon, path: '/flags' },
];

interface BottomTabBarProps {
    activeTab: number;
    setActiveTab: (idx: number) => void;
}

const BottomTabBar: React.FC<BottomTabBarProps> = ({ activeTab, setActiveTab }) => {
    const navigate = useNavigate();

    return (
        <nav className="fixed bottom-0 left-0 right-0 bg-white border-t shadow flex justify-around py-2 z-50">
            {tabs.map((tab, idx) => (
                <button
                    key={tab.name}
                    className={`flex flex-col items-center flex-1 ${activeTab === idx ? 'text-indigo-600' : 'text-gray-400'}`}
                    onClick={() => {
                        setActiveTab(idx);
                        navigate(tab.path);
                    }}
                >
                    <tab.icon className="h-7 w-7 mb-1" />
                    <span className="text-xs">{tab.name}</span>
                </button>
            ))}
        </nav>
    );
};

export default BottomTabBar;