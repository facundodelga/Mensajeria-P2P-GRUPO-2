package org.example.modelo;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.util.List;

public interface IConversacion {
    void addMensajeEntrante(Mensaje mensaje);
    void addMensajeSaliente(UsuarioDTO contacto, Mensaje mensaje);
    List<Mensaje> getMensajes(UsuarioDTO contacto);
    void setConversacionPendiente(UsuarioDTO contacto);
}
