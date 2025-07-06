# Backend TODO - Navigation System

## Overview
This document outlines the backend requirements for the updated bottom navigation system with the new tab structure and profile account switcher functionality.

## Tab Structure
- 📅 Calendar (existing)
- 🧑‍🤝‍🧑 Players (new)
- 📘 Team Profile (new)
- 👥 Profile with Account Switcher (new)

---

## 1. Players Tab Backend

### Database Schema
```sql
-- Players table
CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id),
    user_id UUID REFERENCES users(id), -- Link to user account
    name VARCHAR(100) NOT NULL,
    position VARCHAR(50) NOT NULL,
    jersey_number VARCHAR(10),
    avatar_url TEXT,
    email VARCHAR(255),
    phone VARCHAR(20),
    date_of_birth DATE,
    joined_date DATE DEFAULT CURRENT_DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Player statistics table
CREATE TABLE player_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id UUID NOT NULL REFERENCES players(id),
    season_id UUID REFERENCES seasons(id),
    goals INTEGER DEFAULT 0,
    assists INTEGER DEFAULT 0,
    matches_played INTEGER DEFAULT 0,
    yellow_cards INTEGER DEFAULT 0,
    red_cards INTEGER DEFAULT 0,
    clean_sheets INTEGER DEFAULT 0,
    minutes_played INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### API Endpoints

#### GET /api/teams/{teamId}/players
**Purpose**: Fetch all players for a team
**Request**:
```json
{
    "teamId": "string",
    "includeInactive": "boolean (optional, default: false)",
    "position": "string (optional, filter by position)"
}
```
**Response**:
```json
{
    "success": true,
    "data": {
        "players": [
            {
                "id": "string",
                "name": "string",
                "position": "string",
                "jerseyNumber": "string",
                "avatar": "string|null",
                "isActive": "boolean",
                "stats": {
                    "goals": "number",
                    "assists": "number",
                    "matches": "number",
                    "yellowCards": "number",
                    "redCards": "number",
                    "cleanSheets": "number",
                    "minutesPlayed": "number"
                },
                "email": "string|null",
                "phone": "string|null",
                "dateOfBirth": "string|null",
                "joinedDate": "string"
            }
        ],
        "total": "number",
        "page": "number",
        "limit": "number"
    }
}
```
**Error Handling**:
```json
{
    "success": false,
    "error": {
        "code": "TEAM_NOT_FOUND|UNAUTHORIZED|INVALID_TEAM_ID",
        "message": "string"
    }
}
```

#### GET /api/players/{playerId}
**Purpose**: Fetch detailed player information
**Response**:
```json
{
    "success": true,
    "data": {
        "player": {
            "id": "string",
            "name": "string",
            "position": "string",
            "jerseyNumber": "string",
            "avatar": "string|null",
            "isActive": "boolean",
            "stats": "PlayerStats",
            "personalInfo": {
                "email": "string",
                "phone": "string",
                "dateOfBirth": "string",
                "joinedDate": "string"
            },
            "team": {
                "id": "string",
                "name": "string"
            }
        }
    }
}
```

### Business Logic
- **Authorization**: Only team members can view player lists
- **Data Filtering**: Support filtering by position, active status
- **Statistics Calculation**: Real-time calculation of player stats
- **Avatar Management**: Handle avatar upload, storage, and CDN delivery

---

## 2. Team Profile Tab Backend

### Database Schema
```sql
-- Team statistics table
CREATE TABLE team_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id),
    season_id UUID REFERENCES seasons(id),
    total_matches INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    draws INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    goals_scored INTEGER DEFAULT 0,
    goals_conceded INTEGER DEFAULT 0,
    points INTEGER DEFAULT 0,
    clean_sheets INTEGER DEFAULT 0,
    yellow_cards INTEGER DEFAULT 0,
    red_cards INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Enhanced teams table
ALTER TABLE teams ADD COLUMN founded_year INTEGER;
ALTER TABLE teams ADD COLUMN home_ground VARCHAR(255);
ALTER TABLE teams ADD COLUMN coach_name VARCHAR(100);
ALTER TABLE teams ADD COLUMN captain_id UUID REFERENCES players(id);
```

### API Endpoints

#### GET /api/teams/{teamId}/profile
**Purpose**: Fetch comprehensive team profile and statistics
**Response**:
```json
{
    "success": true,
    "data": {
        "team": {
            "id": "string",
            "name": "string",
            "description": "string",
            "logo": "string|null",
            "founded": "string",
            "homeGround": "string",
            "coach": "string",
            "captain": {
                "id": "string",
                "name": "string"
            }
        },
        "stats": {
            "totalMatches": "number",
            "wins": "number",
            "draws": "number",
            "losses": "number",
            "goalsScored": "number",
            "goalsConceded": "number",
            "points": "number",
            "cleanSheets": "number",
            "yellowCards": "number",
            "redCards": "number"
        },
        "season": {
            "id": "string",
            "name": "string",
            "startDate": "string",
            "endDate": "string"
        }
    }
}
```

#### PUT /api/teams/{teamId}/profile
**Purpose**: Update team profile information
**Request**:
```json
{
    "name": "string (optional)",
    "description": "string (optional)",
    "homeGround": "string (optional)",
    "coachName": "string (optional)"
}
```
**Authorization**: Only team admin/coach can update

### Business Logic
- **Statistics Calculation**: Real-time calculation from match data
- **Season Management**: Support for multiple seasons
- **Role-based Access**: Different permissions for players vs coaches
- **Logo Management**: Handle team logo upload and storage

---

## 3. Profile Tab with Account Switcher Backend

### Database Schema
```sql
-- User accounts table (for multi-account support)
CREATE TABLE user_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    role VARCHAR(50) NOT NULL, -- 'Player', 'Coach', 'Admin'
    is_active BOOLEAN DEFAULT false,
    joined_date TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, team_id)
);

