// Mock Leaflet object
const L = {
  // Mock icon function
  icon: jest.fn(() => ({
    iconUrl: 'mock-icon.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
  })),

  // Mock Icon object with Default property
  Icon: {
    Default: {
      mergeOptions: jest.fn(),
      prototype: {
        _getIconUrl: jest.fn(() => 'mock-icon-url'),
      },
    },
  },

  // Mock divIcon function
  divIcon: jest.fn(() => ({
    className: 'mock-div-icon',
    iconSize: [20, 20],
  })),

  // Mock latLng function
  latLng: jest.fn((lat, lng) => ({ lat, lng })),

  // Mock latLngBounds function
  latLngBounds: jest.fn(() => ({
    extend: jest.fn(),
    isValid: jest.fn(() => true),
    getNorthEast: jest.fn(() => ({ lat: 0, lng: 0 })),
    getSouthWest: jest.fn(() => ({ lat: 0, lng: 0 })),
  })),

  // Mock map methods
  map: jest.fn(() => ({
    setView: jest.fn(),
    panTo: jest.fn(),
    getZoom: jest.fn(() => 13),
    getCenter: jest.fn(() => ({ lat: 0, lng: 0 })),
    on: jest.fn(),
    off: jest.fn(),
    addLayer: jest.fn(),
    removeLayer: jest.fn(),
  })),

  // Mock marker function
  marker: jest.fn(() => ({
    addTo: jest.fn(),
    setLatLng: jest.fn(),
    getLatLng: jest.fn(() => ({ lat: 0, lng: 0 })),
    bindPopup: jest.fn(),
    openPopup: jest.fn(),
  })),

  // Mock polyline function
  polyline: jest.fn(() => ({
    addTo: jest.fn(),
    setStyle: jest.fn(),
    getBounds: jest.fn(),
  })),

  // Mock control object
  control: {
    layers: jest.fn(() => ({
      addTo: jest.fn(),
    })),
  },

  // Mock tileLayer function
  tileLayer: jest.fn(() => ({
    addTo: jest.fn(),
  })),
};

export default L; 