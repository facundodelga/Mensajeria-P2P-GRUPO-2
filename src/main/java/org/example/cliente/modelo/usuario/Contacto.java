// Archivo: org/example/cliente/modelo/usuario/Contacto.java

package org.example.cliente.modelo.usuario;

import java.io.Serializable; // Importante para la serialización

/**
 * Representa un contacto en la agenda de un usuario.
 * Puede ser el propio usuario o un contacto externo.
 */
public class Contacto implements Serializable {
    private static final long serialVersionUID = 1L; // Para control de versiones en serialización

    private String nombre;
    private String ip;
    private int puerto;

    // Constructor para el propio usuario
    public Contacto(Usuario usuario) {
        this.nombre = usuario.getNombre();
        this.ip = usuario.getIp();
        this.puerto = usuario.getPuerto();
    }

    // Constructor para contactos externos (ej. agregados por el usuario)
    public Contacto(String nombre, String ip, int puerto) {
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    // Setters (si son necesarios, pero para Contacto a menudo son inmutables)
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    /**
     * Implementación de equals para comparar objetos Contacto por su contenido.
     * Dos contactos son iguales si tienen el mismo nombre, IP y puerto.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si es la misma instancia, son iguales
        if (o == null || getClass() != o.getClass()) return false; // Si es nulo o de diferente clase, no son iguales

        Contacto contacto = (Contacto) o; // Castear a Contacto

        // Comparar nombre, IP y puerto
        if (puerto != contacto.puerto) return false;
        if (!nombre.equals(contacto.nombre)) return false; // Usar .equals() para Strings
        return ip != null ? ip.equals(contacto.ip) : contacto.ip == null; // Manejar IP nula
    }

    /**
     * Implementación de hashCode consistente con equals.
     * NECESARIO para que los objetos Contacto funcionen correctamente como claves en HashMap.
     */
    @Override
    public int hashCode() {
        int result = nombre.hashCode();
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + puerto;
        return result;
    }

    @Override
    public String toString() {
        return "Contacto{" +
                "nombre='" + nombre + '\'' +
                ", ip='" + ip + '\'' +
                ", puerto=" + puerto +
                '}';
    }
}