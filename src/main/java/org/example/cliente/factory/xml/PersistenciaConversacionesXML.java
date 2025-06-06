package org.example.cliente.factory.xml;

import org.example.cliente.factory.IPersistenciaConversaciones;
import org.example.cliente.modelo.IAgenda;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.*;
import java.util.*;

public class PersistenciaConversacionesXML implements IPersistenciaConversaciones {

    private static final String BASE_DIR = "conversaciones/";
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Contacto usuarioDTO;

    public PersistenciaConversacionesXML(Contacto usuarioDTO) {
        this.usuarioDTO = usuarioDTO;
    }

    @Override
    public void guardarConversaciones(Map<Contacto, Conversacion> conversaciones) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("conversaciones");
            doc.appendChild(root);

            for (Map.Entry<Contacto, Conversacion> entry : conversaciones.entrySet()) {
                Contacto contacto = entry.getKey();
                Conversacion conv = entry.getValue();

                Element conversacionElem = doc.createElement("conversacion");
                conversacionElem.setAttribute("usuario", contacto.getNombre());
                root.appendChild(conversacionElem);

                Element mensajesElem = doc.createElement("mensajes");
                conversacionElem.appendChild(mensajesElem);

                for (Mensaje mensaje : conv.getMensajes()) {
                    boolean enviado = mensaje.getEmisor().getNombre().equalsIgnoreCase(usuarioDTO.getNombre());

                    Element mensajeElem = doc.createElement("mensaje");
                    mensajeElem.setAttribute("tipo", enviado ? "E" : "R");
                    mensajeElem.setAttribute("fecha", FORMATO_FECHA.format(mensaje.getFecha()));
                    mensajeElem.setTextContent(mensaje.getContenido());

                    mensajesElem.appendChild(mensajeElem);
                }
            }

            File carpeta = new File(BASE_DIR);
            if (!carpeta.exists()) carpeta.mkdirs();

            File archivo = new File(carpeta, usuarioDTO.getNombre() + ".xml");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(archivo));

            System.out.println("Conversaciones guardadas en XML para " + usuarioDTO.getNombre());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Contacto, Conversacion> cargarConversaciones(IAgenda agendaServicio) {
        Map<Contacto, Conversacion> conversaciones = new HashMap<>();
        File archivo = new File(BASE_DIR + usuarioDTO.getNombre() + ".xml");

        if (!archivo.exists()) return conversaciones;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(archivo);

            NodeList conversacionNodes = doc.getElementsByTagName("conversacion");

            for (int i = 0; i < conversacionNodes.getLength(); i++) {
                Element conversacionElem = (Element) conversacionNodes.item(i);
                String nombreContacto = conversacionElem.getAttribute("usuario");

                Contacto contacto = agendaServicio.buscaNombreContacto(nombreContacto);
                if (contacto == null) {
                    continue;
                }

                List<Mensaje> mensajes = new ArrayList<>();
                NodeList mensajeNodes = conversacionElem.getElementsByTagName("mensaje");

                for (int j = 0; j < mensajeNodes.getLength(); j++) {
                    Element mensajeElem = (Element) mensajeNodes.item(j);

                    String tipo = mensajeElem.getAttribute("tipo");
                    Date fecha = FORMATO_FECHA.parse(mensajeElem.getAttribute("fecha"));
                    String contenido = mensajeElem.getTextContent();

                    Contacto emisor = tipo.equals("E") ? usuarioDTO : contacto;
                    Contacto receptor = tipo.equals("E") ? contacto : usuarioDTO;

                    mensajes.add(new Mensaje(fecha, contenido, emisor, receptor));
                }

                conversaciones.put(contacto, new Conversacion(mensajes));
            }

            System.out.println("Conversaciones cargadas desde XML para " + usuarioDTO.getNombre());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return conversaciones;
    }
}
