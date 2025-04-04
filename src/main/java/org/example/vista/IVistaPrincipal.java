package org.example.vista;

import org.example.modelo.usuario.UsuarioDTO;

import javax.swing.*;

public interface IVistaPrincipal {

    JTextField getCampoBusqueda();
    JTextField getCampoMensaje();
    JButton getBotonEnviar();
    JList<UsuarioDTO> getListaChats();
    DefaultListModel<UsuarioDTO> getModeloChats();
    JLabel getEtiquetaContacto();
    JPanel getPanelMensajes();
    JPanel getPanelChatActual();
    JScrollPane getScrollMensajes();
    UsuarioDTO mostrarAgregarContacto();
    public JList<UsuarioDTO> getListaContactos();
    public DefaultListModel<UsuarioDTO> getModeloContactos();

    void setAccionAgregarContacto(Runnable accion);

    void mostrar();
}