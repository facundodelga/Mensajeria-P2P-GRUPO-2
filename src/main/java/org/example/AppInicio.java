package org.example;

import org.example.controlador.*;
import org.example.servidor.ServidorDirectorio;
import org.example.vista.VentanaInicioSesion;
import org.example.vista.VentanaPrincipal;

import java.io.IOException;

public class AppInicio {
    public static final int PUERTO_SERVIDOR_DIRECTORIO = 8000;
    public static void main(String[] args) {
        try {
            ServidorDirectorio servidorDirectorio = new ServidorDirectorio(PUERTO_SERVIDOR_DIRECTORIO);
            new Thread(() -> {
                try {
                    servidorDirectorio.iniciar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
