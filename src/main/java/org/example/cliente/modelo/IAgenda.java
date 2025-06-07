package org.example.cliente.modelo;

import org.example.cliente.modelo.usuario.Contacto;

import java.util.List;

public interface IAgenda {
    void addContacto(Contacto contacto) throws ContactoRepetidoException;
    Contacto buscaNombreContacto(Contacto contacto);
    List<Contacto> getContactos();
}
