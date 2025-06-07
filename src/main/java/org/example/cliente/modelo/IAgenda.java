package org.example.cliente.modelo;

import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.usuario.Usuario;

public interface IAgenda {
    void addContacto(Contacto contacto) throws ContactoRepetidoException;
    Contacto buscaNombreContacto(Contacto contacto);
    Usuario getUsuario();
}
