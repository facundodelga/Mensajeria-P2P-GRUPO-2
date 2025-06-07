// src/main/java/org/example/servidor/ManejadorRegistro.java

package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List; // Importar List para la copia de mensajes pendientes
import java.util.Map; // Importar Map
import java.io.Serializable; // Importar Serializable para el Map

/**
 * Clase que maneja el registro de usuarios en el servidor.
 * Implementa la interfaz Runnable para permitir la ejecución en un hilo separado.
 */
public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private ServidorPrincipal servidorDirectorio;
    private boolean corriendo = false;
    private Contacto usuario;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ManejadorRegistro(Socket socket, ServidorPrincipal servidorDirectorio) {
        this.socket = socket;
        this.servidorDirectorio = servidorDirectorio;
    }

    @Override
    public void run() {
        try {
            // Crear flujos una sola vez
            // Es crucial que ObjectOutputStream se cree ANTES que ObjectInputStream para evitar deadlocks en el handshake de streams
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // Registro del usuario
            Contacto usuarioDTO = (Contacto) entrada.readObject();

            boolean estaConectado = servidorDirectorio.getConectados().getUsuarios().containsKey(usuarioDTO.getNombre());
            boolean estaDirectorio = servidorDirectorio.getDirectorio().getUsuarios().containsKey(usuarioDTO.getNombre());

            // La lógica actual: si está en 'conectados' Y en 'directorio', entonces en uso.
            // Si 'conectados' es para sesiones activas, y 'directorio' para todos los usuarios registrados,
            // entonces 'if (estaConectado)' solo podría ser suficiente para "en uso".
            // Pero mantendremos tu lógica si así lo deseas.
            if (estaConectado && estaDirectorio) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                System.out.println("Cliente " + usuarioDTO.getNombre() + " rechazado: nickname en uso.");
                return;
            }

            this.usuario = usuarioDTO;
            servidorDirectorio.getDirectorio().addUsuario(usuario.getNombre(), usuario); // Add to all known users
            servidorDirectorio.getConectados().addUsuario(usuario.getNombre(), usuario); // Add to currently connected users
            servidorDirectorio.addManejador(usuario, this); // Add to map for routing
            salida.writeObject("Registro exitoso.");
            salida.flush();
            this.servidorDirectorio.setCambios(true); // Notify server of changes

            // Enviar mensajes pendientes (si los hay y la cola está implementada)
            enviarMensajesPendientes();

            this.corriendo = true;
            while (corriendo) {
                Object msg = entrada.readObject();
                if (msg == null) {
                    System.out.println("El cliente " + usuario.getNombre() + " ha enviado un objeto nulo o se ha desconectado.");
                    this.corriendo = false;
                    break;
                }

                System.out.println("Objeto recibido de " + usuario.getNombre() + ", tipo: " + msg.getClass().getName());

                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Server: Mensaje recibido de " + mensaje.getEmisor().getNombre() + " para " + mensaje.getReceptor().getNombre() + ". Contenido (cifrado): " + mensaje.getContenido());
                    enviarMensaje(mensaje); // This method routes the message
                } else if (msg instanceof Map) { // <-- ¡¡¡ESTE ES EL BLOQUE CLAVE AÑADIDO PARA EL CIFRADO!!!
                    Map<String, Serializable> map = (Map<String, Serializable>) msg;
                    if ("CLAVE_PUBLICA_DH".equals(map.get("tipo"))) {
                        Contacto receptor = (Contacto) map.get("receptor");
                        System.out.println("Server: Clave pública DH recibida de " + usuario.getNombre() + " para " + receptor.getNombre());

                        ManejadorRegistro manejadorDestino = servidorDirectorio.getManejadores().get(receptor);
                        if (manejadorDestino != null) {
                            try {
                                manejadorDestino.enviarObjetoACliente(map); // Reenviar el mapa completo (con la clave pública DH)
                                System.out.println("Server: Clave pública DH reenviada a " + receptor.getNombre());
                            } catch (IOException e) {
                                System.err.println("Server: Error al reenviar clave pública DH a " + receptor.getNombre() + ": " + e.getMessage());
                                // Opcional: Notificar al emisor que el envío de clave falló
                                enviarObjetoACliente("Error al enviar clave pública DH a " + receptor.getNombre() + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("Server: Destinatario " + receptor.getNombre() + " para clave DH está offline.");
                            // Opcional: Almacenar la clave pública para cuando el destinatario se conecte, o simplemente notificar al emisor.
                            enviarObjetoACliente("El intercambio de claves falló: " + receptor.getNombre() + " no está conectado.");
                        }
                    } else {
                        System.out.println("Server: Mapa de tipo desconocido recibido de " + usuario.getNombre() + ": " + map.get("tipo"));
                    }
                } else if (msg instanceof Contacto) {
                    Contacto contacto = (Contacto) msg;
                    System.out.println("Server: Solicitud de contacto recibida de " + usuario.getNombre() + " para: " + contacto.getNombre());
                    if (contacto.getNombre().equals("Contactos")) { // Este es el flag que usa el cliente para pedir la lista de contactos
                        enviarContactos();
                    } else {
                        // Lógica para buscar un contacto específico si fuera necesario, aunque el cliente pide el directorio completo
                        System.out.println("Server: Solicitud de contacto específico no manejada: " + contacto.getNombre());
                        enviarObjetoACliente("Solicitud de contacto específico no soportada.");
                    }
                } else {
                    System.out.println("Server: Objeto de tipo desconocido recibido de " + usuario.getNombre() + ": " + msg.getClass().getName());
                }
            }
        } catch (SocketException e) {
            System.out.println("Cliente " + usuario.getNombre() + " ha cerrado su conexión o se ha desconectado.");
        } catch (EOFException e) { // End Of File exception typically means stream closed unexpectedly
            System.out.println("Cliente " + usuario.getNombre() + " ha cerrado su stream de entrada.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la conexión con " + usuario.getNombre() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Hilo de " + usuario.getNombre() + " interrumpido: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
        } finally {
            // Asegurarse de que el cliente sea desregistrado y los recursos cerrados
            if (usuario != null) {
                servidorDirectorio.getConectados().getUsuarios().remove(usuario.getNombre());
                servidorDirectorio.getManejadores().remove(usuario); // Remove from routing map
                this.servidorDirectorio.setCambios(true); // Notify of client removal
                System.out.println("Cliente " + usuario.getNombre() + " desconectado y desregistrado.");
            }
            closeResources();
        }
    }

    // Nuevo método para enviar cualquier objeto a este cliente (útil para reenviar Mapas también)
    public synchronized void enviarObjetoACliente(Object obj) throws IOException {
        salida.writeObject(obj);
        salida.flush();
    }

    // Métodos existentes (enviarMensajeACliente es el mismo que enviarObjetoACliente pero específico para Mensaje)
    public void enviarMensajeACliente(Mensaje mensaje) throws IOException {
        System.out.println("Server: Enviando mensaje a " + usuario.getNombre() + " (desde cola/reenvío): " + mensaje.getContenido());
        salida.writeObject(mensaje);
        salida.flush();
    }

    private void enviarMensajesPendientes() throws IOException, InterruptedException {
        // Copia la lista para evitar ConcurrentModificationException
        ArrayList<Mensaje> mensajesAEnviar = new ArrayList<>();
        for (Mensaje mensaje : servidorDirectorio.getColaMensajes().getMensajesRecibidos()) {
            if (mensaje.getReceptor().getNombre().equals(usuario.getNombre())) { // Compara por nombre para simplificar
                mensajesAEnviar.add(mensaje);
            }
        }

        for (Mensaje mensaje : mensajesAEnviar) {
            System.out.println("Server: Enviando mensaje pendiente a " + usuario.getNombre() + ": " + mensaje.getContenido());
            salida.writeObject(mensaje);
            salida.flush();
            Thread.sleep(50); // Pequeña pausa para evitar saturación
            servidorDirectorio.getColaMensajes().getMensajesRecibidos().remove(mensaje); // Eliminar después de enviar
        }
        // Mejorar: usar un iterador para remover de forma segura o ConcurrentLinkedQueue para ColaMensajes
        // Si ColaMensajes.getMensajesRecibidos() devuelve la lista subyacente directamente,
        // esto podría causar problemas de concurrencia si otro hilo modifica la lista mientras se itera.
        // Lo ideal sería que ColaMensajes tenga un método "getAndRemovePendingMessagesForUser(Contacto usuario)".
    }

    private void enviarContactos() throws IOException {
        // Enviar todos los usuarios del directorio principal (registrados)
        ArrayList<Contacto> contactosList = new ArrayList<>(servidorDirectorio.getDirectorio().getUsuarios().values());
        DirectorioDTO contactos = new DirectorioDTO(contactosList);
        System.out.println("Server: Enviando lista completa de contactos a " + usuario.getNombre() + ". Total: " + contactosList.size());
        salida.writeObject(contactos);
        salida.flush();
    }

    private void enviarMensaje(Mensaje mensaje) {
        ManejadorRegistro manejadorDestino = servidorDirectorio.getManejadores().get(mensaje.getReceptor());
        if (manejadorDestino != null) {
            try {
                manejadorDestino.enviarMensajeACliente(mensaje); // Usa el método específico para Mensaje
            } catch (IOException e) {
                System.out.println("Server: Error al enviar el mensaje a " + mensaje.getReceptor().getNombre() + ". Añadiendo a cola de pendientes.");
                servidorDirectorio.getColaMensajes().getMensajesRecibidos().add(mensaje);
                this.servidorDirectorio.setCambios(true); // Indicar que hay cambios en la cola
            }
        } else {
            servidorDirectorio.getColaMensajes().getMensajesRecibidos().add(mensaje);
            System.out.println("Server: El receptor " + mensaje.getReceptor().getNombre() + " no está conectado. El mensaje se almacenará.");
            this.servidorDirectorio.setCambios(true); // Indicar que hay cambios en la cola
        }
    }

    private void closeResources() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Server: Recursos de " + (usuario != null ? usuario.getNombre() : "cliente desconocido") + " cerrados.");
        } catch (IOException e) {
            System.err.println("Server: Error al cerrar recursos para " + (usuario != null ? usuario.getNombre() : "cliente desconocido") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}