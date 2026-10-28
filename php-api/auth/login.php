<?php
/**
 * POST /auth/login.php
 * Authenticates user credentials and returns a Bearer session token.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$input = get_json_input();
require_fields($input, ['email', 'password']);

$email = validate_email($input['email']);
$password = (string)$input['password'];

$pdo = DB::getConnection();
$stmt = $pdo->prepare("SELECT id, name, email, password_hash, role, enrollment_no, is_approved FROM users WHERE email = ? LIMIT 1");
$stmt->execute([$email]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password_hash'])) {
    json_error(401, 'INVALID_CREDENTIALS', 'Invalid email or password.');
}

// Check approval for organizers
if ($user['role'] === 'organizer' && (int)$user['is_approved'] !== 1) {
    json_error(403, 'ACCOUNT_PENDING', 'Account awaiting administrator approval.');
}

// Generate secure 64-char token (32 random bytes in hex)
$token = bin2hex(random_bytes(32));

$updateStmt = $pdo->prepare("UPDATE users SET token = ? WHERE id = ?");
$updateStmt->execute([$token, $user['id']]);

json_ok([
    'token' => $token,
    'user' => [
        'id' => (int)$user['id'],
        'name' => $user['name'],
        'email' => $user['email'],
        'role' => $user['role'],
        'enrollment_no' => $user['enrollment_no']
    ]
], 'Login successful.');

