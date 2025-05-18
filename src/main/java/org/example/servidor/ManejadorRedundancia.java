package org.example.servidor;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ManejadorRedundancia extends Thread implements IRedundancia {

    private ServidorPrincipal servidor;
    private boolean running;
    private ObjectOutputStream out;

    public ManejadorRedundancia(Socket socket, ServidorPrincipal servidor) {
        this.servidor = servidor;
        try {

            this.out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Se conectó un servidor del puerto " + socket.getPort() + " como backup.");
            this.servidor.setCambios(true); // Asegura resincronización de estado
            this.running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long maxTime = 5000; // Heartbeat de 5 segundos.
        long before = System.currentTimeMillis();
        while (running) {
            try {
                if (this.servidor.hayCambios()) {
                    // Envía la fila actualizada
                    this.enviarEstado();
                    before = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - before >= maxTime) {
                    // Implementación de Heartbeat
                    this.enviarPulso();
                    before = System.currentTimeMillis();
                }
            } catch (IOException e1) {
                running = false;
                System.out.println("Se desconectó el servidor de backup");
            }
        }
    }

    @Override
    public void enviarPulso() throws IOException{
        this.out.writeObject("PULSO");
    }

    @Override
    public void enviarEstado() throws IOException{
        //Aca se deberia de clonar el estado del directorio y la cola de mensajes
        ServidorDTO estado = new ServidorDTO((Directorio)this.servidor.getDirectorio().clonar(),(ColaMensajes) this.servidor.getColaMensajes().clonar());
        this.out.writeObject(estado);
        this.servidor.setCambios(false);
    }

}
