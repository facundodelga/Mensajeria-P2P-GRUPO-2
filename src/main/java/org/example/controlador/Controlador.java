package org.example.controlador;

import org.example.conexion.IConexion;
import org.example.modelo.*;
import org.example.modelo.mensaje.Mensaje;
import org.example.conexion.Conexion;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;
import org.example.vista.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

public class Controlador implements ActionListener, Observer {
    private static Controlador instancia = null;
    private IVistaPrincipal vista;
    private IVistaInicioSesion vistaInicioSesion;
    private IVistaAgregarContacto vistaAgregarContacto;
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
        }else if(e.getActionCommand().equalsIgnoreCase("botonAgregarContacto")) {
            agregarNuevoContacto();

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

    private void nuevoContacto() {
//
//        UsuarioDTO usuarioDTO = new UsuarioDTO(
//                vistaAgregarContacto.getNombre(),
//                vistaAgregarContacto.getIP(),
//                Integer.parseInt(vistaAgregarContacto.getPuerto()));
//
//        agendaServicio.addContacto(usuarioDTO);

    }

    private void agregarNuevoContacto() {

        UsuarioDTO nuevoContacto = vista.mostrarAgregarContacto();
        agendaServicio.addContacto(nuevoContacto);

/*
        if (nombre.isEmpty() || ip.isEmpty() || puerto.isEmpty()) {
            mostrarMensajeFlotante("<html>Usuario registrado sin éxito:<br>Todos los campos deben completarse correctamente.</html>", new Color(200, 50, 50));
            return;

        }
        System.out.println("Nombre: " + nombre + " IP: " + ip + " Puerto: " + puerto);

 */
    }

    private void mostrarMensajeFlotante(String texto, Color fondo) {
        JDialog mensaje = new JDialog((Frame) vista, false);
        mensaje.setUndecorated(true);
        mensaje.getContentPane().setBackground(fondo);

        JLabel label = new JLabel("<html><div style='text-align: center;'>" + texto + "</div></html>", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // padding interno
        mensaje.getContentPane().add(label);

        mensaje.pack(); // ajusta automaticamente al contenido
        mensaje.setLocationRelativeTo((Component) vista);
        mensaje.setAlwaysOnTop(true);
        mensaje.setVisible(true);

        new Timer(2000, e -> mensaje.dispose()).start();
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