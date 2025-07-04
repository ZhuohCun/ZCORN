-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: www.zcscloud.net    Database: zcorn
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `device` (
  `id` int NOT NULL COMMENT 'device id',
  `name` varchar(255) DEFAULT NULL COMMENT 'the name of the device',
  `type` int DEFAULT NULL COMMENT '1.soil sensor 2.temperature sensor 3.camera 4.watering device 5.drug sprayer 6.water sprayer',
  `cvalue` float DEFAULT NULL COMMENT 'current value',
  `isoperator` int DEFAULT NULL,
  `turntime` bigint DEFAULT NULL,
  `isforcedoff` int DEFAULT NULL,
  `physicalid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'serial number of a specific device',
  `label` int DEFAULT NULL COMMENT 'the group label',
  `id$place` int DEFAULT NULL COMMENT 'the place a specific device is belonged to',
  `isdeleted` int DEFAULT NULL COMMENT 'is the device deleted',
  PRIMARY KEY (`id`),
  KEY `id$place` (`id$place`),
  CONSTRAINT `device_ibfk_1` FOREIGN KEY (`id$place`) REFERENCES `place` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
INSERT INTO `device` VALUES (1,'1#ss',1,25,0,NULL,0,'123',1,1,0),(2,'1#ts',2,25,0,NULL,0,'111',1,1,0),(3,'1#ca',3,-1,0,NULL,0,'546',1,1,0),(4,'1#wd',4,0,1,1751284287338,0,'765',1,1,0),(5,'1#ds',5,0,1,1751388449962,0,'6554',1,1,0),(6,'1#ws',6,0,1,1751284177941,0,'678',1,1,0),(7,'2#ss',1,25,0,NULL,0,'656',2,1,0),(8,'2#ts',2,20,0,NULL,0,'446',2,1,0),(9,'2#ca',3,-1,0,NULL,0,'767',2,1,0),(10,'2#wd',4,0,1,NULL,0,'876',2,1,0),(11,'2#ds',5,0,1,NULL,0,'735',2,1,0),(12,'2#ws',6,0,1,NULL,0,'789',2,1,0);
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-04 18:21:50
