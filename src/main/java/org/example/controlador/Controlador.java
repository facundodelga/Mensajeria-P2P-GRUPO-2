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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private Controlador() {


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
        } else if(e.getActionCommand().equalsIgnoreCase("Enviar")) {
            enviarMensaje();
        } else if(e.getActionCommand().equalsIgnoreCase("IniciarChat")) {
            iniciarChat();
        }
    }

    private void iniciarChat() {
        System.out.println("Inicio de chat");
        UsuarioDTO selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null) {

            System.out.println("inicio de chat "+selectedValue);
            this.conversacionServicio.agregarConversacion(selectedValue);

            vista.getModeloChats().addElement(selectedValue);
            vista.getListaChats().setSelectedValue(selectedValue, true);
            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        } else {
            mostrarMensajeFlotante("Seleccione un contacto", Color.RED);
        }
    }

    private void enviarMensaje()  {

        Mensaje mensaje = new Mensaje(vista.getCampoMensaje().getText(),this.usuarioDTO);

        try {
            conexion.enviarMensaje(vista.getListaChats().getSelectedValue(),mensaje);
            this.conversacionServicio.addMensajeSaliente(vista.getListaChats().getSelectedValue(),mensaje);
            vista.getCampoMensaje().setText("");

            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    true,
                    sdf.format(mensaje.getFecha())));
        } catch (IOException e) {
            mostrarMensajeFlotante("Error al enviar el mensaje", Color.RED);
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
        new Thread(conexion).start();
        vista.mostrar();
        vista.titulo("Usuario: " + nombre + " | Ip: "+ "127.0.0.1" + " | Puerto: " + puerto);
    }

    public void agregarNuevoContacto() {

        UsuarioDTO nuevoContacto = vista.mostrarAgregarContacto();
        agendaServicio.addContacto(nuevoContacto);
        vista.getModeloContactos().addElement(nuevoContacto);

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

    public void recibirMensaje(Mensaje mensaje){
        this.conversacionServicio.addMensajeEntrante(mensaje);
        String fechaFormateada = sdf.format(mensaje.getFecha());
        if(!vista.getModeloChats().contains(mensaje.getEmisor())){

            if(!vista.getModeloContactos().contains(mensaje.getEmisor())){
                vista.getModeloChats().addElement(mensaje.getEmisor());
                vista.getModeloContactos().addElement(mensaje.getEmisor());
            }else {
                vista.getModeloChats().addElement(this.agendaServicio.buscaNombreContacto(mensaje.getEmisor()));
            }
        }

        if(mensaje.getEmisor().equals(vista.getListaChats().getSelectedValue())) {
            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    false,
                    fechaFormateada));

        }else{
            // Notificar visualmente que hay un nuevo mensaje
            int index = vista.getModeloChats().indexOf(mensaje.getEmisor());
            if (index >= 0) {
                UsuarioDTO chatConNotificacion = vista.getModeloChats().get(index);
//                chatConNotificacion.tieneNotificacion();
                vista.getModeloChats().set(index, chatConNotificacion);
            }
        }

    }



    @Override
    public void update(Observable o, Object arg) {
        Mensaje mensaje = (Mensaje) arg;
        recibirMensaje(mensaje);

       // vista.addMensaje("[" + mensaje.getEmisor().getNombre() + " | " + fechaFormateada + "]: " + mensaje.getContenido());
    }

    public void setVistaInicioSesion(IVistaInicioSesion vistaInicioSesion) {
        this.vistaInicioSesion = vistaInicioSesion;
    }

    public void setVistaPrincipal(IVistaPrincipal vista) {
        this.vista = vista;
    }

    public void cargarConversacion(UsuarioDTO selectedValue) {
        int index = vista.getModeloChats().indexOf(selectedValue);
        if (index >= 0) {
            UsuarioDTO chatConNotificacion = vista.getModeloChats().get(index);
//            chatConNotificacion.leido();
            vista.getModeloChats().set(index, chatConNotificacion);
        }
        ArrayList<Mensaje> mensajes = (ArrayList<Mensaje>) this.conversacionServicio.getMensajes(selectedValue);
        // Limpiar el panel de mensajes
        vista.getPanelMensajes().removeAll();

        for(Mensaje mensaje : mensajes) {
            String fechaFormateada = sdf.format(mensaje.getFecha());
            // Agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    mensaje.getEmisor().equals(usuarioDTO),
                    fechaFormateada));
        }


        // Actualizar la vista
        vista.getPanelMensajes().revalidate();
        vista.getPanelMensajes().repaint();
    }
}