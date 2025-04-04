package org.example.modelo;

import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;

public class AgendaServicio implements IAgenda {
    private Usuario usuario;
    public AgendaServicio(Usuario usuario){
        this.usuario = usuario;
    }


    @Override
    public void addContacto(UsuarioDTO contacto) {
        usuario.getContactos().add(contacto);
        System.out.println(contacto.toString());
    }

    @Override
    public UsuarioDTO buscaNombreContacto(UsuarioDTO contacto) {
        for (UsuarioDTO c : usuario.getContactos()) {
            if (c.equals(contacto)) {
                return c;
            }
        }
        return null;
    }
}
