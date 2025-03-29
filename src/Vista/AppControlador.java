package Vista;

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
    private String contactoActual = null;

    public AppControlador(AppVista vista) {
        this.vista = vista;

        // Datos iniciales de ejemplo
        DefaultListModel<String> modelo = vista.getModeloChats();
        modelo.addElement("Contacto 1");
        modelo.addElement("Contacto 2");
        modelo.addElement("Contacto 3");

        // Eventos
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
                filtrarChats();
            }
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

            // Simular respuesta
            javax.swing.Timer respuestaTimer = new javax.swing.Timer(1000, evt -> {
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

        // Scroll automático al final
        JScrollBar barra = vista.getScrollMensajes().getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> barra.setValue(barra.getMaximum()));
    }

    private void filtrarChats() {
        String filtro = vista.getCampoBusqueda().getText().toLowerCase();
        DefaultListModel<String> modeloOriginal = vista.getModeloChats();
        DefaultListModel<String> modeloFiltrado = new DefaultListModel<>();

        for (int i = 0; i < modeloOriginal.getSize(); i++) {
            String nombre = modeloOriginal.getElementAt(i);
            if (nombre.toLowerCase().contains(filtro)) {
                modeloFiltrado.addElement(nombre);
            }
        }

        vista.getListaChats().setModel(modeloFiltrado);
    }

    public static void borrarMensajesDeContacto(String contacto) {
        if (mensajesPorContacto.containsKey(contacto)) {
            mensajesPorContacto.remove(contacto);
        }
    }
}



