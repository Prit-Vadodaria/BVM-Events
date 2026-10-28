<?php
/**
 * GET /attendance/event.php?id={id}
 * List students who checked into an event (Organizer owner or Admin only).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

$user = require_auth(['organizer', 'admin']);
$userId = (int)$user['id'];
$userRole = $user['role'];

if (!isset($_GET['id']) || !is_numeric($_GET['id'])) {
    json_error(400, 'VALIDATION_ERROR', "Parameter 'id' is required.");
}

$eventId = (int)$_GET['id'];
$pdo = DB::getConnection();

// Validate event existence and owner
$stmt = $pdo->prepare("SELECT id, title, organizer_id FROM events WHERE id = ?");
$stmt->execute([$eventId]);
$event = $stmt->fetch();

if (!$event) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

if ($userRole === 'organizer' && (int)$event['organizer_id'] !== $userId) {
    json_error(403, 'NOT_OWNER', 'You do not have permission to view attendance for this event.');
}

// Fetch attendee list
$attStmt = $pdo->prepare("
    SELECT 
        a.student_id,
        u.name,
        u.enrollment_no,
        a.check_in_time
    FROM attendance a
    JOIN users u ON a.student_id = u.id
    WHERE a.event_id = ?
    ORDER BY a.check_in_time ASC
");
$attStmt->execute([$eventId]);
$attendees = $attStmt->fetchAll();

foreach ($attendees as &$a) {
    $a['student_id'] = (int)$a['student_id'];
}
unset($a);

json_ok($attendees, 'Attendee list retrieved successfully.', [
    'event_title' => $event['title'],
    'total_count' => count($attendees)
]);

