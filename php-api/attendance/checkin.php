<?php
/**
 * POST /attendance/checkin.php
 * GPS Check-In for students with geofence validation and anti-spoofing checks.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';
require_once __DIR__ . '/../lib/geo.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['student']);
$studentId = (int)$user['id'];

$input = get_json_input();
require_fields($input, ['event_id', 'lat', 'lng']);

$eventId = (int)$input['event_id'];
$lat = (float)$input['lat'];
$lng = (float)$input['lng'];
$isMock = !empty($input['is_mock']);

$pdo = DB::getConnection();

// 1. Event exists
$stmt = $pdo->prepare("
    SELECT e.id, e.title, e.start_time, e.end_time, 
           v.latitude, v.longitude, v.radius_meters,
           (NOW() BETWEEN e.start_time AND e.end_time) AS is_ongoing
    FROM events e
    JOIN venues v ON e.venue_id = v.id
    WHERE e.id = ?
    LIMIT 1
");
$stmt->execute([$eventId]);
$event = $stmt->fetch();

if (!$event) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

// 2. Mock location check
if ($isMock) {
    json_error(403, 'MOCK_LOCATION', 'Mock or simulated location detected. Please use genuine GPS.');
}

// 3. Event is currently ongoing check
if ((int)$event['is_ongoing'] !== 1) {
    json_error(400, 'EVENT_NOT_ONGOING', 'Check-in is only available while the event is ongoing.');
}

// 4. Geofence radius check (Haversine distance)
$venueLat = (float)$event['latitude'];
$venueLng = (float)$event['longitude'];
$radiusM = (int)$event['radius_meters'];

$distanceM = haversine_m($lat, $lng, $venueLat, $venueLng);

if ($distanceM > $radiusM) {
    if (!headers_sent()) {
        header('Content-Type: application/json; charset=utf-8');
        http_response_code(400);
    }
    echo json_encode([
        'success' => false,
        'message' => "You are outside the venue radius. You are {$distanceM}m away (must be within {$radiusM}m).",
        'error_code' => 'OUT_OF_RANGE',
        'data' => [
            'distance_m' => $distanceM,
            'radius_m' => $radiusM
        ],
        'server_time' => get_server_time()
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

// 5. Insert attendance record (Unique key constraint uq_event_student prevents duplicates)
$checkInTime = get_server_time();

try {
    $insertStmt = $pdo->prepare("
        INSERT INTO attendance (event_id, student_id, check_in_time, latitude, longitude)
        VALUES (?, ?, ?, ?, ?)
    ");
    $insertStmt->execute([$eventId, $studentId, $checkInTime, $lat, $lng]);

    json_ok([
        'check_in_time' => $checkInTime,
        'distance_m' => $distanceM
    ], 'Check-in successful! Attendance recorded.');

} catch (PDOException $e) {
    // MySQL 1062 = Duplicate entry for unique key
    if ($e->errorInfo[1] == 1062) {
        json_error(409, 'ALREADY_CHECKED_IN', 'You have already checked in for this event.');
    }
    throw $e;
}
