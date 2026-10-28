<?php
/**
 * Database connection singleton using PDO.
 */

require_once __DIR__ . '/response.php';

class DB {
    private static ?PDO $instance = null;

    public static function getConnection(): PDO {
        if (self::$instance !== null) {
            return self::$instance;
        }

        $configFile = __DIR__ . '/../config.php';
        $sampleConfigFile = __DIR__ . '/../config.sample.php';

        if (file_exists($configFile)) {
            $config = require $configFile;
        } elseif (file_exists($sampleConfigFile)) {
            $config = require $sampleConfigFile;
        } else {
            json_error(500, 'CONFIG_ERROR', 'Database configuration file missing.');
        }

        $tz = $config['app']['timezone'] ?? 'Asia/Kolkata';
        date_default_timezone_set($tz);

        $db = $config['db'];
        $dsn = "mysql:host={$db['host']};port={$db['port']};dbname={$db['name']};charset={$db['charset']}";

        try {
            $pdo = new PDO($dsn, $db['user'], $db['pass'], [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
            ]);

            $mysqlTz = $config['app']['mysql_timezone'] ?? '+05:30';
            $pdo->exec("SET time_zone = '{$mysqlTz}'");

            self::$instance = $pdo;
            return self::$instance;
        } catch (PDOException $e) {
            error_log("Database connection error: " . $e->getMessage());
            json_error(500, 'DB_CONNECTION_ERROR', 'Failed to connect to the database.');
        }
    }
}

