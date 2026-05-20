**PROJECT PROPOSAL & ARCHITECTURE DOCUMENT**

**PROJECT CODENAME:** Operation Jarvis
**CONTEXT:** Red Team Competition, Adomji Cantonment College (Military Supervised, Controlled Environment, Dummy Data/Volunteers)
**OBJECTIVE:** To demonstrate the critical vulnerabilities of modern Android devices when users are socially engineered into granting Accessibility and Overlay permissions under the guise of "Next-Generation AI Assistants."
**PRIORITY:** Speed of development and operational reliability over aesthetic polish.

---

### EXECUTIVE SUMMARY

This document outlines the architecture for "Project Jarvis," a proof-of-concept Remote Administration Tool designed for the upcoming Red Team competition. The project exploits the intersection of legitimate remote support technologies and malicious overlay lockout techniques. 

The lure is an "AI-Powered Smartphone Controller" (similar to Iron Man's Jarvis) that promises voice-activated phone management. In reality, once the user grants the necessary permissions (Accessibility, Overlay, MediaProjection) to enable the "AI," the operator gains full, covert remote control of the device. The tool utilizes a screen-state sensor to ensure the operator only strikes when the user is inactive, displaying a "System Update" lockout screen to blind the victim during the takeover.

### ARCHITECTURE SELECTION: The Hybrid Approach

We are not building networking or video streaming from scratch. We are combining two proven open-source technologies to achieve the fastest time-to-weaponization.

**1. The Remote Control Engine: RustDesk (Open Source)**
RustDesk provides the entire client-server infrastructure for screen mirroring and input injection. It already implements the Android AccessibilityService for touch injection and handles NAT traversal via a lightweight relay server. We will fork the Android client and strip it down to its bare essentials.

**2. The AI Lure & Logic: Custom Android App (Built from Scratch/Template)**
We will build the front-facing "Jarvis AI" app. This app contains the social engineering UI, the screen-state sensor, and the overlay module. Once the user grants permissions to the "AI," this app silently triggers the stripped-down RustDesk service in the background.

### OPERATIONAL FLOW

**Phase 1: The Lure (Social Engineering)**
The volunteer (victim) installs "Jarvis AI." The app requests Accessibility (to "read the screen for context"), Overlay (to "display AR widgets"), and Battery Optimization whitelisting (to "keep the AI listening"). The user grants these, believing they are enabling a smart assistant.

**Phase 2: The Silent Connection**
The "Jarvis AI" app connects to our relay server in the background. The operator (Red Teamer) sees the device come online on their Mac M2 control center.

**Phase 3: The Status Check (Screen State)**
The operator sends a "Status Request" command. The Jarvis app checks the Android `PowerManager` to determine if the screen is on or off.
*   **Screen ON:** The app reports "Active." The operator waits.
*   **Screen OFF:** The app reports "Sleeping." The operator is clear to proceed.

**Phase 4: The Takeover (The Lockout)**
The operator initiates the remote session. 
1.  The Jarvis app instantly launches a full-screen, un-dismissable Overlay Activity displaying "Android System Update: Installing Security Patch... Do not turn off your device."
2.  The victim's screen is completely blinded.
3.  Simultaneously, the Jarvis app programmatically starts the RustDesk screen mirroring service (MediaProjection) **without prompting the user**, as the necessary permissions were already granted to the "Jarvis" package.

**Phase 5: The Strike**
The operator views the phone screen on the Mac M2 and uses the mouse/keyboard to navigate (e.g., open PayPal, request password reset, intercept SMS OTP). The victim sees only the "System Update" screen.

**Phase 6: The Release**
The operator disconnects. The Jarvis app dismisses the overlay. The victim wakes the phone, sees nothing amiss, and continues normally.

---

### TECHNICAL SPECIFICATIONS & MODULE BREAKDOWN

**Module 1: The "Jarvis AI" Stager (Android App)**
*   **Function:** The user-facing lure and the brain of the operation.
*   **Key Components:**
    *   `JarvisMainActivity`: A simple UI with a "Start Listening" button that triggers the permission requests.
    *   `ScreenStateReceiver`: A `BroadcastReceiver` listening for `ACTION_SCREEN_ON/OFF`. It reports state to the C2.
    *   `LockoutOverlayActivity`: An Activity with `SYSTEM_ALERT_WINDOW` that draws the fake system update screen.
    *   `ServiceTrigger`: Logic that receives a command from the C2 and uses `startForegroundService()` to launch the hidden RustDesk service.

**Module 2: The Hidden RustDesk Service (Android App - Forked)**
*   **Function:** The video streamer and input injector.
*   **Modifications:**
    *   Strip all RustDesk UI (ID entry boxes, settings menus).
    *   Change the package name to match the Jarvis app (or make it a hidden service within the same package).
    *   Modify the Accessibility Service config to intercept screen events and inject touches.
    *   **Crucial Fix:** Modify the `MediaProjection` request logic. Instead of showing the system dialog ("Jarvis AI will start capturing everything on your screen"), the app uses the pre-granted permissions to bypass the prompt.

**Module 3: The Relay Server (Linux VPS - 1GB RAM)**
*   **Setup:** Install `hbbs` (RustDesk ID/Rendezvous server) and `hbbr` (RustDesk Relay server).
*   **Load:** Minimal. Will easily handle 10-20 concurrent Red Team sessions on 1GB RAM.

**Module 4: The Operator Control Center (Mac M2)**
*   **Setup:** Standard RustDesk Desktop Client.
*   **Usage:** Operator enters the VPS IP, the Victim's Device ID, and clicks connect when the status is "Sleeping."

---

### DEVELOPMENT TIMELINE (Fast Track)

*   **Day 1-2:** Fork RustDesk, compile server binaries on VPS. Verify standard connection works.
*   **Day 3-4:** Build the "Jarvis AI" Stager UI. Implement the permission requests and the `ScreenStateReceiver`.
*   **Day 5-6:** Integrate the `LockoutOverlayActivity`. Test the fake "System Update" screen.
*   **Day 7-8:** The hardest part—programmatically triggering the RustDesk screen capture without the user prompt. This requires hooking into the `MediaProjection` manager using the app's existing permissions.
*   **Day 9-10:** End-to-end testing on dummy devices. Refining the "Status Check" latency.

### CONCLUSION

Project Jarvis exploits the fundamental flaw in Android's permission model: Users will trade ultimate device control for perceived convenience (an AI assistant). By combining the robust networking of RustDesk with targeted social engineering, the Red Team can demonstrate how quickly a device can be compromised—silently, and without the victim ever seeing it happen.


[ short note that for the Jarvis feature we can use any open source thing which is already built, for example:  https://github.com/Aryan-0001/Jarvis-AI.git  and you can go and read the repository readme file or you can search for something similar. We will go with the best one. We need to do the best combination. ]





eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFkaWNydHFweWNjdGhubWFjZ3p1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxODI4NTIsImV4cCI6MjA5Mjc1ODg1Mn0.KUn9Nf1WGiBb31mEHrU5-NDo_K-BEUA0laGL6yFdnT0 [ anon key

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFkaWNydHFweWNjdGhubWFjZ3p1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NzE4Mjg1MiwiZXhwIjoyMDkyNzU4ODUyfQ.j0yn5FvKRYAzYPSNMcZOXqUeOPafSEp7qkHAMrBslp8 [ service role key



Project url [ https://adicrtqpyccthnmacgzu.supabase.co/rest/v1/ ]


vps info 


Server information
	•	Service package: MSK
	•	Setup date: 2026-05-08
	•	Domain name: 208414.landvps.online
	•	Server IP address: 77.105.133.37
	•	User: root
	•	Password: n036mKelPVXd


[as you should know that I am using one single project in the database platform for all the cyber security projects so you need to look at the SQL folder in this directory to understand what are the queries are already in the project so that you don't clash with anything and this project remains separate as well as working. I hope these credentials will be enough and to run the Jarvis for the mobile you will need to select an open source project for us which can be edited fast for our use case and you can search in the web for this as soon as you do that after that, I will give you the API key for things like 11 labs as well as groq because I need to give you the free things.  And there is only one file for this mobile app, I guess which is maybe Jarvis mobile or something like that.



Below is some data about what is done till now : [no information available and you have to search or dive deep into the full folder or I should say the directory and also need to look at the VPS as necessary to understand it fully and also to detect the next moves]