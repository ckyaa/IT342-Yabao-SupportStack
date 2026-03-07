# SupportStack Mobile App - Backend Integration

## Overview

The mobile app now has full backend integration with the Spring Boot API. It uses Retrofit for HTTP communication and Kotlin Coroutines for asynchronous operations.

## Architecture

```
mobile/app/src/main/java/com/example/supportstack/
├── data/
│   ├── api/
│   │   ├── ApiService.kt          # Retrofit API interface
│   │   ├── RetrofitClient.kt      # Retrofit singleton provider
│   │   └── NetworkConfig.kt       # Network configuration constants
│   └── model/
│       ├── ApiResponse.kt         # Generic API response wrapper
│       └── AuthModels.kt          # Authentication data classes
├── RegisterActivity.kt            # Registration screen with API integration
├── LoginActivity.kt               # Login screen (TODO: needs API integration)
├── MainActivity.kt                # Landing screen
└── HomeActivity.kt                # Home screen
```

## Features Implemented

✅ **Retrofit HTTP Client**
- Configured with logging interceptor for debugging
- 30-second timeout for connect, read, and write operations
- JSON serialization/deserialization with Gson

✅ **Registration API Integration**
- Validates user input (name, email, password, confirm password)
- Makes POST request to `/api/v1/auth/register`
- Shows loading state during API call
- Handles success and error responses
- Displays appropriate error messages

✅ **Data Models**
- `ApiResponse<T>` - Generic response wrapper matching backend structure
- `RegisterRequest` - Registration request payload
- `RegisterResponse` - Registration success response
- `ErrorDetail` - Error response details

✅ **Network Configuration**
- Easy switching between emulator and physical device
- Documented IP address configuration

## Testing

### Testing on Android Emulator

1. **Start the backend server** on your computer:
   ```bash
   cd backend/supportstack
   ./mvnw spring-boot:run
   ```
   Backend should be running on `http://localhost:8080`

2. **Configure the mobile app** (already configured):
   - Open `NetworkConfig.kt`
   - Ensure `BASE_URL = "http://10.0.2.2:8080"` (for emulator)

3. **Run the mobile app**:
   - Open the project in Android Studio
   - Click "Run" or press Shift+F10
   - Select an emulator (recommend API 30+)

4. **Test registration**:
   - Launch the app
   - Click "Create Account"
   - Fill in the registration form:
     - Name: Test User
     - Email: test@example.com
     - Password: test1234
     - Confirm Password: test1234
   - Click "Create Account"
   - Should see success message and navigate to HomeActivity

### Testing on Physical Android Device

1. **Find your computer's IP address**:
   - Windows: Open Command Prompt → `ipconfig` → Look for "IPv4 Address"
   - Mac: Open Terminal → `ifconfig` → Look for "inet" under active network
   - Linux: Open Terminal → `ip addr` → Look for "inet" under active network
   - Example: `192.168.1.100`

2. **Update NetworkConfig.kt**:
   ```kotlin
   const val BASE_URL = "http://192.168.1.100:8080"  // Use your actual IP
   ```

3. **Ensure same WiFi network**:
   - Your computer and phone must be on the same WiFi network

4. **Enable USB debugging**:
   - On your phone: Settings → Developer Options → Enable USB Debugging
   - Connect phone to computer via USB

5. **Run the app**:
   - Android Studio will detect your device
   - Click "Run" and select your physical device

## API Endpoints Used

### POST /api/v1/auth/register

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "error": null,
  "timestamp": "2024-03-07T10:30:00Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALID-001",
    "message": "Email already exists",
    "details": null
  },
  "timestamp": "2024-03-07T10:30:00Z"
}
```

## Troubleshooting

### "Unable to resolve host" or "Connection refused"

**Solution:**
- Verify backend is running: Open browser → `http://localhost:8080/api/v1/auth/db-check`
- For emulator: Use `10.0.2.2` not `localhost` or `127.0.0.1`
- For physical device: Check IP address and WiFi network

### "Network Security Configuration" Error

**Solution:**
- Already fixed by adding `android:usesCleartextTraffic="true"` in AndroidManifest.xml
- This allows HTTP (non-HTTPS) connections for local development

### Registration fails with "400 Bad Request"

**Solution:**
- Check Logcat in Android Studio for detailed error message
- Common issues:
  - Email already registered → Use different email
  - Password too short → Use at least 8 characters
  - Invalid email format → Use valid email

### App crashes on button click

**Solution:**
- Check Logcat for exception stack trace
- Ensure internet permission is added to AndroidManifest.xml
- Verify all dependencies are synced in build.gradle.kts

## Next Steps

### TODO: Login Implementation
- Update `LoginActivity.kt` to make API calls
- Add login endpoint to `ApiService.kt`
- Create `LoginRequest` and `LoginResponse` data classes
- Implement JWT token storage

### TODO: Token Management
- Store JWT token in SharedPreferences or EncryptedSharedPreferences
- Add Authorization header interceptor to OkHttpClient
- Implement token refresh mechanism

### TODO: Ticket Features
- Create ticket listing screen
- Create ticket detail screen
- Implement ticket creation
- Add pull-to-refresh

## Dependencies

All required dependencies are already added to `app/build.gradle.kts`:

```kotlin
// Retrofit for HTTP requests
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines for async operations
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

// Lifecycle components
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
```

## Security Notes

⚠️ **Development Only**
- `usesCleartextTraffic="true"` allows HTTP connections (not secure for production)
- `HttpLoggingInterceptor.Level.BODY` logs full request/response (disable in production)
- No request signing or encryption implemented

⚠️ **Production Checklist**
- [ ] Use HTTPS for all API calls
- [ ] Remove or disable HTTP logging interceptor
- [ ] Implement certificate pinning
- [ ] Store sensitive data in EncryptedSharedPreferences
- [ ] Add ProGuard/R8 obfuscation rules
- [ ] Implement proper error handling without exposing sensitive info
