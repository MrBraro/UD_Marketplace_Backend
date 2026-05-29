
CREATE DATABASE IF NOT EXISTS `marketplace`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `marketplace`;



CREATE TABLE IF NOT EXISTS `usuario` (
    codigo_user        INT PRIMARY KEY AUTO_INCREMENT,
    primer_nomb        VARCHAR(100),
    segundo_nom        VARCHAR(100),
    primer_apel        VARCHAR(100),
    segundo_apel       VARCHAR(100),
    fecha_nacimiento   DATE,
    correo_institu     VARCHAR(150) UNIQUE NOT NULL,
    password_hash      VARCHAR(255) NOT NULL,
    activo             BOOLEAN DEFAULT TRUE,
    menor_edad         BOOLEAN DEFAULT FALSE,
    permiso_user_menor LONGBLOB,
    -- Nuevos campos requeridos
    genero             VARCHAR(20),
    perimiso_user      VARCHAR(50) NOT NULL DEFAULT 'COMPRADOR',
    tel_user           VARCHAR(20),
    two_factor_code    VARCHAR(6),
    two_factor_expiry  DATETIME,
    bloqueado_hasta    DATETIME
);

CREATE TABLE IF NOT EXISTS `tel_user` (
    codigo_user INT NOT NULL,
    tel_user    VARCHAR(20) NOT NULL,
    PRIMARY KEY (codigo_user, tel_user),
    CONSTRAINT fk_tel_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user)
);

CREATE TABLE IF NOT EXISTS `vendedor` (
    codigo_user  INT PRIMARY KEY,
    calificacion DECIMAL(3,2) DEFAULT 0.00,
    CONSTRAINT fk_vendedor_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user)
);

CREATE TABLE IF NOT EXISTS `comprador` (
    codigo_user INT PRIMARY KEY,
    CONSTRAINT fk_comprador_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user)
);

CREATE TABLE IF NOT EXISTS `administrador` (
    codigo_user     INT PRIMARY KEY,
    numero_contrato INT NOT NULL UNIQUE,
    CONSTRAINT fk_admin_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user)
);

-- Tokens de sesión invalidados (blacklist para logout)
CREATE TABLE IF NOT EXISTS `invalidated_tokens` (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    token          VARCHAR(2048) NOT NULL,
    invalidated_at DATETIME NOT NULL
);

-- Registro de intentos fallidos de autenticación 
CREATE TABLE IF NOT EXISTS `intento_fallido_auth` (
    id_intento       INT PRIMARY KEY AUTO_INCREMENT,
    correo_intentado VARCHAR(150),
    ip_origen        VARCHAR(50),
    fecha_hora       DATETIME NOT NULL,
    exitoso          BOOLEAN DEFAULT FALSE
);

-- Tokens de recuperación de contraseña
CREATE TABLE IF NOT EXISTS `token_recuperacion` (
    id_token         INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user      INT NOT NULL,
    token            VARCHAR(255) UNIQUE NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    usado            BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_token_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user)
);

CREATE TABLE IF NOT EXISTS `documentos` (
    id_doc      INT PRIMARY KEY AUTO_INCREMENT,
    tipo_doc    VARCHAR(100),
    version     VARCHAR(50),
    activo      BOOLEAN DEFAULT TRUE,
    codigo_user INT NOT NULL,
    CONSTRAINT fk_doc_admin FOREIGN KEY (codigo_user)
        REFERENCES `administrador`(codigo_user)
);

CREATE TABLE IF NOT EXISTS `promocion` (
    id_prom          INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user      INT NOT NULL,
    tipo_prom        VARCHAR(100),
    descripcion_prom TEXT,
    CONSTRAINT fk_prom_admin FOREIGN KEY (codigo_user)
        REFERENCES `administrador`(codigo_user)
);


