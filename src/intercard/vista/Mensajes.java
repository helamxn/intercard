package intercard.vista;

import java.awt.Component;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Mensajes que se repiten en varias ventanas.
 */
public class Mensajes {

    /**
     * Muestra un mensaje cuando falla una operación con la base de datos.
     */
    public static void errorBaseDatos(Component ventana, SQLException e) {
        JOptionPane.showMessageDialog(ventana,
                "Ocurrió un problema con la base de datos:\n" + e.getMessage(),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
