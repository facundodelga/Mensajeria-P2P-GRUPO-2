package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;

import static java.lang.Thread.sleep;

/**
 * Clase que maneja el registro de usuarios en el servidor.
 * Implementa la interfaz Runnable para permitir la ejecución en un hilo separado.
 */

public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private ServidorDirectorio servidorDirectorio;
    private boolean corriendo = false;
    private Contacto usuario; // Almacena el usuario registrado con este hilo

    /**
     * Constructor para ManejadorRegistro.
     * Inicializa el socket y el mapa de usuarios para manejar el registro de usuarios.
     *
     * @param socket   El socket para la comunicación con el cliente.
     * @param servidorDirectorio El servidor de directorio que gestiona los usuarios registrados.
     */
    public ManejadorRegistro(Socket socket, ServidorDirectorio servidorDirectorio) {
        this.socket = socket;
        this.servidorDirectorio = servidorDirectorio;
        try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())) {

            Contacto usuarioDTO = (Contacto) entrada.readObject();


            // Verificar si el nickname ya está en uso
            if (this.servidorDirectorio.getUsuarios().containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
            } else {
                // Registrar el nuevo usuario en el mapa
                this.servidorDirectorio.getUsuarios().put(usuarioDTO.getNombre(), usuarioDTO);
                // Guardar el socket del usuario en el mapa
                this.servidorDirectorio.getSockets().put(usuarioDTO, socket);

                this.usuario = usuarioDTO;
                salida.writeObject("Registro exitoso.");
            }
            salida.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Ejecuta el proceso de registro en un hilo separado.
     * Lee un objeto UsuarioDTO del flujo de entrada, verifica si el nickname ya está en uso,
     * y envía una respuesta al cliente indicando el éxito o fracaso del registro.
     */
    @Override
    public void run() {

        try {
            while(corriendo) {
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                Object msg = entrada.readObject();
                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                    Socket socketDestino = servidorDirectorio.getSockets().get(mensaje.getReceptor());

                    try{ //intento enviar el mensaje
                        ObjectOutputStream salida = new ObjectOutputStream(socketDestino.getOutputStream());
                        salida.writeObject(mensaje);
                        salida.flush();

                    }catch (IOException e){
                        System.out.println("No se pudo enviar el mensaje a " + mensaje.getReceptor());
                        this.servidorDirectorio.getMensajesRecibidos().add(mensaje);
                    }
                } else {
                    System.out.println("No es un mensaje");
                }
                if (msg instanceof String) {
                    String mensajeOperacion = (String) msg;
                    if(mensajeOperacion.equals("MensajesPendientes")) {
                        System.out.println("Enviando mensajes pendientes...");
                        // Enviar mensajes pendientes al cliente
                        ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                        for (Mensaje mensaje : this.servidorDirectorio.getMensajesRecibidos()) {
                            if(mensaje.getReceptor().equals(usuario)) {
                                salida.writeObject(mensaje);

                            }

                            salida.flush();
                            sleep(50); // Espera un poco para evitar congestión
                        }
                        // Limpiar la lista de mensajes pendientes
                        this.servidorDirectorio.getMensajesRecibidos().removeIf(mensaje -> mensaje.getReceptor().equals(usuario));

                    }

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
}