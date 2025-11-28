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
-- Table structure for table `osch_users`
--

DROP TABLE IF EXISTS `osch_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `osch_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `avatar_path` varchar(200) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'online',
  `join_time` datetime DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `secret_code` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `uq_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `osch_users`
--

LOCK TABLES `osch_users` WRITE;
/*!40000 ALTER TABLE `osch_users` DISABLE KEYS */;
INSERT INTO `osch_users` VALUES (1,'blerinademaj','Blerin@*25','Blerina','Demaj','Avatar/5.png','user','online','2025-09-21 22:38:16',NULL,NULL),(2,'bardh.rugova','Bardh*7A','Bardh','Rugova','avatars/1.png','user','online','2025-09-21 22:26:36','192.168.178.27',NULL),(3,'xarbnorx','Arbnor/9K','Arbnor','Krasniqi','Avatar/3.png','user','online','2025-09-21 22:26:46',NULL,NULL),(4,'kreshnik.h','kresh123','Kreshnik','Hoti','avatars/3.png','user','offline','2025-09-21 22:26:56',NULL,NULL),(5,'dardan.berisha','Dard!5X','Dardan','Berisha','avatars/8.png','user','offline','2025-09-21 22:27:06',NULL,NULL),(6,'ilirian_','Ilir*12M','Ilir','Morina','avatars/6.png','user','offline','2025-09-21 22:27:16',NULL,NULL),(7,'taulant.sh','Taul@6Hh','Taulant','Shala','avatars/6.png','user','offline','2025-09-21 22:27:26',NULL,NULL),(8,'rron.gashi','Rron,31M','Rron','Gashi','avatars/5.png','user','offline','2025-09-21 22:27:36',NULL,NULL),(9,'drinii__','Drin!77Q','Drin','Bytyçi','avatars/4.png','user','offline','2025-09-21 22:27:46',NULL,NULL),(10,'fortesa.p','Festa*19','Fortesë','Palokaj','7/default.png','user','offline','2025-09-21 22:27:56',NULL,NULL),(11,'teuta12','Teuta!3B','Teuta','Gegaj','1/default.png','user','offline','2025-09-21 22:28:06',NULL,NULL),(12,'rozafa.k','Roza@5T','Rozafa','Kolaj','avatars/2.png','user','offline','2025-09-21 22:28:16',NULL,NULL),(13,'art.pr','Art/21R','Art','Prenkaj','avatars/2.png','user','offline','2025-09-21 22:28:31',NULL,NULL),(14,'rrita.gjergji','Rrita@21L','Rrita','Gjergji','avatars/3.png','user','offline','2025-09-21 22:28:46',NULL,NULL),(15,'furtuna.g','Furtuna!14','Furtuna','Gashi','avatars/6.png','user','offline','2025-09-21 22:29:01',NULL,NULL),(16,'natyra_k','Natyra@8F','Natyra','Kurtaj','avatars/9.png','user','offline','2025-09-21 22:29:16',NULL,NULL),(17,'vese.istogu','Vese*12K','Vesë','Istogu','avatars/8.png','user','offline','2025-09-21 22:29:36',NULL,NULL),(18,'arberesha.j','Arb!90','Arbëresha','Jashari','avatars/9.png','user','offline','2025-09-21 22:29:56',NULL,NULL),(19,'lumbardh.k','Lumbi!7D','Lumbardh','Kastrati','avatars/7.png','user','offline','2025-09-21 22:30:16',NULL,NULL),(20,'krenar.u','Kren@15Q','Krenar','Uka','avatars/8.png','user','offline','2025-09-21 22:30:31',NULL,NULL),(21,'diell.l','Diell!2M','Diell','Lajçi','avatars/4.png','user','offline','2025-09-21 22:30:46',NULL,NULL),(22,'rreze.k','rreze*11','Rrezarta','Kelmendi','avatars/3.png','user','offline','2025-09-21 22:31:01',NULL,NULL),(23,'dren.b','dren@18','Dren','Berisha','avatars/3.png','user','offline','2025-09-21 22:31:16',NULL,NULL),(24,'dedvukaj.mark','Mark*88C','Mark','Dedvukaj','avatars/1.png','user','offline','2025-09-21 22:31:31',NULL,NULL),(25,'fjolla.gjergji','Fjolla/3X','Fjolla','Gjinaj','avatars/2.png','user','offline','2025-09-21 22:31:46',NULL,NULL),(26,'shkodran.n','Arbesa,7N','Shkodran','Nikçi','avatars/3.png','user','offline','2025-09-21 22:32:16',NULL,NULL),(27,'era.geci','Era!9Y','Era','Geci','avatars/4.png','user','offline','2025-09-21 22:32:36',NULL,NULL),(28,'mark.ceta','Mark!11K','Mark','Çeta','avatars/5.png','user','offline','2025-09-21 22:32:56',NULL,NULL),(29,'drita.pr','Drita@6Z','Drita','Prelvukaj','avatars/6.png','user','offline','2025-09-21 22:33:16',NULL,NULL),(30,'bardha.l','Bardh!3A','Bardha','Lekaj','avatars/7.png','user','offline','2025-09-21 22:33:36',NULL,NULL),(31,'monun.d','Monun*5G','Monun','Dushaj','avatars/8.png','user','offline','2025-09-21 22:34:16',NULL,NULL),(32,'mal.kelmendi','Mmali/12','Mal','Kelmendi','avatars/9.png','user','offline','2025-09-21 22:34:36',NULL,NULL),(33,'glauk.m','Glauk@88','Glauk','Marku','avatars/1.png','user','offline','2025-09-21 22:35:16',NULL,NULL),(34,'luan.dushku','Luan!77','Luan','Dushku','avatars/2.png','user','offline','2025-09-21 22:35:46',NULL,NULL),(35,'flutura.z','Flut*44','Fluturë','Zefi','avatars/2.png','user','offline','2025-09-21 22:36:16',NULL,NULL),(36,'ereza.k','Ereza!99','Erëza','Krasniqi','avatars/4.png','user','offline','2025-09-21 22:36:46',NULL,NULL),(37,'arber.gj','Arber,18','Arbër','Gjonbalaj','avatars/3.png','user','offline','2025-09-21 22:37:16',NULL,NULL),(38,'ilirian.sh','Ilir@55','Ilirian','Shoshi','avatars/6.png','user','offline','2025-09-21 22:37:46',NULL,NULL),(39,'yll.kelmendi','Yll@92Z','Yll','Kelmendi','avatars/8.png','user','offline','2025-09-21 22:38:16',NULL,NULL),(40,'zgjimi7','Zgjim!22','Zgjim','Kelmendi','avatars/7.png','user','offline','2025-09-21 22:26:26',NULL,NULL),(41,'meritabunjaku','MBpass123','Merita','Bunjaku','avatars/default.png','user','online','2025-09-22 22:34:39',NULL,'123010'),(42,'lediii','ledi5','Ledion','Demaj','Avatar/4.png','user','online','2025-09-23 18:23:19',NULL,'111222'),(43,'test','test','Testing','Test','Avatar/4.png','user','online','2025-09-24 22:38:40',NULL,'123456');
/*!40000 ALTER TABLE `osch_users` ENABLE KEYS */;
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
