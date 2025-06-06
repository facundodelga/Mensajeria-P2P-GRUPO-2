package org.example.cliente.factory.xml;

import org.example.cliente.factory.IPersistencia;
import org.example.cliente.factory.PersistenciaFactory;
import org.example.cliente.modelo.usuario.Contacto;

public class PersistenciaXMLFactory implements PersistenciaFactory {

    @Override
    public IPersistencia crearPersistencia(Contacto usuarioDTO) {
        return new PersistenciaXML(usuarioDTO);
    }
}
