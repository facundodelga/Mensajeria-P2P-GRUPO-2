package org.example.vista;

import org.example.modelo.usuario.UsuarioDTO;

import javax.swing.*;
import java.awt.event.WindowAdapter;

public interface IVistaPrincipal {

    JTextField getCampoBusqueda();
    JTextField getCampoMensaje();
    JButton getBotonEnviar();
    JList<ChatPantalla> getListaChats();
    DefaultListModel<ChatPantalla> getModeloChats();
    JLabel getEtiquetaContacto();
    JPanel getPanelMensajes();
    JPanel getPanelChatActual();
    JScrollPane getScrollMensajes();
    UsuarioDTO mostrarAgregarContacto();
    public JList<UsuarioDTO> getListaContactos();
    public DefaultListModel<UsuarioDTO> getModeloContactos();

    void setAccionAgregarContacto(Runnable accion);
    public void addMensajeBurbuja(MensajePantalla mensaje);
    public void titulo(String texto);
    void mostrar();

    void addWindowListener(WindowAdapter windowAdapter);
}