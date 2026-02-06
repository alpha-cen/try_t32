# Auth User Service - API Quick Reference

Quick reference guide for all API endpoints in the Auth User Service.

---

## 🔐 Authentication Endpoints

### Public Endpoints (No Auth Required)
```
POST   /api/auth/register                # Register new user
POST   /api/auth/login                   # Login and get JWT token
POST   /api/auth/forgot-password         # Request password reset
POST   /api/auth/confirm-forgot-password # Confirm password reset
```

### Protected Endpoints (JWT Required)
```
GET    /api/auth/me                      # Get current user info
POST   /api/auth/refresh                 # Refresh JWT token
POST   /api/auth/change-password         # Change password
POST   /api/auth/logout                  # Logout
```

---

## 👤 User Profile Management (JWT Required)

```
GET    /api/users/me                     # Get my profile
PUT    /api/users/me                     # Update my profile
GET    /api/users/me/full                # Get profile + addresses
DELETE /api/users/me                     # Delete my account
```

**Request Example (Update Profile):**
```json
PUT /api/users/me
{
  "email": "new@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

---

## 📍 User Address Management (JWT Required)

```
GET    /api/users/me/addresses           # List all my addresses
GET    /api/users/me/addresses/{id}      # Get specific address
GET    /api/users/me/addresses/default   # Get my default address
POST   /api/users/me/addresses           # Create new address
PUT    /api/users/me/addresses/{id}      # Update address
DELETE /api/users/me/addresses/{id}      # Delete address
PATCH  /api/users/me/addresses/{id}/default  # Set as default
```

**Request Example (Create Address):**
```json
POST /api/users/me/addresses
{
  "addressLine1": "123 Main St",
  "addressLine2": "Apt 4B",
  "city": "San Francisco",
  "state": "California",
  "postalCode": "94102",
  "country": "USA",
  "isDefault": true,
  "addressType": "BOTH"
}
```

**Address Types:**
- `SHIPPING` - For shipping only
- `BILLING` - For billing only
- `BOTH` - For both shipping and billing (default)

---

## 🛡️ Admin User Management (JWT + ADMIN Role Required)

### User Management
```
GET    /api/admin/users                  # List all users (with search)
GET    /api/admin/users/{id}             # Get user by ID
GET    /api/admin/users/{id}/full        # Get user + all addresses
PUT    /api/admin/users/{id}             # Update any user
DELETE /api/admin/users/{id}             # Delete any user
GET    /api/admin/users/{id}/addresses   # Get user's addresses
```

### System Statistics
```
GET    /api/admin/users/statistics       # Get user statistics
```

**Search Example:**
```
GET /api/admin/users?search=john
```

**Update User Example:**
```json
PUT /api/admin/users/123
{
  "username": "newusername",
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+1234567890",
  "role": "ADMIN",
  "password": "newpassword123"  // Optional
}
```

**Statistics Response:**
```json
{
  "totalUsers": 150,
  "adminCount": 5,
  "userCount": 145,
  "totalAddresses": 287
}
```

---

## 📊 Health & Monitoring

### Public Health Checks
```
GET    /actuator/health                  # Health status
GET    /actuator/info                    # Application info
```

### Metrics (Protected)
```
GET    /actuator/metrics                 # All metrics
GET    /actuator/prometheus              # Prometheus format metrics
```

---

## 🔑 Authentication Flow

### 1. Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "token": "eyJraWQiOiJ...",
  "refreshToken": "...",
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### 3. Use JWT Token
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "message": "Validation failed",
  "timestamp": "2024-01-15T10:30:00",
  "details": {}
}
```

### 401 Unauthorized
```json
{
  "message": "Unauthorized - Invalid or missing JWT token"
}
```

### 403 Forbidden
```json
{
  "message": "Access denied - Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "message": "Resource not found: User not found with id: 123"
}
```

---

## 📈 Prometheus Metrics

Custom metrics available at `/actuator/prometheus`:

### Authentication Metrics
```
auth_login_success_total              # Successful logins
auth_login_failure_total              # Failed logins
auth_registration_success_total       # Successful registrations
auth_registration_failure_total       # Failed registrations
auth_password_reset_total             # Password resets
auth_password_change_total            # Password changes
auth_token_refresh_total              # Token refreshes
```

### User Management Metrics
```
user_profile_update_total             # Profile updates
user_deletion_total                   # Account deletions
admin_user_update_total               # Admin user updates
```

### Address Management Metrics
```
address_created_total                 # New addresses
address_updated_total                 # Address updates
address_deleted_total                 # Address deletions
address_default_changed_total         # Default address changes
```

---

## 🌐 Base URL

- **Local Development:** `http://localhost:8080`
- **Docker Compose:** `http://localhost:8080`
- **Production:** `https://your-domain.com`

---

## 🔗 Related Documentation

- **[USER_MANAGEMENT_API.md](USER_MANAGEMENT_API.md)** - Complete API documentation
- **[README.md](README.md)** - Setup and deployment guide
- **[OBSERVABILITY.md](OBSERVABILITY.md)** - Monitoring and metrics
- **[USER_MANAGEMENT_SUMMARY.md](USER_MANAGEMENT_SUMMARY.md)** - Feature summary

---

## 💡 Common Use Cases

### 1. User Registration & Login
```
POST /api/auth/register  →  POST /api/auth/login  →  GET /api/users/me
```

### 2. Add Shipping Address
```
Login  →  POST /api/users/me/addresses (isDefault: true)
```

### 3. Update Profile
```
Login  →  PUT /api/users/me
```

### 4. Admin: Search Users
```
Admin Login  →  GET /api/admin/users?search=john
```

### 5. Admin: View User Details
```
Admin Login  →  GET /api/admin/users/{id}/full
```

---

**Last Updated:** February 2026  
**Version:** 1.0.0
