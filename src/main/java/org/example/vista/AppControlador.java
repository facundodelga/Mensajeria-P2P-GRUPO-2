package Vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class AppControlador {

    private final AppVista vista;
    private final Map<String, List<Mensaje>> mensajesPorContacto = new HashMap<>();
    private final List<String> listaContactos = new ArrayList<>();
    private final List<String> listaChats = new ArrayList<>();
    private String contactoActual = null;
    private boolean mostrandoContactos = false;

    public AppControlador(AppVista vista) {
        this.vista = vista;
        listaContactos.addAll(List.of("Contacto 1", "Contacto 2", "Contacto 3", "Contacto 4"));

        cargarLista(listaChats);

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

        vista.getCampoMensaje().addActionListener(e -> vista.getBotonEnviar().doClick());
        vista.getBotonEnviar().addActionListener(e -> enviarMensaje());

        vista.getCampoBusqueda().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarLista();
            }
        });

        vista.getBotonChats().addActionListener(e -> {
            mostrandoContactos = false;
            cargarLista(listaChats);
            filtrarLista();
        });

        vista.getBotonContactos().addActionListener(e -> {
            mostrandoContactos = true;
            cargarLista(listaContactos);
            filtrarLista();
        });

        // Accion del botón "Agregar contacto"
        vista.setAccionAgregarContacto(this::mostrarDialogoAgregarContacto);
    }

    private void mostrarDialogoAgregarContacto() {
        AgregarContactoDialog dialog = new AgregarContactoDialog(vista);
        dialog.setVisible(true);

        String nombre = dialog.getNombre();
        String ip = dialog.getIP();
        String puerto = dialog.getPuerto();

        if (nombre.isEmpty() || ip.isEmpty() || puerto.isEmpty()) {
        	mostrarMensajeFlotante("<html>Usuario registrado sin éxito:<br>Todos los campos deben completarse correctamente.</html>", new Color(200, 50, 50));

            return;
        }

        if (!listaContactos.contains(nombre)) {
            listaContactos.add(nombre);
            if (mostrandoContactos) {
                cargarLista(listaContactos);
                filtrarLista();
            }

            mostrarMensajeFlotante("Usuario registrado con éxito", new Color(0, 128, 0));
        } else {
            mostrarMensajeFlotante("El contacto ya existe.", new Color(200, 50, 50));
        }
    }
    
    private void mostrarMensajeFlotante(String texto, Color fondo) {
        JDialog mensaje = new JDialog(vista, false);
        mensaje.setUndecorated(true);
        mensaje.getContentPane().setBackground(fondo);

        JLabel label = new JLabel("<html><div style='text-align: center;'>" + texto + "</div></html>", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // padding interno
        mensaje.getContentPane().add(label);

        mensaje.pack(); // ajusta automaticamente al contenido
        mensaje.setLocationRelativeTo(vista);
        mensaje.setAlwaysOnTop(true);
        mensaje.setVisible(true);

        new Timer(2000, e -> mensaje.dispose()).start();
    }

    private void enviarMensaje() {
        if (contactoActual == null) return;

        String texto = vista.getCampoMensaje().getText().trim();
        if (!texto.isEmpty()) {
            mensajesPorContacto.putIfAbsent(contactoActual, new ArrayList<>());
            mensajesPorContacto.get(contactoActual).add(new Mensaje(texto, true));
            vista.getCampoMensaje().setText("");

            if (!listaChats.contains(contactoActual)) {
                listaChats.add(contactoActual);
                if (!mostrandoContactos) cargarLista(listaChats);
            }

            mostrarMensajes();

            // Simula una respuesta
            Timer timer = new Timer(1000, e -> {
                mensajesPorContacto.get(contactoActual).add(new Mensaje("Recibido 👍", false));
                mostrarMensajes();
            });
            timer.setRepeats(false);
            timer.start();
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
        List<String> fuente = mostrandoContactos ? listaContactos : listaChats;

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
}
