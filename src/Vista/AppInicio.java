package Vista;

import javax.swing.SwingUtilities;

public class AppInicio {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppVista vista = new AppVista();
            new AppControlador(vista);
        });
    }
}

