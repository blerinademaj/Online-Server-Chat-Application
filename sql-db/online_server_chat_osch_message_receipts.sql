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
-- Table structure for table `osch_message_receipts`
--

DROP TABLE IF EXISTS `osch_message_receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `osch_message_receipts` (
  `message_id` int(11) NOT NULL,
  `recipient_id` int(11) NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `delivered_at` datetime DEFAULT NULL,
  `seen_at` datetime DEFAULT NULL,
  PRIMARY KEY (`message_id`,`recipient_id`),
  KEY `recipient_id` (`recipient_id`),
  CONSTRAINT `osch_message_receipts_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `osch_messages` (`id`),
  CONSTRAINT `osch_message_receipts_ibfk_2` FOREIGN KEY (`recipient_id`) REFERENCES `osch_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `osch_message_receipts`
--

LOCK TABLES `osch_message_receipts` WRITE;
/*!40000 ALTER TABLE `osch_message_receipts` DISABLE KEYS */;
INSERT INTO `osch_message_receipts` VALUES (19,42,'sent',NULL,NULL),(20,42,'sent',NULL,NULL),(21,42,'sent',NULL,NULL),(22,42,'sent',NULL,NULL),(23,42,'sent',NULL,NULL),(24,42,'sent',NULL,NULL),(25,42,'sent',NULL,NULL),(26,42,'sent',NULL,NULL),(27,42,'sent',NULL,NULL),(28,42,'sent',NULL,NULL),(29,42,'sent',NULL,NULL),(30,42,'sent',NULL,NULL),(31,42,'sent',NULL,NULL),(32,42,'sent',NULL,NULL),(33,42,'sent',NULL,NULL),(34,42,'sent',NULL,NULL),(35,42,'sent',NULL,NULL),(36,42,'sent',NULL,NULL),(37,42,'sent',NULL,NULL),(38,42,'sent',NULL,NULL),(39,42,'sent',NULL,NULL),(40,42,'sent',NULL,NULL),(41,42,'sent',NULL,NULL),(42,42,'sent',NULL,NULL),(43,42,'sent',NULL,NULL),(44,42,'sent',NULL,NULL),(45,42,'sent',NULL,NULL),(46,42,'sent',NULL,NULL),(47,42,'sent',NULL,NULL),(48,42,'sent',NULL,NULL),(49,42,'sent',NULL,NULL),(50,4,'delivered','2025-09-24 17:13:41',NULL),(51,1,'sent',NULL,NULL),(51,42,'sent',NULL,NULL),(52,4,'delivered','2025-09-24 17:32:54',NULL),(52,42,'sent',NULL,NULL),(53,1,'delivered','2025-09-24 17:33:06',NULL),(53,42,'sent',NULL,NULL),(54,1,'delivered','2025-09-24 17:41:24',NULL),(54,4,'sent',NULL,NULL),(54,42,'sent',NULL,NULL),(55,4,'sent',NULL,NULL),(55,35,'delivered','2025-09-24 17:42:02',NULL),(55,42,'sent',NULL,NULL),(56,4,'sent',NULL,NULL),(56,35,'delivered','2025-09-24 17:42:40',NULL),(56,42,'sent',NULL,NULL),(57,35,'delivered','2025-09-24 17:43:20',NULL),(58,4,'sent',NULL,NULL),(58,35,'sent',NULL,NULL),(58,42,'sent',NULL,NULL),(59,4,'sent',NULL,NULL),(59,35,'sent',NULL,NULL),(59,42,'sent',NULL,NULL),(60,4,'sent',NULL,NULL),(60,35,'sent',NULL,NULL),(60,42,'sent',NULL,NULL),(61,4,'sent',NULL,NULL),(61,35,'sent',NULL,NULL),(61,42,'sent',NULL,NULL),(62,1,'sent',NULL,NULL),(62,4,'sent',NULL,NULL),(62,35,'sent',NULL,NULL),(62,42,'sent',NULL,NULL),(63,4,'sent',NULL,NULL),(63,35,'sent',NULL,NULL),(63,42,'sent',NULL,NULL),(63,43,'sent',NULL,NULL),(64,4,'sent',NULL,NULL),(64,35,'sent',NULL,NULL),(64,42,'sent',NULL,NULL),(64,43,'sent',NULL,NULL),(65,4,'sent',NULL,NULL),(65,35,'sent',NULL,NULL),(65,42,'sent',NULL,NULL),(65,43,'sent',NULL,NULL),(66,4,'sent',NULL,NULL),(66,35,'sent',NULL,NULL),(66,42,'sent',NULL,NULL),(66,43,'sent',NULL,NULL),(67,4,'sent',NULL,NULL),(67,35,'sent',NULL,NULL),(67,42,'sent',NULL,NULL),(67,43,'sent',NULL,NULL),(83,1,'sent',NULL,NULL),(84,1,'sent',NULL,NULL),(85,1,'sent',NULL,NULL),(86,1,'sent',NULL,NULL),(87,1,'sent',NULL,NULL),(88,1,'sent',NULL,NULL),(89,1,'sent',NULL,NULL),(90,1,'sent',NULL,NULL),(91,1,'sent',NULL,NULL),(92,1,'sent',NULL,NULL),(93,1,'sent',NULL,NULL);
/*!40000 ALTER TABLE `osch_message_receipts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-28 22:53:13
