package intercard.modelo;

/**
 * Persona que compra y recarga tarjetas interoperables.
 * Corresponde a la tabla "cliente" de la base de datos.
 */
public class Cliente extends Persona {

    private int idCliente; // Código que le asigna la base de datos (0 si aún no se guardó)
    private String correo;

    public Cliente(String dni, String nombres, String apellidos, String telefono, String correo) {
        super(dni, nombres, apellidos, telefono);
        this.correo = correo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
