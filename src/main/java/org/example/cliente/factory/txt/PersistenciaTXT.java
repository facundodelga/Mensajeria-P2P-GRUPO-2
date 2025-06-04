package org.example.cliente.factory.txt;

import org.example.cliente.factory.IPersistencia;
import org.example.cliente.modelo.ContactoRepetidoException;
import org.example.cliente.modelo.IAgenda;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PersistenciaTXT implements IPersistencia {

    private static final String BASE_DIR = "conversaciones/";
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Contacto usuarioDTO;

    public PersistenciaTXT(Contacto usuarioDTO) {
        this.usuarioDTO = usuarioDTO;
    }

    @Override
    public void guardarConversaciones(Map<Contacto, Conversacion> conversaciones) {
        File carpeta = new File(BASE_DIR);

        if (!carpeta.exists()) {
            if (carpeta.mkdirs()) {
                System.out.println("Directorio creado correctamente.");
            } else {
                System.err.println("No se pudo crear el directorio.");
            }
        }

        File archivo = new File(carpeta, usuarioDTO.getNombre() + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (Map.Entry<Contacto, Conversacion> entry : conversaciones.entrySet()) {
                Contacto contacto = entry.getKey();
                Conversacion conv = entry.getValue();

                writer.write("#Contacto:" + contacto.getNombre() + "|" + contacto.getIp() + "|" + contacto.getPuerto());
                writer.newLine();

                for (Mensaje mensaje : conv.getMensajes()) {
                    boolean enviado = mensaje.getEmisor().getNombre().equalsIgnoreCase(usuarioDTO.getNombre());
                    String direccion = enviado ? "E" : "R";
                    String linea = String.format("%s|%s|%s|%s",
                            direccion,
                            FORMATO_FECHA.format(mensaje.getFecha()),
                            enviado ? mensaje.getReceptor().getNombre() : mensaje.getEmisor().getNombre(),
                            mensaje.getContenido()
                    );
                    writer.write(linea);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Guardando conversaciones de " + usuarioDTO.getNombre() + "...");
    }

    @Override
    public Map<Contacto, Conversacion> cargarConversaciones(IAgenda agendaServicio) {
        Map<Contacto, Conversacion> conversaciones = new HashMap<>();
        File archivo = new File(BASE_DIR + usuarioDTO.getNombre() + ".txt");

        if (!archivo.exists()) return conversaciones;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            Contacto contactoActual = null;
            List<Mensaje> mensajes = new ArrayList<>();

            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("#Contacto:")) {
                    // Guarda la conversación anterior si había una activa
                    if (contactoActual != null) {
                        conversaciones.put(contactoActual, new Conversacion(new ArrayList<>(mensajes)));
                        mensajes.clear();
                    }
                    String[] datosContacto = linea.substring("#Contacto:".length()).split("\\|");
                    String nombreContacto = datosContacto[0];
                    String ip = datosContacto.length > 1 ? datosContacto[1] : "127.0.0.1";
                    int puerto = datosContacto.length > 2 ? Integer.parseInt(datosContacto[2]) : -1;

                    Contacto buscado = agendaServicio.buscaNombreContacto(nombreContacto);
                    if (buscado != null) {
                        contactoActual = buscado;
                    } else {
                        contactoActual = new Contacto(nombreContacto, ip, puerto);
                        agendaServicio.addContacto(contactoActual);
                    }

                } else {
                    String[] partes = linea.split("\\|", 4);
                    if (partes.length < 4 || contactoActual == null) continue;

                    String tipo = partes[0];
                    Date fecha = FORMATO_FECHA.parse(partes[1]);
                    String otroNombre = partes[2];
                    String contenido = partes[3];

                    Contacto emisor, receptor;
                    if (tipo.equals("E")) {
                        emisor = usuarioDTO;
                        receptor = contactoActual;
                    } else {
                        emisor = contactoActual;
                        receptor = usuarioDTO;
                    }

                    mensajes.add(new Mensaje(fecha, contenido, emisor, receptor));
                }
            }

            // Agrega la última conversación leída
            if (contactoActual != null && !mensajes.isEmpty()) {
                conversaciones.put(contactoActual, new Conversacion(mensajes));
            }

        } catch (IOException | ParseException | ContactoRepetidoException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Cargando conversaciones de " + usuarioDTO.getNombre() + "...");
        return conversaciones;
    }
}
