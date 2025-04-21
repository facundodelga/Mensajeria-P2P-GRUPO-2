package org.example.cliente.vista;

import java.util.List;

public interface IMensajeria {
    void enviarMensaje(String mensaje);
    List<MensajePantalla> obtenerMensajes(String contacto);
    void agregarContacto(String nombre, String ip, String puerto);
}