package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static java.lang.Thread.sleep; // Keep for now, but generally avoid in production for robustness

/**
 * Clase que maneja el registro de usuarios en el servidor.
 * Implementa la interfaz Runnable para permitir la ejecución en un hilo separado.
 */
public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private ServidorPrincipal servidorDirectorio; // Renamed to clarify its role (was just servidorDirectorio)
    private boolean corriendo = false;
    private Contacto usuario; // Represents the Contacto of the client handled by this thread
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ManejadorRegistro(Socket socket, ServidorPrincipal servidorDirectorio) {
        this.socket = socket;
        this.servidorDirectorio = servidorDirectorio;
    }

    @Override
    public void run() {
        try {
            // Create streams ONCE. Server creates OOS first, then OIS.
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // --- HANDSHAKE ---
            // Expect "CLIENTE_HANDSHAKE" string first from client
            Object initialMsg = entrada.readObject();
            if (!(initialMsg instanceof String) || !initialMsg.equals("CLIENTE_HANDSHAKE")) {
                System.err.println("ManejadorRegistro: Primer mensaje inesperado: " + initialMsg);
                salida.writeObject("ERROR: Protocolo de inicio de sesión inválido.");
                salida.flush();
                socket.close();
                return;
            }
            System.out.println("ManejadorRegistro: Handshake 'CLIENTE_HANDSHAKE' recibido.");


            // --- USER REGISTRATION ---
            // Then expect the Contacto (UsuarioDTO) object
            Contacto usuarioDTO = (Contacto) entrada.readObject();
            this.usuario = usuarioDTO; // Assign the received user as this handler's user

            boolean estaConectado = servidorDirectorio.getConectados().getUsuarios().containsKey(usuarioDTO.getNombre());
            boolean estaDirectorio = servidorDirectorio.getDirectorio().getUsuarios().containsKey(usuarioDTO.getNombre());

            if (estaConectado && estaDirectorio) {
                System.out.println("ManejadorRegistro: Nickname '" + usuarioDTO.getNombre() + "' ya está en uso.");
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            servidorDirectorio.getDirectorio().addUsuario(usuario.getNombre(), usuario); // Add to permanent directory
            servidorDirectorio.getConectados().addUsuario(usuario.getNombre(), usuario); // Add to currently connected users
            servidorDirectorio.addManejador(usuario.getNombre(), this); // Use String name as key for handlers map

            salida.writeObject("CONEXION_ESTABLECIDA"); // Send success message
            salida.flush();
            this.servidorDirectorio.setCambios(true); // Notify server of changes

            // Send pending messages right after successful registration
            enviarMensajesPendientes();

            this.corriendo = true;
            // --- MAIN COMMUNICATION LOOP ---
            while (corriendo) {
                Object msg = entrada.readObject(); // Read objects from client

                if (msg == null) { // Client disconnected gracefully (stream ended)
                    System.out.println("ManejadorRegistro para " + usuario.getNombre() + ": El cliente se ha desconectado.");
                    this.corriendo = false;
                    break;
                }

                System.out.println("ManejadorRegistro para " + usuario.getNombre() + ": Objeto recibido de tipo: " + msg.getClass().getName());

                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    // Server receives and logs the ENCRYPTED content. It doesn't decrypt.
                    System.out.println("Soy " + usuario.getNombre() + ": Mensaje recibido de " + mensaje.getEmisor() + " para " + mensaje.getReceptor() + ": " + mensaje.getContenidoCifrado());
                    enviarMensaje(mensaje); // Forward the message
                } else if (msg instanceof Contacto) { // Client requests contacts
                    Contacto contactoCommand = (Contacto) msg;
                    System.out.println("Soy " + usuario.getNombre() + ": Comando de contacto recibido: " + contactoCommand.getNombre());
                    if (contactoCommand.getNombre().equals("Contactos")) { // This is the specific command for contacts
                        enviarContactos();
                    } else {
                        // This else block seems to indicate a search for a specific contact
                        // If the client sends a Contacto object *other* than the "Contactos" command
                        // it implies a request to find that specific contact in the directory.
                        // Your client side `obtenerContactos()` sends `new Contacto("Contactos", "111", 0);`
                        // So this else branch might not be hit currently for that specific client function.
                        System.out.println("Soy " + usuario.getNombre() + ": Comando de contacto desconocido o contacto no encontrado en directorio.");
                        salida.writeObject("Comando de contacto no reconocido o contacto no encontrado.");
                        salida.flush();
                    }
                } else if (msg instanceof String) { // Handle potential string commands from client
                    String command = (String) msg;
                    if ("MensajesPendientes".equals(command)) {
                        System.out.println("ManejadorRegistro para " + usuario.getNombre() + ": Solicitud de mensajes pendientes.");
                        enviarMensajesPendientes();
                    } else {
                        System.out.println("ManejadorRegistro para " + usuario.getNombre() + ": Comando de cadena desconocido: " + command);
                    }
                }
                else {
                    System.out.println("ManejadorRegistro para " + usuario.getNombre() + ": Objeto desconocido recibido: " + msg.getClass().getName());
                }
            }
        } catch (SocketException e) {
            System.out.println("ManejadorRegistro para " + (usuario != null ? usuario.getNombre() : "desconocido") + ": El cliente se ha desconectado inesperadamente (SocketException): " + e.getMessage());
        } catch (EOFException e) {
            System.out.println("ManejadorRegistro para " + (usuario != null ? usuario.getNombre() : "desconocido") + ": El cliente ha cerrado la conexión (EOFException).");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ManejadorRegistro para " + (usuario != null ? usuario.getNombre() : "desconocido") + ": Error de E/S o clase no encontrada: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("ManejadorRegistro para " + (usuario != null ? usuario.getNombre() : "desconocido") + ": Hilo interrumpido: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        } finally {
            // Cleanup: remove user from connected list, update server status, close socket
            if (usuario != null) {
                System.out.println("ManejadorRegistro: Limpiando recursos para " + usuario.getNombre());
                servidorDirectorio.getConectados().getUsuarios().remove(usuario.getNombre());
                servidorDirectorio.removeManejador(usuario.getNombre());
                this.servidorDirectorio.setCambios(true);
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("ManejadorRegistro: Error al cerrar socket para " + (usuario != null ? usuario.getNombre() : "desconocido") + ": " + e.getMessage());
                e.printStackTrace();
            }
            this.corriendo = false; // Ensure running flag is false
        }
    }

    public void enviarMensajeACliente(Mensaje mensaje) throws IOException {
        // Server sends the message AS-IS (encrypted content)
        System.out.println("ManejadorRegistro: Enviando mensaje a " + usuario.getNombre() + " desde " + mensaje.getEmisor() + ": " + mensaje.getContenidoCifrado().substring(0, Math.min(mensaje.getContenidoCifrado().length(), 20)) + "...");
        salida.writeObject(mensaje);
        salida.flush();
    }

    private void enviarMensajesPendientes() throws IOException, InterruptedException {
        // Create a temporary list to avoid ConcurrentModificationException if removeIf is used on original
        // and to ensure we iterate only once on the messages that are pending.
        ArrayList<Mensaje> messagesToSend = new ArrayList<>();
        // Collect messages for THIS user
        for (Mensaje mensaje : servidorDirectorio.getColaMensajes().getMensajesRecibidos()) {
            // Compare receptor string with this handler's user's name
            if (mensaje.getReceptor().equals(this.usuario.getNombre())) {
                messagesToSend.add(mensaje);
            }
        }

        // Send messages and then remove them from the queue
        for (Mensaje mensaje : messagesToSend) {
            System.out.println("ManejadorRegistro: Enviando mensaje pendiente a " + usuario.getNombre() + ": " + mensaje.getContenidoCifrado().substring(0, Math.min(mensaje.getContenidoCifrado().length(), 20)) + "...");
            salida.writeObject(mensaje);
            salida.flush();
            Thread.sleep(50); // Small delay to prevent network saturation or client buffer issues
            servidorDirectorio.getColaMensajes().getMensajesRecibidos().remove(mensaje); // Remove after successful send
        }
        this.servidorDirectorio.setCambios(true); // Notify server of changes to message queue
    }


    private void enviarContactos() throws IOException {
        // Get all contacts from the directory and create a DTO
        ArrayList<Contacto> contactosList = new ArrayList<>(servidorDirectorio.getDirectorio().getUsuarios().values());
        DirectorioDTO contactos = new DirectorioDTO(contactosList);
        System.out.println("ManejadorRegistro: Enviando lista de contactos a " + usuario.getNombre() + ": " + contactos.getContactos().size() + " usuarios.");
        salida.writeObject(contactos);
        salida.flush();
    }

    private void enviarMensaje(Mensaje mensaje) {
        // Look up handler by RECEPTOR'S NAME (String)
        ManejadorRegistro manejadorDestino = servidorDirectorio.getManejadores().get(mensaje.getReceptor());
        if (manejadorDestino != null) {
            try {
                manejadorDestino.enviarMensajeACliente(mensaje);
                System.out.println("ManejadorRegistro: Mensaje reenviado a " + mensaje.getReceptor());
            } catch (IOException e) {
                // If sending fails, store message for later (offline delivery)
                System.out.println("ManejadorRegistro: Error al enviar el mensaje a " + mensaje.getReceptor() + ". Almacenando mensaje pendiente.");
                servidorDirectorio.getColaMensajes().getMensajesRecibidos().add(mensaje);
                this.servidorDirectorio.setCambios(true);
            }
        } else {
            // If recipient is not connected, store message as pending
            servidorDirectorio.getColaMensajes().getMensajesRecibidos().add(mensaje);
            System.out.println("ManejadorRegistro: El receptor '" + mensaje.getReceptor() + "' no está conectado. El mensaje se almacenará como pendiente.");
            this.servidorDirectorio.setCambios(true);
        }
    }

    // You might want a stop method for graceful shutdown
    public void stop() {
        this.corriendo = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // This will cause SocketException in run() loop, breaking it
            }
        } catch (IOException e) {
            System.err.println("Error al intentar cerrar socket en stop(): " + e.getMessage());
        }
    }
}