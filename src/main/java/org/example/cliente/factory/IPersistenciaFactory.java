package org.example.cliente.factory;

import org.example.cliente.modelo.usuario.Contacto;

public interface IPersistenciaFactory {
    IPersistenciaConversaciones crearPersistenciaConversaciones(Contacto usuario);
    IPersistenciaAgenda crearPersistenciaAgenda(Contacto usuario);
}