-- User sessions table
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    active_account_id UUID REFERENCES user_accounts(id),
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### API Endpoints

#### GET /api/users/profile
**Purpose**: Fetch current user profile with active account
**Response**:
```json
{
    "success": true,
    "data": {
        "user": {
            "id": "string",
            "name": "string",
            "email": "string",
            "avatar": "string|null"
        },
        "activeAccount": {
            "id": "string",
            "teamId": "string",
            "teamName": "string",
            "role": "string",
            "isActive": "boolean"
        }
    }
}
```

#### GET /api/users/available-accounts
**Purpose**: Fetch all accounts available to the user
**Response**:
```json
{
    "success": true,
    "data": {
        "accounts": [
            {
                "id": "string",
                "name": "string",
                "email": "string",
                "avatar": "string|null",
                "teamName": "string",
                "role": "string",
                "isActive": "boolean"
            }
        ]
    }
}
```

#### POST /api/users/switch-account
**Purpose**: Switch to a different account
**Request**:
```json
{
    "targetAccountId": "string"
}
```
**Response**:
```json
{
    "success": true,
    "data": {
        "newToken": "string",
        "activeAccount": {
            "id": "string",
            "teamId": "string",
            "teamName": "string",
            "role": "string"
        }
    }
}
```

#### PUT /api/users/profile
**Purpose**: Update user profile information
**Request**:
```json
{
    "name": "string (optional)",
    "email": "string (optional)",
    "avatar": "file (optional)"
}
```

### Business Logic
- **Multi-Account Support**: Users can belong to multiple teams
- **Session Management**: Handle account switching with new tokens
- **Authorization**: Validate account access permissions
- **Avatar Management**: Handle profile picture upload and storage

---

## 4. Navigation Analytics

### Database Schema
```sql
-- Navigation analytics table
CREATE TABLE navigation_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    session_id UUID REFERENCES user_sessions(id),
    tab_name VARCHAR(50) NOT NULL, -- 'calendar', 'players', 'team_profile', 'profile'
    action_type VARCHAR(50) NOT NULL, -- 'view', 'switch_account', 'navigate'
    timestamp TIMESTAMP DEFAULT NOW(),
    metadata JSONB -- Additional context data
);
```

### API Endpoints

#### POST /api/analytics/navigation
**Purpose**: Log navigation events
**Request**:
```json
{
    "tabName": "string",
    "actionType": "string",
    "metadata": {
        "previousTab": "string (optional)",
        "accountId": "string (optional)",
        "duration": "number (optional)"
    }
}
```

### Business Logic
- **Event Tracking**: Log all navigation interactions
- **Performance Monitoring**: Track tab load times
- **User Behavior Analysis**: Analyze navigation patterns
- **Privacy Compliance**: Ensure GDPR compliance for analytics

---

## 5. Error Handling & Security

### Error Codes
- `TEAM_NOT_FOUND`: Team doesn't exist or user doesn't have access
- `PLAYER_NOT_FOUND`: Player doesn't exist
- `UNAUTHORIZED`: User doesn't have permission
- `INVALID_ACCOUNT_SWITCH`: Cannot switch to specified account
- `SESSION_EXPIRED`: User session has expired
- `RATE_LIMIT_EXCEEDED`: Too many requests

### Security Measures
- **JWT Token Validation**: Validate tokens on all requests
- **Role-based Access Control**: Enforce permissions based on user role
- **Input Validation**: Sanitize all user inputs
- **Rate Limiting**: Prevent abuse with rate limiting
- **CORS Configuration**: Proper CORS setup for mobile app

### Performance Optimization
- **Database Indexing**: Index on frequently queried fields
- **Caching**: Cache team and player data
- **Pagination**: Implement pagination for large datasets
- **CDN Integration**: Use CDN for avatar and logo delivery

---

## 6. Testing Requirements

### Unit Tests
- Test all API endpoints with valid and invalid inputs
- Test authorization logic for different user roles
- Test account switching functionality
- Test error handling scenarios

### Integration Tests
- Test complete user flows (login → navigation → account switch)
- Test data consistency across related tables
- Test performance under load

### Security Tests
- Test authentication and authorization
- Test input validation and sanitization
- Test rate limiting functionality

---

## 7. Deployment Considerations

### Environment Setup
- **Development**: Local development environment
- **Staging**: Pre-production testing environment
- **Production**: Live production environment

### Database Migrations
- Create migration scripts for new tables
- Handle data migration for existing users
- Backup strategy before deployment

### Monitoring
- **Application Monitoring**: Track API performance
- **Database Monitoring**: Monitor query performance
- **Error Tracking**: Log and alert on errors
- **User Analytics**: Track feature usage

---

## 8. Future Enhancements

### Planned Features
- **Real-time Updates**: WebSocket integration for live data
- **Push Notifications**: Notify users of important events
- **Offline Support**: Cache data for offline viewing
- **Advanced Analytics**: Detailed user behavior insights
- **Multi-language Support**: Internationalization
- **Dark Mode**: Theme support

### Technical Debt
- **Code Refactoring**: Improve code organization
- **Performance Optimization**: Optimize slow queries
- **Security Hardening**: Additional security measures
- **Documentation**: Comprehensive API documentation 