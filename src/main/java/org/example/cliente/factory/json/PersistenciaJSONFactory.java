package org.example.cliente.factory.json;

import org.example.cliente.factory.IPersistencia;
import org.example.cliente.factory.PersistenciaFactory;
import org.example.cliente.modelo.usuario.Contacto;

public class PersistenciaJSONFactory implements PersistenciaFactory {
    @Override
    public IPersistencia crearPersistencia(Contacto usuarioDTO) {
        return null;
    }
}
