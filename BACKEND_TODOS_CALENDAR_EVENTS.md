# Backend TODOs - Calendar & Events Section

## Overview
This document outlines the backend requirements for the Calendar & Events functionality in the Goal Getters FC app. The frontend is currently implemented with in-memory storage and needs proper backend integration.

## Database Schema

### Events Table
```sql
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('PRACTICE', 'GAME', 'OTHER')),
    date DATE NOT NULL,
    time TIME NOT NULL,
    venue VARCHAR(255) NOT NULL,
    opponent VARCHAR(255),
    description TEXT,
    team_id UUID NOT NULL REFERENCES teams(id),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern JSONB, -- For recurring events
    is_cancelled BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancelled_by UUID REFERENCES users(id)
);

-- Indexes for performance
CREATE INDEX idx_events_team_date ON events(team_id, date);
CREATE INDEX idx_events_created_by ON events(created_by);
CREATE INDEX idx_events_type ON events(type);
```

### Event Participants Table (for RSVPs)
```sql
CREATE TABLE event_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'DECLINED', 'MAYBE')),
    response_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    UNIQUE(event_id, user_id)
);
```

## API Endpoints

### 1. Events CRUD Operations

#### GET /api/events
**Purpose**: Fetch events for a team with filtering options
**Query Parameters**:
- `team_id` (required): UUID of the team
- `start_date` (optional): Start date for range (YYYY-MM-DD)
- `end_date` (optional): End date for range (YYYY-MM-DD)
- `type` (optional): Filter by event type (PRACTICE, GAME, OTHER)
- `limit` (optional): Number of events to return (default: 50)
- `offset` (optional): Pagination offset (default: 0)

**Response**:
```json
{
  "success": true,
  "events": [
    {
      "id": "uuid",
      "title": "Team Practice",
      "type": "PRACTICE",
      "date": "2024-12-15",
      "time": "15:00:00",
      "venue": "Main Field",
      "opponent": null,
      "description": "Regular team practice session",
      "created_by": "uuid",
      "created_at": "2024-12-01T10:00:00Z",
      "is_recurring": false,
      "participant_count": 12,
      "user_response": "CONFIRMED" // Current user's RSVP status
    }
  ],
  "total_count": 25,
  "has_more": true
}
```

#### POST /api/events
**Purpose**: Create a new event
**Request Body**:
```json
{
  "title": "Team Practice",
  "type": "PRACTICE",
  "date": "2024-12-15",
  "time": "15:00",
  "venue": "Main Field",
  "opponent": null,
  "description": "Regular team practice session",
  "team_id": "uuid",
  "is_recurring": false,
  "recurrence_pattern": null
}
```

**Response**:
```json
{
  "success": true,
  "event": {
    "id": "uuid",
    "title": "Team Practice",
    "type": "PRACTICE",
    "date": "2024-12-15",
    "time": "15:00:00",
    "venue": "Main Field",
    "opponent": null,
    "description": "Regular team practice session",
    "created_by": "uuid",
    "created_at": "2024-12-01T10:00:00Z"
  }
}
```

#### GET /api/events/{event_id}
**Purpose**: Get detailed information about a specific event
**Response**:
```json
{
  "success": true,
  "event": {
    "id": "uuid",
    "title": "Team Practice",
    "type": "PRACTICE",
    "date": "2024-12-15",
    "time": "15:00:00",
    "venue": "Main Field",
    "opponent": null,
    "description": "Regular team practice session",
    "created_by": {
      "id": "uuid",
      "name": "Coach Smith",
      "avatar": "url"
    },
    "created_at": "2024-12-01T10:00:00Z",
    "participants": [
      {
        "user_id": "uuid",
        "name": "John Doe",
        "status": "CONFIRMED",
        "response_at": "2024-12-01T11:00:00Z"
      }
    ],
    "participant_stats": {
      "confirmed": 12,
      "declined": 2,
      "pending": 5,
      "maybe": 1
    }
  }
}
```

#### PUT /api/events/{event_id}
**Purpose**: Update an existing event
**Request Body**: Same as POST /api/events
**Response**: Same as POST /api/events

#### DELETE /api/events/{event_id}
**Purpose**: Cancel/delete an event
**Response**:
```json
{
  "success": true,
  "message": "Event cancelled successfully"
}
```

### 2. Event Participation (RSVP)

#### POST /api/events/{event_id}/rsvp
**Purpose**: Respond to an event invitation
**Request Body**:
```json
{
  "status": "CONFIRMED", // CONFIRMED, DECLINED, MAYBE
  "notes": "I'll be there!"
}
```

**Response**:
```json
{
  "success": true,
  "message": "RSVP updated successfully"
}
```

#### GET /api/events/{event_id}/participants
**Purpose**: Get list of participants for an event
**Response**:
```json
{
  "success": true,
  "participants": [
    {
      "user_id": "uuid",
      "name": "John Doe",
      "avatar": "url",
      "status": "CONFIRMED",
      "response_at": "2024-12-01T11:00:00Z",
      "notes": "I'll be there!"
    }
  ]
}
```

### 3. Calendar Views

