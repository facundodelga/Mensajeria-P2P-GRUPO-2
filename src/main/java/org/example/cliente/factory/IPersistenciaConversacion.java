package org.example.cliente.factory;

import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.config.ConfiguracionAplicacion;

import java.util.List;

public interface IPersistenciaConversacion {
    void guardarConversacion(Conversacion conversacion) throws Exception;
    Conversacion cargarConversacion(String conversacionId) throws Exception;
    List<String> listarIdsConversaciones() throws Exception;

    void guardarConfiguracion(ConfiguracionAplicacion config) throws Exception;
    ConfiguracionAplicacion cargarConfiguracion() throws Exception;
}