CREATE TABLE IF NOT EXISTS `categoria` (
    id_categoria       INT PRIMARY KEY AUTO_INCREMENT,
    nombre_cat         VARCHAR(100) NOT NULL,
    activo_cat         BOOLEAN DEFAULT TRUE,
    -- Nuevos campos requeridos
    descripcion_cat    VARCHAR(500),
    contador_productos INT DEFAULT 0,
    codigo_admin       INT,
    CONSTRAINT fk_cat_admin FOREIGN KEY (codigo_admin)
        REFERENCES `administrador`(codigo_user)
);

CREATE TABLE IF NOT EXISTS `producto` (
    id_pub            INT PRIMARY KEY AUTO_INCREMENT,
    codigo_usuario    INT NOT NULL,
    nombre_pub        VARCHAR(150),
    descripcion_pub   VARCHAR(500),
    imagen_pub        LONGBLOB,
    ubicacion         VARCHAR(200),
    precio_pub        DECIMAL(12,2),
    disponibilidad    BOOLEAN DEFAULT TRUE,
    id_categoria      INT,
    -- Nuevos campos requeridos
    condiciones_venta VARCHAR(500),
    fecha_registro    DATETIME,
    activo_pub        BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_prod_vendedor  FOREIGN KEY (codigo_usuario)
        REFERENCES `vendedor`(codigo_user),
    CONSTRAINT fk_prod_categoria FOREIGN KEY (id_categoria)
        REFERENCES `categoria`(id_categoria)
);


CREATE TABLE IF NOT EXISTS `cupon` (
    id_cupon     INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user  INT NOT NULL,
    fecha_inicio DATE,
    fecha_fin    DATE,
    id_prom      INT,
    CONSTRAINT fk_cupon_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user),
    CONSTRAINT fk_cupon_prom    FOREIGN KEY (id_prom)
        REFERENCES `promocion`(id_prom)
);


CREATE TABLE IF NOT EXISTS `orden` (
    id_orden        INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user     INT NOT NULL,
    codigo_vendedor INT,
    total_compra    DECIMAL(12,2),
    estado_orden    VARCHAR(50),
    fecha_compr     DATE,
    datetime_compra DATETIME,
    id_cupon        INT,
    id_pub          INT,
    CONSTRAINT fk_orden_usuario  FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user),
    CONSTRAINT fk_orden_vendedor FOREIGN KEY (codigo_vendedor)
        REFERENCES `vendedor`(codigo_user),
    CONSTRAINT fk_orden_cupon    FOREIGN KEY (id_cupon)
        REFERENCES `cupon`(id_cupon),
    CONSTRAINT fk_orden_producto FOREIGN KEY (id_pub)
        REFERENCES `producto`(id_pub)
);


CREATE TABLE IF NOT EXISTS `detalle_orden_entrega` (
    id_detalle           INT PRIMARY KEY AUTO_INCREMENT,
    id_orden             INT NOT NULL UNIQUE,
    nombre_producto      VARCHAR(150),
    descripcion_prod     VARCHAR(500),
    precio_unitario      DECIMAL(12,2),
    imagen_producto      LONGBLOB,
    fecha_generacion     DATETIME,
    confirmacion_digital VARCHAR(255) UNIQUE,
    CONSTRAINT fk_detalle_orden FOREIGN KEY (id_orden)
        REFERENCES `orden`(id_orden)
);



CREATE TABLE IF NOT EXISTS `pqr` (
    radicado           INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user        INT NOT NULL,
    tipo_pqr           VARCHAR(100),
    fecha_creacion_pqr DATE,
    hora_creacion_pqr  TIME,
    imagen_pqr         LONGBLOB,
    descripcion_pqr    VARCHAR(500),
    codigo_admin       INT,
    estado_pqr         VARCHAR(50),
    CONSTRAINT fk_pqr_usuario FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user),
    CONSTRAINT fk_pqr_admin   FOREIGN KEY (codigo_admin)
        REFERENCES `administrador`(codigo_user)
);


