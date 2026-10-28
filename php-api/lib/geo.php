<?php
/**
 * Geographic calculation helpers.
 */

/**
 * Calculates great-circle distance between two GPS coordinates using the Haversine formula.
 *
 * @param float $lat1 Latitude of point 1 in degrees
 * @param float $lon1 Longitude of point 1 in degrees
 * @param float $lat2 Latitude of point 2 in degrees
 * @param float $lon2 Longitude of point 2 in degrees
 * @return float Distance in meters
 */
function haversine_m(float $lat1, float $lon1, float $lat2, float $lon2): float {
    $earthRadius = 6371000.0; // Earth radius in meters

    $dLat = deg2rad($lat2 - $lat1);
    $dLon = deg2rad($lon2 - $lon1);

    $a = sin($dLat / 2) * sin($dLat / 2) +
         cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
         sin($dLon / 2) * sin($dLon / 2);

    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));

    return round($earthRadius * $c, 2);
}

