package intercard;

import intercard.datos.Conexion;
import intercard.logica.SistemaVentas;
import intercard.vista.VentanaLogin;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Punto de inicio del Sistema de Ventas de Tarjetas Interoperables.
 *
 * Organización del proyecto:
 * - intercard.modelo : clases con los datos (Persona, Cliente, Empleado, Tarjeta, Venta).
 * - intercard.datos  : conexión a SQL Server y clases DAO que leen y guardan en las tablas.
 * - intercard.logica : SistemaVentas (reglas del negocio) y ConsultaDni (servicio web).
 * - intercard.vista  : ventanas (JFrame) y secciones (JPanel) editables en el diseñador.
 *
 * La carpeta "basedatos" del proyecto tiene el script SQL para crear la base de datos.
 * La conexión usa la autenticación de Windows (ver intercard.datos.Conexion).
 *
 * @author ASUS Vivobook
 */
public class InterCard {

    public static void main(String[] args) {
        // Antes de abrir el sistema se comprueba que la base de datos responde
        try {
            Conexion.probarConexion();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos InterCardDB.\n\n"
                    + "Motivo: " + e.getMessage() + "\n\n"
                    + "Revise que:\n"
                    + "- SQL Server esté iniciado y con TCP/IP activo en el puerto 1433.\n"
                    + "- Se haya ejecutado el script de la carpeta \"basedatos\".\n"
                    + "- La librería mssql-jdbc_auth (.dll) esté en la carpeta lib\n"
                    + "  y el proyecto se ejecute con -Djava.library.path=lib.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Se crea un solo objeto SistemaVentas y se comparte con todas las ventanas
        SistemaVentas sistema = new SistemaVentas();

        // Se usa el aspecto predeterminado (Default) de Swing, sin estilos personalizados
        java.awt.EventQueue.invokeLater(() -> {
            VentanaLogin login = new VentanaLogin(sistema);
            login.setVisible(true);
        });
    }
}
