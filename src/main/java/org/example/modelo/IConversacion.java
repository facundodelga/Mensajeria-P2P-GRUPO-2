package org.example.modelo;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.Contacto;

import java.util.List;

public interface IConversacion {
    void addMensajeEntrante(Mensaje mensaje);
    void addMensajeSaliente(Contacto contacto, Mensaje mensaje);
    List<Mensaje> getMensajes(Contacto contacto);
    void agregarConversacion(Contacto contacto);
    void setConversacionPendiente(Contacto contacto);
}
