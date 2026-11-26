-- =====================================================================
-- Esquema de la base de datos del control de acceso.
-- Se ejecuta al arrancar la aplicacion (ver ConexionDB.inicializar()).
-- Compatible con H2 y, salvo detalles menores, con MySQL/PostgreSQL.
-- =====================================================================

CREATE TABLE trabajador (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cedula        VARCHAR(20)  NOT NULL UNIQUE,
    nombre        VARCHAR(100) NOT NULL,
    rol           VARCHAR(30)  NOT NULL,   -- nombre del enum Rol
    nivel_acceso  VARCHAR(20)  NOT NULL    -- nombre del enum Nivel
);

CREATE TABLE tarjeta_rfid (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid_hash       CHAR(64) NOT NULL UNIQUE,  -- SHA-256 en hexadecimal; nunca el UID real
    trabajador_id  BIGINT   NOT NULL REFERENCES trabajador(id),
    activa         BOOLEAN  NOT NULL DEFAULT TRUE,
    fecha_emision  DATE     NOT NULL
);

CREATE TABLE zona (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo           VARCHAR(10)  NOT NULL UNIQUE,
    nombre           VARCHAR(100) NOT NULL,
    nivel_requerido  VARCHAR(20)  NOT NULL
);

CREATE TABLE registro_acceso (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid_hash    CHAR(64)    NOT NULL,
    tarjeta_id  BIGINT      NULL REFERENCES tarjeta_rfid(id),  -- NULL = tarjeta desconocida
    zona_id     BIGINT      NOT NULL REFERENCES zona(id),
    fecha_hora  TIMESTAMP   NOT NULL,
    resultado   VARCHAR(40) NOT NULL
);

-- Acelera la consulta de "intentos fallidos recientes de esta tarjeta"
CREATE INDEX idx_registro_uid_fecha ON registro_acceso(uid_hash, fecha_hora);

CREATE TABLE alerta (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo         VARCHAR(30)  NOT NULL,
    mensaje      VARCHAR(255) NOT NULL,
    fecha_hora   TIMESTAMP    NOT NULL,
    registro_id  BIGINT       NOT NULL REFERENCES registro_acceso(id)
);
