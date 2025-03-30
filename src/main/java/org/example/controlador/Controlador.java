package org.example.controlador;

import org.example.conexion.IConexion;
import org.example.modelo.*;
import org.example.modelo.mensaje.Mensaje;
import org.example.conexion.Conexion;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;
import org.example.vista.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

public class Controlador implements ActionListener, Observer {
    private static Controlador instancia = null;
    private IVistaPrincipal vista;
    private IVistaInicioSesion vistaInicioSesion;
    private IUsuario usuarioServicio;
    private IAgenda agendaServicio;
    private IConversacion conversacionServicio;
    private IConexion conexion;
    private UsuarioDTO usuarioDTO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public Controlador() {
       

    }

    public static Controlador getInstancia() {
        if (instancia == null) {
            instancia = new Controlador();
        }
        return instancia;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equalsIgnoreCase("Iniciar")) {
            iniciarServidor();
        }
    }

    public void iniciarServidor() {
        String nombre = vistaInicioSesion.getNombre();
        int puerto = Integer.parseInt(vistaInicioSesion.getPuerto());

        vistaInicioSesion.ocultar();

        Usuario usuario = new Usuario(nombre, "127.0.0.1", puerto);
        this.usuarioServicio = new UsuarioServicio(usuario);
        this.agendaServicio = new AgendaServicio(usuario);
        this.conversacionServicio = new ConversacionServicio(usuario);
        this.conexion = new Conexion();
        this.usuarioDTO = new UsuarioDTO(usuario);

        conexion.iniciarServidor(puerto);

        vista.mostrar();

    }

    @Override
    public void update(Observable o, Object arg) {
        Mensaje mensaje = (Mensaje) arg;
        this.conversacionServicio.addMensajeEntrante(mensaje);
        String fechaFormateada = sdf.format(mensaje.getFecha());

       // vista.addMensaje("[" + mensaje.getEmisor().getNombre() + " | " + fechaFormateada + "]: " + mensaje.getContenido());
    }

    public void setVistaInicioSesion(IVistaInicioSesion vistaInicioSesion) {
        this.vistaInicioSesion = vistaInicioSesion;
    }

    public void setVistaPrincipal(IVistaPrincipal vista) {
        this.vista = vista;
    }
}