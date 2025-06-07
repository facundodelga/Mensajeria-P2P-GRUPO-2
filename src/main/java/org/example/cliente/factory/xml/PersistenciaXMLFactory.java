package org.example.cliente.factory.xml;

import org.example.cliente.factory.IPersistenciaAgenda;
import org.example.cliente.factory.IPersistenciaConversaciones;
import org.example.cliente.factory.IPersistenciaFactory;
import org.example.cliente.modelo.usuario.Contacto;

public class PersistenciaXMLFactory implements IPersistenciaFactory {

    @Override
    public IPersistenciaConversaciones crearPersistenciaConversaciones(Contacto usuarioDTO) {
        return new PersistenciaConversacionesXML(usuarioDTO);
    }

    @Override
    public IPersistenciaAgenda crearPersistenciaAgenda(Contacto usuarioDTO) {
        return new PersistenciaAgendaXML(usuarioDTO);
    }
}
