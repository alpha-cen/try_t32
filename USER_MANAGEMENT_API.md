# User Management & Address API Documentation

This document details all user management and address-related endpoints in the Auth User Service.

## Table of Contents
- [User Profile Management](#user-profile-management)
- [User Address Management](#user-address-management)
- [Admin User Management](#admin-user-management)
- [Data Models](#data-models)

---

## User Profile Management

### Get Current User Profile
Get the profile of the currently authenticated user.

**Endpoint:** `GET /api/users/me`

**Authentication:** Required (JWT Token)

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

### Update Current User Profile
Update profile information for the currently authenticated user.

**Endpoint:** `PUT /api/users/me`

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+1234567890"
}
```

**Response:** Same as Get Current User Profile

---

### Get Full User Profile
Get user profile with all associated addresses.

**Endpoint:** `GET /api/users/me/full`

**Authentication:** Required (JWT Token)

**Response:**
```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "role": "USER"
  },
  "addresses": [
    {
      "id": 1,
      "userId": 1,
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "city": "San Francisco",
      "state": "California",
      "postalCode": "94102",
      "country": "USA",
      "isDefault": true,
      "addressType": "BOTH"
    }
  ]
}
```

---

### Delete Current User Account
Delete the currently authenticated user's account.

**Endpoint:** `DELETE /api/users/me`

**Authentication:** Required (JWT Token)

**Response:**
```json
{
  "message": "Account successfully deleted"
}
```

---

## User Address Management

### Get All User Addresses
Get all addresses for the currently authenticated user.

**Endpoint:** `GET /api/users/me/addresses`

**Authentication:** Required (JWT Token)

**Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "San Francisco",
    "state": "California",
    "postalCode": "94102",
    "country": "USA",
    "isDefault": true,
    "addressType": "BOTH",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

---

### Get Address by ID
Get a specific address for the currently authenticated user.

**Endpoint:** `GET /api/users/me/addresses/{addressId}`

**Authentication:** Required (JWT Token)

**Response:** Same as single address in array above

---

### Get Default Address
Get the default address for the currently authenticated user.

**Endpoint:** `GET /api/users/me/addresses/default`

**Authentication:** Required (JWT Token)

**Response:** Same as single address in array above

---

### Create New Address
Create a new address for the currently authenticated user.

**Endpoint:** `POST /api/users/me/addresses`

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "addressLine1": "456 Oak Avenue",
  "addressLine2": "Suite 200",
  "city": "Los Angeles",
  "state": "California",
  "postalCode": "90001",
  "country": "USA",
  "isDefault": false,
  "addressType": "SHIPPING"
}
```

**Validation Rules:**
- `addressLine1`: Required, max 255 characters
- `addressLine2`: Optional, max 255 characters
- `city`: Required, max 100 characters
- `state`: Required, max 100 characters
- `postalCode`: Required, max 20 characters
- `country`: Required, max 100 characters
- `isDefault`: Optional, defaults to false
- `addressType`: Optional, defaults to "BOTH" (can be SHIPPING, BILLING, or BOTH)

**Response:** HTTP 201 Created with address object

---

### Update Address
Update an existing address.

**Endpoint:** `PUT /api/users/me/addresses/{addressId}`

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "addressLine1": "456 Oak Avenue Updated",
  "city": "Los Angeles",
  "state": "California",
  "postalCode": "90002",
  "isDefault": true
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response:** Updated address object

---

### Delete Address
Delete an address.

**Endpoint:** `DELETE /api/users/me/addresses/{addressId}`

**Authentication:** Required (JWT Token)

**Response:** HTTP 204 No Content

---

### Set Default Address
Set a specific address as the default.

**Endpoint:** `PATCH /api/users/me/addresses/{addressId}/default`

**Authentication:** Required (JWT Token)

**Response:** Updated address object with `isDefault: true`

**Note:** This will automatically unset the default flag on any other addresses.

---

## Admin User Management

All admin endpoints require `ADMIN` role.

### Get All Users
Get a list of all users with optional search.

**Endpoint:** `GET /api/admin/users?search={searchTerm}`

**Authentication:** Required (JWT Token with ADMIN role)

**Query Parameters:**
- `search` (optional): Search by username or email

**Response:**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "role": "USER",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "addressCount": 2
  }
]
```

---

### Get User by ID (Admin)
Get a specific user's details.

**Endpoint:** `GET /api/admin/users/{userId}`

**Authentication:** Required (JWT Token with ADMIN role)

**Response:** Same as user object in array above

---

### Get User Full Details (Admin)
Get user with all associated data including addresses.

**Endpoint:** `GET /api/admin/users/{userId}/full`

**Authentication:** Required (JWT Token with ADMIN role)

**Response:**
```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "role": "USER",
    "addressCount": 2
  },
  "addresses": [...]
}
```

---

### Update User (Admin)
Update any user's information.

**Endpoint:** `PUT /api/admin/users/{userId}`

**Authentication:** Required (JWT Token with ADMIN role)

**Request Body:**
```json
{
  "username": "new_username",
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+1234567890",
  "role": "ADMIN",
  "password": "newpassword123"
}
```

**Note:** All fields are optional. Password field will update the user's password.

**Response:** Updated user object

---

### Delete User (Admin)
Delete any user account.

**Endpoint:** `DELETE /api/admin/users/{userId}`

**Authentication:** Required (JWT Token with ADMIN role)

**Response:**
```json
{
  "message": "User successfully deleted"
}
```

---

### Get User Statistics
Get system-wide user statistics.

**Endpoint:** `GET /api/admin/users/statistics`

**Authentication:** Required (JWT Token with ADMIN role)

**Response:**
```json
{
  "totalUsers": 150,
  "adminCount": 5,
  "userCount": 145,
  "totalAddresses": 287
}
```

---

### Get User Addresses (Admin)
Get all addresses for a specific user.

**Endpoint:** `GET /api/admin/users/{userId}/addresses`

**Authentication:** Required (JWT Token with ADMIN role)

**Response:** Array of address objects

---

## Data Models

### User Model
```typescript
{
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  role: "USER" | "ADMIN";
  createdAt: string;
  updatedAt: string;
}
```

### Address Model
```typescript
{
  id: number;
  userId: number;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  addressType: "SHIPPING" | "BILLING" | "BOTH";
  createdAt: string;
  updatedAt: string;
}
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "message": "Validation error message",
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
  "message": "Resource not found: User not found with id: 123",
  "timestamp": "2024-01-15T10:30:00",
  "details": {}
}
```

### 500 Internal Server Error
```json
{
  "message": "Internal server error",
  "timestamp": "2024-01-15T10:30:00",
  "details": {}
}
```

---

## Observability

All user management and address operations are tracked with Prometheus metrics:

- `user.profile.update` - User profile updates
- `user.deletion` - User account deletions
- `admin.user.update` - Admin user updates
- `address.created` - Address creations
- `address.updated` - Address updates
- `address.deleted` - Address deletions
- `address.default.changed` - Default address changes

These metrics can be viewed in Grafana at `http://localhost:3000`
