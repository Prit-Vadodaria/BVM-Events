<?php
/**
 * POST /admin/approve_organizer.php
 * Approve or reject a pending organizer account (Admin only).
 * Body: {user_id: int, approve: bool}
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['admin']);

$input = get_json_input();
require_fields($input, ['user_id', 'approve']);

$targetUserId = (int)$input['user_id'];
$approve = (bool)$input['approve'];

$pdo = DB::getConnection();

// Check user existence
$stmt = $pdo->prepare("SELECT id, name, email, role, is_approved FROM users WHERE id = ? LIMIT 1");
$stmt->execute([$targetUserId]);
$targetUser = $stmt->fetch();

if (!$targetUser) {
    json_error(404, 'USER_NOT_FOUND', 'User account not found.');
}

if ($targetUser['role'] !== 'organizer') {
    json_error(400, 'INVALID_USER_ROLE', 'Only organizer accounts can be approved or rejected.');
}

if ($approve) {
    $updateStmt = $pdo->prepare("UPDATE users SET is_approved = 1 WHERE id = ?");
    $updateStmt->execute([$targetUserId]);

    json_ok([
        'user_id' => $targetUserId,
        'approved' => true
    ], "Organizer {$targetUser['name']} has been approved.");
} else {
    // Reject by deleting the pending account
    $delStmt = $pdo->prepare("DELETE FROM users WHERE id = ? AND is_approved = 0");
    $delStmt->execute([$targetUserId]);

    json_ok([
        'user_id' => $targetUserId,
        'approved' => false
    ], "Organizer registration for {$targetUser['name']} has been rejected and removed.");
}

