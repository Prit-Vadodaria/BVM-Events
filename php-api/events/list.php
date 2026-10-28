<?php
/**
 * GET /events/list.php
 * List events with filtering, search, status calculation, bookmark and attendance counts.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

$user = require_auth();
$userId = (int)$user['id'];
$userRole = $user['role'];

$category = isset($_GET['category']) ? sanitize_string($_GET['category']) : null;
$search = isset($_GET['search']) ? sanitize_string($_GET['search']) : null;
$statusFilter = isset($_GET['status']) ? strtolower(sanitize_string($_GET['status'])) : null;
$mine = isset($_GET['mine']) && ($_GET['mine'] === '1' || $_GET['mine'] === 'true');

$pdo = DB::getConnection();

// Base query
$query = "
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
        (
            SELECT COUNT(*) 
            FROM bookmarks 
            WHERE event_id = e.id AND student_id = ?
        ) AS is_bookmarked,
        (
            SELECT COUNT(*) 
            FROM attendance 
            WHERE event_id = e.id
        ) AS attendee_count
    FROM events e
    JOIN venues v ON e.venue_id = v.id
    JOIN users u ON e.organizer_id = u.id
    WHERE 1=1
";

$params = [$userId];

// Organizer "mine" filter
if ($mine) {
    if ($userRole !== 'organizer' && $userRole !== 'admin') {
        json_error(403, 'FORBIDDEN', 'Only organizers can view their own events.');
    }
    $query .= " AND e.organizer_id = ?";
    $params[] = $userId;
}

// Category filter
if (!empty($category) && $category !== 'All') {
    $query .= " AND e.category = ?";
    $params[] = $category;
}

// Search filter (LIKE on title)
if (!empty($search)) {
    $query .= " AND e.title LIKE ?";
    $params[] = '%' . $search . '%';
}

// Status filter
if ($statusFilter === 'upcoming') {
    $query .= " AND NOW() < e.start_time";
} elseif ($statusFilter === 'ongoing') {
    $query .= " AND NOW() BETWEEN e.start_time AND e.end_time";
} elseif ($statusFilter === 'all') {
    // No time constraint
} else {
    // Default: upcoming + ongoing (not ended)
    if (!$mine) {
        $query .= " AND e.end_time >= NOW()";
    }
}

$query .= " ORDER BY e.start_time ASC";

$stmt = $pdo->prepare($query);
$stmt->execute($params);
$events = $stmt->fetchAll();

// Convert numeric types
foreach ($events as &$ev) {
    $ev['id'] = (int)$ev['id'];
    $ev['venue_id'] = (int)$ev['venue_id'];
    $ev['organizer_id'] = (int)$ev['organizer_id'];
    $ev['is_bookmarked'] = ((int)$ev['is_bookmarked']) > 0;
    $ev['attendee_count'] = (int)$ev['attendee_count'];
}
unset($ev);

json_ok($events, 'Events retrieved successfully.');

