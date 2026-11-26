package intercard.modelo;

/**
 * Persona que atiende en el punto de venta e inicia sesión en el sistema.
 * Corresponde a la tabla "empleado" de la base de datos.
 *
 * La contraseña no se guarda en este objeto: solo la base de datos
 * guarda su huella (hash) y la verifica al iniciar sesión.
 */
public class Empleado extends Persona {

    private int idEmpleado;
    private String usuario;
    private String cargo;

    public Empleado(String dni, String nombres, String apellidos, String telefono,
            String usuario, String cargo) {
        super(dni, nombres, apellidos, telefono);
        this.usuario = usuario;
        this.cargo = cargo;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
