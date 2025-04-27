package org.example.cliente.modelo.mensaje;

import org.example.cliente.modelo.usuario.Contacto;

import java.io.Serializable;
import java.util.Date;

public class Mensaje implements Serializable {
    private Date fecha = new Date();
    private String contenido;
    private Contacto emisor;
    private Contacto receptor;

    public Mensaje(String contenido, Contacto usuario) {
        this.contenido = contenido;
        this.emisor = usuario;

    }

    public Mensaje(String contenido, Contacto emisor,Contacto receptor) {
        this.contenido = contenido;
        this.receptor=receptor;
        this.emisor=emisor;
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

    public Contacto getReceptor() {
        return receptor;
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
