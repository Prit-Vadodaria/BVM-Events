<?php
/**
 * GET /attendance/mine.php
 * List of events attended by the logged-in student, ordered newest first.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';

$user = require_auth(['student']);
$studentId = (int)$user['id'];

$pdo = DB::getConnection();
$stmt = $pdo->prepare("
    SELECT 
        a.event_id,
        e.title,
        e.category,
        v.name AS venue_name,
        a.check_in_time
    FROM attendance a
    JOIN events e ON a.event_id = e.id
    JOIN venues v ON e.venue_id = v.id
    WHERE a.student_id = ?
    ORDER BY a.check_in_time DESC
");

$stmt->execute([$studentId]);
$history = $stmt->fetchAll();

foreach ($history as &$row) {
    $row['event_id'] = (int)$row['event_id'];
}
unset($row);

json_ok($history, 'Attendance history retrieved successfully.');
