package org.example.cliente.factory.json;

import org.example.cliente.factory.IPersistenciaAgenda;
import org.example.cliente.factory.IPersistenciaConversaciones;
import org.example.cliente.factory.IPersistenciaFactory;
import org.example.cliente.factory.txt.PersistenciaAgendaTXT;
import org.example.cliente.factory.txt.PersistenciaConversacionesTXT;
import org.example.cliente.modelo.usuario.Contacto;

public class PersistenciaJSONFactory implements IPersistenciaFactory {

    @Override
    public IPersistenciaConversaciones crearPersistenciaConversaciones(Contacto usuarioDTO) {
        return new PersistenciaConversacionesJSON(usuarioDTO);
    }

    @Override
    public IPersistenciaAgenda crearPersistenciaAgenda(Contacto usuarioDTO) {
        return new PersistenciaAgendaJSON(usuarioDTO);
    }
}

