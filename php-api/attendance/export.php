<?php
/**
 * GET /attendance/export.php?id={id}
 * Streams attendance list as CSV file (Organizer owner or Admin only).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';

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
    json_error(403, 'NOT_OWNER', 'You do not have permission to export attendance for this event.');
}

// Fetch attendee list
$attStmt = $pdo->prepare("
    SELECT 
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

// Output CSV headers
header('Content-Type: text/csv; charset=utf-8');
header('Content-Disposition: attachment; filename="attendance_' . $eventId . '.csv"');
header('Pragma: no-cache');
header('Expires: 0');

$output = fopen('php://output', 'w');

// Write UTF-8 BOM so Microsoft Excel correctly displays UTF-8 encoded text
fprintf($output, chr(0xEF) . chr(0xBB) . chr(0xBF));

// Header row
fputcsv($output, ['Name', 'Enrollment No', 'Check-in Time']);

foreach ($attendees as $row) {
    fputcsv($output, [
        $row['name'],
        $row['enrollment_no'] ?? 'N/A',
        $row['check_in_time']
    ]);
}

fclose($output);
exit;
