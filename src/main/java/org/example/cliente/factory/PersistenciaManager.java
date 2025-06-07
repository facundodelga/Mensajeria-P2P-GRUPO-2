package org.example.cliente.factory;

import org.example.cliente.modelo.IAgenda;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.usuario.Contacto;

import java.util.List;
import java.util.Map;

public class PersistenciaManager {
    private final IPersistenciaFactory factory;
    private final Contacto usuario;

    public PersistenciaManager(String formato, Contacto usuario) {
        this.factory = new FactorySelector(formato).crearFactory(formato);
        this.usuario = usuario;
    }

    public List<Contacto> cargarAgenda() {
        IPersistenciaAgenda persistenciaAgenda = factory.crearPersistenciaAgenda(usuario);
        return persistenciaAgenda.cargarAgenda();
    }

    public void guardarAgenda(List<Contacto> contactos) {
        IPersistenciaAgenda persistenciaAgenda = factory.crearPersistenciaAgenda(usuario);
        persistenciaAgenda.guardarAgenda(contactos);
    }

    public Map<Contacto, Conversacion> cargarConversaciones(IAgenda agenda) {
        IPersistenciaConversaciones persistenciaConversaciones = factory.crearPersistenciaConversaciones(usuario);
        return persistenciaConversaciones.cargarConversaciones(agenda);
    }

    public void guardarConversaciones(Map<Contacto, Conversacion> conversaciones) {
        IPersistenciaConversaciones persistenciaConversaciones = factory.crearPersistenciaConversaciones(usuario);
        persistenciaConversaciones.guardarConversaciones(conversaciones);
    }
}
