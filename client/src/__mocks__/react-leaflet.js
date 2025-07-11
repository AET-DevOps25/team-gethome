import React from 'react';

// Mock MapContainer component
export const MapContainer = ({ children, ...props }) => (
  <div data-testid="map-container" {...props}>
    {children}
  </div>
);

// Mock TileLayer component
export const TileLayer = (props) => (
  <div data-testid="tile-layer" {...props} />
);

// Mock Marker component
export const Marker = ({ children, ...props }) => (
  <div data-testid="marker" {...props}>
    {children}
  </div>
);

// Mock Popup component
export const Popup = ({ children, ...props }) => (
  <div data-testid="popup" {...props}>
    {children}
  </div>
);

// Mock Polyline component
export const Polyline = (props) => (
  <div data-testid="polyline" {...props} />
);

// Mock useMap hook
export const useMap = () => ({
  setView: jest.fn(),
  panTo: jest.fn(),
  getZoom: jest.fn(() => 13),
  getCenter: jest.fn(() => ({ lat: 0, lng: 0 })),
  on: jest.fn(),
  off: jest.fn(),
  addLayer: jest.fn(),
  removeLayer: jest.fn(),
});

// Mock useMapEvents hook
export const useMapEvents = (events) => {
  return {
    setView: jest.fn(),
    panTo: jest.fn(),
    getZoom: jest.fn(() => 13),
    getCenter: jest.fn(() => ({ lat: 0, lng: 0 })),
  };
}; 