package org.example.cliente.modelo;

import org.example.cliente.modelo.usuario.Contacto;

public interface IAgenda {
    void addContacto(Contacto contacto) throws ContactoRepetidoException;
    Contacto buscaNombreContacto(String nombreContacto);

}
