package org.example.modelo;

import org.example.modelo.usuario.Contacto;

public interface IAgenda {
    void addContacto(Contacto contacto) throws ContactoRepetidoException;
    Contacto buscaNombreContacto(Contacto contacto);

}
