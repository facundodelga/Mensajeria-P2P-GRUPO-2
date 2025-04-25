package org.example.servidor;

public class mainServidor {
    public static void main(String[] args) {
        // Crear una instancia de Servidor
        try{
            Servidor servidor = new Servidor();

            // Iniciar el servidor
            servidor.iniciar();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
