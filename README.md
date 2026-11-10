# Security Check — v1 (preset demo credentials)

This is a deliberately simple LAN-only demonstration:
- The Android app displays preset credentials `Test` / `test@123`.
- The user can submit them to the Python server running in Kali.
- Kali verifies the fixed demo pair and prints the submitted demo username/password and result.
- Preferences allow a server IP and server port. The port field has the placeholder `e.g. 4444`.

**Important:** These are public, hard-coded demo credentials, not real user credentials.
Do not reuse this design for collecting real account passwords. The server uses plain HTTP
for an isolated lab demonstration; do not expose it to the internet or use real credentials.

## A. Start Kali backend
Extract the ZIP in Kali, then:

```bash
cd Security-Check-v1/Kali-Backend
python3 server.py --port 4444
```

To use another port, replace `4444` with that port.

The server binds to `0.0.0.0` and listens on the chosen port.

## B. VirtualBox networking for phone-hotspot setup

Use NAT with port forwarding for this initial setup:

1. Shut down the Kali VM.
2. VirtualBox → Kali VM → Settings → Network → Adapter 1.
3. Enable adapter; Attached to: **NAT**.
4. Advanced → Port Forwarding → add:
   - Name: `SecurityCheck`
   - Protocol: `TCP`
   - Host IP: leave blank
   - Host Port: `4444`
   - Guest IP: leave blank
   - Guest Port: `4444`
5. Start Kali and run the Python server on port 4444.
6. On Windows, connect to your phone hotspot and run `ipconfig`.
   Find the IPv4 address of the Wi-Fi adapter connected to the phone hotspot.
7. In the Android app Preferences, enter that Windows IPv4 address and port `4444`.

If you choose another port, use that same port for:
- the Python command (`--port PORT`)
- VirtualBox Host Port and Guest Port
- the Android app's Server Port field.

The phone, Windows PC, and Kali VM must be able to communicate through this path.
If Windows Firewall prompts, allow the Python server / inbound TCP port on Private networks
only. Do not expose the demo server to the public internet.

## C. Build Android APK
Open `Android-App` in Android Studio. Allow Gradle sync.
Build → Build Bundle(s) / APK(s) → Build APK(s).
Install the generated APK on your Android phone.

## D. Test
1. Start the Kali Python server.
2. Open Security Check.
3. The login fields show `Test` and `test@123`.
4. Tap the three-dot menu → Preferences.
5. Enter the Windows Wi-Fi IPv4 address and port `4444`.
6. Tap Login.
7. Kali prints the submitted demo pair and whether it matched.

## Notes
- This project is a learning prototype, not production authentication.
- It deliberately uses HTTP and prints a public demo password for demonstration only.
- For real authentication, never log passwords; use HTTPS, secure password hashing,
  server-side account storage, rate limiting, and appropriate access controls.
