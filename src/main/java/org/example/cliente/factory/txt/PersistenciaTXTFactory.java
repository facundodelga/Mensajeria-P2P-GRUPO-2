package org.example.cliente.factory.txt;

import org.example.cliente.factory.IPersistencia;
import org.example.cliente.factory.PersistenciaFactory;
import org.example.cliente.modelo.usuario.Contacto;

public class PersistenciaTXTFactory implements PersistenciaFactory {

    @Override
    public IPersistencia crearPersistencia(Contacto usuarioDTO) {
        return new PersistenciaTXT(usuarioDTO);
    }
}
