package org.example.vista;

import org.example.controlador.Controlador;

public class AppInicio {
    public static void main(String[] args) {
        Controlador.getInstancia().setVistaInicioSesion(new VentanaInicioSesion());
        Controlador.getInstancia().setVistaPrincipal(new VentanaPrincipal());



        /*
        javax.swing.SwingUtilities.invokeLater(() -> {
            VentanaInicioSesion login = new VentanaInicioSesion();
            login.setOnLoginExitoso(() -> {
                VentanaPrincipal vista = new VentanaPrincipal();
                new AppControlador(vista);
                vista.setVisible(true);
            });
            login.setVisible(true);
        });
        */
    }
}
