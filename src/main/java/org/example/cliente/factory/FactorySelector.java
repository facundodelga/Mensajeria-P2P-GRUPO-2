package org.example.cliente.factory;


import org.example.cliente.factory.json.PersistenciaJSONFactory;
import org.example.cliente.factory.txt.PersistenciaTXTFactory;
import org.example.cliente.factory.xml.PersistenciaXMLFactory;
import org.example.cliente.modelo.usuario.Contacto;

public class FactorySelector {
    private IPersistenciaFactory persistenciaFactory;

    public FactorySelector(String formato) {
        this.persistenciaFactory = crearFactory(formato);
    }

    public IPersistenciaFactory crearFactory(String formato) {
        switch (formato.toLowerCase()) {
            case "txt":
                return new PersistenciaTXTFactory();
            case "json":
                return new PersistenciaJSONFactory();
            case "xml":
                return new PersistenciaXMLFactory();
            default:
                throw new IllegalArgumentException("Formato de persistencia no soportado: " + formato);
        }
    }
}
