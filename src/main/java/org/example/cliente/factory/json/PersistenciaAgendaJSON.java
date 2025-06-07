package org.example.cliente.factory.json;

import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.factory.IPersistenciaAgenda;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaAgendaJSON implements IPersistenciaAgenda {

    private static final String BASE_DIR = "agendas/";
    private final Contacto usuarioDTO;

    public PersistenciaAgendaJSON(Contacto usuarioDTO) {
        this.usuarioDTO = usuarioDTO;
    }

    @Override
    public void guardarAgenda(List<Contacto> contactos) {
        File carpeta = new File(BASE_DIR);
        if (!carpeta.exists()) carpeta.mkdirs();

        File archivo = new File(carpeta, usuarioDTO.getNombre() + "_agenda.json");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write("{\n  \"contactos\": [\n");

            for (int i = 0; i < contactos.size(); i++) {
                Contacto c = contactos.get(i);
                writer.write("    {\n");
                writer.write("      \"nombre\": \"" + escapeJSON(c.getNombre()) + "\",\n");
                writer.write("      \"ip\": \"" + escapeJSON(c.getIp()) + "\",\n");
                writer.write("      \"puerto\": " + c.getPuerto() + "\n");
                writer.write("    }" + (i < contactos.size() -1 ? "," : "") + "\n");
            }

            writer.write("  ]\n}");
        } catch (IOException e) {
            System.err.println("Error al guardar la agenda JSON: " + e.getMessage());
        }
    }

    @Override
    public List<Contacto> cargarAgenda() {
        List<Contacto> contactos = new ArrayList<>();
        File archivo = new File(BASE_DIR + usuarioDTO.getNombre() + "_agenda.json");

        if (!archivo.exists()) return contactos;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonBuilder.append(linea.trim());
            }

            String json = jsonBuilder.toString();

            // Buscar el array contactos
            int idxContactos = json.indexOf("\"contactos\"");
            if (idxContactos == -1) return contactos;

            int startArray = json.indexOf("[", idxContactos);
            int endArray = json.indexOf("]", startArray);
            if (startArray == -1 || endArray == -1) return contactos;

            String arrayContactos = json.substring(startArray +1, endArray);

            // Dividir por objetos (sencillo, asumiendo formato correcto)
            String[] objetos = arrayContactos.split("\\},\\s*\\{");

            for (String obj : objetos) {
                obj = obj.trim();
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                String nombre = extraerCampo(obj, "nombre");
                String ip = extraerCampo(obj, "ip");
                String puertoStr = extraerCampo(obj, "puerto");
                int puerto = 0;
                try {
                    puerto = Integer.parseInt(puertoStr);
                } catch (NumberFormatException e) {
                    // Ignorar contacto si puerto no válido
                    continue;
                }
                contactos.add(new Contacto(nombre, ip, puerto));
            }

        } catch (IOException e) {
            System.err.println("Error al cargar la agenda JSON: " + e.getMessage());
        }

        return contactos;
    }

    // Extrae el valor simple de un campo "clave": "valor" o "clave": valor
    private String extraerCampo(String jsonObj, String clave) {
        int idx = jsonObj.indexOf("\"" + clave + "\"");
        if (idx == -1) return "";

        int start = jsonObj.indexOf(":", idx);
        if (start == -1) return "";

        String resto = jsonObj.substring(start +1).trim();

        if (resto.startsWith("\"")) {
            // Valor entre comillas
            int end = resto.indexOf("\"", 1);
            if (end == -1) return "";
            return resto.substring(1, end);
        } else {
            // Valor sin comillas (número)
            int end = resto.indexOf(",");
            if (end == -1) end = resto.indexOf("}");
            if (end == -1) end = resto.length();
            return resto.substring(0, end).trim();
        }
    }

    // Escapa caracteres básicos JSON
    private String escapeJSON(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
