<?php
/**
 * Authentication and role authorization helper.
 */

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/response.php';

function get_bearer_token(): ?string {
    $header = null;

    if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
        $header = trim($_SERVER['HTTP_AUTHORIZATION']);
    } elseif (isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
        $header = trim($_SERVER['REDIRECT_HTTP_AUTHORIZATION']);
    } elseif (function_exists('getallheaders')) {
        $headers = getallheaders();
        foreach ($headers as $key => $value) {
            if (strcasecmp($key, 'Authorization') === 0) {
                $header = trim($value);
                break;
            }
        }
    }

    if ($header && preg_match('/Bearer\s+(\S+)/i', $header, $matches)) {
        return $matches[1];
    }

    return null;
}

/**
 * Requires a valid Bearer token and optionally verifies user roles.
 *
 * @param array $roles Allowed roles, e.g. ['student', 'organizer', 'admin']
 * @return array User row data
 */
function require_auth(array $roles = []): array {
    $token = get_bearer_token();

    if (!$token) {
        json_error(401, 'UNAUTHORIZED', 'Authentication token required.');
    }

    $pdo = DB::getConnection();
    $stmt = $pdo->prepare("SELECT id, name, email, role, enrollment_no, is_approved FROM users WHERE token = ? LIMIT 1");
    $stmt->execute([$token]);
    $user = $stmt->fetch();

    if (!$user) {
        json_error(401, 'INVALID_TOKEN', 'Invalid or expired session token.');
    }

    // Check if organizer is approved
    if ($user['role'] === 'organizer' && (int)$user['is_approved'] !== 1) {
        json_error(403, 'ACCOUNT_PENDING', 'Account awaiting administrator approval.');
    }

    // Role check if specific roles are required
    if (!empty($roles) && !in_array($user['role'], $roles, true)) {
        json_error(403, 'FORBIDDEN', 'You do not have permission to perform this action.');
    }

    return $user;
}

