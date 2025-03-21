package org.example.controlador;

import org.example.mensaje.Mensaje;
import org.example.sistema.Sistema;
import org.example.usuario.Usuario;
import org.example.usuario.UsuarioDTO;
import org.example.vista.Vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Controlador implements ActionListener {
    private Vista vista;
    private Sistema sistema;

    public Controlador(Vista vista) {
        this.vista = vista;
        this.sistema = Sistema.getInstancia();
        this.vista.getIniciarServidorButton().addActionListener(this);
        this.vista.getConectarButton().addActionListener(this);
        this.vista.getEnviarMensajeButton().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getIniciarServidorButton()) {
            String nombre = vista.getNombre();
            String host = vista.getHost();
            int puerto = vista.getPuerto();
            UsuarioDTO usuario = new UsuarioDTO(nombre, host, puerto);

            sistema.configurarServidor(UsuarioDTO.toUsuario(usuario));
            new Thread(sistema).start();
            vista.addMensaje("Servidor iniciado en " + host + ":" + puerto);
        } else if (e.getSource() == vista.getConectarButton()) {
            String host = vista.getHost();
            int puerto = vista.getPuerto();
            try {
                Socket socket = new Socket(host, puerto);
                sistema.agregarConexionDeSalida(vista.getNombre(),socket);
                vista.addMensaje("Conectado a " + host + ":" + puerto);
            } catch (IOException ex) {
                ex.printStackTrace();
                vista.addMensaje("Error al conectar a " + host + ":" + puerto);
            }
        } else if (e.getSource() == vista.getEnviarMensajeButton()) {
            String mensaje = vista.getMensaje();
            Usuario usuario = Sistema.getInstancia().getUsuario();
            Mensaje mensajeObj = new Mensaje(mensaje, new UsuarioDTO(usuario));
            String host = vista.getHost();
            int puerto = vista.getPuerto();
            String nombreReceptor = vista.getNombre();
            UsuarioDTO usuarioDTO = new UsuarioDTO(nombreReceptor, host, puerto);
            Sistema.getInstancia().enviarMensaje(usuarioDTO,mensajeObj);
            // Aquí puedes agregar la lógica para enviar el mensaje
            vista.addMensaje("Mensaje enviado: " + mensaje);
        }
    }
}