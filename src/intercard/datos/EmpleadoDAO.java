package intercard.datos;

import intercard.modelo.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO para la tabla "empleado".
 */
public class EmpleadoDAO {

    /**
     * Busca un empleado activo con ese usuario y esa huella (hash) de contraseña.
     * Devuelve el empleado si los datos son correctos, o null si no lo son.
     */
    public Empleado buscarPorCredenciales(String usuario, String contrasenaHash) throws SQLException {
        String sql = "SELECT * FROM empleado WHERE usuario = ? AND contrasena_hash = ? AND activo = 1";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, contrasenaHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Empleado empleado = new Empleado(
                            rs.getString("dni"),
                            rs.getString("nombres"),
                            rs.getString("apellidos"),
                            rs.getString("telefono"),
                            rs.getString("usuario"),
                            rs.getString("cargo"));
                    empleado.setIdEmpleado(rs.getInt("id_empleado"));
                    return empleado;
                }
                return null;
            }
        }
    }
}
