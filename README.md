# BVM Events

**BVM Events** is a native Android campus event discovery and GPS-based attendance management system built for Birla Vishvakarma Mahavidyalaya (BVM) students, organizers, and campus administrators.

The system was designed and developed for **Course 4CP49: Android Programming** (Birla Vishvakarma Mahavidyalaya Engineering College).

---

## 1. System Overview & Architecture

```
┌────────────────────────────────────────────────────────┐
│                   BVM Android Client                   │
│      Java 17 • minSdk 26 • targetSdk 36 • Material 3    │
│  ViewBinding • Retrofit • OkHttp • Glide • PlayServices │
│             AlarmManager • WorkManager                 │
└───────────────────────────┬────────────────────────────┘
                            │ REST / JSON (HTTP)
                            ▼
┌────────────────────────────────────────────────────────┐
│                    PHP 8 REST API                      │
│     Vanilla PHP 8 • PDO Prepared Statements • IST      │
│  Haversine Geofencing • finfo MIME Safety • Anti-Mock  │
└───────────────────────────┬────────────────────────────┘
                            │ MySQL (port 3306)
                            ▼
┌────────────────────────────────────────────────────────┐
│                    MySQL Database                      │
│  users • venues • events • event_images • attendance   │
│           bookmarks • unique constraints               │
└────────────────────────────────────────────────────────┘
```

---

## 2. Default Seeded Credentials

| Role | Name | Email | Password | Details |
|------|------|-------|----------|---------|
| **Admin** | Campus Administrator | `admin@bvm.local` | `admin123` | Approves pending organizers, deletes any event |
| **Organizer A** | ACM Student Chapter | `acm@bvm.edu` | `password123` | Creates events, uploads posters & photos, exports attendance CSV |
| **Organizer B** | IEEE Student Branch | `ieee@bvm.edu` | `password123` | Independent organizer (cannot modify ACM events) |
| **Student** | Rahul Patel | `rahul@bvm.edu` | `password123` | Enrollment `21CP042`, bookmarks, GPS check-in, attendance history |

*Note: New organizer registrations default to `is_approved = 0` and cannot log in until approved by the Admin in the **Approvals** screen.*

---

## 3. Seeded Campus Venues (BVM Campus Coordinates)

| Venue | Latitude | Longitude | Radius |
|-------|----------|-----------|--------|
| **BVM Auditorium** | 22.552800 | 72.924200 | 80 meters |
| **Central Workshop** | 22.553400 | 72.923800 | 60 meters |
| **Computer Lab 1** | 22.552950 | 72.924650 | 40 meters |
| **Math Lab (Room 201)** | 22.552600 | 72.924400 | 35 meters |
| **Library Lawn** | 22.552400 | 72.923700 | 75 meters |

---

## 4. Setup Instructions

### Backend & Database Setup (Local Dev with XAMPP)

1. **Start Apache & MySQL:**
   - Launch XAMPP Control Panel and start **Apache** and **MySQL**.

