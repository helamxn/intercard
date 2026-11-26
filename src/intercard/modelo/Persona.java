package intercard.modelo;

/**
 * Clase base que representa a una persona del sistema.
 * Cliente y Empleado heredan de esta clase (herencia).
 */
public class Persona {

    // Atributos privados (encapsulamiento)
    private String dni;
    private String nombres;
    private String apellidos;
    private String telefono;

    public Persona(String dni, String nombres, String apellidos, String telefono) {
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    /**
     * Devuelve el nombre y apellido juntos, listo para mostrar en pantalla.
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // Getters y setters
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
