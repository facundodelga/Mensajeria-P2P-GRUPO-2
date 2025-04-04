package org.example.modelo.usuario;

import org.example.modelo.conversacion.Conversacion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Usuario {
    private String nombre;
    private String ip;
    private int puerto;
    private List<UsuarioDTO> contactos = new ArrayList<>();
    private Map<UsuarioDTO, Conversacion> conversaciones = new ConcurrentHashMap<>();

    public Usuario(String nombre, String ip, int puerto) {
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public Map<UsuarioDTO, Conversacion> getConversaciones() {
        return conversaciones;
    }

    public List<UsuarioDTO> getContactos() {
        return contactos;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", ip='" + ip + '\'' +
                ", puerto=" + puerto +
                ", contactos=" + contactos +
                ", conversaciones=" + conversaciones +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return puerto == usuario.puerto && Objects.equals(ip, usuario.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, ip, puerto);
    }

}
