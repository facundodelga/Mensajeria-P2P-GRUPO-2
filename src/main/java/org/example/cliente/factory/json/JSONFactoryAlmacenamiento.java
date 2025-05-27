package org.example.cliente.factory.json; // Tu paquete existente

import org.example.cliente.factory.*;

public class JSONFactoryAlmacenamiento implements AbstractFactory {

    public IPersistenciaConversacion createConversacionPersistencia() {
        return new ServicioAlmacenamientoJSON();
    }

    public IPersistenciaContactos createRepositorio() {
        // Implementa o devuelve null/una excepción si no es relevante para este TP
        // Por ahora, asumimos que no se requiere persistencia de Contactos JSON en este TP
        // o que tienes tu propia implementación.
        throw new UnsupportedOperationException("Persistencia de contactos en JSON no implementada para este TP.");
    }

    @Override
    public ILogRepositorio createRepositorioService() {
        return null;
    }
}