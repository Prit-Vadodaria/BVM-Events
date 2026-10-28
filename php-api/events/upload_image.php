<?php
/**
 * POST /events/upload_image.php
 * Upload an image (poster or gallery photo) for an event.
 * Multipart/form-data: event_id (int), is_poster (0 or 1), image (file).
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/auth.php';
require_once __DIR__ . '/../lib/response.php';
require_once __DIR__ . '/../lib/validate.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_error(405, 'METHOD_NOT_ALLOWED', 'Only POST requests are allowed.');
}

$user = require_auth(['organizer']);
$userId = (int)$user['id'];

if (!isset($_POST['event_id']) || !is_numeric($_POST['event_id'])) {
    json_error(400, 'VALIDATION_ERROR', "Field 'event_id' is required.");
}

$eventId = (int)$_POST['event_id'];
$isPoster = (isset($_POST['is_poster']) && ((int)$_POST['is_poster'] === 1 || $_POST['is_poster'] === 'true')) ? 1 : 0;

$pdo = DB::getConnection();

// Validate event existence and ownership
$stmt = $pdo->prepare("SELECT id, organizer_id FROM events WHERE id = ?");
$stmt->execute([$eventId]);
$event = $stmt->fetch();

if (!$event) {
    json_error(404, 'EVENT_NOT_FOUND', 'Event not found.');
}

if ((int)$event['organizer_id'] !== $userId) {
    json_error(403, 'NOT_OWNER', 'You do not have permission to upload photos for this event.');
}

// Validate file upload presence
if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    $errCode = $_FILES['image']['error'] ?? 'NO_FILE';
    json_error(400, 'UPLOAD_ERROR', "Image upload failed with error code: {$errCode}");
}

$file = $_FILES['image'];
$maxSize = 5 * 1024 * 1024; // 5 MB

if ($file['size'] > $maxSize) {
    json_error(400, 'FILE_TOO_LARGE', 'The uploaded image exceeds the maximum allowed size of 5 MB.');
}

// Security: Validate actual MIME type using fileinfo (never trust client MIME)
$finfo = new finfo(FILEINFO_MIME_TYPE);
$mime = $finfo->file($file['tmp_name']);

$allowedMimes = [
    'image/jpeg' => 'jpg',
    'image/pjpeg' => 'jpg',
    'image/png' => 'png',
    'image/webp' => 'webp'
];

if (!array_key_exists($mime, $allowedMimes)) {
    json_error(400, 'INVALID_FILE_TYPE', "Invalid image type ({$mime}). Only JPEG, PNG, and WebP images are allowed.");
}

$extension = $allowedMimes[$mime];
$filename = bin2hex(random_bytes(16)) . '.' . $extension;

$uploadDir = __DIR__ . '/../uploads/';
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

$destination = $uploadDir . $filename;

if (!move_uploaded_file($file['tmp_name'], $destination)) {
    json_error(500, 'FILE_SAVE_ERROR', 'Failed to save the uploaded image to storage.');
}

// Store relative path
$relativeUrl = 'uploads/' . $filename;

// If this is set as poster, clear any existing poster flag for this event
if ($isPoster === 1) {
    $clearPosterStmt = $pdo->prepare("UPDATE event_images SET is_poster = 0 WHERE event_id = ?");
    $clearPosterStmt->execute([$eventId]);
}

$insertStmt = $pdo->prepare("INSERT INTO event_images (event_id, image_url, is_poster) VALUES (?, ?, ?)");
$insertStmt->execute([$eventId, $relativeUrl, $isPoster]);
$imageId = (int)$pdo->lastInsertId();

json_ok([
    'id' => $imageId,
    'image_url' => $relativeUrl,
    'is_poster' => ($isPoster === 1)
], 'Image uploaded successfully.');

