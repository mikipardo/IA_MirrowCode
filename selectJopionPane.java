import javax.swing.*;

public class JOptionPaneEjemplo {

    public static void main(String[] args) {
        // Opciones para el menú desplegable
        String[] opciones = {"Opción 1", "Opción 2", "Opción 3"};

        // Mostrar el JOptionPane con un menú desplegable
        String seleccion = (String) JOptionPane.showInputDialog(
                null,
                "Selecciona una opción:",
                "Selección",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        // Verificar la selección del usuario
        if (seleccion != null) {
            // Mostrar un JOptionPane con respuesta dependiendo de la selección
            if (seleccion.equals("Opción 1")) {
                JOptionPane.showMessageDialog(null, "Seleccionaste la Opción 1");
            } else {
                int respuesta = JOptionPane.showConfirmDialog(null, "¿Estás seguro?");
                if (respuesta == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(null, "Seleccionaste " + seleccion + " y elegiste Sí");
                } else if (respuesta == JOptionPane.NO_OPTION) {
                    JOptionPane.showMessageDialog(null, "Seleccionaste " + seleccion + " y elegiste No");
                } else {
                    JOptionPane.showMessageDialog(null, "Seleccionaste " + seleccion + " y no elegiste ninguna opción");
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "No seleccionaste ninguna opción");
        }
    }
}
