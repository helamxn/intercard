/* =====================================================================
   InterCard - Sistema de Ventas de Tarjetas Interoperables
   Script: crea la base de datos, las tablas y los datos de prueba.

   Se ejecuta una sola vez, en un servidor donde todavía NO existe
   la base de datos InterCardDB.

   Ejecutar con SQL Server Management Studio, o desde la consola:
     sqlcmd -S localhost -E -C -f 65001 -i 01_crear_base_datos.sql
   (-f 65001 indica que el archivo está en UTF-8, para que las tildes se guarden bien)
   ===================================================================== */

CREATE DATABASE InterCardDB;
GO

USE InterCardDB;
GO

/* ---------------------------------------------------------------------
   Tabla CLIENTE: personas que compran y recargan tarjetas
   --------------------------------------------------------------------- */
CREATE TABLE dbo.cliente (
    id_cliente      INT IDENTITY(1,1) NOT NULL,
    dni             CHAR(8)       NOT NULL,
    nombres         VARCHAR(100)  NOT NULL,
    apellidos       VARCHAR(100)  NOT NULL,
    telefono        VARCHAR(15)   NULL,
    correo          VARCHAR(100)  NULL,
    fecha_registro  DATETIME2(0)  NOT NULL CONSTRAINT DF_cliente_fecha_registro DEFAULT SYSDATETIME(),
    CONSTRAINT PK_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT UQ_cliente_dni UNIQUE (dni),
    CONSTRAINT CK_cliente_dni CHECK (dni NOT LIKE '%[^0-9]%' AND LEN(dni) = 8)
);
GO

/* ---------------------------------------------------------------------
   Tabla EMPLEADO: usuarios que inician sesión en el sistema.
   La contraseña NO se guarda: se guarda su huella SHA-256 (64 caracteres hex).
   --------------------------------------------------------------------- */
CREATE TABLE dbo.empleado (
    id_empleado      INT IDENTITY(1,1) NOT NULL,
    dni              CHAR(8)       NOT NULL,
    nombres          VARCHAR(100)  NOT NULL,
    apellidos        VARCHAR(100)  NOT NULL,
    telefono         VARCHAR(15)   NULL,
    usuario          VARCHAR(30)   NOT NULL,
    contrasena_hash  CHAR(64)      NOT NULL,
    cargo            VARCHAR(50)   NOT NULL,
    activo           BIT           NOT NULL CONSTRAINT DF_empleado_activo DEFAULT 1,
    CONSTRAINT PK_empleado PRIMARY KEY (id_empleado),
    CONSTRAINT UQ_empleado_dni UNIQUE (dni),
    CONSTRAINT UQ_empleado_usuario UNIQUE (usuario)
);
GO

/* ---------------------------------------------------------------------
   Tabla TARJETA: cada tarjeta pertenece a un cliente (titular)
   --------------------------------------------------------------------- */
CREATE TABLE dbo.tarjeta (
    id_tarjeta     INT IDENTITY(1,1) NOT NULL,
    codigo         VARCHAR(10)    NOT NULL,
    id_cliente     INT            NOT NULL,
    saldo          DECIMAL(10,2)  NOT NULL CONSTRAINT DF_tarjeta_saldo DEFAULT 0,
    activa         BIT            NOT NULL CONSTRAINT DF_tarjeta_activa DEFAULT 1,
    fecha_emision  DATE           NOT NULL CONSTRAINT DF_tarjeta_fecha_emision DEFAULT CAST(GETDATE() AS DATE),
    CONSTRAINT PK_tarjeta PRIMARY KEY (id_tarjeta),
    CONSTRAINT UQ_tarjeta_codigo UNIQUE (codigo),
    CONSTRAINT FK_tarjeta_cliente FOREIGN KEY (id_cliente) REFERENCES dbo.cliente (id_cliente),
    CONSTRAINT CK_tarjeta_saldo CHECK (saldo >= 0)
);
GO

/* ---------------------------------------------------------------------
   Tabla VENTA: cada operación del punto de venta
   (venta de una tarjeta nueva o recarga de una tarjeta existente)
   --------------------------------------------------------------------- */
CREATE TABLE dbo.venta (
    id_venta     INT IDENTITY(1,1) NOT NULL,
    fecha        DATETIME2(0)   NOT NULL CONSTRAINT DF_venta_fecha DEFAULT SYSDATETIME(),
    concepto     VARCHAR(20)    NOT NULL,
    id_cliente   INT            NOT NULL,
    id_empleado  INT            NOT NULL,
    id_tarjeta   INT            NOT NULL,
    monto        DECIMAL(10,2)  NOT NULL,
    CONSTRAINT PK_venta PRIMARY KEY (id_venta),
    CONSTRAINT FK_venta_cliente  FOREIGN KEY (id_cliente)  REFERENCES dbo.cliente (id_cliente),
    CONSTRAINT FK_venta_empleado FOREIGN KEY (id_empleado) REFERENCES dbo.empleado (id_empleado),
    CONSTRAINT FK_venta_tarjeta  FOREIGN KEY (id_tarjeta)  REFERENCES dbo.tarjeta (id_tarjeta),
    CONSTRAINT CK_venta_concepto CHECK (concepto IN ('Venta de tarjeta', 'Recarga')),
    CONSTRAINT CK_venta_monto    CHECK (monto > 0)
);
GO

-- Índice para que la consulta del historial por fecha sea rápida
CREATE INDEX IX_venta_fecha ON dbo.venta (fecha);
GO

/* =====================================================================
   DATOS DE PRUEBA
   ===================================================================== */

-- Empleados de prueba. Usuario / contraseña:  admin / admin123   y   cajero / cajero123
INSERT INTO dbo.empleado (dni, nombres, apellidos, telefono, usuario, contrasena_hash, cargo) VALUES
('40000001', 'Ana',  'Torres', '900000001', 'admin',
    CONVERT(CHAR(64), HASHBYTES('SHA2_256', 'admin123'), 2), 'Administrador'),
('40000002', 'Luis', 'Ramos',  '900000002', 'cajero',
    CONVERT(CHAR(64), HASHBYTES('SHA2_256', 'cajero123'), 2), 'Cajero');
GO

-- Clientes de prueba
INSERT INTO dbo.cliente (dni, nombres, apellidos, telefono, correo) VALUES
('70000001', 'Carla', 'Mendoza', '910000001', 'carla@correo.com'),
('70000002', 'Jorge', 'Quispe',  '910000002', 'jorge@correo.com'),
('70000003', 'Rosa',  'Huamán',  '910000003', 'rosa@correo.com');
GO

-- Tarjeta TI-00001 de Carla con S/ 10.00 y TI-00002 de Jorge con S/ 20.00
INSERT INTO dbo.tarjeta (codigo, id_cliente, saldo) VALUES
('TI-00001', (SELECT id_cliente FROM dbo.cliente WHERE dni = '70000001'), 10.00),
('TI-00002', (SELECT id_cliente FROM dbo.cliente WHERE dni = '70000002'), 20.00);
GO

-- Ventas atendidas por el cajero: costo de la tarjeta (S/ 4.50) + saldo inicial
INSERT INTO dbo.venta (concepto, id_cliente, id_empleado, id_tarjeta, monto)
SELECT 'Venta de tarjeta', t.id_cliente, e.id_empleado, t.id_tarjeta, 4.50 + t.saldo
FROM dbo.tarjeta t
CROSS JOIN dbo.empleado e
WHERE e.usuario = 'cajero'
ORDER BY t.id_tarjeta;
GO

PRINT 'Base de datos InterCardDB lista.';
GO
