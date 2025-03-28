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
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

public class Controlador implements ActionListener, Observer {
    private static Controlador instancia;
    private Vista vista;
    private Sistema sistema;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public Controlador() {
        this.sistema = Sistema.getInstancia();
        this.vista = new Vista();
        this.vista.getIniciarServidorButton().addActionListener(this);
        this.vista.getConectarButton().addActionListener(this);
        this.vista.getEnviarMensajeButton().addActionListener(this);
    }

    public static Controlador getInstancia() {
        if (instancia == null) {
            instancia = new Controlador();
        }
        return instancia;
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

    public void setVista(Vista vista) {
        this.vista = vista;
    }

    @Override
    public void update(Observable o, Object arg) {

        Mensaje mensaje = (Mensaje) arg;
        String fechaFormateada = sdf.format(mensaje.getFecha());

        vista.addMensaje("[" + mensaje.getUsuario().getNombre() + " | " + fechaFormateada + "]: " + mensaje.getContenido());

    }
}