-- Ejecutar con una cuenta administradora del MySQL externo.
-- Sustituir ORIGEN_CLIENTE por el origen autorizado de las conexiones.
-- MySQL muestra la contraseña inicial de cada cuenta nueva.
CREATE DATABASE IF NOT EXISTS martini_identity CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS martini_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS martini_contact CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'martini_identity'@'ORIGEN_CLIENTE' IDENTIFIED BY RANDOM PASSWORD;
CREATE USER IF NOT EXISTS 'martini_catalog'@'ORIGEN_CLIENTE' IDENTIFIED BY RANDOM PASSWORD;
CREATE USER IF NOT EXISTS 'martini_contact'@'ORIGEN_CLIENTE' IDENTIFIED BY RANDOM PASSWORD;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON martini_identity.* TO 'martini_identity'@'ORIGEN_CLIENTE';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP
    ON martini_catalog.* TO 'martini_catalog'@'ORIGEN_CLIENTE';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP
    ON martini_contact.* TO 'martini_contact'@'ORIGEN_CLIENTE';
