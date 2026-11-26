package intercard.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Datos para conectarse a la base de datos InterCardDB en SQL Server.
 *
 * Se usa AUTENTICACIÓN DE WINDOWS: la aplicación entra a SQL Server con el
 * mismo usuario de Windows que la está ejecutando, sin usuario ni contraseña.
 *
 * Requisitos:
 * - Haber ejecutado el script basedatos/01_crear_base_datos.sql.
 * - SQL Server con el protocolo TCP/IP activo en el puerto 1433.
 * - En la carpeta lib: el driver mssql-jdbc (.jar) y la librería
 *   mssql-jdbc_auth (.dll) que permite la autenticación de Windows.
 *   El proyecto ya indica dónde está la .dll (-Djava.library.path=lib en
 *   Propiedades del proyecto > Run > VM Options).
 */
public class Conexion {

    // integratedSecurity=true  -> usar la autenticación de Windows
    // encrypt=true             -> cifrar la conexión
    // trustServerCertificate   -> aceptar el certificado propio de un SQL Server local (solo para desarrollo)
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=InterCardDB;integratedSecurity=true;"
            + "encrypt=true;trustServerCertificate=true";

    /**
     * Abre una conexión nueva. Quien la pide debe cerrarla al terminar
     * (por eso en los DAO se usa try-with-resources: try (Connection con = ...) { }).
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Intenta conectarse una vez para comprobar que todo está bien configurado.
     * Lanza SQLException con el motivo si no se pudo conectar.
     */
    public static void probarConexion() throws SQLException {
        try (Connection con = obtenerConexion()) {
            // Si llega aquí, la conexión funcionó; se cierra automáticamente.
        }
    }
}
