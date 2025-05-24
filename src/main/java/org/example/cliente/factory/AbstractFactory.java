package org.example.cliente.factory;

public interface AbstractFactory {
    IPersistenciaConversacion createLlamados();
    IPersistenciaContactos createRepositorio();
}
