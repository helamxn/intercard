# InterCard

Sistema de escritorio para la venta y recarga de **tarjetas interoperables** de transporte.
Proyecto académico hecho en Java con Swing (NetBeans) y SQL Server.

## Funcionalidades

- Inicio de sesión de empleados.
- Registro de clientes (con consulta de nombres por DNI).
- Venta de tarjetas y recarga de saldo.
- Bloqueo y activación de tarjetas.
- Historial de ventas con filtros por tipo de operación y por fecha.
- Estadísticas en la pantalla de inicio (gráficos con JFreeChart).

## Requisitos

- JDK 27 o superior.
- Apache NetBeans.
- SQL Server con TCP/IP activo en el puerto 1433.
- Windows (la conexión usa la autenticación de Windows).

Las librerías ya están en la carpeta `lib`: JFreeChart, el driver JDBC de SQL Server
y la DLL `mssql-jdbc_auth` para la autenticación de Windows.

## Cómo ejecutarlo

1. Crear la base de datos con el script `basedatos/01_crear_base_datos.sql`
   (desde SQL Server Management Studio, o con
   `sqlcmd -S localhost -E -C -f 65001 -i basedatos/01_crear_base_datos.sql`).
2. Abrir el proyecto en NetBeans.
3. Ejecutar con **Run Project** (F6).

Usuarios de prueba:

| Usuario  | Contraseña  |
|----------|-------------|
| `admin`  | `admin123`  |
| `cajero` | `cajero123` |

## Estructura

```
basedatos/            Script SQL de la base de datos (tablas y datos de prueba)
lib/                  Librerías externas
src/intercard/
├── modelo/           Clases de datos: Persona, Cliente, Empleado, Tarjeta, Venta
├── datos/            Conexión a SQL Server y clases DAO
├── logica/           SistemaVentas (reglas del negocio) y ConsultaDni
└── vista/            Ventanas (JFrame) y secciones (JPanel) del diseñador de NetBeans
```
