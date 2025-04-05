package org.example.modelo;

import org.example.modelo.usuario.UsuarioDTO;

public interface IAgenda {
    void addContacto(UsuarioDTO contacto) throws ContactoRepetidoException;
    UsuarioDTO buscaNombreContacto(UsuarioDTO contacto);

}
