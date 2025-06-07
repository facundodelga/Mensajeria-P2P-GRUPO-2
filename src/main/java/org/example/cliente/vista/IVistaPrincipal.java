package org.example.cliente.vista;

import org.example.cliente.modelo.usuario.Contacto;

import javax.swing.*;
import java.awt.event.WindowAdapter;

public interface IVistaPrincipal {

    /**
     * Muestra un diálogo de confirmación para cerrar sesión.
     * @return true si el usuario confirma que quiere cerrar sesión, false en caso contrario
     */
    public boolean mostrarConfirmacionCerrarSesion() ;

    JTextField getCampoMensaje();
    JButton getBotonEnviar();
    JList<ChatPantalla> getListaChats();
    DefaultListModel<ChatPantalla> getModeloChats();
    JLabel getEtiquetaContacto();
    JPanel getPanelMensajes();
    JPanel getPanelChatActual();
    JScrollPane getScrollMensajes();
    Contacto mostrarAgregarContacto();
    public JList<Contacto> getListaContactos();
    public DefaultListModel<Contacto> getModeloContactos();
    boolean mostrarDialogoReconexion();
    boolean mostrarDialogoReintentarConexion();
    void cerrarDialogoReconexion();

    void setAccionAgregarContacto(Runnable accion);
    public void addMensajeBurbuja(MensajePantalla mensaje);
    public void titulo(String texto);
    void mostrar();
    void ocultar();
    void limpiarCampos();

    void informacionDelUsuario(Contacto contacto);

    void addWindowListener(WindowAdapter windowAdapter);


}