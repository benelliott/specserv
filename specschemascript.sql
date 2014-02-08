SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `specschema` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `specschema` ;

-- -----------------------------------------------------
-- Table `specschema`.`captures`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `specschema`.`captures` (
  `idcaptures` INT NOT NULL AUTO_INCREMENT,
  `species` VARCHAR(100) NULL,
  `latitude` DOUBLE NULL,
  `longitude` DOUBLE NULL,
  PRIMARY KEY (`idcaptures`),
  UNIQUE INDEX `idcaptures_UNIQUE` (`idcaptures` ASC))
ENGINE = InnoDB;

DROP USER 'specserv';
FLUSH privileges;
CREATE USER 'specserv' IDENTIFIED BY 'specserv';

GRANT ALL ON `specschema`.* TO 'specserv';

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
