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
            // Inicializamos los flujos de entrada y salida correctamente
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();  // Asegurarnos de que el flujo esté listo para escribir

            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            // Leer el usuario desde el flujo de entrada (suponiendo que el cliente envía un objeto de tipo Contacto)
            Contacto usuario = (Contacto) entrada.readObject();

            // Comprobamos si el nombre de usuario ya está en uso
            if (this.servidorDirectorio.getUsuarios().containsKey(usuario.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
                salida.flush();
                socket.close();
                return;
            }

            // Registrar al usuario en el servidor
            this.usuario = usuario;
            this.servidorDirectorio.getUsuarios().put(usuario.getNombre(), usuario);
            this.servidorDirectorio.getSockets().put(usuario, socket);
            salida.writeObject("Registro exitoso.");
            salida.flush();

            // Enviar mensajes pendientes si los hay
            enviarMensajesPendientes();

            this.corriendo = true;
            while (corriendo) {
                try {
                    // Leer el siguiente objeto enviado por el cliente
                    Object msg = entrada.readObject();

                    if (msg == null) {
                        System.out.println("El cliente se ha desconectado.");
                        this.corriendo = false;
                        break;
                    }

                    // Procesar el mensaje recibido
                    if (msg instanceof Mensaje) {
                        Mensaje mensaje = (Mensaje) msg;
                        System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());

                        // Buscar el socket del receptor
                        Socket socketDestino = servidorDirectorio.getSockets().get(mensaje.getReceptor());
                        if (socketDestino != null) {
                            try {
                                // Enviar el mensaje al receptor
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

                        // Comando para enviar mensajes pendientes
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
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    corriendo = false;  // Detener el hilo si se encuentra un error
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // Cerrar los flujos y el socket al finalizar
                if (salida != null) salida.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarMensajesPendientes() throws IOException {
        // Enviar todos los mensajes pendientes para este usuario
        for (Mensaje mensaje : this.servidorDirectorio.getMensajesRecibidos()) {
            if (mensaje.getReceptor().equals(usuario)) {
                salida.writeObject(mensaje);
            }
        }
        salida.flush();
    }
}
