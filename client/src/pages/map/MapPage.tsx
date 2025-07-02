import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { AlertTriangle } from 'lucide-react';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import BottomTabBar from '../../components/BottomTabBar';
import { useNavigate } from 'react-router-dom';

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

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <div className="flex-1 flex flex-col items-center px-4 pb-20 pt-4 w-full">
        <h1 className="text-xl font-bold mb-2">Your Location</h1>

        {/* Search Bar */}
        <form onSubmit={handleSearch} className="flex w-full max-w-md mb-4 gap-2">
          <input
            type="text"
            className="border rounded px-3 py-2 flex-1"
            placeholder="Search for a location..."
            value={search}
            onChange={e => {
              setSearch(e.target.value);
              if (searchError) setSearchError(null); // Clear error when user types
            }}
          />
          <button
            type="submit"
            className="bg-indigo-600 text-white px-4 py-2 rounded font-semibold"
          >
            Search
          </button>
        </form>
        {searchError && <div className="text-red-500 mb-2">{searchError}</div>}

        {error && <p className="text-red-500 text-center mb-2">{error}</p>}

        {loading ? (
          <p className="text-gray-600 text-center">Getting your location...</p>
        ) : (position || searchResult) ? (
          <MapContainer
            center={searchResult || position!}
            zoom={15}
            className="flex-1"
            style={{ width: '100%', borderRadius: 12, minHeight: 350, maxHeight: 500 }}
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
                  Search Result
                </Popup>
              </Marker>
            )}
            <RecenterMap position={searchResult || position!} />
          </MapContainer>
        ) : (
          <p className="text-gray-600 text-center">No position available</p>
        )}

        {/* Emergency Button */}
        <button
          className="fixed bottom-24 right-6 bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-6 rounded-full shadow-lg z-[1000]"
          onClick={() => navigate('/reports')}
        >
          <AlertTriangle className="h-7 w-7" />
        </button>
      </div>

      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default MapPage;