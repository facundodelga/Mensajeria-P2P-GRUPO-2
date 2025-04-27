package org.example.cliente.vista;

import org.example.cliente.modelo.usuario.Contacto;

import javax.swing.*;
import java.util.ArrayList;

public interface IVistaAgregarContacto {
    // Getters para acceder a los campos (sin tomar los placeholders)
    String getNombre();

    String getIP();

    String getPuerto();
    void mostrar();
    void ocultar();

    void actualizarDirectorio(ArrayList<Contacto> contactos);
}
