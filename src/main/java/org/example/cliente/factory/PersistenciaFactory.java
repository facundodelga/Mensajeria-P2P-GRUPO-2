package org.example.cliente.factory;

import org.example.cliente.modelo.usuario.Contacto;

public interface PersistenciaFactory {
    IPersistencia crearPersistencia(Contacto usuarioDTO);
}
