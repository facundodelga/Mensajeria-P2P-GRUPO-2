package org.example.vista;

import java.util.List;

public interface IMensajeria {
    void enviarMensaje(String mensaje);
    List<Mensaje> obtenerMensajes(String contacto);
    void agregarContacto(String nombre, String ip, String puerto);
}