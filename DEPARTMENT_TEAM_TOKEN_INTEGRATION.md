# Department and Team ID Integration in JWT Tokens

## Overview

This implementation adds department and team ID information to JWT tokens when users log in or register. The system automatically checks if a user is assigned to a department or team and includes this information in the token if available.

## Features

### 1. Automatic Department and Team ID Fetching
- When a user logs in or registers, the system automatically calls external services to check if the user is assigned to a department or team
- If the user has assignments, the IDs are included in the JWT token
- If the user is not assigned, the token is generated without department/team IDs (null values)

### 2. Feign Clients
Two new Feign clients have been created to communicate with external services:

- **DepartmentClient**: Calls `/api/departments/user/{userId}` to get department ID
- **TeamClient**: Calls `/api/teams/user/{userId}` to get team ID

### 3. JWT Token Enhancement
The JWT token now includes:
- `userId`: User's unique identifier
- `organizationId`: Organization the user belongs to
- `departmentId`: Department ID (if assigned)
- `teamId`: Team ID (if assigned)
- `authorities`: User's permissions/roles

### 4. Utility Methods
New methods added to `JwtUtil`:
- `extractDepartmentId(String token)`: Extract department ID from token
- `extractTeamId(String token)`: Extract team ID from token
- `hasDepartmentId(String token)`: Check if token contains department ID
- `hasTeamId(String token)`: Check if token contains team ID

New methods added to `OrganizationContextUtil`:
- `getCurrentDepartmentId()`: Get department ID from current request context
- `getCurrentTeamId()`: Get team ID from current request context
- `hasDepartmentId()`: Check if current context has department ID
- `hasTeamId()`: Check if current context has team ID

## Configuration

Add the following properties to `application.properties`:

```properties
department.service.url=http://department-service:8080
team.service.url=http://team-service:8080
```

## Error Handling

- If department or team service calls fail, the authentication process continues without failing
- Users without department or team assignments can still log in successfully
- Debug logging is added to track when department/team IDs are fetched or when errors occur

## Usage Examples

### Getting Department/Team IDs from Current Context
```java
@Autowired
private OrganizationContextUtil organizationContextUtil;

// Get current department ID
UUID departmentId = organizationContextUtil.getCurrentDepartmentId();

// Get current team ID
UUID teamId = organizationContextUtil.getCurrentTeamId();

// Check if user has department/team assignments
boolean hasDepartment = organizationContextUtil.hasDepartmentId();
boolean hasTeam = organizationContextUtil.hasTeamId();
```

### Extracting from JWT Token
```java
@Autowired
private JwtUtil jwtUtil;

// Extract department and team IDs from token
UUID departmentId = jwtUtil.extractDepartmentId(token);
UUID teamId = jwtUtil.extractTeamId(token);

// Check if token contains department/team IDs
boolean hasDepartment = jwtUtil.hasDepartmentId(token);
boolean hasTeam = jwtUtil.hasTeamId(token);
```

## Backward Compatibility

- The old `generateToken(UserDetails, UUID, UUID)` method is maintained for backward compatibility
- Existing tokens without department/team IDs will continue to work
- New tokens will include department/team IDs when available

## Security Considerations

- Department and team IDs are included in JWT claims, so they are visible to clients
- The system gracefully handles cases where external services are unavailable
- No sensitive information is exposed beyond what's necessary for authorization 