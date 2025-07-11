import React from 'react';

interface ManMarkerIconProps {
  size?: number;
  isActive?: boolean;
  accuracy?: number;
}

const ManMarkerIcon: React.FC<ManMarkerIconProps> = ({ 
  size = 32, 
  isActive = true, 
  accuracy 
}) => {
  const pulseAnimation = isActive ? `
    @keyframes pulse {
      0% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.1); opacity: 0.7; }
      100% { transform: scale(1); opacity: 1; }
    }
    .pulse { animation: pulse 2s infinite; }
  ` : '';

  const accuracyColor = accuracy && accuracy > 0 ? 
    (accuracy < 10 ? '#4CAF50' : accuracy < 50 ? '#FF9800' : '#F44336') : 
    '#1976d2';

  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <style>{pulseAnimation}</style>
      
      {/* Accuracy Circle */}
      {accuracy && accuracy > 0 && (
        <svg 
          width={size + 20} 
          height={size + 20} 
          viewBox={`0 0 ${size + 20} ${size + 20}`} 
          style={{ position: 'absolute', top: -10, left: -10 }}
        >
          <circle 
            cx={(size + 20) / 2} 
            cy={(size + 20) / 2} 
            r={Math.min((size / 2) + 8, 20)} 
            stroke={accuracyColor} 
            strokeWidth="2" 
            fill="none" 
            opacity="0.3"
            strokeDasharray="5,5"
          />
        </svg>
      )}
      
      {/* Main Icon */}
      <svg 
        width={size} 
        height={size} 
        viewBox="0 0 32 32" 
        fill="none" 
        xmlns="http://www.w3.org/2000/svg"
        className={isActive ? 'pulse' : ''}
      >
        {/* Background circle for better visibility */}
        <circle cx="16" cy="16" r="15" fill="white" stroke={accuracyColor} strokeWidth="2" opacity="0.9" />
        
        {/* Head */}
        <circle cx="16" cy="10" r="3" stroke={accuracyColor} strokeWidth="2" fill="white" />
        
        {/* Body */}
        <rect x="14" y="13" width="4" height="8" rx="2" stroke={accuracyColor} strokeWidth="2" fill="white" />
        
        {/* Legs */}
        <line x1="16" y1="21" x2="16" y2="26" stroke={accuracyColor} strokeWidth="2" />
        <line x1="14" y1="26" x2="16" y2="21" stroke={accuracyColor} strokeWidth="2" />
        <line x1="18" y1="26" x2="16" y2="21" stroke={accuracyColor} strokeWidth="2" />
        
        {/* Arms */}
        <line x1="12" y1="16" x2="14" y2="15" stroke={accuracyColor} strokeWidth="2" />
        <line x1="20" y1="16" x2="18" y2="15" stroke={accuracyColor} strokeWidth="2" />
        
        {/* Active indicator dot */}
        {isActive && (
          <circle cx="16" cy="16" r="2" fill={accuracyColor} opacity="0.8" />
        )}
      </svg>
      
      {/* Status indicator */}
      <div style={{
        position: 'absolute',
        top: -2,
        right: -2,
        width: 8,
        height: 8,
        borderRadius: '50%',
        backgroundColor: isActive ? '#4CAF50' : '#F44336',
        border: '2px solid white',
        boxShadow: '0 1px 3px rgba(0,0,0,0.3)'
      }} />
    </div>
  );
};

export default ManMarkerIcon; 