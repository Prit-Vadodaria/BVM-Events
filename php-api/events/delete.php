<?php
/**
 * POST /events/delete.php
 * Delete an event (Event Organizer owner, or Admin).
 * Also cleans up image files from the uploads/ directory.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['organizer', 'admin']);
$userId = (int)$user['id'];
$userRole = $user['role'];

$input = get_json_input();
require_fields($input, ['id']);
$eventId = (int)$input['id'];

$pdo = DB::getConnection();

// Check event existence and permission
$stmt = $pdo->prepare("SELECT id, organizer_id FROM events WHERE id = ?");
$stmt->execute([$eventId]);
$event = $stmt->fetch();

if (!$event) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

if ($userRole === 'organizer' && (int)$event['organizer_id'] !== $userId) {
    json_error(403, 'NOT_OWNER', 'You do not have permission to delete this event.');
}

// Fetch and delete associated physical image files
$imgStmt = $pdo->prepare("SELECT image_url FROM event_images WHERE event_id = ?");
$imgStmt->execute([$eventId]);
$images = $imgStmt->fetchAll();

foreach ($images as $img) {
    $filePath = __DIR__ . '/../' . ltrim($img['image_url'], '/');
    if (file_exists($filePath) && is_file($filePath)) {
        @unlink($filePath);
    }
}

// Delete event row (Foreign keys cascade to event_images, attendance, bookmarks)
$delStmt = $pdo->prepare("DELETE FROM events WHERE id = ?");
$delStmt->execute([$eventId]);

json_ok(null, 'Event deleted successfully.');

