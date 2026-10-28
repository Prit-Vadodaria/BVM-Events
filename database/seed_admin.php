<?php
/**
 * CLI script to seed an admin user into the bvm_events database.
 * Usage: php database/seed_admin.php "Admin Name" admin@bvm.local password123
 */

if (php_sapi_name() !== 'cli') {
    die("This script can only be run from the command line.\n");
}

$name = $argv[1] ?? 'Admin';
$email = $argv[2] ?? 'admin@bvm.local';
$password = $argv[3] ?? 'admin123';

if (empty($name) || empty($email) || empty($password)) {
    die("Usage: php seed_admin.php <name> <email> <password>\n");
}

$configFile = __DIR__ . '/../php-api/config.php';
$sampleConfigFile = __DIR__ . '/../php-api/config.sample.php';

if (file_exists($configFile)) {
    $config = require $configFile;
} elseif (file_exists($sampleConfigFile)) {
    $config = require $sampleConfigFile;
} else {
    $config = [
        'db' => [
            'host' => '127.0.0.1',
            'port' => 3306,
            'name' => 'bvm_events',
            'user' => 'root',
            'pass' => '',
            'charset' => 'utf8mb4'
        ]
    ];
}

$dbConfig = $config['db'];
$dsn = "mysql:host={$dbConfig['host']};port={$dbConfig['port']};dbname={$dbConfig['name']};charset={$dbConfig['charset']}";

try {
    $pdo = new PDO($dsn, $dbConfig['user'], $dbConfig['pass'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false
    ]);

    // Check if email already exists
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $existing = $stmt->fetch();

    $hash = password_hash($password, PASSWORD_DEFAULT);

    if ($existing) {
        $update = $pdo->prepare("UPDATE users SET name = ?, password_hash = ?, role = 'admin', is_approved = 1 WHERE email = ?");
        $update->execute([$name, $hash, $email]);
        echo "Admin user updated successfully (ID: {$existing['id']}, Email: {$email})\n";
    } else {
        $insert = $pdo->prepare("INSERT INTO users (name, email, password_hash, role, is_approved) VALUES (?, ?, ?, 'admin', 1)");
        $insert->execute([$name, $email, $hash]);
        $newId = $pdo->lastInsertId();
        echo "Admin user created successfully (ID: {$newId}, Email: {$email})\n";
    }
} catch (PDOException $e) {
    die("Database error: " . $e->getMessage() . "\n");
}

