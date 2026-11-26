package intercard.datos;

import intercard.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO (Data Access Object): clase que guarda y lee clientes de la tabla "cliente".
 * Todas las consultas usan PreparedStatement con "?" para evitar inyección SQL.
 */
public class ClienteDAO {

    /**
     * Guarda un cliente nuevo y le asigna el id generado por la base de datos.
     */
    public void insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (dni, nombres, apellidos, telefono, correo) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getDni());
            ps.setString(2, cliente.getNombres());
            ps.setString(3, cliente.getApellidos());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getCorreo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Devuelve el cliente con ese DNI, o null si no existe.
     */
    public Cliente buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE dni = ?";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearCliente(rs);
                }
                return null;
            }
        }
    }

    /**
     * Devuelve todos los clientes ordenados por apellidos.
     */
    public ArrayList<Cliente> listar() throws SQLException {
        ArrayList<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY apellidos, nombres";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearCliente(rs));
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Cliente.
     * Es "static" y sin "private" para que TarjetaDAO y VentaDAO (del mismo paquete)
     * también la usen cuando sus consultas traen los datos del cliente.
     */
    static Cliente crearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
                rs.getString("dni"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("telefono"),
                rs.getString("correo"));
        cliente.setIdCliente(rs.getInt("id_cliente"));
        return cliente;
    }
}
