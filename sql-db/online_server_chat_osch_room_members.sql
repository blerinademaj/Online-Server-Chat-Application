CREATE DATABASE  IF NOT EXISTS `online_server_chat` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `online_server_chat`;
-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: online_server_chat
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.28-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `osch_room_members`
--

DROP TABLE IF EXISTS `osch_room_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `osch_room_members` (
  `user_id` int(11) NOT NULL,
  `room_code` char(6) NOT NULL,
  `is_admin` tinyint(1) DEFAULT 0,
  `status` varchar(20) DEFAULT NULL,
  `joined_at` datetime DEFAULT NULL,
  `left_at` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`,`room_code`),
  KEY `room_code` (`room_code`),
  CONSTRAINT `osch_room_members_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `osch_users` (`id`),
  CONSTRAINT `osch_room_members_ibfk_2` FOREIGN KEY (`room_code`) REFERENCES `osch_rooms` (`room_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `osch_room_members`
--

LOCK TABLES `osch_room_members` WRITE;
/*!40000 ALTER TABLE `osch_room_members` DISABLE KEYS */;
INSERT INTO `osch_room_members` VALUES (1,'',0,'active','2025-09-23 22:19:13',NULL),(1,'213728',0,'active','2025-10-23 21:24:28',NULL),(1,'289187',0,'active','2025-10-25 23:17:53',NULL),(4,'',0,'active','2025-09-24 17:16:40',NULL),(35,'',0,'active','2025-09-24 17:41:24',NULL),(42,'',0,'active','2025-09-23 20:25:41',NULL),(43,'',0,'active','2025-09-24 22:39:18',NULL),(43,'069880',0,'active','2025-10-23 22:14:40',NULL),(43,'213728',0,'active','2025-10-23 22:03:13',NULL);
/*!40000 ALTER TABLE `osch_room_members` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-28 22:53:12
