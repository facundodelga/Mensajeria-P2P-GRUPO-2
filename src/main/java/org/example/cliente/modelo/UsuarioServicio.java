package org.example.cliente.modelo;

import org.example.cliente.modelo.usuario.Usuario;

/**
 * Clase que proporciona servicios relacionados con un usuario.
 * Implementa la interfaz IUsuario.
 */
public class UsuarioServicio implements IUsuario {
    private Usuario usuario;

    /**
     * Constructor de la clase UsuarioServicio.
     * @param usuario El usuario al que pertenecen los servicios.
     */
    public UsuarioServicio(Usuario usuario){
        this.usuario = usuario;
    }

    /**
     * Obtiene el nombre del usuario.
     * @return El nombre del usuario.
     */
    @Override
    public String getNombre() {
        return usuario.getNombre();
    }

    /**
     * Obtiene la dirección IP del usuario.
     * @return La dirección IP del usuario.
     */
    @Override
    public String getIp() {
        return usuario.getIp();
    }

    /**
     * Obtiene el puerto de conexión del usuario.
     * @return El puerto de conexión del usuario.
     */
    @Override
    public int getPuerto() {
        return usuario.getPuerto();
    }
}