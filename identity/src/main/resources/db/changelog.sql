-- liquibase formatted sql

-- changeset martini:identity-users
CREATE TABLE users (
    id VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    email VARCHAR(254) COLLATE utf8mb4_0900_as_cs NOT NULL UNIQUE,
    username VARCHAR(80) COLLATE utf8mb4_0900_as_ci NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    role VARCHAR(8) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    nombre_negocio VARCHAR(255),
    tipo_negocio VARCHAR(255),
    empleados VARCHAR(255),
    telefono VARCHAR(255),
    direccion VARCHAR(255),
    rut_empresa VARCHAR(255),
    intereses JSON NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- rollback DROP TABLE users;

-- changeset martini:identity-sessions
CREATE TABLE login_sessions (
    token_hash VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    user_id VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    expires_at BIGINT NOT NULL,
    INDEX sessions_expiry (expires_at),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- rollback DROP TABLE login_sessions;
