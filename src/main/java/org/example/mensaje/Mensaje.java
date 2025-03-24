package org.example.mensaje;

import org.example.usuario.Usuario;
import org.example.usuario.UsuarioDTO;

import java.io.Serializable;
import java.util.Date;

public class Mensaje implements Serializable {
    private Date fecha = new Date();
    private String contenido;
    private UsuarioDTO usuario;

    public Mensaje(String contenido, UsuarioDTO usuario) {
        this.contenido = contenido;
        this.usuario = usuario;

    }

    public Date getFecha() {
        return fecha;
    }

    public String getContenido() {
        return contenido;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "fecha=" + fecha +
                ", contenido='" + contenido + '\'' +
                ", usuario=" + usuario +
                '}';
    }
}
