package intercard.datos;

import intercard.modelo.Cliente;
import intercard.modelo.Empleado;
import intercard.modelo.Tarjeta;
import intercard.modelo.Venta;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * DAO para la tabla "venta".
 *
 * Vender una tarjeta y recargarla cambian DOS tablas a la vez, así que se hacen
 * dentro de una TRANSACCIÓN: o se guardan todos los cambios (commit) o ninguno
 * (rollback). Así nunca queda, por ejemplo, una tarjeta creada sin su venta.
 */
public class VentaDAO {

    /**
     * Crea la tarjeta nueva del cliente y registra su venta.
     */
    public Venta registrarVentaTarjeta(Cliente cliente, Empleado empleado, String codigoTarjeta,
            double saldoInicial, double total) throws SQLException {
        String sqlTarjeta = "INSERT INTO tarjeta (codigo, id_cliente, saldo, activa, fecha_emision) "
                + "VALUES (?, ?, ?, 1, ?)";
        LocalDate hoy = LocalDate.now();

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false); // Inicia la transacción
            try {
                // 1. Guardar la tarjeta
                int idTarjeta;
                try (PreparedStatement ps = con.prepareStatement(sqlTarjeta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, codigoTarjeta);
                    ps.setInt(2, cliente.getIdCliente());
                    ps.setDouble(3, saldoInicial);
                    ps.setDate(4, Date.valueOf(hoy));
                    ps.executeUpdate();
                    idTarjeta = leerIdGenerado(ps);
                }
                Tarjeta tarjeta = new Tarjeta(idTarjeta, codigoTarjeta, saldoInicial, true, hoy, cliente);

                // 2. Guardar la venta
                Venta venta = insertarVenta(con, Venta.VENTA_TARJETA, cliente, empleado, tarjeta, total);

                con.commit(); // Todo salió bien: se guardan los dos cambios
                return venta;
            } catch (SQLException e) {
                con.rollback(); // Algo falló: se deshacen los cambios
                throw e;
            }
        }
    }

    /**
     * Suma el monto al saldo de la tarjeta y registra la recarga.
     */
    public Venta registrarRecarga(Tarjeta tarjeta, Empleado empleado, double monto) throws SQLException {
        // La condición "activa = 1" evita recargar una tarjeta que fue bloqueada
        String sqlSaldo = "UPDATE tarjeta SET saldo = saldo + ? WHERE id_tarjeta = ? AND activa = 1";

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                // 1. Actualizar el saldo
                try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                    ps.setDouble(1, monto);
                    ps.setInt(2, tarjeta.getIdTarjeta());
                    int filas = ps.executeUpdate();
                    if (filas == 0) {
                        throw new SQLException("La tarjeta " + tarjeta.getCodigo() + " no existe o está bloqueada.");
                    }
                }

                // 2. Guardar la recarga
                Venta venta = insertarVenta(con, Venta.RECARGA, tarjeta.getTitular(), empleado, tarjeta, monto);

                con.commit();
                return venta;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Busca ventas aplicando filtros opcionales.
     *
     * @param concepto Venta.VENTA_TARJETA, Venta.RECARGA, o null para todos los tipos.
     * @param fecha    un día concreto, o null para todas las fechas.
     */
    public ArrayList<Venta> buscar(String concepto, LocalDate fecha) throws SQLException {
        // Se arma la consulta agregando solo los filtros que se usan
        StringBuilder sql = new StringBuilder(
                "SELECT v.id_venta, v.fecha, v.concepto, v.monto, "
                + "c.id_cliente, c.dni, c.nombres, c.apellidos, c.telefono, c.correo, "
                + "e.id_empleado, e.dni AS emp_dni, e.nombres AS emp_nombres, e.apellidos AS emp_apellidos, "
                + "e.telefono AS emp_telefono, e.usuario AS emp_usuario, e.cargo AS emp_cargo, "
                + "t.id_tarjeta, t.codigo, t.saldo, t.activa, t.fecha_emision "
                + "FROM venta v "
                + "INNER JOIN cliente c ON c.id_cliente = v.id_cliente "
                + "INNER JOIN empleado e ON e.id_empleado = v.id_empleado "
                + "INNER JOIN tarjeta t ON t.id_tarjeta = v.id_tarjeta "
                + "WHERE 1 = 1 ");
        if (concepto != null) {
            sql.append("AND v.concepto = ? ");
        }
        if (fecha != null) {
            sql.append("AND CAST(v.fecha AS DATE) = ? "); // compara solo el día, sin la hora
        }
        sql.append("ORDER BY v.fecha DESC, v.id_venta DESC");

        ArrayList<Venta> lista = new ArrayList<>();
        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int posicion = 1;
            if (concepto != null) {
                ps.setString(posicion, concepto);
                posicion++;
            }
            if (fecha != null) {
                ps.setDate(posicion, Date.valueOf(fecha));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearVenta(rs));
                }
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM venta";

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Suma los montos de las ventas de un tipo (o de todas si concepto es null).
     */
    public double sumarMontos(String concepto) throws SQLException {
        String sql = "SELECT ISNULL(SUM(monto), 0) FROM venta";
        if (concepto != null) {
            sql = sql + " WHERE concepto = ?";
        }

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (concepto != null) {
                ps.setString(1, concepto);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    // ---------------- Métodos de apoyo ----------------

    /**
     * Inserta una fila en "venta" usando la conexión (y transacción) recibida.
     */
    private Venta insertarVenta(Connection con, String concepto, Cliente cliente, Empleado empleado,
            Tarjeta tarjeta, double monto) throws SQLException {
        String sql = "INSERT INTO venta (fecha, concepto, id_cliente, id_empleado, id_tarjeta, monto) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        LocalDateTime ahora = LocalDateTime.now().withNano(0);

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(ahora));
            ps.setString(2, concepto);
            ps.setInt(3, cliente.getIdCliente());
            ps.setInt(4, empleado.getIdEmpleado());
            ps.setInt(5, tarjeta.getIdTarjeta());
            ps.setDouble(6, monto);
            ps.executeUpdate();
            int idVenta = leerIdGenerado(ps);
            return new Venta(idVenta, ahora, concepto, cliente, empleado, tarjeta, monto);
        }
    }

    /**
     * Lee el id (IDENTITY) que la base de datos asignó a la fila recién insertada.
     */
    private int leerIdGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Venta
     * (con su cliente, empleado y tarjeta).
     */
    private Venta crearVenta(ResultSet rs) throws SQLException {
        Cliente cliente = ClienteDAO.crearCliente(rs);

        Empleado empleado = new Empleado(
                rs.getString("emp_dni"),
                rs.getString("emp_nombres"),
                rs.getString("emp_apellidos"),
                rs.getString("emp_telefono"),
                rs.getString("emp_usuario"),
                rs.getString("emp_cargo"));
        empleado.setIdEmpleado(rs.getInt("id_empleado"));

        Tarjeta tarjeta = TarjetaDAO.crearTarjeta(rs, cliente);

        return new Venta(
                rs.getInt("id_venta"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getString("concepto"),
                cliente,
                empleado,
                tarjeta,
                rs.getDouble("monto"));
    }
}
