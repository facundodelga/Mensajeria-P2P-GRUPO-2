package org.example.modelo;

import org.example.modelo.usuario.Usuario;

public class UsuarioServicio implements IUsuario {
    private Usuario usuario;
    public UsuarioServicio(Usuario usuario){
        this.usuario = usuario;
    }

    @Override
    public String getNombre() {
        return usuario.getNombre();
    }

    @Override
    public String getIp() {
        return usuario.getIp();
    }

    @Override
    public int getPuerto() {
        return usuario.getPuerto();
    }








}
