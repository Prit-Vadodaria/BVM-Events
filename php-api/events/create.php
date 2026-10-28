<?php
/**
 * POST /events/create.php
 * Create a new event (Organizers only).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['organizer']);
$organizerId = (int)$user['id'];

$input = get_json_input();
require_fields($input, ['title', 'category', 'venue_id', 'start_time', 'end_time']);

$title = sanitize_string($input['title']);
$description = isset($input['description']) ? sanitize_string($input['description']) : '';
$category = validate_enum($input['category'], ['Technical', 'Cultural', 'Sports', 'Other'], 'category');
$venueId = (int)$input['venue_id'];
$startTime = validate_datetime($input['start_time'], 'start_time');
$endTime = validate_datetime($input['end_time'], 'end_time');

if (strlen($title) < 3) {
    json_error(400, 'VALIDATION_ERROR', 'Title must be at least 3 characters.');
}

// Validate venue exists
$pdo = DB::getConnection();
$venueStmt = $pdo->prepare("SELECT id FROM venues WHERE id = ?");
$venueStmt->execute([$venueId]);
if (!$venueStmt->fetch()) {
    json_error(400, 'INVALID_VENUE', 'The selected venue does not exist.');
}

// Validate start_time and end_time
$startTs = strtotime($startTime);
$endTs = strtotime($endTime);

if ($endTs <= $startTs) {
    json_error(400, 'VALIDATION_ERROR', 'Event end time must be after the start time.');
}

$insertStmt = $pdo->prepare("
    INSERT INTO events (title, description, category, venue_id, start_time, end_time, organizer_id)
    VALUES (?, ?, ?, ?, ?, ?, ?)
");
$insertStmt->execute([$title, $description, $category, $venueId, $startTime, $endTime, $organizerId]);
$eventId = (int)$pdo->lastInsertId();

json_ok(['id' => $eventId], 'Event created successfully.');

