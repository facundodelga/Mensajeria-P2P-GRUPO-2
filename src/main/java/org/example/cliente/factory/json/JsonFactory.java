package org.example.cliente.factory.json;


import org.example.cliente.factory.AbstractFactory;

public class JsonFactory implements AbstractFactory {

    @Override
    public ILogLlamados createLlamados() {
        return new JsonLlamados();
    }

    @Override
    public ILogRepositorio createRepositorio() {
        return new JsonRepositorio();
    }
}
