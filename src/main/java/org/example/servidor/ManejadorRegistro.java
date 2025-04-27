package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Clase que maneja el registro de usuarios en el servidor.
 * Implementa la interfaz Runnable para permitir la ejecución en un hilo separado.
 */

public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private Servidor servidorDirectorio;
    private boolean corriendo = false;
    private Contacto usuario;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ManejadorRegistro(Socket socket, Servidor servidorDirectorio) {
        this.socket = socket;
        this.servidorDirectorio = servidorDirectorio;
    }

    @Override
    public void run() {
        try {
            // Crear flujos una sola vez
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // Registro del usuario
            Contacto usuarioDTO = (Contacto) entrada.readObject();
            if (servidorDirectorio.getUsuarios().containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            this.usuario = usuarioDTO;
            servidorDirectorio.addUsuario(usuario.getNombre(), usuario);
            servidorDirectorio.addSocket(usuario, socket);
            servidorDirectorio.addManejador(usuario, this);
            salida.writeObject("Registro exitoso.");
            salida.flush();

            // Enviar mensajes pendientes
            enviarMensajesPendientes();

            this.corriendo = true;
            while (corriendo) {
                Object msg = entrada.readObject();
                if (msg == null) {
                    System.out.println("El cliente se ha desconectado.");
                    this.corriendo = false;
                    break;
                }

                System.out.println("Objeto recibido de tipo: " + msg.getClass().getName());
                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Soy " + usuario.getNombre() + ": Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                    enviarMensaje(mensaje);
                } else if (msg instanceof Contacto) {
                    Contacto contacto = (Contacto) msg;
                    System.out.println("Soy " + usuario.getNombre() + ": Contacto recibido: " + contacto.getNombre());
                    if (contacto.getNombre().equals("Contactos")) {
                        enviarContactos();
                    } else {
                        System.out.println("Soy " + usuario.getNombre() + ": Contacto no encontrado");
                        salida.writeObject("Contacto no encontrado");
                        salida.flush();
                    }
//                } else if (msg instanceof Contacto) {
//                    Contacto contacto = (Contacto) msg;
//                    System.out.println("Contacto recibido: " + contacto.getNombre());
//                    Contacto contactoEncontrado = servidorDirectorio.getUsuarios().get(contacto.getNombre());
//                    if (contactoEncontrado != null) {
//                        System.out.println("Contacto encontrado: " + contactoEncontrado.getNombre());
//                        salida.writeObject(contactoEncontrado);
//                        salida.flush();
//                    } else {
//                        System.out.println("Contacto no encontrado");
//                        salida.writeObject("Contacto no encontrado");
//                        salida.flush();
//                    }
                } else {
                    System.out.println("Objeto desconocido recibido: " + msg);
                }
            }
        } catch (SocketException e) {
            System.out.println("El cliente se ha desconectado.");
            //se elimina del mapa de sockets
            servidorDirectorio.getSockets().remove(usuario);
            servidorDirectorio.getUsuarios().remove(usuario.getNombre());
            this.corriendo = false;
        } catch (EOFException e) {
            System.out.println("El cliente se ha desconectado.");
            servidorDirectorio.getSockets().remove(usuario);
            servidorDirectorio.getUsuarios().remove(usuario.getNombre());
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
        for (Mensaje mensaje : servidorDirectorio.getMensajesRecibidos()) {
            if (mensaje.getReceptor().equals(usuario)) {
                System.out.println("Enviando mensaje pendiente a " + usuario.getNombre() + ": " + mensaje.getContenido());
                salida.writeObject(mensaje);
                salida.flush();
                Thread.sleep(50);
            }
        }
        servidorDirectorio.getMensajesRecibidos().removeIf(mensaje -> mensaje.getReceptor().equals(usuario));
    }

    private void enviarContactos() throws IOException {
        ArrayList<Contacto> contactosList = new ArrayList<>(servidorDirectorio.getUsuarios().values());
        DirectorioDTO contactos = new DirectorioDTO(contactosList);
        System.out.println("Enviando lista de contactos: " + contactos);
        salida.writeObject(contactos);
        salida.flush();
    }

    private void enviarMensaje(Mensaje mensaje) {
        ManejadorRegistro manejadorDestino = servidorDirectorio.getManejadores().get(mensaje.getReceptor());
        if (manejadorDestino != null) {
            try {
                manejadorDestino.enviarMensajeACliente(mensaje);
            } catch (IOException e) {
                System.out.println("No se pudo enviar el mensaje a " + mensaje.getReceptor());
                servidorDirectorio.getMensajesRecibidos().add(mensaje);
            }
        } else {
            servidorDirectorio.getMensajesRecibidos().add(mensaje);
            System.out.println("El receptor no está conectado. El mensaje se almacenará.");
        }
    }
}