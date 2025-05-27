package org.example.cliente.factory.json;


import org.example.cliente.factory.AbstractFactory;
import org.example.cliente.factory.ILogRepositorio;
import org.example.cliente.factory.IPersistenciaConversacion;

public class JsonFactory implements AbstractFactory {

    public ILogRepositorio createRepositorio() {
        return new JsonRepositorio();
    }

    public IPersistenciaConversacion createConversacionService() {
        return null;
    }

    @Override
    public ILogLlamados createLlamadosService() {
        return null;
    }

    @Override
    public ILogRepositorio createRepositorioService() {
        return null;
    }
}
