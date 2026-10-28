<?php
/**
 * GET /admin/pending_organizers.php
 * List unapproved organizer accounts (Admin only).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';

$user = require_auth(['admin']);

$pdo = DB::getConnection();
$stmt = $pdo->query("
    SELECT id, name, email, created_at 
    FROM users 
    WHERE role = 'organizer' AND is_approved = 0 
    ORDER BY created_at ASC
");
$pending = $stmt->fetchAll();

foreach ($pending as &$row) {
    $row['id'] = (int)$row['id'];
}
unset($row);

json_ok($pending, 'Pending organizers retrieved successfully.');
