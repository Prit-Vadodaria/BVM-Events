<?php
/**
 * Database and application configuration template.
 * Copy this file to config.php and update your credentials.
 */

return [
    'db' => [
        'host' => '127.0.0.1',
        'port' => 3306,
        'name' => 'bvm_events',
        'user' => 'root',
        'pass' => '',
        'charset' => 'utf8mb4'
    ],
    'app' => [
        'timezone' => 'Asia/Kolkata',
        'mysql_timezone' => '+05:30',
        'upload_dir' => __DIR__ . '/uploads/',
        'max_upload_size' => 5 * 1024 * 1024 // 5 MB
    ]
];

