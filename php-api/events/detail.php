<?php
/**
 * GET /events/detail.php?id={id}
 * Returns full details of an event including venue coordinates, organizer, gallery images, bookmark & checkin status.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

$user = require_auth();
$userId = (int)$user['id'];

if (!isset($_GET['id']) || !is_numeric($_GET['id'])) {
    json_error(400, 'VALIDATION_ERROR', "Parameter 'id' is required and must be an integer.");
}

$eventId = (int)$_GET['id'];
$pdo = DB::getConnection();

$stmt = $pdo->prepare("
    SELECT 
        e.id,
        e.title,
        e.description,
        e.category,
        e.start_time,
        e.end_time,
        (
            CASE 
                WHEN NOW() < e.start_time THEN 'upcoming'
                WHEN NOW() BETWEEN e.start_time AND e.end_time THEN 'ongoing'
                ELSE 'ended'
            END
        ) AS status,
        v.id AS v_id,
        v.name AS v_name,
        v.latitude AS v_latitude,
        v.longitude AS v_longitude,
        v.radius_meters AS v_radius,
        u.id AS org_id,
        u.name AS org_name,
        u.email AS org_email,
        (
            SELECT COUNT(*) 
            FROM bookmarks 
            WHERE event_id = e.id AND student_id = ?
        ) AS is_bookmarked,
        (
            SELECT COUNT(*) 
            FROM attendance 
            WHERE event_id = e.id AND student_id = ?
        ) AS has_checked_in,
        (
            SELECT COUNT(*) 
            FROM attendance 
            WHERE event_id = e.id
        ) AS attendee_count
    FROM events e
    JOIN venues v ON e.venue_id = v.id
    JOIN users u ON e.organizer_id = u.id
    WHERE e.id = ?
    LIMIT 1
");

$stmt->execute([$userId, $userId, $eventId]);
$row = $stmt->fetch();

if (!$row) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

// Fetch images for this event
$imgStmt = $pdo->prepare("SELECT id, image_url, is_poster FROM event_images WHERE event_id = ? ORDER BY is_poster DESC, id ASC");
$imgStmt->execute([$eventId]);
$rawImages = $imgStmt->fetchAll();

$images = [];
foreach ($rawImages as $img) {
    $images[] = [
        'id' => (int)$img['id'],
        'image_url' => $img['image_url'],
        'is_poster' => (int)$img['is_poster'] === 1
    ];
}

$eventDetail = [
    'id' => (int)$row['id'],
    'title' => $row['title'],
    'description' => $row['description'],
    'category' => $row['category'],
    'start_time' => $row['start_time'],
    'end_time' => $row['end_time'],
    'status' => $row['status'],
    'venue' => [
        'id' => (int)$row['v_id'],
        'name' => $row['v_name'],
        'latitude' => (float)$row['v_latitude'],
        'longitude' => (float)$row['v_longitude'],
        'radius_meters' => (int)$row['v_radius']
    ],
    'organizer' => [
        'id' => (int)$row['org_id'],
        'name' => $row['org_name'],
        'email' => $row['org_email']
    ],
    'images' => $images,
    'is_bookmarked' => ((int)$row['is_bookmarked']) > 0,
    'has_checked_in' => ((int)$row['has_checked_in']) > 0,
    'attendee_count' => (int)$row['attendee_count']
];

json_ok($eventDetail, 'Event details retrieved successfully.');
