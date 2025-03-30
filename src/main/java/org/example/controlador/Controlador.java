package org.example.controlador;

import org.example.conexion.IConexion;
import org.example.modelo.*;
import org.example.modelo.mensaje.Mensaje;
import org.example.conexion.Conexion;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;
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
    private IUsuario usuarioServicio;
    private IAgenda agendaServicio;
    private IConversacion conversacionServicio;
    private IConexion conexion;
    private UsuarioDTO usuarioDTO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public Controlador() {

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



            Usuario usuario = new Usuario(nombre, host, puerto);
            this.usuarioServicio = new UsuarioServicio(usuario);
            this.agendaServicio = new AgendaServicio(usuario);
            this.conversacionServicio = new ConversacionServicio(usuario);
            this.conexion = new Conexion();

            this.usuarioDTO = new UsuarioDTO(usuario);

            conexion.iniciarServidor(puerto);
            new Thread(this.conexion).start();
            vista.addMensaje("Servidor iniciado en " + host + ":" + puerto);
        } else if (e.getSource() == vista.getConectarButton()) {
            String host = vista.getHost();
            int puerto = vista.getPuerto();
            try {
                Socket socket = new Socket(host, puerto);
                this.conexion.agregarConexionDeSalida(vista.getNombre(),socket);
                vista.addMensaje("Conectado a " + host + ":" + puerto);
            } catch (IOException ex) {
                ex.printStackTrace();
                vista.addMensaje("Error al conectar a " + host + ":" + puerto);
            }
        } else if (e.getSource() == vista.getEnviarMensajeButton()) {
            String mensaje = vista.getMensaje();

            Mensaje mensajeObj = new Mensaje(mensaje, this.usuarioDTO);
            String host = vista.getHost();
            int puerto = vista.getPuerto();
            String nombreReceptor = vista.getNombre();

            UsuarioDTO usuarioDTO = new UsuarioDTO(nombreReceptor, host, puerto);

            try {
                this.conexion.enviarMensaje(usuarioDTO,mensajeObj);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            conversacionServicio.addMensajeSaliente(usuarioDTO,mensajeObj);

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
        this.conversacionServicio.addMensajeEntrante(mensaje);
        String fechaFormateada = sdf.format(mensaje.getFecha());

        vista.addMensaje("[" + mensaje.getEmisor().getNombre() + " | " + fechaFormateada + "]: " + mensaje.getContenido());
    }
}