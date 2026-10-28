<?php
/**
 * POST /bookmarks/toggle.php
 * Toggles bookmark status for an event (Student only).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['student']);
$studentId = (int)$user['id'];

$input = get_json_input();
require_fields($input, ['event_id']);
$eventId = (int)$input['event_id'];

$pdo = DB::getConnection();

// Validate event existence
$checkEvent = $pdo->prepare("SELECT id, title, start_time FROM events WHERE id = ?");
$checkEvent->execute([$eventId]);
$event = $checkEvent->fetch();

if (!$event) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

// Check existing bookmark
$stmt = $pdo->prepare("SELECT id FROM bookmarks WHERE event_id = ? AND student_id = ?");
$stmt->execute([$eventId, $studentId]);
$existing = $stmt->fetch();

if ($existing) {
    // Remove bookmark
    $del = $pdo->prepare("DELETE FROM bookmarks WHERE event_id = ? AND student_id = ?");
    $del->execute([$eventId, $studentId]);

    json_ok([
        'bookmarked' => false,
        'event_id' => $eventId
    ], 'Event removed from bookmarks.');
} else {
    // Add bookmark
    $ins = $pdo->prepare("INSERT INTO bookmarks (event_id, student_id) VALUES (?, ?)");
    $ins->execute([$eventId, $studentId]);

    json_ok([
        'bookmarked' => true,
        'event_id' => $eventId,
        'start_time' => $event['start_time'],
        'title' => $event['title']
    ], 'Event added to bookmarks.');
}

