package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private Servidor servidorDirectorio;
    private boolean corriendo = false;
    private Contacto usuario;
    private ObjectOutputStream salida;

    public ManejadorRegistro(Socket socket, Servidor servidorDirectorio) {
        this.socket = socket;
        this.servidorDirectorio = servidorDirectorio;
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream()); // Crear aquí
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            Contacto usuarioDTO = (Contacto) entrada.readObject();

            if (this.servidorDirectorio.getUsuarios().containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            this.usuario = usuarioDTO;
            this.servidorDirectorio.getUsuarios().put(usuarioDTO.getNombre(), usuarioDTO);
            this.servidorDirectorio.getSockets().put(usuarioDTO, socket);
            salida.writeObject("Registro exitoso.");
            salida.flush();

            enviarMensajesPendientes();

            this.corriendo = true;
            while (corriendo) {
                Object msg = entrada.readObject();
                if (msg == null) {
                    System.out.println("El cliente se ha desconectado.");
                    this.corriendo = false;
                    break;
                }

                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                    Socket socketDestino = servidorDirectorio.getSockets().get(mensaje.getReceptor());
                    if (socketDestino != null) {
                        try {
                            ObjectOutputStream salidaDestino = new ObjectOutputStream(socketDestino.getOutputStream());
                            salidaDestino.writeObject(mensaje);
                            salidaDestino.flush();
                        } catch (IOException e) {
                            System.out.println("No se pudo enviar el mensaje a " + mensaje.getReceptor());
                            this.servidorDirectorio.getMensajesRecibidos().add(mensaje);
                        }
                    } else {
                        this.servidorDirectorio.getMensajesRecibidos().add(mensaje);
                        System.out.println("El receptor no está conectado. El mensaje se almacenará para su posterior entrega.");
                    }
                } else if (msg instanceof String) {
                    String mensajeOperacion = (String) msg;
                    if (mensajeOperacion.equals("MensajesPendientes")) {
                        System.out.println("Enviando mensajes pendientes...");
                        for (Mensaje mensaje : this.servidorDirectorio.getMensajesRecibidos()) {
                            if (mensaje.getReceptor().equals(usuario)) {
                                salida.writeObject(mensaje);
                            }
                        }
                        salida.flush();
                    } else if (mensajeOperacion.equals("Contactos")) {
                        System.out.println("Enviando contactos...");
                        ArrayList<Contacto> contactos = new ArrayList<>(this.servidorDirectorio.getUsuarios().values());
                        salida.writeObject(contactos);
                        salida.flush();
                    } else {
                        System.out.println("Comando no reconocido: " + mensajeOperacion);
                    }
                } else {
                    System.out.println("No es un mensaje");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace ();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarMensajesPendientes() throws IOException {
        for (Mensaje mensaje : this.servidorDirectorio.getMensajesRecibidos()) {
            if (mensaje.getReceptor().equals(usuario)) {
                salida.writeObject(mensaje);
            }
        }
        salida.flush();
    }
}