package org.example.cliente.factory;

import org.example.config.FormatoAlmacenamiento; // Nuevo import


// Modificada para reflejar el uso de IPersistenciaConversacion
public interface AbstractFactory {
    IPersistenciaConversacion createConversacionPersistencia(); // Nombre de método más claro
    IPersistenciaContactos createRepositorio(); // Mantener tu método original para Contactos

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