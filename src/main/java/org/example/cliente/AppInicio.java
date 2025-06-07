package org.example.cliente;

import org.example.cliente.controlador.Controlador;

import org.example.servidor.Servidor;
import org.example.cliente.vista.VentanaInicioSesion;
import org.example.cliente.vista.VentanaPrincipal;

import java.io.IOException;

public class AppInicio {
    public static final int PUERTO_SERVIDOR_DIRECTORIO = 8000;
    public static void main(String[] args) {

        Controlador.getInstancia().setVistaInicioSesion(new VentanaInicioSesion());
        Controlador.getInstancia().setVistaPrincipal(new VentanaPrincipal());



    }
}
