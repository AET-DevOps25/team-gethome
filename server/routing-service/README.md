# Routing Service

This service handles route planning and location flagging for the GetHome application.

## Port
The service runs on port **8083**.

## Endpoints

### Flag Management

#### POST /api/routes/flags
Flag a location as unsafe.

**Request Body:**
```json
{
  "latitude": 52.520008,
  "longitude": 13.404954,
  "reason": "Construction work blocking the path"
}
```

**Response:**
```json
{
  "id": "flag-id",
  "latitude": 52.520008,
  "longitude": 13.404954,
  "reason": "Construction work blocking the path",
  "timestamp": "2025-07-06T10:30:00",
  "reportedBy": "user-id"
}
```

#### GET /api/routes/flags
Get all flags within a specified distance from a location.

**Query Parameters:**
- `lat` (double): Latitude of the center point
- `lon` (double): Longitude of the center point  
- `distance` (double): Maximum distance in meters

**Example:** `/api/routes/flags?lat=52.520008&lon=13.404954&distance=1000`

**Response:**
```json
[
  {
    "id": "flag-id",
    "latitude": 52.520008,
    "longitude": 13.404954,
    "reason": "Construction work",
    "timestamp": "2025-07-06T10:30:00",
    "reportedBy": "user-id"
  }
]
```

### Route Planning

#### POST /api/routes/plan
Plan a route from start to end point, avoiding flagged locations.

**Request Body:**
```json
{
  "startLatitude": 52.520008,
  "startLongitude": 13.404954,
  "endLatitude": 52.530008,
  "endLongitude": 13.414954,
  "journeyId": "optional-journey-id",
  "avoidanceRadiusMeters": 500
}
```

**Response:**
```json
{
  "id": "route-id",
  "userId": "user-id",
  "journeyId": "journey-id",
  "startLatitude": 52.520008,
  "startLongitude": 13.404954,
  "endLatitude": 52.530008,
  "endLongitude": 13.414954,
  "routePoints": [
    {
      "latitude": 52.520008,
      "longitude": 13.404954,
      "sequenceNumber": 0
    }
  ],
  "createdAt": "2025-07-06T10:30:00",
  "updatedAt": "2025-07-06T10:30:00",
  "status": "PLANNED",
  "estimatedDurationMinutes": 15,
  "estimatedDistanceKm": 1.2
}
```

#### PUT /api/routes/replan
Replan an existing route, avoiding newly reported flags.

**Request Body:**
```json
{
  "journeyId": "journey-id",
  "avoidanceRadiusMeters": 500
}
```

**Response:** Same as route planning response.

#### GET /api/routes/{journeyId}
Get an existing route by journey ID.

**Response:** Same as route planning response.

## Authentication

All endpoints require authentication via Bearer token in the Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Database

The service uses MongoDB with the following collections:
- `flags`: Stores location flags with coordinates, reason, and timestamp
- `routes`: Stores planned routes with waypoints and metadata

## Configuration

Key configuration properties in `application.properties`:
- `server.port=8083`
- `spring.data.mongodb.database=routing`
- `jwt.secret` and `jwt.expiration` for token validation

## Health Check

GET `/health` - Returns service status and version information.
