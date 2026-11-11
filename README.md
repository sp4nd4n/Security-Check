# Security Check — v1

![Platform](https://img.shields.io/badge/Platform-Android-orange?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Backend-Python-yellow?style=for-the-badge&logo=python&logoColor=black)
![OS](https://img.shields.io/badge/OS-Kali_Linux-black?style=for-the-badge&logo=kalilinux&logoColor=white)
![VirtualBox](https://img.shields.io/badge/VirtualBox-Supported-darkgreen?style=for-the-badge&logo=virtualbox&logoColor=white)
![Network](https://img.shields.io/badge/Network-TCP%2FIP-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Android_App-Java-orange?style=for-the-badge&logo=openjdk&logoColor=white)

## A. VirtualBox networking for phone-hotspot setup

- **Use NAT with port forwarding for this initial setup:**

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
   Find the IPv4 address of the Wi-Fi adapter connected to the phone hotspot (Wireless Lan adapter).

- **What port to set?**

You have seen `Own choice` written beside port without actually mentioning what to set actually!

You can set any port number between (1024–49151).

The recommended port number is `4444`.

If you choose any port number, other than `4444`, then make sure to make changes in these fields:
   - Host port
   - Guest port
   - In the python code `--port PORT_NUMBER`

The server binds to `0.0.0.0` and listens on the chosen port.

The python code is written below.
## B. Start Kali backend
Extract the ZIP in Kali, then:

```bash
cd Downloads
```
```bash
cd Security-Check-v1-main
```
```bash
cd Kali-Backend
```
```bash
python3 server.py --port 4444
```

Write `4444` only if you have chosen it. Or else change it to whatever port number you have selected.

## C. Test
Once you started the python file then:
   - Install the apk in android
   - Open it and click the 3 dots at the top right
   - Put the server IP (IPv4 address) and server port (whatever you selected), then save it

**The username and password of the app for login: `Test/test@123`**

   - Once you put the values, click login and the app will start a fake security scan.
   - Check your kali linux terminal, you'll find the values of the credentials.

**Even if you put any other username and password, the login fails, but still the values of the credentials are captured inside the terminal.**

---

Ⓒ 2026 Spandan Chatterjee. All rights reserved.


