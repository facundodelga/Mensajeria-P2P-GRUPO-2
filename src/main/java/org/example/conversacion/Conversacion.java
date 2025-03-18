package org.example.conversacion;

import org.example.mensaje.Mensaje;
import org.example.usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

public class Conversacion {
    private Usuario usuario;
    private List<Mensaje> mensajes = new ArrayList<>();

    public Conversacion(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Mensaje> getMensajes() {
        return mensajes;
    }

    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }
}
