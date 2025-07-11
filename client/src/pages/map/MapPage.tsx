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
    Divider,
    Tooltip
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
import ManMarkerIcon from '../../components/ManMarkerIcon';
import { useMapEvent } from 'react-leaflet';

// Add global styles for better map markers
const globalStyles = `
  .man-marker {
    background: transparent !important;
    border: none !important;
  }
  
  .leaflet-marker-icon.man-marker {
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
  }
  
  .leaflet-div-icon {
    background: transparent !important;
    border: none !important;
  }
`;

// Inject styles
if (typeof document !== 'undefined') {
  const styleTag = document.createElement('style');
  styleTag.textContent = globalStyles;
  if (!document.head.querySelector('style[data-map-styles]')) {
    styleTag.setAttribute('data-map-styles', 'true');
    document.head.appendChild(styleTag);
  }
}

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
  const [addDangerMode, setAddDangerMode] = useState(false);
  const [newDangerLocation, setNewDangerLocation] = useState<[number, number] | null>(null);
  const [dangerDialogOpen, setDangerDialogOpen] = useState(false);
  const [dangerForm, setDangerForm] = useState({ name: '', description: '', dangerLevel: 'MEDIUM', tags: '' });
  const [dangerSubmitting, setDangerSubmitting] = useState(false);
  const [locationAccuracy, setLocationAccuracy] = useState<number | null>(null);
  const [isLocationActive, setIsLocationActive] = useState(false);
  const [lastLocationUpdate, setLastLocationUpdate] = useState<Date | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser.');
      setPosition(fallbackLocation);
      setLoading(false);
      setIsLocationActive(false);
      return;
    }

    setError(null);
    setLoading(true);
    setIsLocationActive(true);

    // Enhanced geolocation options for better tracking
    const geoOptions = {
      enableHighAccuracy: true,
      timeout: 10000, // 10 second timeout for faster response
      maximumAge: 60000, // Cache for 1 minute to improve performance
    };

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        console.log('Location update received:', {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          accuracy: pos.coords.accuracy,
          timestamp: new Date(pos.timestamp)
        });

        const newPosition: [number, number] = [pos.coords.latitude, pos.coords.longitude];
        
        // Only update if accuracy is reasonable (within 100m) or it's been more than 2 minutes
        const now = new Date();
        const shouldUpdate = !lastLocationUpdate || 
                           (now.getTime() - lastLocationUpdate.getTime()) > 120000 || // 2 minutes
                           pos.coords.accuracy < 100; // Good accuracy

        // Always clear loading state when we get any position response
        setLoading(false);
        setIsLocationActive(true);
        
        if (shouldUpdate) {
          setPosition(newPosition);
          setLocationAccuracy(pos.coords.accuracy);
          setLastLocationUpdate(now);
          setError(null);
          
          // Load nearby danger zones when position is available
          loadNearbyDangerZones(pos.coords.latitude, pos.coords.longitude);
          generateSafetyTips(pos.coords.latitude, pos.coords.longitude);
          
          console.log('Position updated with accuracy:', pos.coords.accuracy, 'meters');
        } else {
          // Still update accuracy even if we don't update position
          setLocationAccuracy(pos.coords.accuracy);
          console.log('Position update skipped due to poor accuracy:', pos.coords.accuracy, 'meters');
        }
      },
      (err) => {
        console.error('Geolocation error:', err);
        setIsLocationActive(false);
        
        // Only set fallback position if we don't have any position yet
        if (!position) {
          setPosition(fallbackLocation);
        }
        
        if(search === ""){
          if (err.code === 1) {
            setError('Location permission denied. Enable location access for accurate tracking.');
          } else if (err.code === 2) {
            setError('Location unavailable. Check GPS and try again.');
          } else if (err.code === 3) {
            setError('Location request timed out. Using approximate location...');
            // Set fallback position and continue
            if (!position) {
              setPosition(fallbackLocation);
            }
          }
        } else {
          setError('Unable to retrieve your location.');
        }
        setLoading(false);
      },
      geoOptions
    );

    // Set up periodic location refresh every 5 minutes (only if we have a position)
    const refreshInterval = setInterval(() => {
      if (position) {
        console.log('Periodic location refresh triggered');
        navigator.geolocation.getCurrentPosition(
          (pos) => {
            const newPosition: [number, number] = [pos.coords.latitude, pos.coords.longitude];
            setPosition(newPosition);
            setLocationAccuracy(pos.coords.accuracy);
            setLastLocationUpdate(new Date());
            setIsLocationActive(true);
            console.log('Periodic location update successful');
          },
          (err) => {
            console.warn('Periodic location refresh failed:', err);
          },
          { ...geoOptions, timeout: 5000 } // Shorter timeout for periodic updates
        );
      }
    }, 300000); // 5 minutes

    return () => {
      navigator.geolocation.clearWatch(watchId);
      clearInterval(refreshInterval);
    };
  }, []); // Remove lastLocationUpdate from dependencies to prevent infinite loop

  const loadNearbyDangerZones = async (lat: number, lng: number) => {
    try {
      const radius = 20000; // 20km for robust testing
      console.log('Requesting danger zones with:', { lat, lng, radius });
      const zones = await routingService.getNearbyDangerZones(lat, lng, radius);
      console.log('Danger zones received from backend:', zones);
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
      console.log('Planning route from:', position, 'to:', searchResult);
      const routeData = await routingService.planRoute(
        position[0],
        position[1],
        searchResult[0],
        searchResult[1],
        true // Avoid danger zones
      );
      console.log('Route planning response received:', routeData);
      setRoute(routeData);
      console.log('Route state set:', routeData);
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
    console.log('Route data received:', routeData);
    if (!routeData?.segments) {
      console.log('No segments found in route data');
      return [];
    }
    console.log('Number of segments:', routeData.segments.length);
    
    const coordinates = routeData.segments.flatMap((segment: any) => {
      console.log('Segment:', segment);
      console.log('Segment coordinates:', segment.coordinates);
      return segment.coordinates?.map((coord: any) => {
        console.log('Coordinate:', coord);
        return [coord.latitude, coord.longitude];
      }) || [];
    });
    
    console.log('Final coordinates array:', coordinates);
    return coordinates;
  };

  const getSafetyScore = () => {
    if (dangerZones.length === 0) return { score: 95, level: 'Excellent', color: 'success' };
    if (dangerZones.length <= 2) return { score: 75, level: 'Good', color: 'warning' };
    return { score: 50, level: 'Caution', color: 'error' };
  };

  const safetyScore = getSafetyScore();

  function DangerZoneMapClickHandler() {
    useMapEvent('click', (e) => {
      if (addDangerMode) {
        setNewDangerLocation([e.latlng.lat, e.latlng.lng]);
        setDangerDialogOpen(true);
      }
    });
    return null;
  }

  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.');
      return;
    }

    setLoading(true);
    setIsLocationActive(true);

    // Force a fresh location update
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        console.log('Manual location update successful:', {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          accuracy: pos.coords.accuracy
        });

        const newPosition: [number, number] = [pos.coords.latitude, pos.coords.longitude];
        setPosition(newPosition);
        setLocationAccuracy(pos.coords.accuracy);
        setLastLocationUpdate(new Date());
        setError(null);
        setLoading(false);
        setIsLocationActive(true);
        
        // Load nearby danger zones for the new position
        loadNearbyDangerZones(pos.coords.latitude, pos.coords.longitude);
        generateSafetyTips(pos.coords.latitude, pos.coords.longitude);
      },
      (err) => {
        console.error('Manual location update failed:', err);
        setLoading(false);
        setIsLocationActive(false);
        
        if (err.code === 1) {
          alert('Location permission denied. Please enable location access in your browser settings.');
        } else if (err.code === 2) {
          alert('Location unavailable. Please check your GPS and network connection.');
        } else if (err.code === 3) {
          alert('Location request timed out. Please try again or move to an area with better GPS signal.');
        } else {
          alert('Unable to retrieve your location. Please check your device settings.');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 0, // Force fresh location for manual requests
      }
    );
  };

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

      {/* Action Buttons */}
      <Box sx={{ px: 2, py: 1 }}>
        {/* Location Status Indicator */}
        {position && (
          <Box mb={2}>
            <Alert 
              severity={isLocationActive ? "success" : "warning"} 
              variant="outlined"
              sx={{ py: 0.5 }}
            >
              <Box display="flex" justifyContent="space-between" alignItems="center" width="100%">
                <Box>
                  <Typography variant="body2" fontWeight="bold">
                    Location: {isLocationActive ? 'Active Tracking' : 'Inactive'}
                  </Typography>
                  {locationAccuracy && (
                    <Typography variant="caption">
                      Accuracy: ±{Math.round(locationAccuracy)}m
                    </Typography>
                  )}
                </Box>
                {lastLocationUpdate && (
                  <Typography variant="caption" color="text.secondary">
                    Updated: {lastLocationUpdate.toLocaleTimeString()}
                  </Typography>
                )}
              </Box>
            </Alert>
          </Box>
        )}

        <Box display="flex" gap={1} mb={2}>
          <Button
            variant={addDangerMode ? 'contained' : 'outlined'}
            color="error"
            onClick={() => setAddDangerMode((v) => !v)}
            sx={{ flex: 1 }}
          >
            {addDangerMode ? 'Cancel Danger Zone' : 'Mark Danger Zone'}
          </Button>
          
          <Tooltip title={
            loading ? "Getting your location..." : 
            isLocationActive ? "Refresh your current location" : 
            "Get your current location"
          }>
            <Button
              variant="outlined"
              color={isLocationActive ? "success" : "primary"}
              onClick={getCurrentLocation}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={16} /> : <LocationOn />}
              sx={{ minWidth: '120px' }}
            >
              {loading ? 'Locating...' : isLocationActive ? 'Refresh' : 'My Location'}
            </Button>
          </Tooltip>
        </Box>
      </Box>

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
              <DangerZoneMapClickHandler />
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
              />
              {position && (
                <Marker
                  position={position}
                  icon={L.divIcon({
                    className: 'man-marker',
                    html: `<div style="transform: translate(-26px, -42px);">${require('react-dom/server').renderToStaticMarkup(
                      <ManMarkerIcon 
                        size={32} 
                        isActive={isLocationActive} 
                        accuracy={locationAccuracy || undefined} 
                      />
                    )}</div>`,
                    iconSize: [52, 52], // Increased size to accommodate accuracy circle
                    iconAnchor: [26, 42]
                  })}
                >
                  <Popup>
                    <div>
                      <strong>Your Location</strong><br/>
                      {locationAccuracy && (
                        <>
                          Accuracy: ±{Math.round(locationAccuracy)}m<br/>
                        </>
                      )}
                      {lastLocationUpdate && (
                        <>
                          Updated: {lastLocationUpdate.toLocaleTimeString()}<br/>
                        </>
                      )}
                      Status: {isLocationActive ? 
                        <span style={{ color: '#4CAF50' }}>🟢 Active</span> : 
                        <span style={{ color: '#F44336' }}>🔴 Inactive</span>
                      }
                    </div>
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
                <>
                  {route.segments?.map((segment: any, index: number) => {
                    const coordinates = segment.coordinates?.map((coord: any) => [coord.latitude, coord.longitude]) || [];
                    
                    
                    return coordinates.length > 0 ? (
                      <Polyline
                        key={index}
                        positions={coordinates}
                        color="blue"
                        weight={4}
                        opacity={0.8}
                      />
                    ) : null;
                  })}
                  
                  {/* Add start and end markers */}
                  {route.startLocation && (
                    <Marker 
                      position={[route.startLocation.latitude, route.startLocation.longitude]}
                      icon={L.divIcon({
                        className: 'start-marker',
                        html: `<div style="background-color: #4CAF50; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
                        iconSize: [20, 20],
                        iconAnchor: [10, 10]
                      })}
                    >
                      <Popup>
                        <div>
                          <strong>Start Point</strong><br/>
                          Safe walking route begins here
                        </div>
                      </Popup>
                    </Marker>
                  )}
                  
                  {route.endLocation && (
                    <Marker 
                      position={[route.endLocation.latitude, route.endLocation.longitude]}
                      icon={L.divIcon({
                        className: 'end-marker',
                        html: `<div style="background-color: #1976D2; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
                        iconSize: [20, 20],
                        iconAnchor: [10, 10]
                      })}
                    >
                      <Popup>
                        <div>
                          <strong>Destination</strong><br/>
                          Safe walking route ends here
                        </div>
                      </Popup>
                    </Marker>
                  )}
                </>
              )}
              {console.log('Rendering danger zones:', dangerZones)}
              {dangerZones.map((zone, index) => (
                <Tooltip
                  key={index}
                  title={<div>
                    <strong>{zone.name}</strong><br/>
                    Level: {zone.dangerLevel}<br/>
                    {zone.description && (<span>Description: {zone.description}<br/></span>)}
                    Reported: {zone.reportedAt ? new Date(zone.reportedAt).toLocaleString() : 'N/A'}<br/>
                    Reports: {zone.reportCount || 1}
                  </div>}
                  arrow
                  placement="top"
                >
                  <span>
                    <Marker
                      position={[zone.location.coordinates[1], zone.location.coordinates[0]]}
                      icon={L.divIcon({
                        className: 'danger-zone-marker',
                        html: `<div style="display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; background: transparent;">`
                          + `<svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <polygon points="14,3 27,25 1,25" fill="#FFB300" stroke="#B71C1C" stroke-width="2"/>
                              <text x="14" y="21" text-anchor="middle" font-size="16" fill="#B71C1C" font-family="Arial" font-weight="bold">!</text>
                            </svg>`
                          + `</div>`,
                        iconSize: [28, 28],
                        iconAnchor: [14, 28]
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
                  </span>
                </Tooltip>
              ))}
              {addDangerMode && newDangerLocation && (
                <Marker position={newDangerLocation} icon={L.divIcon({
                  className: 'danger-zone-marker',
                  html: `<div style="background-color: red; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white;"></div>`
                })}>
                  <Popup>New Danger Zone</Popup>
                </Marker>
              )}
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

      {/* Danger Zone Report Dialog */}
      <Dialog open={dangerDialogOpen} onClose={() => setDangerDialogOpen(false)}>
        <DialogTitle>Report Danger Zone</DialogTitle>
        <DialogContent>
          <TextField label="Name" fullWidth margin="normal" value={dangerForm.name} onChange={e => setDangerForm(f => ({ ...f, name: e.target.value }))} />
          <TextField label="Description" fullWidth margin="normal" value={dangerForm.description} onChange={e => setDangerForm(f => ({ ...f, description: e.target.value }))} />
          <TextField label="Danger Level" select fullWidth margin="normal" value={dangerForm.dangerLevel} onChange={e => setDangerForm(f => ({ ...f, dangerLevel: e.target.value }))} SelectProps={{ native: true }}>
            <option value="LOW">Low</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </TextField>
          <TextField label="Tags (comma separated)" fullWidth margin="normal" value={dangerForm.tags} onChange={e => setDangerForm(f => ({ ...f, tags: e.target.value }))} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDangerDialogOpen(false)}>Cancel</Button>
          <Button disabled={dangerSubmitting} onClick={async () => {
            if (!newDangerLocation) return;
            setDangerSubmitting(true);
            try {
              await routingService.reportDangerZone({
                name: dangerForm.name,
                description: dangerForm.description,
                dangerLevel: dangerForm.dangerLevel,
                location: { latitude: newDangerLocation[0], longitude: newDangerLocation[1] },
                tags: dangerForm.tags.split(',').map(t => t.trim()).filter(Boolean)
              });
              setDangerDialogOpen(false);
              setAddDangerMode(false);
              setNewDangerLocation(null);
              setDangerForm({ name: '', description: '', dangerLevel: 'MEDIUM', tags: '' });
              await loadNearbyDangerZones(position?.[0] || 0, position?.[1] || 0);
            } catch (e) {
              alert('Failed to report danger zone');
            } finally {
              setDangerSubmitting(false);
            }
          }} color="error" variant="contained">Submit</Button>
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