#### GET /api/calendar/{team_id}/month/{year}/{month}
**Purpose**: Get events for a specific month
**Response**:
```json
{
  "success": true,
  "month": {
    "year": 2024,
    "month": 12,
    "events_by_day": {
      "15": [
        {
          "id": "uuid",
          "title": "Team Practice",
          "type": "PRACTICE",
          "time": "15:00:00"
        }
      ],
      "22": [
        {
          "id": "uuid",
          "title": "Match vs Eagles",
          "type": "GAME",
          "time": "14:00:00"
        }
      ]
    }
  }
}
```

#### GET /api/calendar/{team_id}/week/{year}/{week}
**Purpose**: Get events for a specific week
**Response**: Similar to month view but for a week

### 4. Event Templates

#### GET /api/events/templates/{team_id}
**Purpose**: Get predefined event templates for quick creation
**Response**:
```json
{
  "success": true,
  "templates": [
    {
      "id": "uuid",
      "name": "Regular Practice",
      "type": "PRACTICE",
      "time": "15:00",
      "venue": "Main Field",
      "description": "Standard team practice session"
    },
    {
      "id": "uuid",
      "name": "Home Game",
      "type": "GAME",
      "time": "14:00",
      "venue": "Home Stadium",
      "description": "Home match"
    }
  ]
}
```

## Business Logic Requirements

### 1. Event Creation Rules
- Only team coaches/managers can create events
- Events cannot be created in the past
- Recurring events should be limited to reasonable patterns (weekly, monthly)
- Maximum event duration should be enforced (e.g., 4 hours)

### 2. Event Modification Rules
- Only event creator or team admin can modify events
- Past events cannot be modified
- Cancelled events cannot be modified
- Changes to recurring events should prompt user about updating all instances

### 3. Participation Rules
- Team members can only RSVP to events for their team
- RSVP deadline should be configurable (e.g., 24 hours before event)
- Automatic reminders should be sent for pending RSVPs

### 4. Notification System
- Push notifications for new events
- Reminders for upcoming events (1 hour, 1 day before)
- RSVP deadline reminders
- Event cancellation notifications

## Error Handling

### Common Error Responses
```json
{
  "success": false,
  "error": {
    "code": "EVENT_NOT_FOUND",
    "message": "Event not found",
    "details": "The requested event does not exist or has been deleted"
  }
}
```

### Error Codes
- `EVENT_NOT_FOUND`: Event doesn't exist
- `INSUFFICIENT_PERMISSIONS`: User can't perform action
- `INVALID_DATE`: Event date is in the past
- `TEAM_NOT_FOUND`: Team doesn't exist
- `DUPLICATE_EVENT`: Event already exists at that time
- `RSVP_CLOSED`: RSVP deadline has passed
- `EVENT_CANCELLED`: Event has been cancelled

## Performance Considerations

### 1. Database Optimization
- Index on (team_id, date) for efficient calendar queries
- Partition events table by year for large datasets
- Use materialized views for complex aggregations

### 2. Caching Strategy
- Cache calendar month views for 5 minutes
- Cache event details for 10 minutes
- Invalidate cache on event modifications

### 3. Pagination
- Implement cursor-based pagination for large event lists
- Limit results to prevent memory issues

## Security Requirements

### 1. Authorization
- Users can only access events for teams they belong to
- Role-based permissions (coach can create, players can view/RSVP)
- Audit trail for all event modifications

### 2. Input Validation
- Sanitize all text inputs
- Validate date/time formats
- Prevent SQL injection and XSS attacks

### 3. Rate Limiting
- Limit event creation to prevent spam
- Rate limit RSVP submissions

## Integration Points

### 1. External Calendar Integration
- Google Calendar export
- iCal format support
- Calendar app integration

### 2. Notification System
- Push notifications via Firebase
- Email notifications for important events
- SMS notifications for urgent changes

### 3. Analytics
- Track event creation patterns
- Monitor RSVP response rates
- Analyze popular event types and times

## Testing Requirements

### 1. Unit Tests
- Event CRUD operations
- RSVP logic
- Date/time validation
- Permission checks

### 2. Integration Tests
- API endpoint testing
- Database transaction testing
- Notification system testing

### 3. Performance Tests
- Load testing for calendar views
- Database query performance
- Cache effectiveness

## Deployment Considerations

### 1. Database Migrations
- Version-controlled schema changes
- Backward compatibility for API changes
- Data migration scripts

### 2. Monitoring
- API response times
- Error rates
- Database performance metrics
- Cache hit rates

### 3. Backup Strategy
- Daily database backups
- Event log backups
- Disaster recovery plan

## Future Enhancements

### 1. Advanced Features
- Event conflict detection
- Automatic venue booking
- Weather integration for outdoor events
- Equipment management

### 2. Social Features
- Event comments and discussions
- Photo sharing for past events
- Event ratings and feedback

### 3. Analytics Dashboard
- Team activity insights
- Attendance trends
- Event success metrics

---

**Note**: This document should be updated as requirements evolve. All backend implementations should follow these specifications to ensure consistency and maintainability. 