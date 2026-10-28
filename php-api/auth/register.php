<?php
/**
 * POST /auth/register.php
 * Register a student or organizer account.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$input = get_json_input();
require_fields($input, ['name', 'email', 'password', 'role']);

$name = sanitize_string($input['name']);
$email = validate_email($input['email']);
$password = (string)$input['password'];
$role = strtolower(sanitize_string($input['role']));
$enrollmentNo = isset($input['enrollment_no']) ? sanitize_string($input['enrollment_no']) : null;

// Validation rules
if (strlen($name) < 2) {
    json_error(400, 'VALIDATION_ERROR', 'Name must be at least 2 characters.');
}

if (strlen($password) < 6) {
    json_error(400, 'VALIDATION_ERROR', 'Password must be at least 6 characters.');
}

if (!in_array($role, ['student', 'organizer'], true)) {
    json_error(400, 'INVALID_ROLE', "Role must be 'student' or 'organizer'.");
}

if ($role === 'student' && empty($enrollmentNo)) {
    json_error(400, 'VALIDATION_ERROR', 'Enrollment number is required for students.');
}

$pdo = DB::getConnection();

// Check if email already exists
$checkStmt = $pdo->prepare("SELECT id FROM users WHERE email = ? LIMIT 1");
$checkStmt->execute([$email]);
if ($checkStmt->fetch()) {
    json_error(409, 'EMAIL_EXISTS', 'An account with this email already exists.');
}

// Students are automatically approved; organizers require admin approval
$isApproved = ($role === 'student') ? 1 : 0;
$passwordHash = password_hash($password, PASSWORD_DEFAULT);

$insertStmt = $pdo->prepare("
    INSERT INTO users (name, email, password_hash, role, enrollment_no, is_approved)
    VALUES (?, ?, ?, ?, ?, ?)
");
$insertStmt->execute([$name, $email, $passwordHash, $role, $enrollmentNo, $isApproved]);
$userId = (int)$pdo->lastInsertId();

$message = ($role === 'organizer')
    ? 'Registration submitted. Account awaiting administrator approval.'
    : 'Registration successful.';

json_ok([
    'user_id' => $userId,
    'role' => $role,
    'is_approved' => $isApproved
], $message);

