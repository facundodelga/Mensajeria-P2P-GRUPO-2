package org.example.cliente.factory.json;

import org.example.cliente.factory.IPersistenciaConversaciones;
import org.example.cliente.modelo.ContactoRepetidoException;
import org.example.cliente.modelo.IAgenda;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.util.Map;

import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;

public class PersistenciaConversacionesJSON implements IPersistenciaConversaciones {

    private static final String BASE_DIR = "conversaciones/";
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Contacto usuarioDTO;

    public PersistenciaConversacionesJSON(Contacto usuarioDTO) {
        this.usuarioDTO = usuarioDTO;
    }

    @Override
    public void guardarConversaciones(Map<Contacto, Conversacion> conversaciones) {
        File carpeta = new File(BASE_DIR);
        if (!carpeta.exists()) carpeta.mkdirs();

        File archivo = new File(carpeta, usuarioDTO.getNombre() + ".json");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write("{\n  \"contactos\": {\n");

            Iterator<Map.Entry<Contacto, Conversacion>> it = conversaciones.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Contacto, Conversacion> entry = it.next();
                Contacto contacto = entry.getKey();
                Conversacion conv = entry.getValue();

                writer.write("    \"" + contacto.getNombre() + "\": {\n");
                writer.write("      \"mensajes\": [\n");

                List<Mensaje> mensajes = conv.getMensajes();
                for (int i = 0; i < mensajes.size(); i++) {
                    Mensaje m = mensajes.get(i);
                    boolean enviado = m.getEmisor().getNombre().equalsIgnoreCase(usuarioDTO.getNombre());
                    String tipo = enviado ? "E" : "R";

                    writer.write("        {\n");
                    writer.write("          \"tipo\": \"" + tipo + "\",\n");
                    writer.write("          \"fecha\": \"" + FORMATO_FECHA.format(m.getFecha()) + "\",\n");
                    writer.write("          \"contenido\": \"" + escapeJSON(m.getContenido()) + "\"\n");
                    writer.write("        }" + (i < mensajes.size() - 1 ? "," : "") + "\n");
                }

                writer.write("      ]\n");
                writer.write("    }" + (it.hasNext() ? "," : "") + "\n");
            }

            writer.write("  }\n}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Contacto, Conversacion> cargarConversaciones(IAgenda agendaServicio) {
        Map<Contacto, Conversacion> conversaciones = new HashMap<>();
        File archivo = new File(BASE_DIR + usuarioDTO.getNombre() + ".json");
        if (!archivo.exists()) return conversaciones;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonBuilder.append(linea.trim());
            }

            String json = jsonBuilder.toString();
            if (!json.startsWith("{") || !json.contains("\"contactos\"")) return conversaciones;

            String contactosJSON = json.substring(json.indexOf("{", json.indexOf("contactos")) + 1, json.lastIndexOf("}"));
            String[] entradas = contactosJSON.split("},\\s*\""); // rudimentario separador

            for (String entrada : entradas) {
                entrada = entrada.trim();
                if (!entrada.endsWith("}")) entrada += "}";

                String nombreContacto = entrada.split("\":\\s*\\{")[0].replace("\"", "").trim();
                String datos = entrada.substring(entrada.indexOf("{") + 1, entrada.lastIndexOf("}"));

                Contacto contacto = agendaServicio.buscaNombreContacto(nombreContacto);
                if (contacto == null) {
                    continue;
                }

                List<Mensaje> mensajes = new ArrayList<>();
                if (datos.contains("\"mensajes\": [")) {
                    String mensajesRaw = datos.substring(datos.indexOf("[") + 1, datos.lastIndexOf("]"));
                    String[] bloques = mensajesRaw.split("\\},\\s*\\{");

                    for (String bloque : bloques) {
                        bloque = bloque.replace("{", "").replace("}", "").trim();
                        String tipo = extraerCampo(bloque, "tipo");
                        String fechaStr = extraerCampo(bloque, "fecha");
                        String contenido = extraerCampo(bloque, "contenido").replace("\\\"", "\"");

                        Date fecha = FORMATO_FECHA.parse(fechaStr);
                        boolean enviado = tipo.equals("E");

                        Contacto emisor = enviado ? usuarioDTO : contacto;
                        Contacto receptor = enviado ? contacto : usuarioDTO;

                        mensajes.add(new Mensaje(fecha, contenido, emisor, receptor));
                    }
                }

                conversaciones.put(contacto, new Conversacion(mensajes));
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return conversaciones;
    }

    // Extrae el valor de un campo tipo "clave": "valor"
    private String extraerCampo(String bloque, String clave) {
        int idx = bloque.indexOf("\"" + clave + "\"");
        if (idx == -1) return "";
        int start = bloque.indexOf(":", idx) + 1;
        String valor = bloque.substring(start).split(",|\\n")[0].trim();
        valor = valor.replaceAll("^\"|\"$", ""); // quita comillas
        return valor;
    }

    // Escapa caracteres JSON básicos
    private String escapeJSON(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
