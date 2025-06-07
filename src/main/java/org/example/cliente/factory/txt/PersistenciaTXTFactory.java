package org.example.cliente.factory.txt;

import org.example.cliente.factory.IPersistenciaAgenda;
import org.example.cliente.factory.IPersistenciaConversaciones;
import org.example.cliente.factory.IPersistenciaFactory;
import org.example.cliente.modelo.usuario.Contacto;

public class PersistenciaTXTFactory implements IPersistenciaFactory {

    @Override
    public IPersistenciaConversaciones crearPersistenciaConversaciones(Contacto usuarioDTO) {
        return new PersistenciaConversacionesTXT(usuarioDTO);
    }

    @Override
    public IPersistenciaAgenda crearPersistenciaAgenda(Contacto usuarioDTO) {
        return new PersistenciaAgendaTXT(usuarioDTO);
    }
}
