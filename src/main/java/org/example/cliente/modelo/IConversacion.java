package org.example.cliente.modelo;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.util.List;

public interface IConversacion {
    void addMensajeEntrante(Mensaje mensaje);
    void addMensajeSaliente(Contacto contacto, Mensaje mensaje);
    List<Mensaje> getMensajes(Contacto contacto);
    void agregarConversacion(Contacto contacto);
    void setConversacionPendiente(Contacto contacto);
}
