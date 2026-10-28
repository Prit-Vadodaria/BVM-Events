<?php
/**
 * Global response and error handling functions.
 */

// Fail loudly in dev logs, cleanly in HTTP responses (no HTML warnings/errors leaked)
ini_set('display_errors', '0');
ini_set('log_errors', '1');
error_reporting(E_ALL);

function get_server_time(): string {
    return date('Y-m-d H:i:s');
}

/**
 * Sends a successful JSON response and terminates.
 *
 * @param mixed $data Data payload
 * @param string $message Friendly message
 * @param array $extra Optional extra top-level fields (e.g. server_time)
 */
function json_ok($data = null, string $message = '', array $extra = []): void {
    if (!headers_sent()) {
        header('Content-Type: application/json; charset=utf-8');
        http_response_code(200);
    }

    $response = [
        'success' => true,
        'message' => $message,
        'data' => $data,
        'server_time' => get_server_time()
    ];

    if (!empty($extra)) {
        $response = array_merge($response, $extra);
    }

    echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

/**
 * Sends a JSON error response and terminates.
 *
 * @param int $statusCode HTTP status code (400, 401, 403, 404, 409, 500)
 * @param string $errorCode Machine-readable error code
 * @param string $message Human-readable error message
 */
function json_error(int $statusCode, string $errorCode, string $message): void {
    if (!headers_sent()) {
        header('Content-Type: application/json; charset=utf-8');
        http_response_code($statusCode);
    }

    $response = [
        'success' => false,
        'message' => $message,
        'error_code' => $errorCode,
        'server_time' => get_server_time()
    ];

    echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

// Global exception handler
set_exception_handler(function (Throwable $e) {
    error_log("Unhandled Exception: " . $e->getMessage() . " in " . $e->getFile() . ":" . $e->getLine());
    json_error(500, 'SERVER_ERROR', 'Internal server error. Please try again later.');
});

