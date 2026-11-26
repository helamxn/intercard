package intercard.logica;

import intercard.datos.ClienteDAO;
import intercard.datos.EmpleadoDAO;
import intercard.datos.TarjetaDAO;
import intercard.datos.VentaDAO;
import intercard.modelo.Cliente;
import intercard.modelo.Empleado;
import intercard.modelo.Tarjeta;
import intercard.modelo.Venta;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;

/**
 * Clase central del sistema (lógica de negocio).
 *
 * Las ventanas (paquete vista) le piden a esta clase lo que necesitan, y esta
 * clase usa los DAO (paquete datos) para leer y guardar en SQL Server.
 *
 *   vista  --->  SistemaVentas (reglas del negocio)  --->  DAO  --->  base de datos
 *
 * Los métodos lanzan SQLException si hay un problema con la base de datos;
 * las ventanas la atrapan y muestran un mensaje al usuario.
 */
public class SistemaVentas {

    public static final double COSTO_TARJETA = 4.50;

    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private TarjetaDAO tarjetaDAO;
    private VentaDAO ventaDAO;

    public SistemaVentas() {
        clienteDAO = new ClienteDAO();
        empleadoDAO = new EmpleadoDAO();
        tarjetaDAO = new TarjetaDAO();
        ventaDAO = new VentaDAO();
    }

    // ---------------- Empleados ----------------

    /**
     * Devuelve el empleado si el usuario y la contraseña son correctos, o null si no lo son.
     */
    public Empleado iniciarSesion(String usuario, String contrasena) throws SQLException {
        return empleadoDAO.buscarPorCredenciales(usuario, calcularHash(contrasena));
    }

    /**
     * Calcula la huella SHA-256 de un texto, en 64 caracteres hexadecimales.
     * En la base de datos se guarda esta huella en lugar de la contraseña.
     */
    private String calcularHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre existe en Java, así que esto no debería ocurrir
            throw new IllegalStateException(e);
        }
    }

    // ---------------- Clientes ----------------

    /**
     * Registra un cliente nuevo. Devuelve false si ya existe un cliente con ese DNI.
     */
    public boolean registrarCliente(Cliente cliente) throws SQLException {
        if (clienteDAO.buscarPorDni(cliente.getDni()) != null) {
            return false;
        }
        clienteDAO.insertar(cliente);
        return true;
    }

    public Cliente buscarCliente(String dni) throws SQLException {
        return clienteDAO.buscarPorDni(dni);
    }

    public ArrayList<Cliente> getClientes() throws SQLException {
        return clienteDAO.listar();
    }

    public int contarClientes() throws SQLException {
        return clienteDAO.contar();
    }

    // ---------------- Tarjetas ----------------

    public Tarjeta buscarTarjeta(String codigo) throws SQLException {
        return tarjetaDAO.buscarPorCodigo(codigo);
    }

    public ArrayList<Tarjeta> getTarjetas() throws SQLException {
        return tarjetaDAO.listar();
    }

    /**
     * Muestra cuál será el código de la próxima tarjeta, sin crearla todavía.
     */
    public String obtenerSiguienteCodigoTarjeta() throws SQLException {
        return tarjetaDAO.obtenerSiguienteCodigo();
    }

    public void bloquearTarjeta(Tarjeta tarjeta) throws SQLException {
        tarjetaDAO.actualizarEstado(tarjeta.getIdTarjeta(), false);
        tarjeta.bloquear();
    }

    public void activarTarjeta(Tarjeta tarjeta) throws SQLException {
        tarjetaDAO.actualizarEstado(tarjeta.getIdTarjeta(), true);
        tarjeta.activar();
    }

    // ---------------- Ventas ----------------

    /**
     * Crea una tarjeta nueva para el cliente y registra la venta.
     * El monto de la venta es el costo de la tarjeta más el saldo inicial.
     */
    public Venta venderTarjeta(Cliente cliente, double saldoInicial, Empleado empleado) throws SQLException {
        String codigo = tarjetaDAO.obtenerSiguienteCodigo();
        double total = COSTO_TARJETA + saldoInicial;
        return ventaDAO.registrarVentaTarjeta(cliente, empleado, codigo, saldoInicial, total);
    }

    /**
     * Agrega saldo a una tarjeta existente y registra la operación como recarga.
     */
    public Venta recargarTarjeta(Tarjeta tarjeta, double monto, Empleado empleado) throws SQLException {
        Venta venta = ventaDAO.registrarRecarga(tarjeta, empleado, monto);
        tarjeta.recargar(monto); // también se actualiza el objeto que se muestra en pantalla
        return venta;
    }

    /**
     * Busca ventas por tipo y por fecha.
     *
     * @param concepto Venta.VENTA_TARJETA, Venta.RECARGA, o null para todos los tipos.
     * @param fecha    un día concreto, o null para todas las fechas.
     */
    public ArrayList<Venta> buscarVentas(String concepto, LocalDate fecha) throws SQLException {
        return ventaDAO.buscar(concepto, fecha);
    }

    public int contarVentas() throws SQLException {
        return ventaDAO.contar();
    }

    // ---------------- Estadísticas (usadas en Inicio) ----------------

    public double calcularMontoTotalVendido() throws SQLException {
        return ventaDAO.sumarMontos(null);
    }

    /**
     * Suma el monto de las ventas de un solo tipo
     * (por ejemplo, Venta.VENTA_TARJETA o Venta.RECARGA).
     */
    public double calcularMontoPorConcepto(String concepto) throws SQLException {
        return ventaDAO.sumarMontos(concepto);
    }

    public int contarTarjetas() throws SQLException {
        return tarjetaDAO.contar();
    }

    public int contarTarjetasActivas() throws SQLException {
        return tarjetaDAO.contarPorEstado(true);
    }

    public int contarTarjetasBloqueadas() throws SQLException {
        return tarjetaDAO.contarPorEstado(false);
    }
}
