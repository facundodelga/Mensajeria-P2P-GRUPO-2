package org.example.servidor;

import java.io.*;
import java.net.Socket;

/**
 * Clase que representa un servidor de directorio para gestionar el registro de usuarios.
 * Escucha conexiones entrantes y delega el manejo de cada conexi贸n a un hilo separado.
 */

public class Servidor extends Thread {
    private ServidorState estado;
    private int puerto, puertoOtro;

    /**
     * Constructor para ServidorDirectorio.
     * Inicializa el ServerSocket en el puerto especificado y crea un mapa para almacenar usuarios.
     * @throws IOException Si hay un error al abrir el puerto.
     */
    public Servidor() throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader("serverConfig.txt"))) {
            this.puerto = Integer.parseInt(reader.readLine().trim());
            this.puertoOtro = Integer.parseInt(reader.readLine().trim());
            //serverSocket.bind(new InetSocketAddress(puerto));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al leer el puerto desde el archivo de configuraci贸n");
        } catch (IOException e) {
            throw new RuntimeException("Error al abrir el archivo de configuraci贸n");
        }
        // Trata de conectarse al otro servidor para ver si es primario o secundario
        try {
            // Encontró servidor primario
            System.out.println("Intentando conectar al servidor " + puertoOtro + ".");
            this.estado = new ServidorSecundario(this);

        } catch (IOException e) { // No encontró servidor primario
            System.out.println("No se encontró el servidor en " + puertoOtro  + ".");
            try {
                System.out.println("Intentando conectar al servidor " +  puerto + ".");
                this.estado = new ServidorPrincipal(this);
            }catch(IOException e1){
                System.out.println("No se pudo abrir el servidor principal: " + puerto + ".");
                System.exit(404);
            }
        }

        this.start();
        System.out.println("Servidor iniciado en el puerto: " + puerto);


    }

    @Override
    public void run() {
        while (true) {
            this.estado.esperarConexiones();
        }
    }

    /**
     * Inicia el servidor y comienza a aceptar conexiones de clientes.
     * Por cada conexi贸n entrante, se crea un nuevo hilo para manejar el registro del usuario.
     */
    public void iniciar() {

    }

    public ServidorState getEstado() {
        return estado;
    }

    public void setEstado(ServidorState estado) {
        this.estado = estado;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public int getPuertoOtro() {
        return puertoOtro;
    }

    public void setPuertoOtro(int puertoOtro) {
        this.puertoOtro = puertoOtro;
    }
}