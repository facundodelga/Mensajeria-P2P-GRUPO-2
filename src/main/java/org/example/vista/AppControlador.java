package org.example.vista;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

public class AppControlador {

    private AppVista vista;
    private static Map<String, List<Mensaje>> mensajesPorContacto = new HashMap<>();
    private List<String> listaContactos = new ArrayList<>();
    private String contactoActual = null;
    private boolean mostrandoContactos = false;

    public AppControlador(AppVista vista) {
        this.vista = vista;

        // Datos iniciales de ejemplo
        listaContactos.add("Contacto 1");
        listaContactos.add("Contacto 2");
        listaContactos.add("Contacto 3");
        listaContactos.add("Contacto 4");
        listaContactos.add("Contacto 5");

        cargarListaChats();

        // Eventos de selección de lista
        vista.getListaChats().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                contactoActual = vista.getListaChats().getSelectedValue();
                if (contactoActual != null) {
                    vista.getPanelChatActual().setVisible(true);
                    vista.getEtiquetaContacto().setText(contactoActual);
                    mostrarMensajes();
                }
            }
        });

        vista.getBotonEnviar().addActionListener(e -> enviarMensaje());
        vista.getCampoMensaje().addActionListener(e -> vista.getBotonEnviar().doClick());

        vista.getCampoBusqueda().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarLista();
            }
        });

        vista.getBotonChats().addActionListener(e -> {
            mostrandoContactos = false;
            cargarListaChats();
        });

        vista.getBotonContactos().addActionListener(e -> {
            mostrandoContactos = true;
            cargarLista(listaContactos);
        });
    }

    private void enviarMensaje() {
        if (contactoActual == null) return;

        String texto = vista.getCampoMensaje().getText().trim();
        if (!texto.isEmpty()) {
            mensajesPorContacto.putIfAbsent(contactoActual, new ArrayList<>());
            mensajesPorContacto.get(contactoActual).add(new Mensaje(texto, true));
            vista.getCampoMensaje().setText("");
            mostrarMensajes();

            if (!listaContactos.contains(contactoActual)) {
                listaContactos.add(contactoActual);
            }
            if (!obtenerListaChats().contains(contactoActual)) {
                cargarListaChats();
            }

            // Simular respuesta
            javax.swing.Timer respuestaTimer = new javax.swing.Timer(1000, evt -> {
                mensajesPorContacto.putIfAbsent(contactoActual, new ArrayList<>());
                mensajesPorContacto.get(contactoActual).add(new Mensaje("Recibido 👍", false));
                mostrarMensajes();
            });
            respuestaTimer.setRepeats(false);
            respuestaTimer.start();
        }
    }

    private void mostrarMensajes() {
        JPanel panelMensajes = vista.getPanelMensajes();
        panelMensajes.removeAll();

        List<Mensaje> mensajes = mensajesPorContacto.getOrDefault(contactoActual, new ArrayList<>());
        for (Mensaje msg : mensajes) {
            MensajeBubble burbuja = new MensajeBubble(msg);

            JPanel alineador = new JPanel();
            alineador.setLayout(new BoxLayout(alineador, BoxLayout.X_AXIS));
            alineador.setOpaque(false);

            if (msg.esMio()) {
                alineador.add(Box.createHorizontalGlue());
                alineador.add(burbuja);
            } else {
                alineador.add(burbuja);
                alineador.add(Box.createHorizontalGlue());
            }

            alineador.setMaximumSize(new Dimension(Integer.MAX_VALUE, burbuja.getPreferredSize().height + 10));
            panelMensajes.add(Box.createVerticalStrut(5));
            panelMensajes.add(alineador);
        }

        panelMensajes.revalidate();
        panelMensajes.repaint();

        JScrollBar barra = vista.getScrollMensajes().getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> barra.setValue(barra.getMaximum()));
    }

    private void filtrarLista() {
        String filtro = vista.getCampoBusqueda().getText().toLowerCase();
        DefaultListModel<String> modelo = new DefaultListModel<>();
        List<String> fuente = mostrandoContactos ? listaContactos : obtenerListaChats();

        for (String nombre : fuente) {
            if (nombre.toLowerCase().contains(filtro)) {
                modelo.addElement(nombre);
            }
        }

        vista.getListaChats().setModel(modelo);
    }

    private void cargarLista(List<String> fuente) {
        DefaultListModel<String> modelo = new DefaultListModel<>();
        for (String nombre : fuente) {
            modelo.addElement(nombre);
        }
        vista.getListaChats().setModel(modelo);
    }

    private void cargarListaChats() {
        List<String> conMensajes = obtenerListaChats();
        cargarLista(conMensajes);
    }

    private List<String> obtenerListaChats() {
        List<String> chatsConMensajes = new ArrayList<>();
        for (String contacto : listaContactos) {
            List<Mensaje> mensajes = mensajesPorContacto.get(contacto);
            if (mensajes != null && !mensajes.isEmpty()) {
                chatsConMensajes.add(contacto);
            }
        }
        return chatsConMensajes;
    }

    public static void borrarMensajesDeContacto(String contacto) {
        mensajesPorContacto.remove(contacto);
    }
}


