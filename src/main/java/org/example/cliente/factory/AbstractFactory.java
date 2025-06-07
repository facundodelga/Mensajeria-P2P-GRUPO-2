package org.example.cliente.factory;

import org.example.config.FormatoAlmacenamiento; // Nuevo import
import org.example.cliente.factory.json.JSONFactoryAlmacenamiento; // Nuevos imports
import org.example.cliente.factory.txt.TextoPlanoFactoryAlmacenamiento;
import org.example.cliente.factory.xml.XMLFactoryAlmacenamiento;


// Modificada para reflejar el uso de IPersistenciaConversacion
public interface AbstractFactory {
    public abstract IPersistenciaConversacion createConversacionService();
    public abstract ILogRepositorio createRepositorioService();

    // Método estático para obtener la fábrica concreta (convenience method)
    static AbstractFactory getFactory(FormatoAlmacenamiento formato) {
        switch (formato) {
            case XML:
                return new XMLFactoryAlmacenamiento();
            case JSON:
                return new JSONFactoryAlmacenamiento();
            case TEXTO_PLANO:
                return new TextoPlanoFactoryAlmacenamiento();
            default:
                throw new IllegalArgumentException("Formato de almacenamiento no soportado: " + formato);
        }
    }
}