2. **Import Database Schema:**
   - Open [phpMyAdmin](http://localhost/phpmyadmin).
   - Create or select database `bvm_events`.
   - Import `database/schema.sql` (creates tables and seeds venues).

3. **Deploy PHP API to Web Root:**
   - Copy or symlink `php-api/` to `C:\xampp\htdocs\bvm_events`.
   - Ensure `C:\xampp\htdocs\bvm_events\uploads` exists and is writable.

4. **Configure Database Credentials:**
   - Copy `php-api/config.sample.php` to `php-api/config.php`:
     ```bash
     cp php-api/config.sample.php php-api/config.php
     ```
   - Verify DB settings (`localhost`, user `root`, empty password).

5. **Seed Administrator:**
   - From project root:
     ```bash
     php database/seed_admin.php "Campus Admin" admin@bvm.local admin123
     ```

6. **Verify API is Live:**
   - Visit [http://localhost/bvm_events/venues/list.php](http://localhost/bvm_events/venues/list.php) in your browser. It should return HTTP 200 with the 5 campus venues.

---

### Android App Setup

1. **Open in Android Studio:**
   - Open `android-app` folder.
   - JDK 17 or JDK 21 (bundled Android Studio JBR).

2. **Connecting Real Android Phone via USB (Recommended):**
   - Enable **Developer Options** and **USB Debugging** on your phone.
   - Connect phone via USB cable and run:
     ```bash
     adb reverse tcp:80 tcp:80
     ```
   - In `android-app/local.properties`:
     ```properties
     base.url=http://localhost/bvm_events/
     ```
   - The phone connects directly to your PC's Apache server through the USB cable.

3. **Alternative (Wi-Fi Local Network):**
   - Connect phone and PC to the same Wi-Fi.
   - Set `base.url=http://<YOUR_PC_IP>/bvm_events/` in `local.properties`.
   - Verify `res/xml/network_security_config.xml` allows cleartext traffic to your IP or localhost.

4. **Build and Install:**
   - Run from Android Studio or terminal:
     ```bash
     ./gradlew assembleDebug
     adb install -r app/build/outputs/apk/debug/app-debug.apk
     ```

---

## 5. Course Syllabus Alignment (4CP49: Android Programming)

| Unit / Concept | Implementation in BVM Events |
|---|---|
| **UI Components & Layouts** | XML layouts, ConstraintLayout, LinearLayout, MaterialCardView, CoordinatorLayout, ViewBinding |
| **Activity & Fragment Lifecycle** | SplashActivity, LoginActivity, RegisterActivity, MainActivity (hosting Fragments), EventDetailActivity, EventFormActivity, ImageUploadActivity, AttendeeListActivity |
| **List & Adapters** | RecyclerViews with custom ViewHolders: `EventAdapter`, `ImageGalleryAdapter`, `AttendanceAdapter`, `AttendeeAdapter`, `PendingOrganizerAdapter` |
| **Networking & REST APIs** | Retrofit 2 + OkHttp 3 with interceptors (Authorization Bearer token header, 401 session expiry redirect), Gson deserialization |
| **Location-Based Services** | FusedLocationProviderClient, runtime permissions (`ACCESS_FINE_LOCATION`), mock location detection (`isMock()`), geofence Haversine verification |
| **Background Processing & Notifications** | AlarmManager exact reminders 15 minutes before bookmarked event starts, BootReceiver to reschedule after restart, WorkManager `UpdateCheckWorker` periodic sync, NotificationManager with Android 8+ NotificationChannel |
| **File Provider & Media** | Camera photo capture via `androidx.core.content.FileProvider`, gallery picker, Bitmap downscaling to max 1600px, multipart image upload |
| **Intents & Sharing** | Implicit intents for CSV attendee export via `Intent.ACTION_SEND` and chooser |
| **Security & Session Management** | Encrypted/SharedPreferences via `SessionManager`, role enforcement, safe file uploads (`.htaccess` execution disabled) |

---

## 6. End-to-End Testing Guide

### A. Organizer Flow
1. Login as `acm@bvm.edu` / `password123`.
2. Tap the `+` FAB on the **My Events** tab.
3. Fill in title, description, category, venue (e.g. Auditorium), and select start and end dates/times.
4. Save event -> redirected to event details.
5. Tap **Upload Photos** -> take photo or pick from gallery -> check "Set as main event poster" -> upload.
6. Check the **Attendees** button -> tap **Export CSV** -> chooser opens to email/share the CSV.

### B. Student Flow (GPS Check-In & Reminders)
1. Login as `rahul@bvm.edu` / `password123`.
2. Browse upcoming and ongoing events on the **Events** tab.
3. Search and filter by category (Technical, Cultural, Sports, Other).
4. Tap an event -> tap the Bookmark icon:
   - Sets exact reminder alarm via `AlarmManager` for 15 minutes before event start.
5. When the event is ongoing, tap **Check In (GPS)**:
   - If outside venue radius: message displays distance in meters and informs you are outside geofence.
   - If inside venue radius: check-in succeeds with green banner and timestamp.
   - If mock location app is active: check-in rejected with mock detection alert.
6. Tap the **Attendance** tab in bottom navigation to view all verified event attendance records.

### C. Admin Flow
1. Register a new user with role **Organizer** (e.g. `robotics@bvm.edu`).
2. Attempt login -> blocked with `"Account awaiting administrator approval"`.
3. Login as Admin (`admin@bvm.local` / `admin123`).
4. In the **Approvals** tab, view the pending organizer.
5. Tap **Approve** -> confirmation dialog -> approved.
6. The organizer can now log in and create events.
7. Switch to the **Events** tab -> tap any event -> tap **Delete** -> confirm -> event and its images are purged.

---

## 7. Known Limitations & Notes

- **HTTP in Local Development**: The backend uses cleartext HTTP for local XAMPP testing. `res/xml/network_security_config.xml` is configured to permit `cleartextTraffic` on `localhost` and `10.0.2.2`. Production deployments should terminate TLS (HTTPS).
- **Periodic Sync Interval**: Android WorkManager enforces a minimum periodic work interval of 15 minutes. Background sync polls `events/updates.php` at this cadence. Immediate updates are available on manual pull-to-refresh.
- **Battery Optimization**: Exact reminder alarms use `setAndAllowWhileIdle()` to fire in Doze mode; users may need to allow "Alarms & reminders" on Android 12+ (API 31+).
