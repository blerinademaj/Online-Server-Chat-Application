SELECT*FROM ONLINE_SERVER_CHAT.OSCH_USERS;
SELECT*FROM ONLINE_SERVER_CHAT.OSCH_MESSAGES;

ALTER TABLE OSCH_USERS 
MODIFY status VARCHAR(20) NOT NULL DEFAULT 'online';
  
UPDATE OSCH_USERS SET status='offline' WHERE status = 'online' OR status='';

SET SQL_SAFE_UPDATES = 0;

UPDATE OSCH_USERS 
SET status = 'offline' 
WHERE status = 'online' OR status = '';

SET SQL_SAFE_UPDATES = 1;  -- nëse do ta kthesh prapë

SHOW PROCESSLIST;
SELECT CONCAT('KILL ', id, ';') 
FROM information_schema.processlist
WHERE user='root' AND command='Sleep';

KILL 465;

SELECT id, username, status FROM OSCH_USERS WHERE username='blerinademaj21';

SELECT * FROM OSCH_ROOMS ORDER BY created_at DESC LIMIT 5;
SELECT * FROM OSCH_ROOM_MEMBERS ORDER BY joined_at DESC LIMIT 5;

SELECT id, sender_id, room_code, dm_id, msg_type, created_at
FROM OSCH_MESSAGES ORDER BY id DESC LIMIT 20;


CREATE USER IF NOT EXISTS 'chatapp'@'%' IDENTIFIED BY 'StrongPass!123';
GRANT ALL PRIVILEGES ON online_server_chat.* TO 'chatapp'@'%';
FLUSH PRIVILEGES;




-- notes
-- 1) Create a proper app user (do NOT use root across LAN)
CREATE USER 'osch'@'%' IDENTIFIED BY 'STRONG_PASS_HERE';

-- 2) Grant only what you need on your schema
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
ON online_server_chat.* TO 'osch'@'%';
FLUSH PRIVILEGES;

-- (Optional) If user already exists but was local-only:
-- ALTER USER 'osch'@'%' IDENTIFIED BY 'STRONG_PASS_HERE';







