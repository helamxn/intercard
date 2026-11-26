package intercard.modelo;

import java.time.LocalDate;

/**
 * Tarjeta interoperable que permite pagar en distintos sistemas de transporte.
 * Cada tarjeta pertenece a un cliente (relación de asociación).
 * Corresponde a la tabla "tarjeta" de la base de datos.
 */
public class Tarjeta {

    private int idTarjeta;
    private String codigo;
    private double saldo;
    private boolean activa;
    private LocalDate fechaEmision;
    private Cliente titular;

    /**
     * Crea una tarjeta con todos sus datos (por ejemplo, al leerla de la base de datos).
     */
    public Tarjeta(int idTarjeta, String codigo, double saldo, boolean activa,
            LocalDate fechaEmision, Cliente titular) {
        this.idTarjeta = idTarjeta;
        this.codigo = codigo;
        this.saldo = saldo;
        this.activa = activa;
        this.fechaEmision = fechaEmision;
        this.titular = titular;
    }

    /**
     * Suma el monto indicado al saldo de la tarjeta.
     */
    public void recargar(double monto) {
        saldo = saldo + monto;
    }

    public void bloquear() {
        activa = false;
    }

    public void activar() {
        activa = true;
    }

    /**
     * Devuelve el estado como texto para mostrarlo en tablas y etiquetas.
     */
    public String getEstadoTexto() {
        if (activa) {
            return "Activa";
        } else {
            return "Bloqueada";
        }
    }

    public int getIdTarjeta() {
        return idTarjeta;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public Cliente getTitular() {
        return titular;
    }
}
