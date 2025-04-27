package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Clase que maneja el registro de usuarios en el servidor.
 * Implementa la interfaz Runnable para permitir la ejecución en un hilo separado.
 */
public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private IDirectorio directorio;
    private IColaMensajes colaMensajes;
    private boolean corriendo = false;
    private Contacto usuario;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private Map<Contacto, ManejadorRegistro> manejadores; // Opcional: Si decides gestionar manejadores fuera del Directorio

    public ManejadorRegistro(Socket socket, IDirectorio directorio, IColaMensajes colaMensajes, Map<Contacto, ManejadorRegistro> manejadores) {
        this.socket = socket;
        this.directorio = directorio;
        this.colaMensajes = colaMensajes;
        this.manejadores = manejadores; // Opcional
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            Contacto usuarioDTO = (Contacto) entrada.readObject();
            if (directorio.getUsuarios().containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            this.usuario = usuarioDTO;
            directorio.addUsuario(usuario.getNombre(), usuario);
            directorio.addSocket(usuario, socket);
            if (manejadores != null) {
                manejadores.put(usuario, this); // Opcional
            }
            salida.writeObject("Registro exitoso.");
            salida.flush();

            enviarMensajesPendientes();

            this.corriendo = true;
            while (corriendo) {
                Object msg = entrada.readObject();
                if (msg == null) {
                    System.out.println("El cliente " + usuario.getNombre() + " se ha desconectado.");
                    this.corriendo = false;
                    break;
                }

                System.out.println("Objeto recibido de " + usuario.getNombre() + " de tipo: " + msg.getClass().getName());
                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println(usuario.getNombre() + ": Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                    enviarMensaje(mensaje);
                } else if (msg instanceof Contacto) {
                    Contacto contacto = (Contacto) msg;
                    System.out.println(usuario.getNombre() + ": Contacto recibido: " + contacto.getNombre());
                    if (contacto.getNombre().equals("Contactos")) {
                        enviarContactos();
                    } else {
                        System.out.println(usuario.getNombre() + ": Solicitud de contacto no implementada.");
                        salida.writeObject("Solicitud de contacto no implementada.");
                        salida.flush();
                    }
                } else {
                    System.out.println(usuario.getNombre() + ": Objeto desconocido recibido: " + msg);
                }
            }
        } catch (SocketException e) {
            System.out.println("El cliente " + usuario.getNombre() + " se ha desconectado.");
            directorio.getSockets().remove(usuario);
            directorio.getUsuarios().remove(usuario.getNombre());
            if (manejadores != null) {
                manejadores.remove(usuario); // Opcional
            }
            this.corriendo = false;
        } catch (EOFException e) {
            System.out.println("El cliente " + usuario.getNombre() + " se ha desconectado.");
            directorio.getSockets().remove(usuario);
            directorio.getUsuarios().remove(usuario.getNombre());
            if (manejadores != null) {
                manejadores.remove(usuario); // Opcional
            }
            this.corriendo = false;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarMensajeACliente(Mensaje mensaje) throws IOException {
        System.out.println("Enviando mensaje a " + usuario.getNombre() + ": " + mensaje.getContenido());
        salida.writeObject(mensaje);
        salida.flush();
    }

    private void enviarMensajesPendientes() throws IOException, InterruptedException {
        ArrayList<Mensaje> mensajesPendientes = colaMensajes.getMensajesRecibidos();
        for (Mensaje mensaje : new ArrayList<>(mensajesPendientes)) { // Iterar sobre una copia para permitir la eliminación
            if (mensaje.getReceptor().equals(usuario)) {
                System.out.println("Enviando mensaje pendiente a " + usuario.getNombre() + ": " + mensaje.getContenido());
                salida.writeObject(mensaje);
                salida.flush();
                colaMensajes.getMensajesRecibidos().remove(mensaje);
                Thread.sleep(50);
            }
        }
    }

    private void enviarContactos() throws IOException {
        ArrayList<Contacto> contactosList = new ArrayList<>(directorio.getUsuarios().values());
        DirectorioDTO contactos = new DirectorioDTO(contactosList);
        System.out.println("Enviando lista de contactos a " + usuario.getNombre() + ": " + contactos);
        salida.writeObject(contactos);
        salida.flush();
    }

    private void enviarMensaje(Mensaje mensaje) {
        ManejadorRegistro manejadorDestino = null;
        if (manejadores != null) {
            manejadorDestino = manejadores.get(mensaje.getReceptor());
        } else {
            // Si los manejadores se gestionan en el Directorio, necesitarías un método para obtenerlo
            // manejadorDestino = directorio.getManejador(mensaje.getReceptor()); // Ejemplo
        }

        if (manejadorDestino != null) {
            try {
                manejadorDestino.enviarMensajeACliente(mensaje);
            } catch (IOException e) {
                System.out.println("No se pudo enviar el mensaje a " + mensaje.getReceptor() + ". Se almacenará.");
                colaMensajes.getMensajesRecibidos().add(mensaje);
            }
        } else {
            colaMensajes.getMensajesRecibidos().add(mensaje);
            System.out.println("El receptor " + mensaje.getReceptor().getNombre() + " no está conectado. El mensaje se almacenará.");
        }
    }
}