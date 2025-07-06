import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap, Polyline } from 'react-leaflet';
import { AlertTriangle, Navigation, Route, MapPin, Search, Shield, Zap } from 'lucide-react';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import BottomTabBar from '../../components/BottomTabBar';
import { useNavigate } from 'react-router-dom';
import { routingService } from '../../services/routingService';
import {
    Box,
    Card,
    CardContent,
    Typography,
    Button,
    TextField,
    Alert,
    Chip,
    CircularProgress,
    Fab,
    Snackbar,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Divider
} from '@mui/material';
import {
    LocationOn,
    Search as SearchIcon,
    Route as RouteIcon,
    Warning,
    CheckCircle,
    Info,
    Security
} from '@mui/icons-material';

L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const fallbackLocation: [number, number] = [40.7580, -73.9855]; // Times Square, NY

const RecenterMap = ({ position }: { position: [number, number] }) => {
  const map = useMap();
  useEffect(() => {
    map.setView(position);
  }, [position, map]);
  return null;
};

const MapPage: React.FC = () => {
  const [position, setPosition] = useState<[number, number] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState(1);
  const [search, setSearch] = useState('');
  const [searchResult, setSearchResult] = useState<[number, number] | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [route, setRoute] = useState<any>(null);
  const [routeLoading, setRouteLoading] = useState(false);
  const [dangerZones, setDangerZones] = useState<any[]>([]);
  const [showEmergencyDialog, setShowEmergencyDialog] = useState(false);
  const [emergencyTriggered, setEmergencyTriggered] = useState(false);
  const [safetyTips, setSafetyTips] = useState<string[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser.');
      setPosition(fallbackLocation);
      setLoading(false);
      return;
    }

    setError(null);
    setLoading(true);

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        setPosition([pos.coords.latitude, pos.coords.longitude]);
        setError(null);
        setLoading(false);
        
        // Load nearby danger zones when position is available
        if (pos.coords.latitude && pos.coords.longitude) {
          loadNearbyDangerZones(pos.coords.latitude, pos.coords.longitude);
          generateSafetyTips(pos.coords.latitude, pos.coords.longitude);
        }
      },
      (err) => {
        setPosition(fallbackLocation);
        if(search === ""){
          if (err.code === 1) setError('Location permission denied.');
          else if (err.code === 2) setError('Location unavailable. Try again later.');
          else if (err.code === 3) setError('');
        }
        else setError('Unable to retrieve your location.');
        setLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );

    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, []);

  const loadNearbyDangerZones = async (lat: number, lng: number) => {
    try {
      const zones = await routingService.getNearbyDangerZones(lat, lng, 1000);
      setDangerZones(zones);
    } catch (error) {
      console.error('Failed to load danger zones:', error);
    }
  };

  const generateSafetyTips = (lat: number, lng: number) => {
    const tips = [
      "Stay in well-lit areas when possible",
      "Keep your phone charged and accessible",
      "Share your location with trusted contacts",
      "Trust your instincts - if something feels wrong, change your route",
      "Consider using the AI companion for additional safety"
    ];
    setSafetyTips(tips);
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setSearchError(null);
    setSearchResult(null);
    if (!search.trim()) return;
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(search)}`
      );
      const data = await res.json();
      if (data && data.length > 0) {
        setSearchResult([parseFloat(data[0].lat), parseFloat(data[0].lon)]);
      } else {
        setSearchError('Location not found.');
      }
    } catch {
      setSearchError('Failed to search location.');
    }
  };

  const handlePlanRoute = async () => {
    if (!position || !searchResult) {
      alert('Please set both start and destination locations');
      return;
    }

    setRouteLoading(true);
    try {
      const routeData = await routingService.planRoute(
        position[0],
        position[1],
        searchResult[0],
        searchResult[1],
        true // Avoid danger zones
      );
      setRoute(routeData);
    } catch (error: any) {
      console.error('Failed to plan route:', error);
      alert('Failed to plan route: ' + (error?.message || 'Unknown error'));
    } finally {
      setRouteLoading(false);
    }
  };

  const handleEmergencyTrigger = async () => {
    if (!position) {
      alert('Location is required for emergency trigger');
      return;
    }

    try {
      await routingService.triggerEmergency(
        position[0],
        position[1],
        'Current location',
        'Manual emergency trigger from map',
        undefined
      );
      setEmergencyTriggered(true);
      setShowEmergencyDialog(false);
    } catch (error: any) {
      console.error('Failed to trigger emergency:', error);
      alert('Failed to trigger emergency: ' + (error?.message || 'Unknown error'));
    }
  };

  const getRouteCoordinates = (routeData: any): [number, number][] => {
    if (!routeData?.segments) return [];
    return routeData.segments.flatMap((segment: any) => 
      segment.coordinates?.map((coord: any) => [coord.latitude, coord.longitude]) || []
    );
  };

  const getSafetyScore = () => {
    if (dangerZones.length === 0) return { score: 95, level: 'Excellent', color: 'success' };
    if (dangerZones.length <= 2) return { score: 75, level: 'Good', color: 'warning' };
    return { score: 50, level: 'Caution', color: 'error' };
  };

  const safetyScore = getSafetyScore();

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      {/* Header */}
      <Box sx={{ bgcolor: 'primary.main', color: 'white', p: 2 }}>
        <Typography variant="h6" align="center">
          Safe Route Planning
        </Typography>
      </Box>

      {/* Safety Status Card */}
      <Box sx={{ px: 2, py: 1 }}>
        <Card>
          <CardContent sx={{ py: 2 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between">
              <Box display="flex" alignItems="center">
                <Security sx={{ mr: 1, color: `${safetyScore.color}.main` }} />
                <Typography variant="body1" fontWeight="bold">
                  Area Safety Score
                </Typography>
              </Box>
              <Chip 
                label={`${safetyScore.score}% - ${safetyScore.level}`}
                color={safetyScore.color as any}
                variant="filled"
              />
            </Box>
            {dangerZones.length > 0 && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {dangerZones.length} danger zone{dangerZones.length > 1 ? 's' : ''} nearby
              </Typography>
            )}
          </CardContent>
        </Card>
      </Box>

      {/* Search Bar */}
      <Box sx={{ px: 2, py: 1 }}>
        <Card>
          <CardContent sx={{ py: 2 }}>
            <form onSubmit={handleSearch}>
              <Box display="flex" gap={1}>
                <TextField
                  fullWidth
                  size="small"
                  placeholder="Search for destination..."
                  value={search}
                  onChange={(e) => {
                    setSearch(e.target.value);
                    if (searchError) setSearchError(null);
                  }}
                  InputProps={{
                    startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />
                  }}
                />
                <Button
                  type="submit"
                  variant="contained"
                  disabled={!search.trim()}
                >
                  Search
                </Button>
              </Box>
            </form>
            {searchError && (
              <Alert severity="error" sx={{ mt: 1 }}>
                {searchError}
              </Alert>
            )}
          </CardContent>
        </Card>
      </Box>

      {/* Route Planning Button */}
      {position && searchResult && (
        <Box sx={{ px: 2, py: 1 }}>
          <Button
            fullWidth
            variant="contained"
            onClick={handlePlanRoute}
            disabled={routeLoading}
            startIcon={routeLoading ? <CircularProgress size={20} /> : <RouteIcon />}
            sx={{ py: 1.5 }}
          >
            {routeLoading ? 'Planning Safe Route...' : 'Plan Safe Route'}
          </Button>
        </Box>
      )}

      {/* Map Container */}
      <Box sx={{ flex: 1, px: 2, py: 1 }}>
        {error && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="300px">
            <CircularProgress />
          </Box>
        ) : (position || searchResult) ? (
          <Card sx={{ height: '400px', overflow: 'hidden' }}>
            <MapContainer
              center={searchResult || position!}
              zoom={15}
              style={{ height: '100%', width: '100%' }}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
              />
              {position && (
                <Marker position={position}>
                  <Popup>
                    {error ? 'Using fallback location' : 'You are here'}
                  </Popup>
                </Marker>
              )}
              {searchResult && (
                <Marker position={searchResult}>
                  <Popup>
                    Destination
                  </Popup>
                </Marker>
              )}
              {route && (
                <Polyline
                  positions={getRouteCoordinates(route)}
                  color="blue"
                  weight={3}
                  opacity={0.7}
                />
              )}
              {dangerZones.map((zone, index) => (
                <Marker
                  key={index}
                  position={[zone.latitude, zone.longitude]}
                  icon={L.divIcon({
                    className: 'danger-zone-marker',
                    html: `<div style="background-color: red; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
                    iconSize: [20, 20],
                    iconAnchor: [10, 10]
                  })}
                >
                  <Popup>
                    <div>
                      <strong>Danger Zone</strong><br/>
                      Level: {zone.dangerLevel}<br/>
                      Category: {zone.category}<br/>
                      {zone.description}
                    </div>
                  </Popup>
                </Marker>
              ))}
              <RecenterMap position={searchResult || position!} />
            </MapContainer>
          </Card>
        ) : (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="300px">
            <Typography color="text.secondary">No position available</Typography>
          </Box>
        )}
      </Box>

      {/* Safety Tips */}
      {safetyTips.length > 0 && (
        <Box sx={{ px: 2, py: 1 }}>
          <Card>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="h6" gutterBottom>
                Safety Tips
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
      )}

      {/* Emergency Button */}
      <Fab
        color="error"
        aria-label="emergency"
        sx={{
          position: 'fixed',
          bottom: 80,
          right: 16,
          zIndex: 1000,
        }}
        onClick={() => setShowEmergencyDialog(true)}
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
            <Warning sx={{ mr: 1, color: 'error.main' }} />
            Emergency SOS
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" paragraph>
            Are you sure you want to trigger an emergency alert? This will:
          </Typography>
          <List dense>
            <ListItem>
              <ListItemIcon>
                <Warning color="error" />
              </ListItemIcon>
              <ListItemText primary="Send your location to all emergency contacts" />
            </ListItem>
            <ListItem>
              <ListItemIcon>
                <Warning color="error" />
              </ListItemIcon>
              <ListItemText primary="Send immediate notifications via email and SMS" />
            </ListItem>
            <ListItem>
              <ListItemIcon>
                <Warning color="error" />
              </ListItemIcon>
              <ListItemText primary="Activate emergency response protocols" />
            </ListItem>
          </List>
          <Alert severity="warning" sx={{ mt: 2 }}>
            Only use this feature in genuine emergency situations.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowEmergencyDialog(false)}>
            Cancel
          </Button>
          <Button 
            onClick={handleEmergencyTrigger}
            variant="contained" 
            color="error"
            startIcon={<AlertTriangle />}
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
          Emergency notification sent to your contacts!
        </Alert>
      </Snackbar>

      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default MapPage;