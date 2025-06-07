package org.example.cliente.factory.xml;

import org.grupo10.factory.AbstractFactory;
import org.grupo10.factory.ILogLlamados;
import org.grupo10.factory.ILogRegistro;
import org.grupo10.factory.ILogRepositorio;

public class XmlFactory implements AbstractFactory {
    @Override
    public ILogLlamados createLlamados() {
        return new XmlLlamados();
    }

    @Override
    public ILogRepositorio createRepositorio() {
        return new XmlRepositorio();
    }
}
