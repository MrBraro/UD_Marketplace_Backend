CREATE DATABASE IF NOT EXISTS `marketplace`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
CREATE TABLE `marketplace`.`usuario` (
    codigo_user      INT PRIMARY KEY AUTO_INCREMENT,
    primer_nomb      VARCHAR(100),
    segundo_nom      VARCHAR(100),
    primer_apel      VARCHAR(100),
    segundo_apel     VARCHAR(100),
    fecha_nacimiento DATE,
    correo_institu   VARCHAR(150),
    activo           BOOLEAN,
    permiso_user     LONGBLOB
);
CREATE TABLE `marketplace`.`tel_user` (
    codigo_user INT NOT NULL,
    tel_user    VARCHAR(20) NOT NULL,
    PRIMARY KEY (codigo_user, tel_user),
    CONSTRAINT fk_tel_usuario FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`usuario`(codigo_user)
);
CREATE TABLE `marketplace`.`vendedor` (
    codigo_user  INT PRIMARY KEY,
    calificacion DECIMAL(3,2),
    CONSTRAINT fk_vendedor_usuario FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`usuario`(codigo_user)
);
CREATE TABLE `marketplace`.`administrador` (
    codigo_user     INT PRIMARY KEY,
    numero_contrato INT NOT NULL UNIQUE,
    CONSTRAINT fk_admin_usuario FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`usuario`(codigo_user)
);
CREATE TABLE `marketplace`.`documentos` (
    id_doc      INT PRIMARY KEY AUTO_INCREMENT,
    tipo_doc    VARCHAR(100),
    version     VARCHAR(50),
    activo      BOOLEAN,
    codigo_user INT NOT NULL,
    CONSTRAINT fk_doc_admin FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`administrador`(codigo_user)
);
CREATE TABLE `marketplace`.`promocion` (
    id_prom          INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user      INT NOT NULL,
    tipo_prom        VARCHAR(100),
    descripcion_prom TEXT,
    CONSTRAINT fk_prom_admin FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`administrador`(codigo_user)
);
CREATE TABLE `marketplace`.`categoria` (
    id_categoria INT PRIMARY KEY AUTO_INCREMENT,
    nombre_cat   VARCHAR(100) NOT NULL
);
CREATE TABLE `marketplace`.`producto` (
    id_pub          INT PRIMARY KEY AUTO_INCREMENT,
    codigo_usuario  INT NOT NULL,
    nombre_pub      VARCHAR(150),
    descripcion_pub VARCHAR(500),
    imagen_pub      BLOB,
    ubicacion       VARCHAR(200),
    precio_pub      DECIMAL(12,2),
    disponibilidad  BOOLEAN,
    id_categoria    INT,
    CONSTRAINT fk_prod_vendedor  FOREIGN KEY (codigo_usuario)
        REFERENCES `marketplace`.`vendedor`(codigo_user),
    CONSTRAINT fk_prod_categoria FOREIGN KEY (id_categoria)
        REFERENCES `marketplace`.`categoria`(id_categoria)
);
CREATE TABLE `marketplace`.`cupon` (
    id_cupon     INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user  INT NOT NULL,
    fecha_inicio DATE,
    fecha_fin    DATE,
    id_prom      INT,
    CONSTRAINT fk_cupon_usuario FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`usuario`(codigo_user),
    CONSTRAINT fk_cupon_prom    FOREIGN KEY (id_prom)
        REFERENCES `marketplace`.`promocion`(id_prom)
);
CREATE TABLE `marketplace`.`orden` (
    id_orden     INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user  INT NOT NULL,
    total_compra DECIMAL(12,2),
    estado_orden VARCHAR(50),
    fecha_compr  DATE,
    id_cupon     INT,
    id_pub       INT,
    CONSTRAINT fk_orden_usuario  FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`usuario`(codigo_user),
    CONSTRAINT fk_orden_cupon    FOREIGN KEY (id_cupon)
        REFERENCES `marketplace`.`cupon`(id_cupon),
    CONSTRAINT fk_orden_producto FOREIGN KEY (id_pub)
        REFERENCES `marketplace`.`producto`(id_pub)
);
CREATE TABLE `marketplace`.`valoracion` (
    id_val           INT PRIMARY KEY AUTO_INCREMENT,
    valo_predefinida VARCHAR(100),
    calificacion     INT,
    fecha_valo       DATE,
    estado_valo      BOOLEAN,
    id_orden         INT,
    id_pub           INT,
    codigo_vendedor  INT,
    CONSTRAINT fk_val_orden    FOREIGN KEY (id_orden)
        REFERENCES `marketplace`.`orden`(id_orden),
    CONSTRAINT fk_val_producto FOREIGN KEY (id_pub)
        REFERENCES `marketplace`.`producto`(id_pub),
    CONSTRAINT fk_val_vendedor FOREIGN KEY (codigo_vendedor)
        REFERENCES `marketplace`.`vendedor`(codigo_user)
);
CREATE TABLE `marketplace`.`pqr` (
    radicado           INT PRIMARY KEY AUTO_INCREMENT,
    codigo_user        INT NOT NULL,
    tipo_pqr           VARCHAR(100),
    fecha_creacion_pqr DATE,
    imagen_pqr         BLOB,
    descripcion_pqr    VARCHAR(500),
    codigo_admin       INT,
    estado_pqr         VARCHAR(50),
    CONSTRAINT fk_pqr_usuario FOREIGN KEY (codigo_user)
        REFERENCES `marketplace`.`usuario`(codigo_user),
    CONSTRAINT fk_pqr_admin   FOREIGN KEY (codigo_admin)
        REFERENCES `marketplace`.`administrador`(codigo_user)
);

