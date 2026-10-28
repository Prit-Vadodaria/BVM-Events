<?php
/**
 * Request validation and helper functions.
 */

require_once __DIR__ . '/response.php';

function get_json_input(): array {
    $raw = file_get_contents('php://input');
    if ($raw === false || trim($raw) === '') {
        return [];
    }

    $decoded = json_decode($raw, true);
    if (!is_array($decoded)) {
        json_error(400, 'INVALID_JSON', 'Malformed or invalid JSON payload.');
    }

    return $decoded;
}

function require_fields(array $data, array $fields): void {
    foreach ($fields as $field) {
        if (!isset($data[$field]) || (is_string($data[$field]) && trim($data[$field]) === '')) {
            json_error(400, 'VALIDATION_ERROR', "Field '{$field}' is required.");
        }
    }
}

function sanitize_string(?string $val): string {
    return trim($val ?? '');
}

function validate_email(string $email): string {
    $email = trim($email);
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        json_error(400, 'INVALID_EMAIL', 'A valid email address is required.');
    }
    return $email;
}

function validate_datetime(string $dt, string $fieldName = 'datetime'): string {
    $dt = trim($dt);
    $d = DateTime::createFromFormat('Y-m-d H:i:s', $dt);
    if (!$d || $d->format('Y-m-d H:i:s') !== $dt) {
        json_error(400, 'INVALID_DATETIME', "Field '{$fieldName}' must be in 'YYYY-MM-DD HH:MM:SS' format.");
    }
    return $dt;
}

function validate_enum(string $val, array $allowed, string $fieldName = 'field'): string {
    if (!in_array($val, $allowed, true)) {
        json_error(400, 'INVALID_VALUE', "Field '{$fieldName}' must be one of: " . implode(', ', $allowed));
    }
    return $val;
}

