<?php
/**
 * GET /bookmarks/mine.php
 * List all events bookmarked by the logged-in student.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';

$user = require_auth(['student']);
$studentId = (int)$user['id'];

$pdo = DB::getConnection();

$stmt = $pdo->prepare("
    SELECT 
        e.id,
        e.title,
        e.description,
        e.category,
        e.venue_id,
        v.name AS venue_name,
        e.organizer_id,
        u.name AS organizer_name,
        e.start_time,
        e.end_time,
        (
            CASE 
                WHEN NOW() < e.start_time THEN 'upcoming'
                WHEN NOW() BETWEEN e.start_time AND e.end_time THEN 'ongoing'
                ELSE 'ended'
            END
        ) AS status,
        (
            SELECT image_url 
            FROM event_images 
            WHERE event_id = e.id AND is_poster = 1 
            LIMIT 1
        ) AS poster_url,
        1 AS is_bookmarked,
        (
            SELECT COUNT(*) 
            FROM attendance 
            WHERE event_id = e.id
        ) AS attendee_count
    FROM bookmarks b
    JOIN events e ON b.event_id = e.id
    JOIN venues v ON e.venue_id = v.id
    JOIN users u ON e.organizer_id = u.id
    WHERE b.student_id = ?
    ORDER BY e.start_time ASC
");

$stmt->execute([$studentId]);
$events = $stmt->fetchAll();

foreach ($events as &$ev) {
    $ev['id'] = (int)$ev['id'];
    $ev['venue_id'] = (int)$ev['venue_id'];
    $ev['organizer_id'] = (int)$ev['organizer_id'];
    $ev['is_bookmarked'] = true;
    $ev['attendee_count'] = (int)$ev['attendee_count'];
}
unset($ev);

json_ok($events, 'Bookmarked events retrieved successfully.');

