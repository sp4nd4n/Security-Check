# Security Check — v1

## A. VirtualBox networking for phone-hotspot setup

Use NAT with port forwarding for this initial setup:

1. Open your kali linux settings.
2. Enable adapter; Attached to: **NAT**.
3. Port Forwarding → add:
   - Name: `SecurityCheck`
   - Protocol: `TCP`
   - Host IP: `0.0.0.0`
   - Host Port: `Own choice`
   - Guest IP: [Write `hostname -I`]
   - Guest Port: `Own choice`
   
4. On Windows, connect to your phone hotspot and run `ipconfig` in cmd.
   Find the IPv4 address of the Wi-Fi adapter connected to the phone hotspot.
5. In the Android app, click the 3 dots at the top right then Preferences, enter that Windows IPv4 address and port `Own choice`.



The phone, Windows PC, and Kali VM must be able to communicate through this path.
If Windows Firewall prompts, allow the Python server / inbound TCP port on Private networks
only. Do not expose the demo server to the public internet.

## B. Start Kali backend
Extract the ZIP in Kali, then:

```bash
cd Downloads
```
```bash
cd Security-Check-v1
```
```bash
cd Kali-backend
```

To use another port, replace `4444` with that port.

The server binds to `0.0.0.0` and listens on the chosen port.

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
