package org.example.modelo;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.util.List;

public interface IUsuarioDAO {
    String getNombre();

    String getIp();

    int getPuerto();

    void addContacto(UsuarioDTO contacto);

    void addMensaje(Mensaje mensaje);
    List<Mensaje> getMensajes(UsuarioDTO contacto);
    void setConversacionPendiente(UsuarioDTO contacto);
}
