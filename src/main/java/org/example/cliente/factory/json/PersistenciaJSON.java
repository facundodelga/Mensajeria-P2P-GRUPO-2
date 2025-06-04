package org.example.cliente.factory.json;

import org.example.cliente.factory.IPersistencia;
import org.example.cliente.modelo.IAgenda;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.usuario.Contacto;

import java.util.Map;

public class PersistenciaJSON implements IPersistencia {

    @Override
    public void guardarConversaciones(Map<Contacto, Conversacion> conversaciones) {

    }

    @Override
    public Map<Contacto, Conversacion> cargarConversaciones(IAgenda agendaServicio) {
        return null;
    }
}
