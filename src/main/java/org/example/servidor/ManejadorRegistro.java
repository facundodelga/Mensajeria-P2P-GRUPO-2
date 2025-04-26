package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
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
    private Contacto usuario; // Almacena el usuario registrado con este hilo

    /**
     * Constructor para ManejadorRegistro.
     * Inicializa el socket y el mapa de usuarios para manejar el registro de usuarios.
     *
     * @param socket   El socket para la comunicación con el cliente.
     * @param servidorDirectorio El servidor de directorio que gestiona los usuarios registrados.
     */
    public ManejadorRegistro(Socket socket, Servidor servidorDirectorio) {
        this.socket = socket;
        System.out.println(socket);
        this.servidorDirectorio = servidorDirectorio;
    }


    /**
     * Ejecuta el proceso de registro en un hilo separado.
     * Lee un objeto UsuarioDTO del flujo de entrada, verifica si el nickname ya está en uso,
     * y envía una respuesta al cliente indicando el éxito o fracaso del registro.
     */
    @Override
    public void run() {
        try {
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());

            Contacto usuarioDTO = (Contacto) entrada.readObject();

            if (this.servidorDirectorio.getUsuarios().containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            // Registro exitoso
            this.usuario = usuarioDTO;
            this.servidorDirectorio.addUsuario(this.usuario.getNombre(), this.usuario);
            this.servidorDirectorio.addSocket(this.usuario, socket);
            System.out.println("Usuario: " + this.usuario.getNombre() + " Socket: " + socket);
            salida.writeObject("Registro exitoso.");
            salida.flush();

            // Enviar mensajes pendientes
            enviarMensajesPendientes(salida);

            this.corriendo = true;
            while(corriendo) {
                Object msg = entrada.readObject();
                if (msg == null) {
                    System.out.println("El cliente se ha desconectado.");
                    this.corriendo = false;
                    break;
                }

                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Soy " + this.usuario.getNombre() + ": Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                    // Verifica si el receptor está conectado
                    Socket socketDestino = servidorDirectorio.getSockets().get(mensaje.getReceptor());
                    System.out.println("socketDestino: " + socketDestino);
                    if (socketDestino != null) {
                        // Si el receptor está conectado, enviar el mensaje
                        try {
                            salida = new ObjectOutputStream(socketDestino.getOutputStream());
                            salida.writeObject(mensaje);
                            salida.flush();
                        } catch (IOException e) {
                            System.out.println("No se pudo enviar el mensaje a " + mensaje.getReceptor());
                            // Si no se pudo enviar, almacenamos el mensaje
                            this.servidorDirectorio.getMensajesRecibidos().add(mensaje);
                        }
                    } else {
                        // Si el receptor está desconectado, se guarda el mensaje para su posterior entrega
                        this.servidorDirectorio.getMensajesRecibidos().add(mensaje);
                        System.out.println("El receptor no está conectado. El mensaje se almacenará para su posterior entrega.");
                    }
                } else if (msg instanceof String) {
                    String mensajeOperacion = (String) msg;
                    if (mensajeOperacion.equals("MensajesPendientes")) {
                        System.out.println("Enviando mensajes pendientes...");
                        // Enviar mensajes pendientes al cliente
                        salida = new ObjectOutputStream(socket.getOutputStream());
                        for (Mensaje mensaje : this.servidorDirectorio.getMensajesRecibidos()) {
                            if (mensaje.getReceptor().equals(usuario)) {
                                salida.writeObject(mensaje);
                            }
                            salida.flush();
                            sleep(50); // Espera un poco para evitar congestión
                        }
                        // Limpiar la lista de mensajes pendientes
                        this.servidorDirectorio.getMensajesRecibidos().removeIf(mensaje -> mensaje.getReceptor().equals(usuario));

                    } else if (mensajeOperacion.equals("Contactos")) {
                        System.out.println("Enviando contactos...");
                        // Enviar contactos al cliente
                        salida = new ObjectOutputStream(socket.getOutputStream());
                        ArrayList<Contacto> contactos = new ArrayList<>(this.servidorDirectorio.getUsuarios().values());
                        // Enviar todos los contactos registrados en el servidor
                        salida.writeObject(contactos);
                        salida.flush();
                    } else {
                        System.out.println("Comando no reconocido: " + mensajeOperacion);

                    }
                }else if (msg instanceof Contacto) { //busco un contacto
                    Contacto contacto = (Contacto) msg;
                    System.out.println("Contacto recibido: " + contacto.getNombre());
                    Contacto contactoEncontrado = this.servidorDirectorio.getUsuarios().get(contacto.getNombre());
                    if (contactoEncontrado != null) {
                        System.out.println("Contacto encontrado: " + contactoEncontrado.getNombre());

                        // Aquí puedes enviar el contacto encontrado al cliente
                        salida = new ObjectOutputStream(socket.getOutputStream());
                        salida.writeObject(contactoEncontrado);
                        salida.flush();

                    } else {
                        System.out.println("Contacto no encontrado");
                        salida = new ObjectOutputStream(socket.getOutputStream());
                        salida.writeObject("Contacto no encontrado");
                        salida.flush();
                    }
                    // Aquí puedes manejar el contacto recibido


                } else {
                    System.out.println("No es un mensaje");
                }
            }

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

    private void enviarMensajesPendientes(ObjectOutputStream salida) throws IOException {
        // Enviar mensajes pendientes al cliente al reconectarse
        for (Mensaje mensaje : this.servidorDirectorio.getMensajesRecibidos()) {
            if (mensaje.getReceptor().equals(usuario)) {
                salida.writeObject(mensaje);
            }
        }
        salida.flush();
    }
}