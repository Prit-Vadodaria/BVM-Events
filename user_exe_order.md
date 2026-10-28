# BVM Events — Step-by-Step Execution Guide

This document outlines the exact chronological steps required to set up and run the **BVM Events** application on any computer and Android device from scratch.

---

## 📋 Prerequisites

Ensure the following software is installed on the computer:
1. **[XAMPP](https://www.apachefriends.org/)** (with PHP 8+ and MySQL).
2. **[Android Studio](https://developer.android.com/studio)** (includes JDK 17/21 and Android SDK).
3. **Android Device** (Android 8.0 / API 26 or higher) OR **Android Studio Emulator**.

---

## Part 1: Backend & Database Setup (XAMPP)

### Step 1: Start Apache & MySQL
1. Open the **XAMPP Control Panel**.
2. Click **Start** for both **Apache** and **MySQL**.
3. Confirm both modules turn green.

---

### Step 2: Create Database & Import Schema
1. Open your browser and go to: **[http://localhost/phpmyadmin/](http://localhost/phpmyadmin/)**
2. Click on **New** in the left sidebar.
3. Enter database name: `bvm_events` with collation `utf8mb4_unicode_ci` (or default).
4. Click **Create**.
5. With `bvm_events` selected, click the **Import** tab at the top.
6. Click **Choose File** and select:
   ```
   <repo-folder>/database/schema.sql
   ```
7. Click **Import** at the bottom of the page.
   *(This creates the 6 tables: `users`, `venues`, `events`, `event_images`, `attendance`, `bookmarks` and seeds 5 campus venues).*

---

### Step 3: Deploy PHP API to XAMPP Web Root
The backend files must reside inside `C:\xampp\htdocs\bvm_events\`.

**Option A (Using Junction / Symlink — Recommended):**
Open PowerShell as Administrator:
```powershell
New-Item -ItemType Junction -Path "C:\xampp\htdocs\bvm_events" -Target "<full-path-to-repo>\php-api"
```

**Option B (Direct Copy):**
Copy the entire `php-api` folder into `C:\xampp\htdocs\` and rename the folder to **`bvm_events`**.

---

### Step 4: Configure Database Connection
1. In `C:\xampp\htdocs\bvm_events\`, look for `config.sample.php`.
2. Make a copy of it and name it **`config.php`**.
3. If using standard default XAMPP settings, the default configuration matches:
   ```php
   <?php
   define('DB_HOST', 'localhost');
   define('DB_NAME', 'bvm_events');
   define('DB_USER', 'root');
   define('DB_PASS', '');
   define('DB_PORT', 3306);
   ```

---

### Step 5: Seed the Admin Account
Open PowerShell or Command Prompt in the project repository root and execute:

```powershell
C:\xampp\php\php.exe database\seed_admin.php "Campus Admin" admin@bvm.local admin123
```
*Output will say: `Admin user created successfully.`*

---

### Step 6: Verify Backend is Working
Open your browser and navigate to:
👉 **[http://localhost/bvm_events/venues/list.php](http://localhost/bvm_events/venues/list.php)**

You should see a JSON response with status `"success": true` returning the 5 BVM campus venues (Auditorium, Workshop, Labs, Library Lawn).

---

## Part 2: Android App Setup & Execution

### Choose ONE of the following execution methods:

---

### 🔹 Method A: Real Phone via USB Cable (Recommended & Fastest)

This method allows your physical phone to access `http://localhost/bvm_events/` on your computer through the USB cable.

#### 1. Enable Developer Options & USB Debugging:
- On your phone: **Settings** > **About Phone** > Tap **Build Number** 7 times rapidly until Developer Mode is unlocked.
- Go to **Settings** > **System / Additional Settings** > **Developer Options**.
- Turn **ON** **USB Debugging** (and "Install via USB" if on MIUI/ColorOS).

#### 2. Connect Phone via USB:
- Plug your phone into your computer.
- A popup will appear on your phone screen: tap **Always allow from this computer** and select **Allow**.

#### 3. Set Up Port Forwarding (Crucial Step):
Open PowerShell on your computer and run:
```powershell
# If adb is in PATH:
adb reverse tcp:80 tcp:80

# OR using the full Android SDK path:
C:\Users\<YourUsername>\AppData\Local\Android\Sdk\platform-tools\adb.exe reverse tcp:80 tcp:80
```
*(This command tunnels port 80 so `localhost` inside the phone reaches port 80 on your PC).*

#### 4. Configure `local.properties`:
In the repository, open `android-app/local.properties` (create it if missing) and ensure it has:
```properties
sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
base.url=http://localhost/bvm_events/
```

#### 5. Open Project and Run:
1. Open **Android Studio**.
2. Click **Open** and select the `android-app` directory.
3. Wait for Gradle sync to complete.
4. Select your connected phone from the top device dropdown.
5. Click the green **Run (▶)** button (or press `Shift + F10`).

---

### 🔹 Method B: Using Android Studio Emulator

1. In Android Studio, open the **Device Manager** (right toolbar).
2. Create and start any Virtual Device running **API 26 to 34** with Google Play Services.
3. In `android-app/local.properties`, set:
   ```properties
   base.url=http://10.0.2.2/bvm_events/
   ```
   *(Android emulators use `10.0.2.2` as an alias to connect to the host computer's `localhost`).*
4. Click the green **Run (▶)** button in Android Studio.

---

### 🔹 Method C: Real Phone via Wi-Fi (Without USB Cable)

1. Connect both your computer and phone to the **same Wi-Fi network**.
2. Find your computer's local IP:
   - In PowerShell, run `ipconfig`.
   - Find your **IPv4 Address** (e.g. `192.168.1.45`).
3. In `android-app/local.properties`, set:
   ```properties
   base.url=http://192.168.1.45/bvm_events/
   ```
4. Build the APK in Android Studio:
   - Click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
5. Transfer the generated APK:
   - Found at: `android-app/app/build/outputs/apk/debug/app-debug.apk`.
   - Send to phone via WhatsApp / Google Drive / Bluetooth and install it.

---

## Part 3: Test Accounts & Verification

Once the app launches on the phone, log in with any of the seeded roles:

### 1. Student Role:
- **Email:** `rahul@bvm.edu`
- **Password:** `password123`
- **Features to try:**
  - Browse upcoming and ongoing campus events.
  - Search events and filter by category (Technical, Cultural, Sports, Other).
  - Tap bookmark icon: sets an exact 15-minute advance reminder alarm.
  - Tap **Check In (GPS)** when an event is ongoing at the venue to log attendance.
  - View verified check-in history under the **Attendance** tab.

### 2. Organizer Role:
- **Email:** `acm@bvm.edu`
- **Password:** `password123`
- **Features to try:**
  - View created events under **My Events**.
  - Tap `+` FAB to create a new event with Date & Time pickers and Venue dropdown.
  - Open event details > tap **Upload Photos** to capture or pick images from gallery.
  - Tap **Attendees** to view checked-in students and tap **Export CSV** to share attendee records.

### 3. Admin Role:
- **Email:** `admin@bvm.local`
- **Password:** `admin123`
- **Features to try:**
  - Register a new account with role **Organizer** from the app login screen. Notice they are blocked with *"Account awaiting administrator approval"*.
  - Log in as Admin: open the **Approvals** tab and tap **Approve** or **Reject**.
  - Switch to the **Events** tab: Admin can delete any event across all organizers.

---

## 🛠️ Common Troubleshooting

| Issue | Cause | Solution |
|---|---|---|
| **"No internet or unable to reach server"** | Phone cannot route to PC's `localhost` | If using USB, run `adb reverse tcp:80 tcp:80` in terminal. If on Wi-Fi, ensure PC and phone are on the exact same Wi-Fi network and Windows Firewall allows port 80. |
| **"Unable to connect to MySQL"** | MySQL service stopped | Open XAMPP Control Panel and ensure the **MySQL** module is running (green). |
| **"Organizer account pending approval"** | New organizers default to unapproved | Log in as Admin (`admin@bvm.local` / `admin123`) and approve the organizer from the Approvals tab. |
| **"Out of range" during check-in** | Student is physically outside venue radius | GPS check-in uses geofencing based on BVM campus venue coordinates. To test check-in, set the event venue to your current location or test near the venue coordinates. |