CREATE TABLE IF NOT EXISTS `interaccion_pqr` (
    id_interaccion INT PRIMARY KEY AUTO_INCREMENT,
    radicado       INT NOT NULL,
    codigo_user    INT NOT NULL,
    mensaje        TEXT NOT NULL,
    fecha_hora     DATETIME NOT NULL,
    CONSTRAINT fk_interaccion_pqr  FOREIGN KEY (radicado)
        REFERENCES `pqr`(radicado),
    CONSTRAINT fk_interaccion_user FOREIGN KEY (codigo_user)
        REFERENCES `usuario`(codigo_user)
);


CREATE TABLE IF NOT EXISTS `resena_predefinida` (
    id_resena    INT PRIMARY KEY AUTO_INCREMENT,
    texto_resena VARCHAR(255) NOT NULL,
    activo       BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS `valoracion` (
    id_val           INT PRIMARY KEY AUTO_INCREMENT,
    valo_predefinida VARCHAR(100),
    calificacion     INT CHECK (calificacion BETWEEN 1 AND 5),
    fecha_valo       DATE,
    estado_valo      BOOLEAN DEFAULT TRUE,
    id_orden         INT,
    id_pub           INT,
    codigo_vendedor  INT,
    -- Nuevos campos requeridos
    codigo_comprador INT,
    id_resena        INT,
    CONSTRAINT fk_val_orden     FOREIGN KEY (id_orden)
        REFERENCES `orden`(id_orden),
    CONSTRAINT fk_val_producto  FOREIGN KEY (id_pub)
        REFERENCES `producto`(id_pub),
    CONSTRAINT fk_val_vendedor  FOREIGN KEY (codigo_vendedor)
        REFERENCES `vendedor`(codigo_user),
    CONSTRAINT fk_val_comprador FOREIGN KEY (codigo_comprador)
        REFERENCES `comprador`(codigo_user),
    CONSTRAINT fk_val_resena    FOREIGN KEY (id_resena)
        REFERENCES `resena_predefinida`(id_resena)
);



CREATE INDEX idx_usuario_correo      ON `usuario`(correo_institu);
CREATE INDEX idx_usuario_activo      ON `usuario`(activo);
CREATE INDEX idx_usuario_rol         ON `usuario`(perimiso_user);
CREATE INDEX idx_producto_categoria  ON `producto`(id_categoria);
CREATE INDEX idx_producto_precio     ON `producto`(precio_pub);
CREATE INDEX idx_producto_nombre     ON `producto`(nombre_pub);
CREATE INDEX idx_producto_activo     ON `producto`(activo_pub);
CREATE INDEX idx_producto_vendedor   ON `producto`(codigo_usuario);
CREATE INDEX idx_orden_comprador     ON `orden`(codigo_user);
CREATE INDEX idx_orden_vendedor_idx  ON `orden`(codigo_vendedor);
CREATE INDEX idx_orden_estado        ON `orden`(estado_orden);
CREATE INDEX idx_valoracion_prod     ON `valoracion`(id_pub);
CREATE INDEX idx_valoracion_vend     ON `valoracion`(codigo_vendedor);
CREATE INDEX idx_valoracion_activa   ON `valoracion`(estado_valo);
CREATE INDEX idx_pqr_usuario         ON `pqr`(codigo_user);
CREATE INDEX idx_pqr_admin           ON `pqr`(codigo_admin);
CREATE INDEX idx_pqr_estado          ON `pqr`(estado_pqr);
CREATE INDEX idx_intento_correo      ON `intento_fallido_auth`(correo_intentado);
CREATE INDEX idx_intento_fecha       ON `intento_fallido_auth`(fecha_hora);



INSERT INTO `resena_predefinida` (texto_resena, activo) VALUES
    ('Excelente producto, cumplió mis expectativas', TRUE),
    ('Buen producto, recomendado', TRUE),
    ('Producto en buen estado, vendedor confiable', TRUE),
    ('El producto llegó tal como se describió', TRUE),
    ('Podría mejorar, pero cumple su función', TRUE),
    ('No cumplió mis expectativas', TRUE),
    ('Producto con defectos, no recomendado', TRUE);

SELECT * FROM `resena_predefinida`;