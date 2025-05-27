package org.example.cliente.factory.xml; // Tu paquete existente

import org.example.cliente.factory.AbstractFactory;
import org.example.cliente.factory.IPersistenciaConversacion;

public class XMLFactoryAlmacenamiento implements AbstractFactory {
    @Override
    public IPersistenciaConversacion createConversacionPersistencia() {
        return new ServicioAlmacenamientoXML();
    }

    @Override
    public IPersistenciaContactos createRepositorio() {
        throw new UnsupportedOperationException("Persistencia de contactos en XML no implementada para este TP.");
    }
}