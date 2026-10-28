<?php
/**
 * GET /venues/list.php
 * Returns list of campus venues.
 */

require_once __DIR__ . '/../lib/db.php';
require_once __DIR__ . '/../lib/response.php';

$pdo = DB::getConnection();
$stmt = $pdo->query("SELECT id, name, latitude, longitude, radius_meters FROM venues ORDER BY name ASC");
$venues = $stmt->fetchAll();

// Format numbers as numeric types
foreach ($venues as &$v) {
    $v['id'] = (int)$v['id'];
    $v['latitude'] = (float)$v['latitude'];
    $v['longitude'] = (float)$v['longitude'];
    $v['radius_meters'] = (int)$v['radius_meters'];
}
unset($v);

json_ok($venues, 'Venues retrieved successfully.');

