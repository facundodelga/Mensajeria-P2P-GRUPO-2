package org.example.controlador;

import org.example.conexion.EnviarMensajeException;
import org.example.conexion.IConexion;
import org.example.conexion.PuertoEnUsoException;
import org.example.modelo.*;
import org.example.modelo.mensaje.Mensaje;
import org.example.conexion.Conexion;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;
import org.example.vista.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Clase Controlador que implementa ActionListener y Observer.
 * Maneja la lógica de la aplicación y la interacción entre la vista y el modelo.
 */
public class Controlador implements ActionListener, Observer {
    private static Controlador instancia = null;
    private IVistaPrincipal vista;
    private IVistaInicioSesion vistaInicioSesion;
    private IAgenda agendaServicio;
    private IConversacion conversacionServicio;
    private IConexion conexion;
    private UsuarioDTO usuarioDTO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private UsuarioServicio usuarioServicio;

    /**
     * Constructor privado para el patrón Singleton.
     */
    private Controlador() {
    }

    /**
     * Obtiene la instancia única de la clase Controlador.
     * @return instancia de Controlador
     */
    public static Controlador getInstancia() {
        if (instancia == null) {
            instancia = new Controlador();
        }
        return instancia;
    }

    /**
     * Maneja los eventos de acción de la interfaz de usuario.
     * @param e el evento de acción
     */
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

    /**
     * Inicia un nuevo chat con el contacto seleccionado.
     */
    private void iniciarChat() {
        System.out.println("Inicio de chat");
        UsuarioDTO selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null && !vista.getModeloChats().contains(new ChatPantalla(selectedValue))) {
            System.out.println("inicio de chat "+selectedValue);
            this.conversacionServicio.agregarConversacion(selectedValue);

            vista.getModeloChats().addElement(new ChatPantalla(selectedValue));
            vista.getListaChats().setSelectedValue(selectedValue, true);
            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        } else if(selectedValue != null && vista.getModeloChats().contains(new ChatPantalla(selectedValue))){
            mostrarMensajeFlotante("El contacto " + selectedValue.getNombre() + " ya tiene un chat iniciado", Color.RED);
        }else{
            mostrarMensajeFlotante("Seleccione un contacto", Color.RED);
        }
    }

    /**
     * Envía un mensaje al contacto seleccionado.
     */
    private void enviarMensaje()  {
        Mensaje mensaje = new Mensaje(vista.getCampoMensaje().getText(),this.usuarioDTO);

        try {

            conexion.enviarMensaje(vista.getListaChats().getSelectedValue().getContacto(),mensaje);
            this.conversacionServicio.addMensajeSaliente(vista.getListaChats().getSelectedValue().getContacto(),mensaje);
            vista.getCampoMensaje().setText("");

            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    true,
                    sdf.format(mensaje.getFecha())));
        } catch (EnviarMensajeException | IOException e) {
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
        }
    }

    /**
     * Inicia el servidor con los datos proporcionados por la vista de inicio de sesión.
     */
    public void iniciarServidor() {
        String nombre = vistaInicioSesion.getNombre();
        if(nombre.isEmpty()){
            mostrarMensajeFlotante("El nombre no puede estar vacío", Color.RED);
            return;
        }
        try {
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
        }catch (NumberFormatException e) {
            mostrarMensajeFlotante("El puerto debe ser un número entre 0 y 65535", Color.RED);

        }catch (PuertoEnUsoException e){
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            vistaInicioSesion.mostrar();
        }



    }

    /**
     * Agrega un nuevo contacto a la agenda y a la vista.
     */
    public void agregarNuevoContacto() {
        UsuarioDTO nuevoContacto = null;

        nuevoContacto = vista.mostrarAgregarContacto();
        if(nuevoContacto == null){
            return;
        }
        try {
            agendaServicio.addContacto(nuevoContacto);
            vista.getModeloContactos().addElement(nuevoContacto);
            mostrarMensajeFlotante("Contacto agregado: " + nuevoContacto.getNombre(), Color.GREEN);
        } catch (ContactoRepetidoException e) {
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
        }


    }

    /**
     * Muestra un mensaje flotante en la interfaz de usuario.
     * @param texto el texto del mensaje
     * @param fondo el color de fondo del mensaje
     */
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

    /**
     * Recibe un mensaje y lo agrega a la conversación correspondiente.
     * @param mensaje el mensaje recibido
     */
    public void recibirMensaje(Mensaje mensaje){
        this.conversacionServicio.addMensajeEntrante(mensaje);
        String fechaFormateada = sdf.format(mensaje.getFecha());

        //Creo un chat para el mensaje si no existe opero
        ChatPantalla chatPantalla = new ChatPantalla(mensaje.getEmisor());

        if(!vista.getModeloChats().contains(chatPantalla)){
            // Si el contacto no existe en la lista de contactos, lo agrego
            if(!vista.getModeloContactos().contains(chatPantalla.getContacto())){
                vista.getModeloChats().addElement(chatPantalla);
                vista.getModeloContactos().addElement(mensaje.getEmisor());
            }else {
                vista.getModeloChats().addElement(new ChatPantalla(this.agendaServicio.buscaNombreContacto(mensaje.getEmisor())));
            }
        }

        if(vista.getListaChats().getSelectedValue() != null
                && mensaje.getEmisor().equals(vista.getListaChats().getSelectedValue().getContacto())) {
            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    false,
                    fechaFormateada));

        }else{
            // Notificar visualmente que hay un nuevo mensaje
            int index = vista.getModeloChats().indexOf(chatPantalla);
            if (index >= 0) {
                ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
                chatConNotificacion.setPendiente();
                vista.getModeloChats().set(index, chatConNotificacion);
            }
        }
    }

    /**
     * Actualiza la vista con el nuevo mensaje recibido.
     * @param o el objeto observable
     * @param arg el argumento pasado al método notifyObservers
     */
    @Override
    public void update(Observable o, Object arg) {
        Mensaje mensaje = (Mensaje) arg;
        recibirMensaje(mensaje);
    }

    /**
     * Establece la vista de inicio de sesión.
     * @param vistaInicioSesion la vista de inicio de sesión
     */
    public void setVistaInicioSesion(IVistaInicioSesion vistaInicioSesion) {
        this.vistaInicioSesion = vistaInicioSesion;
    }

    /**
     * Establece la vista principal.
     * @param vista la vista principal
     */
    public void setVistaPrincipal(IVistaPrincipal vista) {
        this.vista = vista;

    }

    /**
     * Carga la conversación seleccionada en la vista.
     * @param selectedValue el chat seleccionado
     */
    public void cargarConversacion(ChatPantalla selectedValue) {
        int index = vista.getModeloChats().indexOf(selectedValue);

        if (index >= 0) {
            ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
            chatConNotificacion.setLeido();
            vista.getModeloChats().set(index, chatConNotificacion);
            ArrayList<Mensaje> mensajes = (ArrayList<Mensaje>) this.conversacionServicio.getMensajes(chatConNotificacion.getContacto());
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


}