package org.example.vista;

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

    void setAccionAgregarContacto(Runnable accion);
}