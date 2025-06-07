package org.example.cliente.factory;


import org.example.cliente.factory.json.JsonFactory;
import org.example.cliente.factory.txt.TxtFactory;
import org.example.cliente.factory.xml.XmlFactory;

public class FactorySelector {
    private AbstractFactory logFactory;

    public FactorySelector(String logFactory) {
        this.logFactory = createLogFactory(logFactory);
    }

    private AbstractFactory createLogFactory(String logFormat) {
        switch (logFormat.toLowerCase()) {
            case "txt":
                return new TxtFactory();
            case "json":
                return new JsonFactory();
            case "xml":
                return new XmlFactory();
            default:
                throw new IllegalArgumentException("Formato de log no soportado: " + logFormat);
        }
    }

    public IPersistenciaConversacion logClientLlamado() {
         return logFactory.createLlamados();
    }
    public IPersistenciaContactos clientRepository(){
        return logFactory.createRepositorio();
    }
}