-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: fittrack
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `fittrack`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `fittrack` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `fittrack`;

--
-- Table structure for table `acceso`
--

DROP TABLE IF EXISTS `acceso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acceso` (
  `idAcceso` int NOT NULL AUTO_INCREMENT,
  `idSocio` int NOT NULL,
  `idEmpleado` int DEFAULT NULL,
  `fecha` date NOT NULL,
  `horaEntrada` time NOT NULL,
  `horaSalida` time DEFAULT NULL,
  PRIMARY KEY (`idAcceso`),
  KEY `idSocio_idx` (`idSocio`),
  KEY `idEmpleado_idx` (`idEmpleado`),
  CONSTRAINT `idEmpleadoFK` FOREIGN KEY (`idEmpleado`) REFERENCES `usuariosistema` (`idEmpleado`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `idSocioFK2` FOREIGN KEY (`idSocio`) REFERENCES `socio` (`idSocio`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acceso`
--

LOCK TABLES `acceso` WRITE;
/*!40000 ALTER TABLE `acceso` DISABLE KEYS */;
INSERT INTO `acceso` VALUES (1,1,1,'2026-11-02','12:33:17','12:33:17'),(2,7,1,'2026-11-02','12:45:21','12:45:21'),(3,10,1,'2026-11-02','12:45:21','12:45:21');
/*!40000 ALTER TABLE `acceso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `acreditacionestudiante`
--

DROP TABLE IF EXISTS `acreditacionestudiante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acreditacionestudiante` (
  `idAcreditacion` int NOT NULL AUTO_INCREMENT,
  `idSocio` int NOT NULL,
  `fechaVerificacion` date NOT NULL,
  `fechaVigencia` date NOT NULL,
  `tipoComprobante` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`idAcreditacion`),
  KEY `idSocio_idx` (`idSocio`),
  CONSTRAINT `idSocio` FOREIGN KEY (`idSocio`) REFERENCES `socio` (`idSocio`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acreditacionestudiante`
--

LOCK TABLES `acreditacionestudiante` WRITE;
/*!40000 ALTER TABLE `acreditacionestudiante` DISABLE KEYS */;
INSERT INTO `acreditacionestudiante` VALUES (1,1,'2026-11-02','2027-02-02','Credencial escolar'),(2,1,'2026-11-02','2027-05-02','Constancia de estudios'),(3,12,'2026-08-01','2026-10-31','Constancia de estudios'),(4,9,'2026-11-02','2027-02-02','Credencial escolar'),(5,12,'2026-08-01','2026-10-31','Constancia de estudios'),(6,9,'2026-11-02','2027-02-02','Credencial escolar'),(7,12,'2026-08-02','2026-11-01','Constancia de estudios'),(8,10,'2026-11-02','2027-02-02','Credencial escolar'),(9,6,'2026-11-02','2027-02-02','Constancia de estudios');
/*!40000 ALTER TABLE `acreditacionestudiante` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empleadosocio`
--

DROP TABLE IF EXISTS `empleadosocio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empleadosocio` (
  `idEmpleado` int NOT NULL,
  `idSocio` int NOT NULL,
  PRIMARY KEY (`idEmpleado`,`idSocio`),
  KEY `idSocioFK3_idx` (`idSocio`),
  CONSTRAINT `idEmpleadoFK2` FOREIGN KEY (`idEmpleado`) REFERENCES `usuariosistema` (`idEmpleado`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `idSocioFK3` FOREIGN KEY (`idSocio`) REFERENCES `socio` (`idSocio`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empleadosocio`
--

LOCK TABLES `empleadosocio` WRITE;
/*!40000 ALTER TABLE `empleadosocio` DISABLE KEYS */;
/*!40000 ALTER TABLE `empleadosocio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membresia`
--

DROP TABLE IF EXISTS `membresia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membresia` (
  `idMembresia` int NOT NULL AUTO_INCREMENT,
  `tipo` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fechaInicio` date DEFAULT NULL,
  `fechaVencimiento` date DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idMembresia`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membresia`
--

LOCK TABLES `membresia` WRITE;
/*!40000 ALTER TABLE `membresia` DISABLE KEYS */;
INSERT INTO `membresia` VALUES (1,'MENSUAL','2026-10-31','2026-11-30',850.00,'VIGENTE'),(2,'MENSUAL','2026-10-31','2026-11-30',850.00,'VIGENTE'),(3,'DOSPERSONAS','2026-10-31','2026-11-30',1200.00,'VIGENTE'),(4,'MENSUAL','2026-11-01','2026-12-01',850.00,'VIGENTE'),(5,'DOSPERSONAS','2026-11-01','2026-12-01',1200.00,'VIGENTE'),(6,'MENSUAL','2026-11-01','2026-12-01',850.00,'VIGENTE'),(7,'DOSPERSONAS','2026-11-01','2026-12-01',1200.00,'VIGENTE'),(8,'MENSUAL','2026-09-30','2026-10-30',850.00,'TOLERANCIA'),(9,'MENSUAL','2026-09-30','2026-10-30',850.00,'TOLERANCIA'),(10,'MENSUAL','2026-12-05','2027-01-05',850.00,'PROGRAMADA'),(11,'DOSPERSONAS','2026-12-02','2027-01-02',1200.00,'PROGRAMADA'),(12,'MENSUAL','2026-08-01','2026-09-01',850.00,'VENCIDA'),(13,'MENSUAL','2026-09-30','2026-10-30',850.00,'TOLERANCIA'),(14,'ESTUDIANTE','2026-11-02','2026-12-02',500.00,'VIGENTE'),(15,'ESTUDIANTE','2026-12-03','2027-01-03',500.00,'PROGRAMADA'),(16,'ESTUDIANTE','2026-11-03','2026-12-03',500.00,'PROGRAMADA'),(17,'ESTUDIANTE','2026-12-05','2027-01-05',500.00,'PROGRAMADA');
/*!40000 ALTER TABLE `membresia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `idPago` int NOT NULL AUTO_INCREMENT,
  `idMembresia` int DEFAULT NULL,
  `idEmpleado` int DEFAULT NULL,
  `fechaPago` date DEFAULT NULL,
  `monto` decimal(10,2) DEFAULT NULL,
  `metodoPago` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idPago`),
  KEY `idMembresia_idx` (`idMembresia`),
  KEY `idEmpleado_idx` (`idEmpleado`),
  CONSTRAINT `idEmpleado` FOREIGN KEY (`idEmpleado`) REFERENCES `usuariosistema` (`idEmpleado`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `idMembresia` FOREIGN KEY (`idMembresia`) REFERENCES `membresia` (`idMembresia`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
INSERT INTO `pago` VALUES (1,9,NULL,'2026-11-01',850.00,'Efectivo'),(2,10,NULL,'2026-11-01',850.00,'Efectivo'),(3,11,NULL,'2026-11-01',1200.00,'Tarjeta'),(4,12,NULL,'2026-11-01',850.00,'Efectivo'),(5,13,1,'2026-11-02',850.00,'Efectivo'),(6,14,1,'2026-11-02',500.00,'Efectivo'),(7,15,1,'2026-11-02',500.00,'Efectivo'),(8,16,1,'2026-11-02',500.00,'Efectivo'),(9,17,1,'2026-11-02',500.00,'Efectivo');
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reporte`
--

DROP TABLE IF EXISTS `reporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reporte` (
  `idReporte` int NOT NULL,
  `idAdministrador` int DEFAULT NULL,
  `tipo` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  PRIMARY KEY (`idReporte`),
  KEY `idAdministradorFK_idx` (`idAdministrador`),
  CONSTRAINT `idAdministradorFK` FOREIGN KEY (`idAdministrador`) REFERENCES `usuariosistema` (`idEmpleado`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reporte`
--

LOCK TABLES `reporte` WRITE;
/*!40000 ALTER TABLE `reporte` DISABLE KEYS */;
/*!40000 ALTER TABLE `reporte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `respaldo`
--

DROP TABLE IF EXISTS `respaldo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `respaldo` (
  `idRespaldo` int NOT NULL AUTO_INCREMENT,
  `idAdministrador` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  PRIMARY KEY (`idRespaldo`),
  KEY `idAdministrador_idx` (`idAdministrador`),
  CONSTRAINT `idAdministrador` FOREIGN KEY (`idAdministrador`) REFERENCES `usuariosistema` (`idEmpleado`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `respaldo`
--

LOCK TABLES `respaldo` WRITE;
/*!40000 ALTER TABLE `respaldo` DISABLE KEYS */;
INSERT INTO `respaldo` VALUES (1,1,'2026-11-02');
/*!40000 ALTER TABLE `respaldo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `socio`
--

DROP TABLE IF EXISTS `socio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `socio` (
  `idSocio` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `apellido` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `correo` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefono` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fechaRegistro` date DEFAULT NULL,
  `estado` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idSocio`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `socio`
--

LOCK TABLES `socio` WRITE;
/*!40000 ALTER TABLE `socio` DISABLE KEYS */;
INSERT INTO `socio` VALUES (1,'Socio','Prueba','prueba.fittrack@example.com','0000000000','2026-10-31','ACTIVO'),(2,'Socio','Modificado','socio.modificado@example.com','0987654321','2026-10-31','ACTIVO'),(3,'Prueba','Individual','individual.20261101@example.com','1111111111','2026-11-01','ACTIVO'),(4,'Prueba','Compartido A','compartidoA.20261101@example.com','2222222222','2026-11-01','ACTIVO'),(5,'Prueba','Compartido B','compartidoB.20261101@example.com','3333333333','2026-11-01','ACTIVO'),(6,'Prueba','Individual','individual.20261101@example.com','1111111111','2026-11-01','ACTIVO'),(7,'Prueba','Compartido A','compartidoA.20261101@example.com','2222222222','2026-11-01','ACTIVO'),(8,'Prueba','Compartido B','compartidoB.20261101@example.com','3333333333','2026-11-01','ACTIVO'),(9,'Prueba','Nuevo','nuevo.20261101@example.com','4444444444','2026-11-01','ACTIVO'),(10,'Prueba','Tolerancia','tolerancia.20261101@example.com','5555555555','2026-11-01','ACTIVO'),(11,'Prueba','Tolerancia','tolerancia.20261101@example.com','5555555555','2026-11-01','ACTIVO'),(12,'Prueba','Tolerancia','tolerancia.20261101@example.com','5555555555','2026-11-02','ACTIVO');
/*!40000 ALTER TABLE `socio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sociomembresia`
--

DROP TABLE IF EXISTS `sociomembresia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sociomembresia` (
  `idSocio` int NOT NULL,
  `idMembresia` int NOT NULL,
  PRIMARY KEY (`idSocio`,`idMembresia`),
  KEY `idMembresia_idx` (`idMembresia`),
  CONSTRAINT `idMembresiaFK` FOREIGN KEY (`idMembresia`) REFERENCES `membresia` (`idMembresia`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `idSocioFK` FOREIGN KEY (`idSocio`) REFERENCES `socio` (`idSocio`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sociomembresia`
--

LOCK TABLES `sociomembresia` WRITE;
/*!40000 ALTER TABLE `sociomembresia` DISABLE KEYS */;
INSERT INTO `sociomembresia` VALUES (1,2),(1,3),(2,3),(3,4),(4,5),(5,5),(6,6),(7,7),(8,7),(10,8),(11,9),(3,10),(4,11),(5,11),(9,12),(12,13),(9,14),(9,15),(10,16),(6,17);
/*!40000 ALTER TABLE `sociomembresia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuariosistema`
--

DROP TABLE IF EXISTS `usuariosistema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuariosistema` (
  `idEmpleado` int NOT NULL AUTO_INCREMENT,
  `rol` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `usuario` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contraseña` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`idEmpleado`),
  UNIQUE KEY `UK_usuario` (`usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuariosistema`
--

LOCK TABLES `usuariosistema` WRITE;
/*!40000 ALTER TABLE `usuariosistema` DISABLE KEYS */;
INSERT INTO `usuariosistema` VALUES (1,'ADMINISTRADOR','admin_prueba','pbkdf2-sha256$600000$EkI+EEfRyZtTygGn/fh15Q==$WScOih0Ct+NNhTxC7gHLgk9FwlhNMMsP7Lt1qLXDtAk=',1),(2,'EMPLEADO','empleado_prueba','pbkdf2-sha256$600000$zklXAoCx+yNk5hYfR5pTag==$3EJwy5fpT9qosc6fM5rpIRTZGOzEfmGekkLQIlFY8xk=',1);
/*!40000 ALTER TABLE `usuariosistema` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'fittrack'
--

--
-- Dumping routines for database 'fittrack'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-02 21:44:05
