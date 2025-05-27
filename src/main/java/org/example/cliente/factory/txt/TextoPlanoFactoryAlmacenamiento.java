package org.example.cliente.factory.txt;

import org.example.cliente.factory.AbstractFactory;
import org.example.cliente.factory.IPersistenciaConversacion;

public class TextoPlanoFactoryAlmacenamiento implements AbstractFactory {
    @Override
    public IPersistenciaConversacion createConversacionPersistencia() {
        return new ServicioAlmacenamientotxt();
    }

    @Override
    public IPersistenciaContactos createRepositorio() {
        throw new UnsupportedOperationException("Persistencia de contactos en Texto Plano no implementada para este TP.");
    }
}