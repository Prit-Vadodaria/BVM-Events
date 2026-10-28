<?php
/**
 * GET /events/updates.php?since=yyyy-MM-dd HH:mm:ss
 * Returns new or modified events since a given timestamp.
 * Used by WorkManager background polling.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

$user = require_auth();

$since = isset($_GET['since']) ? sanitize_string($_GET['since']) : null;

if (empty($since)) {
    // Default to 24 hours ago if not specified
    $since = date('Y-m-d H:i:s', time() - 86400);
} else {
    $since = validate_datetime($since, 'since');
}

$pdo = DB::getConnection();

$stmt = $pdo->prepare("
    SELECT 
        id,
        title,
        (created_at > ?) AS is_new,
        updated_at,
        start_time,
        end_time
    FROM events
    WHERE updated_at > ?
      AND end_time >= NOW()
    ORDER BY updated_at ASC
");

$stmt->execute([$since, $since]);
$rows = $stmt->fetchAll();

$updates = [];
foreach ($rows as $r) {
    $updates[] = [
        'id' => (int)$r['id'],
        'title' => $r['title'],
        'is_new' => (int)$r['is_new'] === 1,
        'updated_at' => $r['updated_at']
    ];
}

json_ok([
    'events' => $updates,
    'count' => count($updates)
], 'Event updates retrieved successfully.');

