# Auth Service API Documentation

Base Path: `/api/v1/auth`
All requests should be sent to the API Gateway (port `8080`) which forwards them to the Auth Service.
Responses follow a consistent format:
```json
{
  "success": true,
  "statusCode": 200,
  "data": { ... },
  "message": "Success message"
}
```

---

## 1. Sign Up (Email & Password)
**Endpoint:** `POST /signup/email`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "yourPassword123!"
}
```

**Response:**
Returns user data and automatically sets `access_token` and `refresh_token` cookies.

---

## 2. Set Username
**Endpoint:** `POST /signup/username`

**Special Requirements:**
Must include the `access_token` cookie (received from signup/login).

**Request Body:**
```json
{
  "username": "uniqueUsername123"
}
```

---

## 3. Login
**Endpoint:** `POST /login`

**Request Body:**
```json
{
  "emailOrUsername": "user@example.com", 
  "password": "yourPassword123!"
}
```

**Response:**
Returns user data and automatically sets `access_token` and `refresh_token` cookies.

---

## 4. Google OAuth Login
**Endpoint:** `POST /oauth/google`

**Request Body:**
```json
{
  "idToken": "eyJhbGciOiJSUz..."
}
```

**Response:**
Returns user data and automatically sets `access_token` and `refresh_token` cookies.

---

## 5. Get User Profile (Me)
**Endpoint:** `GET /me`

**Special Requirements:**
Must include the `access_token` cookie. The API Gateway will read this cookie and inject an `X-User-Id` header into the request sent to the Auth Service.

**Request Body:**
*None*

---

## 6. Update Password
**Endpoint:** `PUT /password`

**Special Requirements:**
Must include the `access_token` cookie.

**Request Body:**
```json
{
  "oldPassword": "oldPassword123!",
  "newPassword": "newSecurePassword456!"
}
```

---

## 7. Refresh Token
**Endpoint:** `POST /refresh`

**Special Requirements:**
Must include the `refresh_token` cookie.

**Request Body:**
*None*

**Response:**
Automatically issues a new set of `access_token` and `refresh_token` cookies.

---

## 8. Logout
**Endpoint:** `POST /logout`

**Special Requirements:**
Must include the `refresh_token` cookie so the server can revoke it.

**Request Body:**
*None*

**Response:**
Revokes the token in the database and clears the `access_token` and `refresh_token` cookies from the browser.
