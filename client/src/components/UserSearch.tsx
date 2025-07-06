import React, { useState, useEffect } from 'react';
import {
    TextField,
    List,
    ListItem,
    ListItemAvatar,
    ListItemText,
    Avatar,
    Box,
    Typography,
    CircularProgress,
    Alert,
    Paper,
    InputAdornment,
} from '@mui/material';
import { Search, Person } from '@mui/icons-material';
import { userManagementService } from '../services/userManagementService';
import { UserSearchResponse } from '../types/user';

interface UserSearchProps {
    onUserSelect: (user: UserSearchResponse) => void;
    placeholder?: string;
}

const UserSearch: React.FC<UserSearchProps> = ({ onUserSelect, placeholder = "Search by email or name..." }) => {
    const [query, setQuery] = useState('');
    const [users, setUsers] = useState<UserSearchResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showResults, setShowResults] = useState(false);

    useEffect(() => {
        const searchUsers = async () => {
            if (query.length < 2) {
                setUsers([]);
                setShowResults(false);
                return;
            }

            setLoading(true);
            setError(null);

            try {
                const results = await userManagementService.searchUsers(query);
                setUsers(results);
                setShowResults(true);
            } catch (err) {
                console.error('Error searching users:', err);
                setError('Failed to search users. Please try again.');
                setUsers([]);
            } finally {
                setLoading(false);
            }
        };

        const debounceTimer = setTimeout(searchUsers, 300);
        return () => clearTimeout(debounceTimer);
    }, [query]);

    const handleUserSelect = (user: UserSearchResponse) => {
        onUserSelect(user);
        setQuery('');
        setShowResults(false);
        setUsers([]);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setQuery(e.target.value);
        if (e.target.value.length < 2) {
            setShowResults(false);
        }
    };

    const handleInputFocus = () => {
        if (users.length > 0) {
            setShowResults(true);
        }
    };

    const handleInputBlur = () => {
        // Delay hiding results to allow for clicks
        setTimeout(() => setShowResults(false), 200);
    };

    return (
        <Box sx={{ position: 'relative', width: '100%' }}>
            <TextField
                fullWidth
                value={query}
                onChange={handleInputChange}
                onFocus={handleInputFocus}
                onBlur={handleInputBlur}
                placeholder={placeholder}
                InputProps={{
                    startAdornment: (
                        <InputAdornment position="start">
                            <Search />
                        </InputAdornment>
                    ),
                    endAdornment: loading && (
                        <InputAdornment position="end">
                            <CircularProgress size={20} />
                        </InputAdornment>
                    ),
                }}
                sx={{ mb: 1 }}
            />

            {error && (
                <Alert severity="error" sx={{ mb: 1 }}>
                    {error}
                </Alert>
            )}

            {showResults && (
                <Paper 
                    elevation={3} 
                    sx={{ 
                        position: 'absolute', 
                        top: '100%', 
                        left: 0, 
                        right: 0, 
                        zIndex: 1000,
                        maxHeight: 300,
                        overflow: 'auto'
                    }}
                >
                    {users.length === 0 && query.length >= 2 && !loading ? (
                        <Box sx={{ p: 2, textAlign: 'center' }}>
                            <Typography variant="body2" color="text.secondary">
                                No users found
                            </Typography>
                        </Box>
                    ) : (
                        <List>
                            {users.map((user) => (
                                <ListItem
                                    key={user.userId}
                                    button
                                    onClick={() => handleUserSelect(user)}
                                    sx={{ cursor: 'pointer' }}
                                >
                                    <ListItemAvatar>
                                        <Avatar>
                                            {user.profilePictureUrl ? (
                                                <img 
                                                    src={user.profilePictureUrl} 
                                                    alt={user.alias || user.email}
                                                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                                />
                                            ) : (
                                                <Person />
                                            )}
                                        </Avatar>
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={user.alias || user.email}
                                        secondary={user.alias ? user.email : 'No alias set'}
                                    />
                                </ListItem>
                            ))}
                        </List>
                    )}
                </Paper>
            )}
        </Box>
    );
};

export default UserSearch; 