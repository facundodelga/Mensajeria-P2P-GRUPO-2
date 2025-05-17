package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

import static java.lang.Thread.sleep;

public class ServidorSecundario implements ServidorState {
    private Servidor servidor;
    private Map<Contacto, ManejadorRegistro> manejadores;
    private IDirectorio directorio;
    private IColaMensajes colaMensajes;
    private ObjectInputStream entradaOtro;

    private int puerto, puertoPrincipal;

    public ServidorSecundario(Servidor servidor) throws IOException {
        this.servidor = servidor;
        this.puerto = servidor.getPuerto();
        this.puertoPrincipal = servidor.getPuertoOtro();

        int portOtro = this.servidor.getPuertoOtro();
        Socket socketOtro = new Socket("127.0.0.1",puerto);
        socketOtro.setSoTimeout(5100); // Heartbeat de 5 segundos. Tolerancia de 0.1 segundos.

        PrintWriter out = new PrintWriter(socketOtro.getOutputStream(), true);
        out.println("SERVIDOR");
        this.entradaOtro = new ObjectInputStream(socketOtro.getInputStream());
        System.out.println("Se encontró un servidor en " + portOtro + ". Iniciando como backup...");

    }

    @Override
    public void esperarConexiones() {

        System.out.println("Esperando conexiones en el servidor secundario...");
        esperarPulsos();
    }

    @Override
    public void cambiarEstado() {
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Implementar la lógica para cambiar el estado del servidor secundario
        System.out.println("Cambiando estado del servidor secundario...");
        System.out.println("Cambiando a modo primario...");  // CAMBIO DE SV SECUNDARIO A PRIMARIO
        try {
            this.servidor.setEstado(new ServidorPrincipal(this.servidor, this.manejadores, this.directorio, this.colaMensajes, false));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void esperarPulsos() {
        long before = System.currentTimeMillis();
        try {
            while (true) { // Espera pulsos/actualizaciones
                Object o = this.entradaOtro.readObject();
                if (o.getClass() == String.class) { // Se recibió un pulso
                    System.out.println("Se recibió un pulso del servidor principal.");
                } else {
                    System.out.println("Se recibió una actualización del servidor principal.");
                    ServidorDTO estado = (ServidorDTO) o;
                    this.directorio = estado.getDirectorio();
                    this.colaMensajes = (estado.getColaMensajes());
                    System.out.println("Se actualizó el estado del servidor secundario.");
                }
                before = System.currentTimeMillis();
            }
        } catch (SocketTimeoutException e) {
            // Pasaron más de 5 segundos. El SV primario no envió pulso.
            System.out.println("No se recibió un pulso en los ultimos " + (System.currentTimeMillis() - before) + " milisegundos.");
            System.out.println("Cambiando a modo primario...");  // CAMBIO DE SV SECUNDARIO A PRIMARIO
            this.cambiarEstado();
        } catch (IOException e) {
            // ERROR DE CONEXION CON EL SV PRIMARIO
            e.printStackTrace();
            System.out.println("Hubo un error de conexión con el servidor primario.");
            System.out.println("Cambiando a modo primario..."); // CAMBIO DE SV SECUNDARIO A PRIMARIO

            this.cambiarEstado();
        } catch (ClassNotFoundException e) {
            // No se encontró la clase del objeto enviado (nunca pasa)
            e.printStackTrace();
        }
    }
}
