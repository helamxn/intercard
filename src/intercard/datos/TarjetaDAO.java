package intercard.datos;

import intercard.modelo.Cliente;
import intercard.modelo.Tarjeta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO para la tabla "tarjeta".
 * Las consultas se unen (JOIN) con "cliente" para traer también los datos del titular.
 */
public class TarjetaDAO {

    // Columnas que se leen en las consultas de tarjetas (tarjeta + su titular)
    private static final String SELECT_TARJETA =
            "SELECT t.id_tarjeta, t.codigo, t.saldo, t.activa, t.fecha_emision, "
            + "c.id_cliente, c.dni, c.nombres, c.apellidos, c.telefono, c.correo "
            + "FROM tarjeta t INNER JOIN cliente c ON c.id_cliente = t.id_cliente ";

    /**
     * Devuelve la tarjeta con ese código, o null si no existe.
     */
    public Tarjeta buscarPorCodigo(String codigo) throws SQLException {
        String sql = SELECT_TARJETA + "WHERE t.codigo = ?";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearTarjeta(rs, ClienteDAO.crearCliente(rs));
                }
                return null;
            }
        }
    }

    /**
     * Devuelve todas las tarjetas ordenadas por código.
     */
    public ArrayList<Tarjeta> listar() throws SQLException {
        ArrayList<Tarjeta> lista = new ArrayList<>();
        String sql = SELECT_TARJETA + "ORDER BY t.codigo";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearTarjeta(rs, ClienteDAO.crearCliente(rs)));
            }
        }
        return lista;
    }

    /**
     * Calcula el código que tendrá la próxima tarjeta. Ejemplo: TI-00003.
     */
    public String obtenerSiguienteCodigo() throws SQLException {
        String sql = "SELECT ISNULL(MAX(id_tarjeta), 0) + 1 FROM tarjeta";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("TI-%05d", rs.getInt(1));
        }
    }

    /**
     * Activa (true) o bloquea (false) una tarjeta.
     */
    public void actualizarEstado(int idTarjeta, boolean activa) throws SQLException {
        String sql = "UPDATE tarjeta SET activa = ? WHERE id_tarjeta = ?";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activa);
            ps.setInt(2, idTarjeta);
            ps.executeUpdate();
        }
    }

    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarjeta";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Cuenta las tarjetas activas (true) o bloqueadas (false).
     */
    public int contarPorEstado(boolean activa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarjeta WHERE activa = ?";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activa);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Tarjeta.
     * También la usa VentaDAO, por eso es "static" y sin "private".
     */
    static Tarjeta crearTarjeta(ResultSet rs, Cliente titular) throws SQLException {
        return new Tarjeta(
                rs.getInt("id_tarjeta"),
                rs.getString("codigo"),
                rs.getDouble("saldo"),
                rs.getBoolean("activa"),
                rs.getDate("fecha_emision").toLocalDate(),
                titular);
    }
}
