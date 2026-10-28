# BVM Events

**BVM Events** is an Android campus event discovery and GPS-based attendance management system built for Birla Vishvakarma Mahavidyalaya (BVM) students and event organizers.

The system consists of:
1. **Android Application (`android-app/`)**: Built in Java with Material Design, ViewBinding, Retrofit, Glide, Google Play Services Location, and WorkManager.
2. **Backend API (`php-api/`)**: Built in PHP 8 with PDO prepared statements and REST JSON endpoints.
3. **Database (`database/`)**: MySQL database with geofenced venues, role-based authorization, and attendance logging.

---

## 1. System Requirements

- **PHP**: PHP 8.0+ (e.g. via [XAMPP](https://www.apachefriends.org/))
- **Database**: MySQL 5.7+ / MariaDB 10.4+
- **Android**: Android Studio Hedgehog / Iguana / Jellyfish (JDK 17 or JDK 21), Android device running Android 8.0+ (API 26+)

---

## 2. Backend & Database Setup (Local Dev with XAMPP)

1. **Start Apache & MySQL:**
   - Open the XAMPP Control Panel and start **Apache** and **MySQL**.

2. **Import Database Schema:**
   - Open [phpMyAdmin](http://localhost/phpmyadmin) in your browser.
   - Import `database/schema.sql`, or run in terminal:
     ```bash
     mysql -u root -p < database/schema.sql
     ```

3. **Deploy PHP API to Web Root:**
   - Link or copy `php-api/` to `C:\xampp\htdocs\bvm_events`:
     ```cmd
     mklink /J "C:\xampp\htdocs\bvm_events" "<path-to-repo>\php-api"
     ```
   - Alternatively, copy the `php-api` folder directly into `C:\xampp\htdocs\` and rename it to `bvm_events`.

4. **Configure Database Credentials:**
   - Copy `php-api/config.sample.php` to `php-api/config.php`:
     ```bash
     cp php-api/config.sample.php php-api/config.php
     ```
   - Update `config.php` with your MySQL user/password (default in XAMPP is user `root` with empty password `""`).

5. **Seed the Admin Account:**
   - Run the admin seed script from CLI:
     ```bash
     php database/seed_admin.php "Campus Admin" admin@bvm.local admin123
     ```

6. **Verify Backend Installation:**
   - Open [http://localhost/bvm_events/venues/list.php](http://localhost/bvm_events/venues/list.php) in your browser or run:
     ```bash
     curl http://localhost/bvm_events/venues/list.php
     ```
   - You should receive a JSON response with status 200 and campus venue coordinates.

---

## 3. Android App Setup

1. **Open Project in Android Studio:**
   - Open the `android-app` folder in Android Studio.
   - Let Gradle sync dependencies.

2. **Configure Base URL:**
   - By default, `local.properties` is configured for USB reverse tethering:
     ```properties
     base.url=http://localhost/bvm_events/
     ```

3. **Connecting a Real Android Phone for Testing:**
   - **Option A (Recommended — USB Debugging + Port Forwarding):**
     1. Connect your Android phone via USB and enable **USB Debugging** in Developer Options.
     2. Run the ADB reverse port forwarding command:
        ```bash
        adb reverse tcp:80 tcp:80
        ```
     3. Keep `base.url=http://localhost/bvm_events/` in `local.properties`. The phone can now directly access your PC's local server via `localhost`!
   - **Option B (Wi-Fi Local Network):**
     1. Connect both PC and phone to the same Wi-Fi.
     2. Find your PC's local IP (e.g. `192.168.1.50`).
     3. Ensure port 80 is allowed in Windows Firewall.
     4. Set `base.url=http://192.168.1.50/bvm_events/` in `local.properties` and add your IP to `res/xml/network_security_config.xml`.

4. **Build and Run:**
   - Click **Run 'app'** or build from terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 4. Architecture & Directory Layout

```
bvm-events/
├── README.md                  # Setup steps, features, screenshots
├── features.md                # System feature specifications
├── IMPLEMENTATION_PLAN.md     # Detailed phase-by-phase implementation plan
├── .gitignore
├── database/
│   ├── schema.sql             # MySQL schema & default venue seed
│   └── seed_admin.php         # CLI script to seed administrator
├── php-api/                   # REST API backend (served at /bvm_events/)
│   ├── .htaccess              # Apache rewrite rules & auth header forwarder
│   ├── config.sample.php      # Template database configuration
│   ├── config.php             # Local credentials (gitignored)
│   ├── lib/
│   │   ├── db.php             # PDO connection singleton (IST timezone)
│   │   ├── response.php       # json_ok(), json_error(), exception handler
│   │   ├── auth.php           # Bearer token validation & role checks
│   │   ├── validate.php       # Request validation & sanitation helpers
│   │   └── geo.php            # Haversine distance calculator
│   ├── auth/                  # register.php, login.php, logout.php
│   ├── events/                # list.php, detail.php, create.php, update.php, delete.php, updates.php, upload_image.php
│   ├── venues/                # list.php
│   ├── attendance/            # checkin.php, event.php, export.php, mine.php
│   ├── bookmarks/             # toggle.php, mine.php
│   ├── admin/                 # pending_organizers.php, approve_organizer.php
│   └── uploads/               # Uploaded event posters & gallery images
└── android-app/               # Native Android application (Java)
```

---

## 5. Security & Design Principles

- **Prepared Statements**: All database operations use PDO prepared statements to prevent SQL injection.
- **Server-driven Timing**: All ongoing/ended event validation and attendance windows use server time in IST (`Asia/Kolkata`), never trusting the device clock.
- **Anti-Mock Location**: FusedLocationProvider mock check (`isMock()`) prevents GPS spoofing.
- **Image Validation**: Uploaded images are validated for size (<= 5 MB) and MIME types using PHP `finfo`, and stored outside public script execution paths.

