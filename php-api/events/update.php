<?php
/**
 * POST /events/update.php
 * Update an existing event (Event Organizer owner only).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['organizer']);
$userId = (int)$user['id'];

$input = get_json_input();
require_fields($input, ['id', 'title', 'category', 'venue_id', 'start_time', 'end_time']);

$eventId = (int)$input['id'];
$title = sanitize_string($input['title']);
$description = isset($input['description']) ? sanitize_string($input['description']) : '';
$category = validate_enum($input['category'], ['Technical', 'Cultural', 'Sports', 'Other'], 'category');
$venueId = (int)$input['venue_id'];
$startTime = validate_datetime($input['start_time'], 'start_time');
$endTime = validate_datetime($input['end_time'], 'end_time');

if (strlen($title) < 3) {
    json_error(400, 'VALIDATION_ERROR', 'Title must be at least 3 characters.');
}

$pdo = DB::getConnection();

// Check event existence and ownership
$stmt = $pdo->prepare("SELECT id, organizer_id FROM events WHERE id = ?");
$stmt->execute([$eventId]);
$event = $stmt->fetch();

if (!$event) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

if ((int)$event['organizer_id'] !== $userId) {
    json_error(403, 'NOT_OWNER', 'You do not have permission to modify this event.');
}

// Validate venue
$venueStmt = $pdo->prepare("SELECT id FROM venues WHERE id = ?");
$venueStmt->execute([$venueId]);
if (!$venueStmt->fetch()) {
    json_error(400, 'INVALID_VENUE', 'The selected venue does not exist.');
}

// Validate timestamps
if (strtotime($endTime) <= strtotime($startTime)) {
    json_error(400, 'VALIDATION_ERROR', 'Event end time must be after start time.');
}

$updateStmt = $pdo->prepare("
    UPDATE events 
    SET title = ?, description = ?, category = ?, venue_id = ?, start_time = ?, end_time = ?, updated_at = NOW()
    WHERE id = ?
");
$updateStmt->execute([$title, $description, $category, $venueId, $startTime, $endTime, $eventId]);

json_ok(['id' => $eventId], 'Event updated successfully.');
