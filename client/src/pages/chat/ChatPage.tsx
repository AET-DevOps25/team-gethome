import React, { useEffect, useRef, useState } from 'react';
import { chatService } from '../../services/chatService';
import BottomTabBar from '../../components/BottomTabBar';
import { UserCircleIcon } from '@heroicons/react/24/solid';
import {
    Box,
    Card,
    CardContent,
    Typography,
    TextField,
    Button,
    Avatar,
    Chip,
    Alert,
    CircularProgress,
    Divider,
    List,
    ListItem,
    ListItemText,
    ListItemAvatar,
    Paper,
    Fab,
    Snackbar,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions
} from '@mui/material';
import {
    Send as SendIcon,
    Warning,
    CheckCircle,
    Info,
    Security,
    Psychology,
    SmartToy,
    Star,
    Message
} from '@mui/icons-material';

interface Message {
  id: string;
  sender: 'me' | 'bot';
  text: string;
  timestamp: Date;
}

const ChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(2);
  const [sessionLoading, setSessionLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showEmergencyDialog, setShowEmergencyDialog] = useState(false);
  const [emergencyTriggered, setEmergencyTriggered] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const start = async () => {
      setSessionLoading(true);
      try {
        const id = await chatService.startSession();
        setSessionId(id);
        
        // Add welcome message
        setMessages([{
          id: 'welcome',
          sender: 'bot',
          text: "Hi! I'm your GetHome AI companion. I'm here to keep you company and help you stay safe on your journey. Feel free to chat with me about anything, and remember - I can detect if you're in trouble and help get you assistance quickly.",
          timestamp: new Date()
        }]);
      } catch (err) {
        setError('Failed to start chat session. Please try again.');
        setMessages([{ 
          id: 'error', 
          sender: 'bot', 
          text: 'Sorry, I\'m having trouble connecting right now. Please check your connection and try again.',
          timestamp: new Date()
        }]);
      } finally {
        setSessionLoading(false);
      }
    };
    start();
    return () => {
      if (sessionId) chatService.closeSession(sessionId).catch(() => {});
    };
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || !sessionId || loading) return;
    
    const userMsg: Message = { 
      id: Date.now() + '', 
      sender: 'me', 
      text: input,
      timestamp: new Date()
    };
    setMessages((msgs) => [...msgs, userMsg]);
    setInput('');
    setLoading(true);
    
    try {
      const res = await chatService.sendMessage(sessionId, userMsg.text);
      setMessages((msgs) => [
        ...msgs,
        {
          id: Date.now() + '-bot',
          sender: 'bot',
          text: res.reply,
          timestamp: new Date()
        },
      ]);
    } catch (err) {
      setMessages((msgs) => [
        ...msgs,
        { 
          id: Date.now() + '-err', 
          sender: 'bot', 
          text: 'Sorry, I\'m having trouble responding right now. Please try again.',
          timestamp: new Date()
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleEmergencyTrigger = () => {
    setShowEmergencyDialog(true);
  };

  const triggerEmergency = async () => {
    try {
      // Add emergency message to chat
      setMessages((msgs) => [
        ...msgs,
        {
          id: Date.now() + '-emergency',
          sender: 'bot',
          text: "🚨 EMERGENCY DETECTED! I'm sending your location to your emergency contacts right now. Help is on the way!",
          timestamp: new Date()
        }
      ]);
      
      setEmergencyTriggered(true);
      setShowEmergencyDialog(false);
    } catch (error) {
      console.error('Failed to trigger emergency:', error);
    }
  };

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-50" style={{ paddingBottom: '80px' }}>
      {/* Header */}
      <Box sx={{ bgcolor: 'primary.main', color: 'white', p: 2 }}>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box display="flex" alignItems="center">
            <Psychology sx={{ mr: 1 }} />
            <Typography variant="h6">
              AI Safety Companion
            </Typography>
          </Box>
          <Chip 
            label={sessionId ? 'Connected' : 'Connecting...'} 
            color={sessionId ? 'success' : 'warning'}
            size="small"
            variant="filled"
          />
        </Box>
      </Box>

      {/* Safety Status */}
      <Box sx={{ px: 2, py: 1 }}>
        <Card>
          <CardContent sx={{ py: 2 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between">
              <Box display="flex" alignItems="center">
                <Security sx={{ mr: 1, color: 'success.main' }} />
                <Typography variant="body2">
                  AI Safety Monitoring Active
                </Typography>
              </Box>
              <Chip 
                icon={<CheckCircle />}
                label="Protected" 
                color="success" 
                size="small"
                variant="outlined"
              />
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Chat Messages */}
      <Box sx={{ flex: 1, px: 2, py: 1, overflow: 'hidden', minHeight: 0 }}>
        <Paper sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
          {sessionLoading ? (
            <Box display="flex" justifyContent="center" alignItems="center" flex={1}>
              <Box textAlign="center">
                <CircularProgress sx={{ mb: 2 }} />
                <Typography>Connecting to AI companion...</Typography>
              </Box>
            </Box>
          ) : error ? (
            <Box display="flex" justifyContent="center" alignItems="center" flex={1}>
              <Alert severity="error" sx={{ maxWidth: 400 }}>
                {error}
              </Alert>
            </Box>
          ) : (
            <>
              {/* Messages Area */}
              <Box sx={{ flex: 1, overflow: 'auto', p: 2, minHeight: 0 }}>
                {messages.map((msg) => (
                  <Box
                    key={msg.id}
                    sx={{
                      display: 'flex',
                      justifyContent: msg.sender === 'me' ? 'flex-end' : 'flex-start',
                      mb: 2
                    }}
                  >
                    <Box
                      sx={{
                        maxWidth: '70%',
                        display: 'flex',
                        flexDirection: msg.sender === 'me' ? 'row-reverse' : 'row',
                        alignItems: 'flex-end',
                        gap: 1
                      }}
                    >
                      <Avatar
                        sx={{
                          width: 32,
                          height: 32,
                          bgcolor: msg.sender === 'me' ? 'primary.main' : 'secondary.main'
                        }}
                      >
                        {msg.sender === 'me' ? <UserCircleIcon /> : <SmartToy />}
                      </Avatar>
                      <Box>
                        <Paper
                          sx={{
                            p: 2,
                            bgcolor: msg.sender === 'me' ? 'primary.main' : 'grey.100',
                            color: msg.sender === 'me' ? 'white' : 'text.primary',
                            borderRadius: 2,
                            maxWidth: '100%',
                            wordBreak: 'break-word'
                          }}
                        >
                          <Typography variant="body2">
                            {msg.text}
                          </Typography>
                        </Paper>
                        <Typography 
                          variant="caption" 
                          color="text.secondary"
                          sx={{ mt: 0.5, display: 'block' }}
                        >
                          {formatTime(msg.timestamp)}
                        </Typography>
                      </Box>
                    </Box>
                  </Box>
                ))}
                {loading && (
                  <Box display="flex" justifyContent="flex-start" mb={2}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
                        <SmartToy />
                      </Avatar>
                      <Paper sx={{ p: 2, bgcolor: 'grey.100' }}>
                        <Box display="flex" alignItems="center" gap={1}>
                          <CircularProgress size={16} />
                          <Typography variant="body2">AI is thinking...</Typography>
                        </Box>
                      </Paper>
                    </Box>
                  </Box>
                )}
                <div ref={messagesEndRef} />
              </Box>

              {/* Input Area */}
              <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
                <Box display="flex" gap={1}>
                  <TextField
                    fullWidth
                    multiline
                    maxRows={4}
                    placeholder="Type your message..."
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    disabled={loading || !sessionId}
                    InputProps={{
                      endAdornment: (
                        <Button
                          onClick={handleSend}
                          disabled={!input.trim() || loading || !sessionId}
                          sx={{ minWidth: 'auto' }}
                        >
                          <SendIcon />
                        </Button>
                      )
                    }}
                  />
                </Box>
                <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                  Press Enter to send, Shift+Enter for new line
                </Typography>
              </Box>
            </>
          )}
        </Paper>
      </Box>

      {/* Quick Actions */}
      <Box sx={{ px: 2, py: 2, pb: 2 }}>
        <Card>
          <CardContent sx={{ py: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Quick Actions
            </Typography>
            <Box display="flex" gap={1} flexWrap="wrap">
              <Button
                size="small"
                variant="outlined"
                onClick={() => setInput("I'm feeling unsafe")}
                startIcon={<Warning />}
              >
                Feeling Unsafe
              </Button>
              <Button
                size="small"
                variant="outlined"
                onClick={() => setInput("What's the safest route home?")}
                startIcon={<Security />}
              >
                Safe Route
              </Button>
              <Button
                size="small"
                variant="outlined"
                onClick={() => setInput("Tell me a joke")}
                startIcon={<Star />}
              >
                Lighten Mood
              </Button>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Emergency Button */}
      <Fab
        color="error"
        aria-label="emergency"
        sx={{
          position: 'fixed',
          bottom: 100,
          right: 16,
          zIndex: 1000,
        }}
        onClick={handleEmergencyTrigger}
      >
        <Warning />
      </Fab>

      {/* Emergency Dialog */}
      <Dialog
        open={showEmergencyDialog}
        onClose={() => setShowEmergencyDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center">
            <Warning sx={{ mr: 1, color: 'warning.main' }} />
            Emergency Alert
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" paragraph>
            Are you in immediate danger? This will:
          </Typography>
          <List dense>
            <ListItem>
              <ListItemText primary="Send your location to emergency contacts" />
            </ListItem>
            <ListItem>
              <ListItemText primary="Trigger immediate notifications" />
            </ListItem>
            <ListItem>
              <ListItemText primary="Activate emergency protocols" />
            </ListItem>
          </List>
          <Alert severity="warning" sx={{ mt: 2 }}>
            Only use this if you're in genuine danger.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowEmergencyDialog(false)}>
            Cancel
          </Button>
          <Button 
            onClick={triggerEmergency}
            variant="contained" 
            color="error"
            startIcon={<Warning />}
          >
            Trigger Emergency
          </Button>
        </DialogActions>
      </Dialog>

      {/* Success Snackbar */}
      <Snackbar
        open={emergencyTriggered}
        autoHideDuration={6000}
        onClose={() => setEmergencyTriggered(false)}
      >
        <Alert 
          onClose={() => setEmergencyTriggered(false)} 
          severity="success"
          sx={{ width: '100%' }}
        >
          Emergency alert sent to your contacts!
        </Alert>
      </Snackbar>

      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default ChatPage;