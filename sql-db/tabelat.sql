CREATE DATABASE online_server_chat;
USE online_server_chat;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(20) NOT NULL,
    avatar_path VARCHAR(100) DEFAULT 'avatars/default.png',
    role ENUM('admin', 'user') DEFAULT 'user',
    status ENUM('online', 'offline', 'busy', 'away') DEFAULT 'offline',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    room_code CHAR(6)
);

ALTER TABLE users 
DROP COLUMN room_code,
ADD COLUMN ip_address VARCHAR(45) AFTER join_time;



USE online_server_chat;
/*
INSERT INTO users (username, password, first_name, last_name, role, status)
VALUES 
('testuser', 'passkey123', 'Test', 'User', 'admin', 'online'),
('alpha', 'alpha123', 'Alpha', 'Bravo', 'user', 'online');
*/
DESC users;
SELECT * FROM users;
CREATE TABLE chat_rooms (
    room_code CHAR(6) PRIMARY KEY,
    created_by VARCHAR(20),
    max_users INT CHECK (max_users BETWEEN 1 AND 10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(20) NOT NULL,
    receiver VARCHAR(20),               -- NULL për mesazhe grupore
    room_code CHAR(6),                  -- NULL për mesazhe private
    content TEXT NOT NULL,
    message_type ENUM('text', 'file') DEFAULT 'text',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (sender) REFERENCES users(username),
    FOREIGN KEY (receiver) REFERENCES users(username)
);

select*from users where status like '%online%'
UPDATE `online_server_chat`.`users` SET `username` = 'blerinademaj21' WHERE (`id` = '1');


UPDATE `online_server_chat`.`users` SET `id` = '40' WHERE (`id` = '1');
INSERT INTO `online_server_chat`.`users` (`id`, `first_name`, `last_name`, `username`, `password`, `avatar_path`, `role`, `status`, `join_time`) VALUES ('1', 'Blerina', 'Demaj', 'blerina.demaj', 'Blerin@*21', 'avatars/default.png', 'admin', 'online', '2025-02-21 18:12:14');


