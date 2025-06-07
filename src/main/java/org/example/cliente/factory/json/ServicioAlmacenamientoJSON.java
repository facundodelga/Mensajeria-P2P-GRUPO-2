package org.example.cliente.factory.json; // Nuevo paquete

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.config.ConfiguracionAplicacion;
import org.example.cliente.factory.IPersistenciaConversacion;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto; // Si Contacto es un objeto complejo

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

// Necesitarás añadir la dependencia Gson a tu pom.xml si usas Maven:
/*
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
*/

public class ServicioAlmacenamientoJSON implements IPersistenciaConversacion {
    private final String RUTA_BASE;
    private final String CONFIG_FILE_NAME = "config.json";

    public ServicioAlmacenamientoJSON() {
        this.RUTA_BASE = ConfiguracionAplicacion.getInstance().getRutaAlmacenamientoBase();
        new File(RUTA_BASE).mkdirs(); // Asegurarse de que el directorio exista
    }

    private String getRutaArchivoConversacion(String conversacionId) {
        return RUTA_BASE + "conversacion_" + conversacionId + ".json";
    }

    private String getRutaArchivoConfiguracion() {
        return RUTA_BASE + CONFIG_FILE_NAME;
    }

    @Override
    public void guardarConversacion(Conversacion conversacion) throws Exception {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try (FileWriter writer = new FileWriter(getRutaArchivoConversacion(conversacion.getId()))) {
            gson.toJson(conversacion, writer);
        }
    }

    @Override
    public Conversacion cargarConversacion(String conversacionId) throws Exception {
        Gson gson = new Gson();
        File file = new File(getRutaArchivoConversacion(conversacionId));
        if (!file.exists()) {
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            Conversacion conversacion = gson.fromJson(reader, Conversacion.class);
            // NOTA: La reconstrucción de SecretKey y la inyección del cifrador
            // se hará en SistemaMensajeria después de la carga.
            return conversacion;
        }
    }

    @Override
    public List<String> listarIdsConversaciones() throws Exception {
        List<String> ids = new ArrayList<>();
        File folder = new File(RUTA_BASE);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().startsWith("conversacion_") && file.getName().endsWith(".json")) {
                    ids.add(file.getName().replace("conversacion_", "").replace(".json", ""));
                }
            }
        }
        return ids;
    }

    @Override
    public void guardarConfiguracion(ConfiguracionAplicacion config) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(getRutaArchivoConfiguracion())) {
            gson.toJson(config, writer);
        }
    }

    @Override
    public ConfiguracionAplicacion cargarConfiguracion() throws Exception {
        Gson gson = new Gson();
        File file = new File(getRutaArchivoConfiguracion());
        if (!file.exists()) {
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, ConfiguracionAplicacion.class);
        }
    }
}