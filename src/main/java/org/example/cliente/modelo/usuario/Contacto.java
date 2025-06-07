package org.example.cliente.modelo.usuario;

import java.io.Serializable;
import java.util.Objects; // Asegúrate de tener esta importación

public class Contacto implements Serializable {
    private String nombre;
    private String ip;
    private int puerto;

    public Contacto() {
    }

    public Contacto(Usuario usuario) {
        this.nombre = usuario.getNombre();
        this.ip = usuario.getIp();
        this.puerto = usuario.getPuerto();
    }

    public Contacto(Contacto c){
        this.nombre = c.getNombre();
        this.ip = c.getIp();
        this.puerto = c.getPuerto();
    }

    public Contacto(String nombre, String ip, int puerto) {
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }
    public static Contacto fromUsuario(Usuario usuario) {
        return new Contacto(usuario);
    }

    public static Usuario toUsuario(Contacto usuarioDTO) {
        return new Usuario(usuarioDTO.getNombre(), usuarioDTO.getIp(), usuarioDTO.getPuerto());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Contacto that = (Contacto) o;
        // CORREGIDO: La igualdad de dos Contacto se basa en su 'nombre' (nickname)
        // La IP y el puerto son detalles de conexión que pueden cambiar, pero el usuario es el mismo.
        return Objects.equals(this.nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        // CORREGIDO: El hashCode debe ser consistente con equals, por lo tanto, se basa en el 'nombre'.
        return Objects.hash(this.nombre);
    }

    @Override
    public String toString() {
        return "Contacto{" + // Cambiado de "UsuarioDTO" a "Contacto" para mayor claridad
                "nombre='" + nombre + '\'' +
                ", ip='" + ip + '\'' +
                ", puerto=" + puerto +
                '}';
    }
}