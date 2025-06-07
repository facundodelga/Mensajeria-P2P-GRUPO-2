package org.example.cliente.factory;

import org.example.cliente.modelo.IAgenda;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.usuario.Contacto;
import java.util.Map;

public interface IPersistenciaConversaciones {
    void guardarConversaciones(Map<Contacto, Conversacion> conversaciones);
    Map<Contacto, Conversacion> cargarConversaciones(IAgenda agendaServicio);
}
