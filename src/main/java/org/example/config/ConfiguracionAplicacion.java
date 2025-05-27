package org.example.config; // Nuevo paquete para configuración

import java.io.File;
import java.io.Serializable;

public class ConfiguracionAplicacion implements Serializable {
    private static final long serialVersionUID = 1L; // Para serialización

    private static ConfiguracionAplicacion instance;
    private FormatoAlmacenamiento formatoAlmacenamientoSeleccionado;
    private String rutaAlmacenamientoBase = "data/"; // Directorio por defecto para almacenar

    private ConfiguracionAplicacion() {
        // Valores por defecto
        this.formatoAlmacenamientoSeleccionado = FormatoAlmacenamiento.JSON; // JSON por defecto
        // Asegúrate de que el directorio base exista al crear la instancia
        new File(rutaAlmacenamientoBase).mkdirs();
    }

    public static synchronized ConfiguracionAplicacion getInstance() {
        if (instance == null) {
            instance = new ConfiguracionAplicacion();
        }
        return instance;
    }

    public FormatoAlmacenamiento getFormatoAlmacenamientoSeleccionado() {
        return formatoAlmacenamientoSeleccionado;
    }

    public void setFormatoAlmacenamientoSeleccionado(FormatoAlmacenamiento formatoAlmacenamientoSeleccionado) {
        this.formatoAlmacenamientoSeleccionado = formatoAlmacenamientoSeleccionado;
    }

    public String getRutaAlmacenamientoBase() {
        return rutaAlmacenamientoBase;
    }

    public void setRutaAlmacenamientoBase(String rutaAlmacenamientoBase) {
        this.rutaAlmacenamientoBase = rutaAlmacenamientoBase;
        new File(rutaAlmacenamientoBase).mkdirs(); // Asegurarse de que el nuevo directorio exista
    }

    // Método para establecer la instancia cargada (usado al cargar de persistencia)
    public static void setLoadedInstance(ConfiguracionAplicacion loadedInstance) {
        ConfiguracionAplicacion.instance = loadedInstance;
        // Asegúrate de que la ruta base de la instancia cargada también exista
        new File(loadedInstance.getRutaAlmacenamientoBase()).mkdirs();
    }
}