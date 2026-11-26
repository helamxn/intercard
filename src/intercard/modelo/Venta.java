package intercard.modelo;

import java.time.LocalDateTime;

/**
 * Registro de una operación realizada en el punto de venta:
 * la venta de una tarjeta nueva o la recarga de una tarjeta existente.
 * Relaciona a un cliente, un empleado y una tarjeta.
 * Corresponde a la tabla "venta" de la base de datos.
 */
public class Venta {

    // Tipos de operación (deben coincidir con los permitidos en la base de datos)
    public static final String VENTA_TARJETA = "Venta de tarjeta";
    public static final String RECARGA = "Recarga";

    private int numero;            // id_venta en la base de datos
    private LocalDateTime fecha;   // fecha y hora de la operación
    private String concepto;
    private Cliente cliente;
    private Empleado empleado;
    private Tarjeta tarjeta;
    private double monto;

    public Venta(int numero, LocalDateTime fecha, String concepto, Cliente cliente,
            Empleado empleado, Tarjeta tarjeta, double monto) {
        this.numero = numero;
        this.fecha = fecha;
        this.concepto = concepto;
        this.cliente = cliente;
        this.empleado = empleado;
        this.tarjeta = tarjeta;
        this.monto = monto;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getConcepto() {
        return concepto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public Tarjeta getTarjeta() {
        return tarjeta;
    }

    public double getMonto() {
        return monto;
    }
}
