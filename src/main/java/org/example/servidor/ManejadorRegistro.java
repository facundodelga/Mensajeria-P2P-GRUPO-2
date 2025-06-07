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
            // ***** ESTE ES EL CAMBIO CRÍTICO: ORDEN INVERTIDO DE STREAMS EN EL SERVIDOR *****
            // 1. Crear el ObjectInputStream PRIMERO. Este se bloqueará esperando la cabecera del cliente.
            entrada = new ObjectInputStream(socket.getInputStream());
            System.out.println("SERVIDOR DEBUG: ObjectInputStream del servidor inicializado.");

            // 2. Luego crear el ObjectOutputStream. Al crearse, envía su propia cabecera,
            // desbloqueando el ObjectInputStream del cliente.
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush(); // ¡IMPORTANTE! Envía la cabecera de serialización inmediatamente.
            System.out.println("SERVIDOR DEBUG: ObjectOutputStream del servidor inicializado y flusheado.");

            // A partir de aquí, los streams en ambos lados ya están establecidos.

            // Registro del usuario
            Contacto usuarioDTO = (Contacto) entrada.readObject();

            boolean estaConectado = servidorDirectorio.getConectados().getUsuarios().containsKey(usuarioDTO.getNombre());
            boolean estaDirectorio = servidorDirectorio.getDirectorio().getUsuarios().containsKey(usuarioDTO.getNombre());
            if (estaConectado && estaDirectorio) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            this.usuario = usuarioDTO;
            servidorDirectorio.getDirectorio().addUsuario(usuario.getNombre(), usuario);
            servidorDirectorio.getConectados().addUsuario(usuario.getNombre(), usuario);
            servidorDirectorio.addManejador(usuario, this);
            salida.writeObject("Registro exitoso.");
            salida.flush();
            this.servidorDirectorio.setCambios(true);

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
                    // Aquí, mensaje.getEmisor() ya es un String
                    System.out.println("Soy " + usuario.getNombre() + ": Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenidoCifrado());
                    enviarMensaje(mensaje);
                } else if (msg instanceof Contacto) {
                    Contacto contacto = (Contacto) msg;
                    System.out.println("Soy " + usuario.getNombre() + ": Contacto recibido: " + contacto.getNombre());
                    // Tu lógica original que imprimía "Contacto no encontrado"
                    if (contacto.getNombre().equals("Contactos")) { // Asumo que "Contactos" es una solicitud especial
                        enviarContactos();
                    } else {
                        System.out.println("Soy " + usuario.getNombre() + ": Contacto no encontrado o no esperado.");
                        salida.writeObject("Contacto no encontrado o no esperado como solicitud.");
                        salida.flush();
                    }
                } else {
                    System.out.println("Objeto desconocido recibido: " + msg);
                }
            }
        } catch (SocketException e) {
            System.out.println("El cliente " + (usuario != null ? usuario.getNombre() : socket.getInetAddress()) + " se ha desconectado (SocketException).");
        } catch (EOFException e) {
            System.out.println("El cliente " + (usuario != null ? usuario.getNombre() : socket.getInetAddress()) + " se ha desconectado (EOFException).");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en ManejadorRegistro para " + (usuario != null ? usuario.getNombre() : socket.getInetAddress()) + ": " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restablece el flag de interrupción
            System.err.println("ManejadorRegistro interrumpido para " + (usuario != null ? usuario.getNombre() : socket.getInetAddress()) + ": " + e.getMessage());
        } finally {
            try {
                // Limpieza de recursos y eliminación del usuario al finalizar el hilo
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("SERVIDOR DEBUG: Socket cerrado para " + (usuario != null ? usuario.getNombre() : "cliente desconocido") + ".");
                }
                if (entrada != null) entrada.close();
                if (salida != null) salida.close();
                // Eliminar el usuario de la lista de conectados del servidor
                if (usuario != null) {
                    servidorDirectorio.getConectados().getUsuarios().remove(usuario.getNombre());
                    servidorDirectorio.getManejadores().remove(usuario);
                    this.servidorDirectorio.setCambios(true);
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar recursos en ManejadorRegistro: " + e.getMessage());
                e.printStackTrace();
            }
            this.corriendo = false; // Asegura que el hilo termina
        }
    }

    public void enviarMensajeACliente(Mensaje mensaje) throws IOException {
        if (salida != null) {
            System.out.println("Enviando mensaje a " + usuario.getNombre() + ": " + mensaje.getContenidoCifrado());
            salida.writeObject(mensaje);
            salida.flush();
        } else {
            System.err.println("Error: ObjectOutputStream 'salida' es null para " + usuario.getNombre() + ". No se puede enviar mensaje.");
        }
    }

    private void enviarMensajesPendientes() throws IOException, InterruptedException {
        ArrayList<Mensaje> mensajesAEnviar = new ArrayList<>();
        for (Mensaje mensaje : servidorDirectorio.getColaMensajes().getMensajesRecibidos()) {
            // El campo 'receptor' en Mensaje es un String, así que comparamos directamente
            if (mensaje.getReceptor().equals(usuario.getNombre())) { // Cambiado: ahora compara String con String
                mensajesAEnviar.add(mensaje);
            }
        }

        if (salida != null) {
            for (Mensaje mensaje : mensajesAEnviar) {
                System.out.println("Enviando mensaje pendiente a " + usuario.getNombre() + ": " + mensaje.getContenidoCifrado());
                salida.writeObject(mensaje);
                salida.flush();
                sleep(50); // Pequeña pausa para evitar sobrecarga
            }
            servidorDirectorio.getColaMensajes().getMensajesRecibidos().removeAll(mensajesAEnviar);
        } else {
            System.err.println("Error: ObjectOutputStream 'salida' es null al intentar enviar mensajes pendientes a " + usuario.getNombre());
        }
    }

    private void enviarContactos() throws IOException {
        if (salida != null) {
            ArrayList<Contacto> contactosList = new ArrayList<>(servidorDirectorio.getDirectorio().getUsuarios().values());
            DirectorioDTO contactos = new DirectorioDTO(contactosList);
            System.out.println("Enviando lista de contactos a " + usuario.getNombre() + ": " + contactos.getContactos().size() + " contactos.");
            salida.writeObject(contactos);
            salida.flush();
        } else {
            System.err.println("Error: ObjectOutputStream 'salida' es null al intentar enviar contactos a " + usuario.getNombre());
        }
    }

    private void enviarMensaje(Mensaje mensaje) {
        // Obtenemos el nombre del receptor directamente del mensaje (que es un String)
        String nombreReceptor = mensaje.getReceptor();

        // Buscamos el ManejadorRegistro asociado a ese nombre de usuario
        ManejadorRegistro manejadorDestino = null;
        for (Contacto conectado : servidorDirectorio.getManejadores().keySet()) {
            if (conectado.getNombre().equals(nombreReceptor)) {
                manejadorDestino = servidorDirectorio.getManejadores().get(conectado);
                break;
            }
        }

        if (manejadorDestino != null && manejadorDestino.corriendo) {
            try {
                manejadorDestino.enviarMensajeACliente(mensaje);
            } catch (IOException e) {
                System.out.println("No se pudo enviar el mensaje a " + nombreReceptor + ". Añadiendo a cola de pendientes.");
                servidorDirectorio.getColaMensajes().getMensajesRecibidos().add(mensaje);
                this.servidorDirectorio.setCambios(true);
            }
        } else {
            System.out.println("El receptor " + nombreReceptor + " no está conectado o su manejador no está corriendo. El mensaje se almacenará.");
            servidorDirectorio.getColaMensajes().getMensajesRecibidos().add(mensaje);
            this.servidorDirectorio.setCambios(true);
        }
    }
}