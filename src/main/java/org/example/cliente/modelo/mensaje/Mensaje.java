package org.example.cliente.modelo.mensaje;

import org.example.cliente.modelo.usuario.Contacto; // Asumo que Contacto es Serializable
import java.io.Serializable;
import java.util.Date;
import java.util.UUID; // Para generar IDs únicos

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L; // Para control de versiones de serialización

    private String id; // ID único del mensaje
    private Date fecha; // Timestamp del mensaje
    private byte[] contenidoCifrado; // Contenido encriptado (incluye IV y GCM Tag)
    private Contacto emisor;
    private Contacto receptor;
    private transient String contenidoDesencriptado; // No se serializa, solo para uso en memoria/UI

    // Constructor para mensajes que se van a enviar (inicialmente con texto plano)
    // El cifrado se realizará externamente (por el CreadorMensaje)
    public Mensaje(String contenidoPlano, Contacto emisor, Contacto receptor) {
        this.id = UUID.randomUUID().toString(); // Generar un ID único
        this.fecha = new Date(); // Fecha actual
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoDesencriptado = contenidoPlano; // Para el emisor, ya está "desencriptado"
        this.contenidoCifrado = null; // Se establecerá después del cifrado
    }

    // Constructor para mensajes cargados de persistencia o recibidos (ya cifrados)
    public Mensaje(String id, Date fecha, byte[] contenidoCifrado, Contacto emisor, Contacto receptor) {
        this.id = id;
        this.fecha = fecha;
        this.contenidoCifrado = contenidoCifrado;
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoDesencriptado = null; // Se desencriptará cuando se necesite
    }

    // Constructor vacío requerido por algunos serializadores (ej. JAXB, Gson para deserializar)
    public Mensaje() {
    }

    public String getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    public byte[] getContenidoCifrado() {
        return contenidoCifrado;
    }

    public Contacto getEmisor() {
        return emisor;
    }

    public Contacto getReceptor() {
        return receptor;
    }

    public String getContenidoDesencriptado() {
        return contenidoDesencriptado;
    }

    // Setters (útiles para la creación/carga y el proceso de cifrado/desencriptado)
    public void setId(String id) {
        this.id = id;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setContenidoCifrado(byte[] contenidoCifrado) {
        this.contenidoCifrado = contenidoCifrado;
    }

    public void setEmisor(Contacto emisor) {
        this.emisor = emisor;
    }

    public void setReceptor(Contacto receptor) {
        this.receptor = receptor;
    }

    public void setContenidoDesencriptado(String contenidoDesencriptado) {
        this.contenidoDesencriptado = contenidoDesencriptado;
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "id='" + id + '\'' +
                ", fecha=" + fecha +
                ", emisor=" + emisor +
                ", receptor=" + receptor +
                ", contenidoCifrado=" + (contenidoCifrado != null ? "[Cifrado]" : "[Nulo]") +
                ", contenidoDesencriptado='" + (contenidoDesencriptado != null ? contenidoDesencriptado : "[No desencriptado]") + '\'' +
                '}';
    }
}