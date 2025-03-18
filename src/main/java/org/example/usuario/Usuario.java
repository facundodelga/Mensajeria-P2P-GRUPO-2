package org.example.usuario;

import org.example.conversacion.Conversacion;
import org.example.mensaje.Mensaje;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Usuario implements Serializable {
    private String nombre;
    private String ip;
    private int puerto;
    private List<UsuarioDTO> contactos;
    private Map<UsuarioDTO, List<Mensaje>> conversaciones = new HashMap<>();

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

    public Map<UsuarioDTO, List<Mensaje>> getConversaciones() {
        return conversaciones;
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
        return puerto == usuario.puerto && Objects.equals(nombre, usuario.nombre) && Objects.equals(ip, usuario.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, ip, puerto);
    }
}
