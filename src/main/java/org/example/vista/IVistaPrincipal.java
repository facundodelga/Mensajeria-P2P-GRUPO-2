package org.example.vista;

import org.example.modelo.usuario.UsuarioDTO;

import javax.swing.*;

public interface IVistaPrincipal {

    JTextField getCampoBusqueda();
    JTextField getCampoMensaje();
    JButton getBotonEnviar();
    JList<String> getListaChats();
    DefaultListModel<String> getModeloChats();
    JLabel getEtiquetaContacto();
    JPanel getPanelMensajes();
    JPanel getPanelChatActual();
    JScrollPane getScrollMensajes();
    JButton getBotonChats();
    JButton getBotonContactos();
    UsuarioDTO mostrarAgregarContacto();

    void setAccionAgregarContacto(Runnable accion);

    void mostrar();
}