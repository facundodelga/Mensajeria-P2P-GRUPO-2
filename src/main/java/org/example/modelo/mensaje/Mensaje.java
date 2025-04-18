package org.example.modelo.mensaje;

import org.example.modelo.usuario.Contacto;

import java.io.Serializable;
import java.util.Date;

public class Mensaje implements Serializable {
    private Date fecha = new Date();
    private String contenido;
    private Contacto emisor;

    public Mensaje(String contenido, Contacto usuario) {
        this.contenido = contenido;
        this.emisor = usuario;

    }

    public Date getFecha() {
        return fecha;
    }

    public String getContenido() {
        return contenido;
    }

    public Contacto getEmisor() {
        return emisor;
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "fecha=" + fecha +
                ", contenido='" + contenido + '\'' +
                ", usuario=" + emisor +
                '}';
    }
}
