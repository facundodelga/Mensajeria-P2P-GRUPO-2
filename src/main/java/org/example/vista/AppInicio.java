package org.example.vista;

public class AppInicio {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            VentanaInicioSesion login = new VentanaInicioSesion();
            login.setOnLoginExitoso(() -> {
                VentanaPrincipal vista = new VentanaPrincipal();
                new AppControlador(vista);
                vista.setVisible(true);
            });
            login.setVisible(true);
        });
    }
}
