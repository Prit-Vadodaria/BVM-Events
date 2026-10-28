<?php
/**
 * POST /auth/logout.php
 * Invalidate current session token.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth();

$pdo = DB::getConnection();
$stmt = $pdo->prepare("UPDATE users SET token = NULL WHERE id = ?");
$stmt->execute([$user['id']]);

json_ok(null, 'Logged out successfully.');

