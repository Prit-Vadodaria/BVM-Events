CREATE DATABASE IF NOT EXISTS bvm_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bvm_events;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('student','organizer','admin') NOT NULL,
  enrollment_no VARCHAR(30) NULL,
  is_approved TINYINT(1) NOT NULL DEFAULT 1,
  token CHAR(64) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_token (token)
);

CREATE TABLE IF NOT EXISTS venues (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  latitude DECIMAL(10,7) NOT NULL,
  longitude DECIMAL(10,7) NOT NULL,
  radius_meters INT NOT NULL DEFAULT 100
);

CREATE TABLE IF NOT EXISTS events (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  category ENUM('Technical','Cultural','Sports','Other') NOT NULL,
  venue_id INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  organizer_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (venue_id) REFERENCES venues(id),
  FOREIGN KEY (organizer_id) REFERENCES users(id),
  INDEX idx_start (start_time), INDEX idx_updated (updated_at)
);

CREATE TABLE IF NOT EXISTS event_images (
  id INT AUTO_INCREMENT PRIMARY KEY,
  event_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_poster TINYINT(1) NOT NULL DEFAULT 0,
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
  id INT AUTO_INCREMENT PRIMARY KEY,
  event_id INT NOT NULL,
  student_id INT NOT NULL,
  check_in_time DATETIME NOT NULL,
  latitude DECIMAL(10,7) NOT NULL,
  longitude DECIMAL(10,7) NOT NULL,
  UNIQUE KEY uq_event_student (event_id, student_id),
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookmarks (
  id INT AUTO_INCREMENT PRIMARY KEY,
  event_id INT NOT NULL,
  student_id INT NOT NULL,
  UNIQUE KEY uq_bm (event_id, student_id),
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed initial campus venues if table is empty
INSERT INTO venues (name, latitude, longitude, radius_meters)
SELECT 'Main Auditorium', 0.0, 0.0, 100
WHERE NOT EXISTS (SELECT 1 FROM venues WHERE name = 'Main Auditorium');

INSERT INTO venues (name, latitude, longitude, radius_meters)
SELECT 'Seminar Hall', 0.0, 0.0, 100
WHERE NOT EXISTS (SELECT 1 FROM venues WHERE name = 'Seminar Hall');

INSERT INTO venues (name, latitude, longitude, radius_meters)
SELECT 'Sports Ground', 0.0, 0.0, 150
WHERE NOT EXISTS (SELECT 1 FROM venues WHERE name = 'Sports Ground');

INSERT INTO venues (name, latitude, longitude, radius_meters)
SELECT 'Computer Lab', 0.0, 0.0, 60
WHERE NOT EXISTS (SELECT 1 FROM venues WHERE name = 'Computer Lab');

