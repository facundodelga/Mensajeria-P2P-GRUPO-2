package org.example.cliente.factory.txt;

import org.example.config.ConfiguracionAplicacion;
import org.example.config.FormatoAlmacenamiento;
import org.example.cliente.factory.IPersistenciaConversacion;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class ServicioAlmacenamientotxt implements IPersistenciaConversacion {
    private final String RUTA_BASE;
    private final String CONFIG_FILE_NAME = "config.txt"; // Cambiado a .txt
    private static final String DELIMITADOR_CONVERSACION = "###CONV_SEP###"; // Delimitador principal de campos
    private static final String DELIMITADOR_MENSAJE = "###MSG_SEP###"; // Delimitador de campos dentro de un mensaje
    private static final String DELIMITADOR_PARTICIPANTES = "###PART_SEP###"; // Delimitador para IDs de participantes

    public ServicioAlmacenamientotxt() {
        this.RUTA_BASE = ConfiguracionAplicacion.getInstance().getRutaAlmacenamientoBase();
        new File(RUTA_BASE).mkdirs();
    }

    private String getRutaArchivoConversacion(String conversacionId) {
        return RUTA_BASE + "conversacion_" + conversacionId + ".txt";
    }

    private String getRutaArchivoConfiguracion() {
        return RUTA_BASE + CONFIG_FILE_NAME;
    }

    @Override
    public void guardarConversacion(Conversacion conversacion) throws Exception {
        StringBuilder sb = new StringBuilder();

        // 1. Datos de la Conversación
        sb.append(conversacion.getId()).append(DELIMITADOR_CONVERSACION);
        sb.append(conversacion.getClaveSecretaCodificada()).append(DELIMITADOR_CONVERSACION);
        sb.append(conversacion.isPendiente()).append(DELIMITADOR_CONVERSACION);

        // 2. Participantes
        sb.append(String.join(DELIMITADOR_PARTICIPANTES, conversacion.getParticipantesIds())).append(DELIMITADOR_CONVERSACION);

        // 3. Mensajes
        List<String> mensajesSerializados = new ArrayList<>();
        for (Mensaje msg : conversacion.getMensajes()) {
            StringBuilder msgSb = new StringBuilder();
            msgSb.append(msg.getId()).append(DELIMITADOR_MENSAJE);
            msgSb.append(msg.getFecha().getTime()).append(DELIMITADOR_MENSAJE);
            msgSb.append(Base64.getEncoder().encodeToString(msg.getContenidoCifrado())).append(DELIMITADOR_MENSAJE);

            // Emisor (Contacto)
            msgSb.append(msg.getEmisor().getNombre()).append(DELIMITADOR_MENSAJE);
            msgSb.append(msg.getEmisor().getIp()).append(DELIMITADOR_MENSAJE);
            msgSb.append(msg.getEmisor().getPuerto()).append(DELIMITADOR_MENSAJE);

            // Receptor (Contacto)
            msgSb.append(msg.getReceptor().getNombre()).append(DELIMITADOR_MENSAJE);
            msgSb.append(msg.getReceptor().getIp()).append(DELIMITADOR_MENSAJE);
            msgSb.append(msg.getReceptor().getPuerto()); // Último campo, no necesita delimitador al final

            mensajesSerializados.add(msgSb.toString());
        }
        sb.append(String.join(DELIMITADOR_CONVERSACION, mensajesSerializados)); // Usar el delimitador principal para separar mensajes

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getRutaArchivoConversacion(conversacion.getId())))) {
            writer.write(sb.toString());
        }
    }

    @Override
    public Conversacion cargarConversacion(String conversacionId) throws Exception {
        File file = new File(getRutaArchivoConversacion(conversacionId));
        if (!file.exists()) {
            return null;
        }

        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                return null; // Archivo vacío
            }
        }

        // Dividir la línea principal en los componentes de la conversación
        String[] partesConversacion = line.split(DELIMITADOR_CONVERSACION, -1); // -1 para mantener trailing empty strings

        if (partesConversacion.length < 4) { // ID, Clave, Pendiente, Participantes, (y luego mensajes)
            throw new IOException("Formato de archivo TXT de conversación inválido: Faltan campos principales.");
        }

        String id = partesConversacion[0];
        String claveSecretaCodificada = partesConversacion[1];
        boolean pendiente = Boolean.parseBoolean(partesConversacion[2]);

        // Participantes
        List<String> participantesIds;
        if (partesConversacion[3].isEmpty()) {
            participantesIds = new ArrayList<>(); // Si no hay participantes, el split daría una cadena vacía
        } else {
            participantesIds = new ArrayList<>(Arrays.asList(partesConversacion[3].split(DELIMITADOR_PARTICIPANTES)));
        }


        Conversacion conversacion = new Conversacion(id, participantesIds, claveSecretaCodificada);
        conversacion.setPendiente(pendiente);

        // Mensajes (el resto de las partesConversacion son mensajes serializados)
        List<Mensaje> mensajes = new ArrayList<>();
        // Los mensajes comienzan desde el índice 4 de partesConversacion
        for (int i = 4; i < partesConversacion.length; i++) {
            String mensajeSerializado = partesConversacion[i];
            String[] partesMensaje = mensajeSerializado.split(DELIMITADOR_MENSAJE, -1);

            // Se espera que haya 9 partes para cada mensaje:
            // 0: msgId, 1: fecha, 2: contenidoCifrado,
            // 3: emisorNombre, 4: emisorIp, 5: emisorPuerto,
            // 6: receptorNombre, 7: receptorIp, 8: receptorPuerto
            if (partesMensaje.length < 9) {
                System.err.println("Advertencia: Formato de mensaje TXT inválido, saltando. Partes encontradas: " + partesMensaje.length + " en " + mensajeSerializado);
                continue; // Saltar mensajes malformados
            }

            String msgId = partesMensaje[0];
            long fechaLong = Long.parseLong(partesMensaje[1]);
            Date fecha = new Date(fechaLong);
            byte[] contenidoCifrado = Base64.getDecoder().decode(partesMensaje[2]);

            // Emisor (Contacto)
            String emisorNombre = partesMensaje[3];
            String emisorIp = partesMensaje[4];
            int emisorPuerto = Integer.parseInt(partesMensaje[5]);
            Contacto emisor = new Contacto(emisorNombre, emisorIp, emisorPuerto);

            // Receptor (Contacto)
            String receptorNombre = partesMensaje[6];
            String receptorIp = partesMensaje[7];
            int receptorPuerto = Integer.parseInt(partesMensaje[8]);
            Contacto receptor = new Contacto(receptorNombre, receptorIp, receptorPuerto);

            mensajes.add(new Mensaje(msgId, fecha, contenidoCifrado, emisor, receptor));
        }
        conversacion.setMensajes(mensajes);
        return conversacion;
    }

    @Override
    public List<String> listarIdsConversaciones() throws Exception {
        List<String> ids = new ArrayList<>();
        File folder = new File(RUTA_BASE);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().startsWith("conversacion_") && file.getName().endsWith(".txt")) {
                    ids.add(file.getName().replace("conversacion_", "").replace(".txt", ""));
                }
            }
        }
        return ids;
    }

    @Override
    public void guardarConfiguracion(ConfiguracionAplicacion config) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(config.getFormatoAlmacenamientoSeleccionado().name()).append("\n");
        sb.append(config.getRutaAlmacenamientoBase());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getRutaArchivoConfiguracion()))) {
            writer.write(sb.toString());
        }
    }

    @Override
    public ConfiguracionAplicacion cargarConfiguracion() throws Exception {
        File file = new File(getRutaArchivoConfiguracion());
        if (!file.exists()) {
            return null;
        }

        String formatoStr = null;
        String rutaBase = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            formatoStr = reader.readLine();
            rutaBase = reader.readLine();
        }

        ConfiguracionAplicacion loadedConfig = ConfiguracionAplicacion.getInstance();

        if (formatoStr != null && !formatoStr.trim().isEmpty()) {
            loadedConfig.setFormatoAlmacenamientoSeleccionado(FormatoAlmacenamiento.valueOf(formatoStr.trim()));
        }
        if (rutaBase != null && !rutaBase.trim().isEmpty()) {
            loadedConfig.setRutaAlmacenamientoBase(rutaBase.trim());
        }
        return loadedConfig;
    }
}