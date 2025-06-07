package org.example.cliente.modelo.usuario;

import java.io.Serializable;
import java.util.Objects; // Para usar Objects.equals y Objects.hash

public class Contacto implements Serializable {
    private String nombre;
    private String ip;
    private int puerto;

    public Contacto(String nombre, String ip, int puerto) {
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }

    // --- Getters ---
    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    // --- Setters (opcional, pero si los usas deben existir) ---
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    // --- Sobreescribir equals y hashCode ES CRÍTICO para colecciones y comparaciones ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contacto contacto = (Contacto) o;
        // La igualdad se basa en el nombre, IP y puerto para identificar un contacto único
        return puerto == contacto.puerto &&
                Objects.equals(nombre, contacto.nombre) &&
                Objects.equals(ip, contacto.ip);
    }

    @Override
    public int hashCode() {
        // Genera un hash consistente basado en los mismos campos que equals
        return Objects.hash(nombre, ip, puerto);
    }

    @Override
    public String toString() {
        return nombre + " (" + ip + ":" + puerto + ")";
    }
}