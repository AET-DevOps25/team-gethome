import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import BottomTabBar from '../../components/BottomTabBar';

L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const fallbackLocation: [number, number] = [40.7580, -73.9855]; // Times Square, NY

// Helper component to update map center when position changes
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
      },
      (err) => {
        setPosition(fallbackLocation);
        if (err.code === 1) setError('Location permission denied.');
        else if (err.code === 2) setError('Location unavailable. Try again later.');
        else if (err.code === 3) setError('Location request timed out.');
        else setError('Unable to retrieve your location.');
        setLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );

    // Cleanup on unmount
    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, []);

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <div className="flex-1 flex flex-col items-center px-4 pb-20 pt-4 w-full">
        <h1 className="text-xl font-bold mb-2">Your Location</h1>

        {error && <p className="text-red-500 text-center mb-2">{error}</p>}

        {loading ? (
          <p className="text-gray-600 text-center">Getting your location...</p>
        ) : position ? (
          <MapContainer
            center={position}
            zoom={15}
            className="flex-1"
            style={{ width: '100%', borderRadius: 12 }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution="&copy; OpenStreetMap contributors"
            />
            <Marker position={position}>
              <Popup>
                {error ? 'Using fallback location' : 'You are here'}
              </Popup>
            </Marker>
            <RecenterMap position={position} />
          </MapContainer>
        ) : (
          <p className="text-gray-600 text-center">No position available</p>
        )}
      </div>

      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default MapPage;
