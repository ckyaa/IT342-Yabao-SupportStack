package com.example.supportstack.data.api

/**
 * Network configuration constants
 * 
 * IMPORTANT: Change BASE_URL based on your testing environment:
 * 
 * For Android Emulator:
 *   - Use: "http://10.0.2.2:8080"
 *   - 10.0.2.2 is a special alias to your host machine's localhost
 * 
 * For Physical Android Device:
 *   - Use: "http://YOUR_COMPUTER_IP:8080"
 *   - Example: "http://192.168.1.100:8080"
 *   - Find your computer's IP address:
 *     Windows: Open Command Prompt and run `ipconfig`
 *     Mac/Linux: Open Terminal and run `ifconfig` or `ip addr`
 *   - Make sure your phone and computer are on the same WiFi network
 * 
 * For Production:
 *   - Use: "https://your-production-domain.com"
 *   - Make sure to use HTTPS for production
 */
object NetworkConfig {
    // Change this URL based on your environment
    const val BASE_URL = "http://10.0.2.2:8080"  // For Android Emulator
    
    // Alternative configurations (uncomment the one you need):
    // const val BASE_URL = "http://192.168.1.100:8080"  // For physical device (replace with your IP)
    // const val BASE_URL = "https://supportstack.onrender.com"  // For production
    
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
