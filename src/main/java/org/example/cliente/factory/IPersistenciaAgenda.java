package org.example.cliente.factory;

import org.example.cliente.modelo.usuario.Contacto;

import java.util.List;

public interface IPersistenciaAgenda {
    void guardarAgenda(List<Contacto> contactos);
    List<Contacto> cargarAgenda();
}