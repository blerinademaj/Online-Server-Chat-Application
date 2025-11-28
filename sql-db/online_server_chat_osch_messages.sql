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
-- Table structure for table `osch_messages`
--

DROP TABLE IF EXISTS `osch_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `osch_messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL,
  `room_code` char(6) DEFAULT NULL,
  `dm_id` int(11) DEFAULT NULL,
  `content` text NOT NULL,
  `msg_type` varchar(20) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `room_code` (`room_code`),
  KEY `dm_id` (`dm_id`),
  CONSTRAINT `osch_messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `osch_users` (`id`),
  CONSTRAINT `osch_messages_ibfk_2` FOREIGN KEY (`room_code`) REFERENCES `osch_rooms` (`room_code`),
  CONSTRAINT `osch_messages_ibfk_3` FOREIGN KEY (`dm_id`) REFERENCES `osch_private_dms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `osch_messages`
--

LOCK TABLES `osch_messages` WRITE;
/*!40000 ALTER TABLE `osch_messages` DISABLE KEYS */;
INSERT INTO `osch_messages` VALUES (1,42,'',NULL,'dewfwref','GROUP','2025-09-23 20:25:41'),(2,42,'',NULL,'ewfwefwe','GROUP','2025-09-23 20:25:42'),(3,42,'',NULL,'ewf','GROUP','2025-09-23 20:25:43'),(4,42,'',NULL,'ewf','GROUP','2025-09-23 20:25:43'),(5,42,'',NULL,'ew','GROUP','2025-09-23 20:25:43'),(6,42,'',NULL,'ewf','GROUP','2025-09-23 20:25:44'),(7,42,'',NULL,'ew','GROUP','2025-09-23 20:25:44'),(8,42,'',NULL,'few','GROUP','2025-09-23 20:25:44'),(9,42,'',NULL,'few','GROUP','2025-09-23 20:25:44'),(10,42,'',NULL,'fe','GROUP','2025-09-23 20:25:44'),(11,42,'',NULL,'e','GROUP','2025-09-23 20:25:45'),(12,42,'',NULL,'fe','GROUP','2025-09-23 20:25:45'),(13,42,'',NULL,'fe','GROUP','2025-09-23 20:25:45'),(14,42,'',NULL,'fe','GROUP','2025-09-23 20:25:45'),(15,42,'',NULL,'fe','GROUP','2025-09-23 20:25:46'),(16,42,'',NULL,'wf','GROUP','2025-09-23 20:25:46'),(17,42,'',NULL,'ew','GROUP','2025-09-23 20:25:46'),(18,42,'',NULL,'fw','GROUP','2025-09-23 20:25:46'),(19,1,'',NULL,'Hello everyone!','GROUP','2025-09-23 22:19:13'),(20,1,'',NULL,'Ky aplikacion shërben për komunikim të drejtpërdrejtë mes përdoruesve.','GROUP','2025-09-23 22:19:23'),(21,1,'',NULL,'Mund të krijoni dhoma grupi, të nisni biseda private dhe të personalizoni profilin tuaj.','GROUP','2025-09-23 22:19:32'),(22,1,'',NULL,'Hello hello :D','GROUP','2025-09-23 22:24:09'),(23,1,'',NULL,'hello everyone','GROUP','2025-09-23 22:27:49'),(24,1,'',NULL,'werdwef','GROUP','2025-09-23 22:28:02'),(25,1,'',NULL,'sdfdsffsd','GROUP','2025-09-23 22:28:42'),(26,1,'',NULL,'f','GROUP','2025-09-23 22:28:42'),(27,1,'',NULL,'ds','GROUP','2025-09-23 22:28:42'),(28,1,'',NULL,'f','GROUP','2025-09-23 22:28:42'),(29,1,'',NULL,'ds','GROUP','2025-09-23 22:28:42'),(30,1,'',NULL,'f','GROUP','2025-09-23 22:28:43'),(31,1,'',NULL,'ds','GROUP','2025-09-23 22:28:43'),(32,1,'',NULL,'f','GROUP','2025-09-23 22:28:43'),(33,1,'',NULL,'dsf','GROUP','2025-09-23 22:28:43'),(34,1,'',NULL,'fds','GROUP','2025-09-23 22:28:43'),(35,1,'',NULL,'s','GROUP','2025-09-23 22:28:44'),(36,1,'',NULL,'f','GROUP','2025-09-23 22:28:44'),(37,1,'',NULL,'s','GROUP','2025-09-23 22:28:44'),(38,1,'',NULL,'dsds','GROUP','2025-09-23 22:28:45'),(39,1,'',NULL,'Hello','GROUP','2025-09-23 22:42:50'),(40,1,'',NULL,'hello','GROUP','2025-09-23 22:43:15'),(41,1,'',NULL,'hello','GROUP','2025-09-23 22:52:01'),(42,1,'',NULL,'hello','GROUP','2025-09-23 23:02:35'),(43,1,'',NULL,'scsaasc','GROUP','2025-09-23 23:05:48'),(44,1,'',NULL,'dvsdvds','GROUP','2025-09-23 23:10:16'),(45,1,'',NULL,'Ky aplikacion shërben për komunikim të drejtpërdrejtë mes përdoruesve.','GROUP','2025-09-24 16:44:22'),(46,1,'',NULL,'Ky aplikacion shërben për komunikim të drejtpërdrejtë mes përdoruesve.','GROUP','2025-09-24 17:10:42'),(47,1,'',NULL,'Mund të krijoni dhoma grupi, të nisni biseda private dhe të personalizoni profilin tuaj.','GROUP','2025-09-24 17:11:13'),(48,1,'',NULL,'? Udhëzim i shpejtë:  Përdorni ‘+’ për të shtuar miq, klikoni ⚙ për cilësime dhe zgjidhni dhomën që doni nga tab-et sipër.','GROUP','2025-09-24 17:12:12'),(49,1,'',NULL,'? Udhëzim i shpejtë:  Përdorni ‘+’ për të shtuar miq, klikoni ⚙ për cilësime dhe zgjidhni dhomën që doni nga tab-et sipër.','GROUP','2025-09-24 17:12:18'),(50,1,NULL,1,'Hello','PRIVATE','2025-09-24 17:13:41'),(51,4,'',NULL,'hello','GROUP','2025-09-24 17:16:40'),(52,1,'',NULL,'Hello','GROUP','2025-09-24 17:32:54'),(53,4,'',NULL,'hello','GROUP','2025-09-24 17:33:06'),(54,35,'',NULL,'hello','GROUP','2025-09-24 17:41:24'),(55,1,'',NULL,'Ky aplikacion shërben për komunikim të drejtpërdrejtë mes përdoruesve.','GROUP','2025-09-24 17:42:02'),(56,1,'',NULL,'hi','GROUP','2025-09-24 17:42:40'),(57,1,NULL,2,'Hello','PRIVATE','2025-09-24 17:43:20'),(58,1,'',NULL,'hello','GROUP','2025-09-24 21:49:03'),(59,1,'',NULL,'hello','GROUP','2025-09-24 21:51:17'),(60,1,'',NULL,'Hello','GROUP','2025-09-24 22:34:16'),(61,1,'',NULL,'Intering','GROUP','2025-09-24 22:37:19'),(62,43,'',NULL,'Hello','GROUP','2025-09-24 22:39:18'),(63,1,'',NULL,'Hello everyone!','GROUP','2025-10-11 23:52:48'),(64,1,'',NULL,'hello','GROUP','2025-10-12 00:45:14'),(65,1,'',NULL,'Naten','GROUP','2025-10-12 01:23:50'),(66,1,'',NULL,'hello everyone','GROUP','2025-10-18 14:24:42'),(67,1,'',NULL,'hi','GROUP','2025-10-18 16:06:17'),(68,1,'213728',NULL,'hi','GROUP','2025-10-23 21:24:28'),(69,1,'213728',NULL,'nhewjjwef','GROUP','2025-10-23 21:40:16'),(70,1,'213728',NULL,'efwefwef','GROUP','2025-10-23 21:40:23'),(71,1,'213728',NULL,'hi','GROUP','2025-10-23 21:40:50'),(72,1,'213728',NULL,'kkjjjkjjk','GROUP','2025-10-23 21:41:43'),(73,1,'213728',NULL,'hi','GROUP','2025-10-23 21:48:26'),(74,1,'213728',NULL,'he;llo','GROUP','2025-10-23 21:48:33'),(75,1,'213728',NULL,'hi','GROUP','2025-10-23 21:48:39'),(76,1,'213728',NULL,'hi','GROUP','2025-10-23 21:50:13'),(77,1,'213728',NULL,'hi','GROUP','2025-10-23 21:54:46'),(78,1,'213728',NULL,'how are you','GROUP','2025-10-23 21:54:54'),(79,1,'213728',NULL,'?','GROUP','2025-10-23 21:58:28'),(80,1,'213728',NULL,'dslkfdfewef','GROUP','2025-10-23 21:58:33'),(81,1,'213728',NULL,'ewfwefwef','GROUP','2025-10-23 21:58:34'),(82,1,'213728',NULL,'fwfewfewf','GROUP','2025-10-23 21:58:36'),(83,43,'213728',NULL,'hi','GROUP','2025-10-23 22:03:13'),(84,43,'213728',NULL,'kknk\\','GROUP','2025-10-23 22:03:16'),(85,43,'213728',NULL,'blerina','GROUP','2025-10-23 22:03:21'),(86,43,'213728',NULL,'blerina','GROUP','2025-10-23 22:03:25'),(87,43,'213728',NULL,'hi','GROUP','2025-10-23 22:03:31'),(88,43,'213728',NULL,'sacascasc','GROUP','2025-10-23 22:05:34'),(89,43,'213728',NULL,'sdasfdsaf','GROUP','2025-10-23 22:09:39'),(90,43,'213728',NULL,'sadasdsad','GROUP','2025-10-23 22:09:40'),(91,43,'213728',NULL,'sadsadsdsad','GROUP','2025-10-23 22:09:43'),(92,43,'213728',NULL,'kdfkjdkfjfjlkfs','GROUP','2025-10-23 22:09:49'),(93,43,'213728',NULL,'sadjsdklskdkjskjksjd','GROUP','2025-10-23 22:09:52'),(94,43,'069880',NULL,'hi','GROUP','2025-10-23 22:14:40'),(95,1,'289187',NULL,'hu','GROUP','2025-10-25 23:17:53');
/*!40000 ALTER TABLE `osch_messages` ENABLE KEYS */;
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
