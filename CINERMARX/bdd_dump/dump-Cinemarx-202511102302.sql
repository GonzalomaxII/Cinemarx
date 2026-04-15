/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.7.2-MariaDB, for Win64 (AMD64)
--
-- Host: br1.aguilucho.ar    Database: Cinemarx
-- ------------------------------------------------------
-- Server version	10.3.39-MariaDB-1:10.3.39+maria~ubu2004

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `Administrador`
--

DROP TABLE IF EXISTS `Administrador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Administrador` (
  `ID_Admin` int(11) NOT NULL AUTO_INCREMENT,
  `DNI` int(11) NOT NULL,
  `ID_Cine` int(11) NOT NULL,
  `Mail` varchar(100) DEFAULT NULL,
  `Contrasena` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID_Admin`),
  KEY `DNI` (`DNI`),
  KEY `ID_Cine` (`ID_Cine`),
  CONSTRAINT `Administrador_ibfk_1` FOREIGN KEY (`DNI`) REFERENCES `Usuario` (`DNI`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Administrador_ibfk_2` FOREIGN KEY (`ID_Cine`) REFERENCES `Cine` (`ID_Cine`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Administrador_Cartelera`
--

DROP TABLE IF EXISTS `Administrador_Cartelera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Administrador_Cartelera` (
  `ID_Admin` int(11) NOT NULL,
  `ID_Cartelera` int(11) NOT NULL,
  PRIMARY KEY (`ID_Admin`,`ID_Cartelera`),
  KEY `ID_Cartelera` (`ID_Cartelera`),
  CONSTRAINT `Administrador_Cartelera_ibfk_1` FOREIGN KEY (`ID_Admin`) REFERENCES `Administrador` (`ID_Admin`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Administrador_Cartelera_ibfk_2` FOREIGN KEY (`ID_Cartelera`) REFERENCES `Cartelera` (`ID_Cartelera`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Boleto`
--

DROP TABLE IF EXISTS `Boleto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Boleto` (
  `ID_Boleto` int(11) NOT NULL AUTO_INCREMENT,
  `NumeroButaca` varchar(10) NOT NULL,
  `ID_Funcion` int(11) NOT NULL,
  `ID_Cliente` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID_Boleto`),
  KEY `ID_Funcion` (`ID_Funcion`),
  KEY `FK_Boleto_Cliente` (`ID_Cliente`),
  CONSTRAINT `Boleto_ibfk_1` FOREIGN KEY (`ID_Funcion`) REFERENCES `Funcion` (`ID_Funcion`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_Boleto_Cliente` FOREIGN KEY (`ID_Cliente`) REFERENCES `Cliente` (`ID_Cliente`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Cartelera`
--

DROP TABLE IF EXISTS `Cartelera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Cartelera` (
  `ID_Cartelera` int(11) NOT NULL DEFAULT 1,
  `FechaInicio` date NOT NULL,
  `FechaFin` date NOT NULL,
  PRIMARY KEY (`ID_Cartelera`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Cine`
--

DROP TABLE IF EXISTS `Cine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Cine` (
  `ID_Cine` int(11) NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(100) NOT NULL,
  `Direccion` varchar(150) NOT NULL,
  PRIMARY KEY (`ID_Cine`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Cliente`
--

DROP TABLE IF EXISTS `Cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Cliente` (
  `ID_Cliente` int(11) NOT NULL AUTO_INCREMENT,
  `DNI` int(11) NOT NULL,
  `Membresia` varchar(10) DEFAULT 'NO VIP',
  `Mail` varchar(100) DEFAULT NULL,
  `Contrasena` varchar(100) DEFAULT NULL,
  `Puntos` int(11) NOT NULL DEFAULT 0,
  `PuntosGastados` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ID_Cliente`),
  KEY `DNI` (`DNI`),
  CONSTRAINT `Cliente_ibfk_1` FOREIGN KEY (`DNI`) REFERENCES `Usuario` (`DNI`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Comprobante`
--

DROP TABLE IF EXISTS `Comprobante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Comprobante` (
  `ID_Comprobante` int(11) NOT NULL AUTO_INCREMENT,
  `NumComprobante` varchar(50) NOT NULL,
  `ID_Cliente` int(11) DEFAULT NULL,
  `FechaCompra` datetime DEFAULT current_timestamp(),
  `MetodoPago` varchar(50) DEFAULT NULL,
  `Canjeado` varchar(2) NOT NULL DEFAULT 'NO',
  PRIMARY KEY (`ID_Comprobante`),
  KEY `ID_Cliente` (`ID_Cliente`),
  CONSTRAINT `Comprobante_ibfk_1` FOREIGN KEY (`ID_Cliente`) REFERENCES `Cliente` (`ID_Cliente`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Comprobante_Boleto`
--

DROP TABLE IF EXISTS `Comprobante_Boleto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Comprobante_Boleto` (
  `ID_Comprobante` int(11) NOT NULL,
  `ID_Boleto` int(11) NOT NULL,
  `Cantidad` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`ID_Comprobante`,`ID_Boleto`),
  KEY `ID_Boleto` (`ID_Boleto`),
  CONSTRAINT `Comprobante_Boleto_ibfk_1` FOREIGN KEY (`ID_Comprobante`) REFERENCES `Comprobante` (`ID_Comprobante`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Comprobante_Boleto_ibfk_2` FOREIGN KEY (`ID_Boleto`) REFERENCES `Boleto` (`ID_Boleto`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Comprobante_Producto`
--

DROP TABLE IF EXISTS `Comprobante_Producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Comprobante_Producto` (
  `ID_Comprobante` int(11) NOT NULL,
  `ID_Prod` int(11) NOT NULL,
  `Cantidad` int(11) NOT NULL DEFAULT 1,
  `Extra` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`ID_Comprobante`,`ID_Prod`),
  KEY `ID_Prod` (`ID_Prod`),
  CONSTRAINT `Comprobante_Producto_ibfk_1` FOREIGN KEY (`ID_Comprobante`) REFERENCES `Comprobante` (`ID_Comprobante`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Comprobante_Producto_ibfk_2` FOREIGN KEY (`ID_Prod`) REFERENCES `Producto` (`ID_Prod`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Empleado`
--

DROP TABLE IF EXISTS `Empleado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Empleado` (
  `ID_Empleado` int(11) NOT NULL AUTO_INCREMENT,
  `DNI` int(11) NOT NULL,
  PRIMARY KEY (`ID_Empleado`),
  KEY `DNI` (`DNI`),
  CONSTRAINT `Empleado_ibfk_1` FOREIGN KEY (`DNI`) REFERENCES `Usuario` (`DNI`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Empleado_Sala`
--

DROP TABLE IF EXISTS `Empleado_Sala`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Empleado_Sala` (
  `ID_Empleado` int(11) NOT NULL,
  `ID_Sala` int(11) NOT NULL,
  PRIMARY KEY (`ID_Empleado`,`ID_Sala`),
  KEY `ID_Sala` (`ID_Sala`),
  CONSTRAINT `Empleado_Sala_ibfk_1` FOREIGN KEY (`ID_Empleado`) REFERENCES `Empleado` (`ID_Empleado`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Empleado_Sala_ibfk_2` FOREIGN KEY (`ID_Sala`) REFERENCES `Sala` (`ID_Sala`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Funcion`
--

DROP TABLE IF EXISTS `Funcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Funcion` (
  `ID_Funcion` int(11) NOT NULL AUTO_INCREMENT,
  `HoraFuncion` time NOT NULL,
  `FechaFuncion` date NOT NULL,
  `Estado` varchar(20) DEFAULT NULL,
  `ID_Pelicula` int(11) NOT NULL,
  `ID_Sala` int(11) NOT NULL,
  `ID_Cartelera` int(11) NOT NULL DEFAULT 1,
  `Idioma` varchar(20) DEFAULT NULL,
  `Precio` decimal(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`ID_Funcion`),
  KEY `ID_Pelicula` (`ID_Pelicula`),
  KEY `ID_Sala` (`ID_Sala`),
  KEY `ID_Cartelera` (`ID_Cartelera`),
  CONSTRAINT `Funcion_ibfk_1` FOREIGN KEY (`ID_Pelicula`) REFERENCES `Pelicula` (`ID_Pelicula`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Funcion_ibfk_2` FOREIGN KEY (`ID_Sala`) REFERENCES `Sala` (`ID_Sala`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Funcion_ibfk_3` FOREIGN KEY (`ID_Cartelera`) REFERENCES `Cartelera` (`ID_Cartelera`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Logs`
--

DROP TABLE IF EXISTS `Logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Logs` (
  `ID_Log` int(11) NOT NULL AUTO_INCREMENT,
  `FechaCambio` datetime NOT NULL DEFAULT current_timestamp(),
  `Descripcion` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`ID_Log`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MetodosPago`
--

DROP TABLE IF EXISTS `MetodosPago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `MetodosPago` (
  `ID_Metodo` int(11) NOT NULL AUTO_INCREMENT,
  `Empresa` varchar(20) NOT NULL,
  `Tipo` varchar(10) NOT NULL,
  `Numero` varchar(16) NOT NULL,
  `NombreTitular` varchar(100) DEFAULT NULL,
  `Pin` varchar(4) NOT NULL,
  `FechaCaducidad` varchar(7) NOT NULL,
  `ID_Cliente` int(11) NOT NULL,
  PRIMARY KEY (`ID_Metodo`),
  UNIQUE KEY `Numero` (`Numero`),
  KEY `ID_Cliente` (`ID_Cliente`),
  CONSTRAINT `MetodosPago_ibfk_1` FOREIGN KEY (`ID_Cliente`) REFERENCES `Cliente` (`ID_Cliente`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Pelicula`
--

DROP TABLE IF EXISTS `Pelicula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Pelicula` (
  `ID_Pelicula` int(11) NOT NULL AUTO_INCREMENT,
  `Genero` varchar(50) DEFAULT NULL,
  `Titulo` varchar(100) NOT NULL,
  `ClasificacionEdad` varchar(10) DEFAULT NULL,
  `Estado` varchar(20) DEFAULT NULL,
  `Imagen` varchar(300) DEFAULT NULL,
  `Sinopsis` varchar(150) DEFAULT NULL,
  `Duracion` int(3) DEFAULT NULL,
  `Trailer` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`ID_Pelicula`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Producto`
--

DROP TABLE IF EXISTS `Producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Producto` (
  `ID_Prod` int(11) NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(100) NOT NULL,
  `Precio` decimal(10,2) NOT NULL,
  `Categoria` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID_Prod`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Sala`
--

DROP TABLE IF EXISTS `Sala`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Sala` (
  `ID_Sala` int(11) NOT NULL AUTO_INCREMENT,
  `Numero` int(11) NOT NULL,
  `CantButacas` int(11) NOT NULL,
  `TipoDeSala` varchar(50) NOT NULL,
  `ID_Cine` int(11) NOT NULL,
  PRIMARY KEY (`ID_Sala`),
  KEY `ID_Cine` (`ID_Cine`),
  CONSTRAINT `Sala_ibfk_1` FOREIGN KEY (`ID_Cine`) REFERENCES `Cine` (`ID_Cine`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Usuario`
--

DROP TABLE IF EXISTS `Usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Usuario` (
  `DNI` int(11) NOT NULL,
  `FechaNac` date NOT NULL,
  `Nombre` varchar(50) NOT NULL,
  `Apellido` varchar(50) NOT NULL,
  PRIMARY KEY (`DNI`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'Cinemarx'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-11-10 23:02:47
