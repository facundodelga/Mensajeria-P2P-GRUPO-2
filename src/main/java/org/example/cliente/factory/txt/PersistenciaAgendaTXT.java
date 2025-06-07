package org.example.cliente.factory.txt;

import org.example.cliente.factory.IPersistenciaAgenda;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaAgendaTXT implements IPersistenciaAgenda {

    private static final String BASE_DIR = "agendas/";
    private final Contacto usuarioDTO;

    public PersistenciaAgendaTXT(Contacto usuarioDTO) {
        this.usuarioDTO = usuarioDTO;
    }

    public void guardarAgenda(List<Contacto> contactos) {
        File carpeta = new File(BASE_DIR);
        if (!carpeta.exists()) carpeta.mkdirs();

        File archivo = new File(carpeta, usuarioDTO.getNombre() + "_agenda.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (Contacto c : contactos) {
                writer.write(c.getNombre() + "|" + c.getIp() + "|" + c.getPuerto());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar la agenda: " + e.getMessage());
        }
    }

    public List<Contacto> cargarAgenda() {
        List<Contacto> contactos = new ArrayList<>();
        File archivo = new File(BASE_DIR + usuarioDTO.getNombre() + "_agenda.txt");

        if (!archivo.exists()) return contactos;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length != 3) continue;

                String nombre = partes[0];
                String ip = partes[1];
                int puerto = Integer.parseInt(partes[2]);

                contactos.add(new Contacto(nombre, ip, puerto));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar la agenda: " + e.getMessage());
        }

        return contactos;
    }
}
