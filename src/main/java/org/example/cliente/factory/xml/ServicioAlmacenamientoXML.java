package org.example.cliente.factory.xml;

import org.example.config.ConfiguracionAplicacion;
import org.example.config.FormatoAlmacenamiento;
import org.example.cliente.factory.IPersistenciaConversacion;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto; // Tu clase Contacto definida

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Implementación de IPersistenciaConversacion para almacenar y cargar objetos Conversacion
 * en formato XML, utilizando SOLO el API DOM de Java (sin librerías XML externas como JAXB).
 *
 * NOTA: Esta implementación es más compleja que JAXB y requiere manejo manual de los nodos XML.
 */
public class ServicioAlmacenamientoXML implements IPersistenciaConversacion {
    private final String RUTA_BASE;
    private final String CONFIG_FILE_NAME = "config.xml";

    public ServicioAlmacenamientoXML() {
        this.RUTA_BASE = ConfiguracionAplicacion.getInstance().getRutaAlmacenamientoBase();
        new File(RUTA_BASE).mkdirs();
    }

    private String getRutaArchivoConversacion(String conversacionId) {
        return RUTA_BASE + "conversacion_" + conversacionId + ".xml";
    }

    private String getRutaArchivoConfiguracion() {
        return RUTA_BASE + CONFIG_FILE_NAME;
    }

    @Override
    public void guardarConversacion(Conversacion conversacion) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // Elemento raíz
        Element rootElement = doc.createElement("conversacion");
        doc.appendChild(rootElement);

        rootElement.setAttribute("id", conversacion.getId());
        rootElement.setAttribute("claveSecretaCodificada", conversacion.getClaveSecretaCodificada());
        rootElement.setAttribute("pendiente", String.valueOf(conversacion.isPendiente()));

        // Participantes
        Element participantesElement = doc.createElement("participantesIds");
        rootElement.appendChild(participantesElement);
        for (String participanteId : conversacion.getParticipantesIds()) {
            Element participanteElement = doc.createElement("participanteId");
            participanteElement.setTextContent(participanteId);
            participantesElement.appendChild(participanteElement);
        }

        // Mensajes
        Element mensajesElement = doc.createElement("mensajes");
        rootElement.appendChild(mensajesElement);
        for (Mensaje msg : conversacion.getMensajes()) {
            Element mensajeElement = doc.createElement("mensaje");
            mensajesElement.appendChild(mensajeElement);

            mensajeElement.setAttribute("id", msg.getId());
            mensajeElement.setAttribute("fecha", String.valueOf(msg.getFecha().getTime()));
            mensajeElement.setAttribute("contenidoCifrado", Base64.getEncoder().encodeToString(msg.getContenidoCifrado()));

            // --- INICIO DE CAMBIOS: Guardar todos los atributos de Contacto ---
            Element emisorElement = doc.createElement("emisor");
            emisorElement.setAttribute("id", msg.getEmisor().getNombre()); // El nombre es el ID
            emisorElement.setAttribute("ip", msg.getEmisor().getIp());
            emisorElement.setAttribute("puerto", String.valueOf(msg.getEmisor().getPuerto()));
            mensajeElement.appendChild(emisorElement);

            Element receptorElement = doc.createElement("receptor");
            receptorElement.setAttribute("id", msg.getReceptor().getNombre()); // El nombre es el ID
            receptorElement.setAttribute("ip", msg.getReceptor().getIp());
            receptorElement.setAttribute("puerto", String.valueOf(msg.getReceptor().getPuerto()));
            mensajeElement.appendChild(receptorElement);
            // --- FIN DE CAMBIOS ---
        }

        // Escribir el contenido en un archivo XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(getRutaArchivoConversacion(conversacion.getId())));
        transformer.transform(source, result);
    }

    @Override
    public Conversacion cargarConversacion(String conversacionId) throws Exception {
        File file = new File(getRutaArchivoConversacion(conversacionId));
        if (!file.exists()) {
            return null;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        Element rootElement = doc.getDocumentElement();
        String id = rootElement.getAttribute("id");
        String claveSecretaCodificada = rootElement.getAttribute("claveSecretaCodificada");
        boolean pendiente = Boolean.parseBoolean(rootElement.getAttribute("pendiente"));

        // Participantes
        List<String> participantesIds = new ArrayList<>();
        NodeList participanteNodes = rootElement.getElementsByTagName("participanteId");
        for (int i = 0; i < participanteNodes.getLength(); i++) {
            Node node = participanteNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                participantesIds.add(node.getTextContent());
            }
        }

        Conversacion conversacion = new Conversacion(id, participantesIds, claveSecretaCodificada);
        conversacion.setPendiente(pendiente);

        // Mensajes
        List<Mensaje> mensajes = new ArrayList<>();
        NodeList mensajeNodes = rootElement.getElementsByTagName("mensaje");
        for (int i = 0; i < mensajeNodes.getLength(); i++) {
            Node node = mensajeNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element mensajeElement = (Element) node;

                String msgId = mensajeElement.getAttribute("id");
                long fechaLong = Long.parseLong(mensajeElement.getAttribute("fecha"));
                Date fecha = new Date(fechaLong);
                byte[] contenidoCifrado = Base64.getDecoder().decode(mensajeElement.getAttribute("contenidoCifrado"));

                // --- INICIO DE CAMBIOS: Cargar todos los atributos de Contacto y usar constructor completo ---
                Element emisorElement = (Element) mensajeElement.getElementsByTagName("emisor").item(0);
                String emisorNombre = emisorElement.getAttribute("id"); // El ID es el nombre
                String emisorIp = emisorElement.getAttribute("ip");
                int emisorPuerto = Integer.parseInt(emisorElement.getAttribute("puerto"));
                Contacto emisor = new Contacto(emisorNombre, emisorIp, emisorPuerto);

                Element receptorElement = (Element) mensajeElement.getElementsByTagName("receptor").item(0);
                String receptorNombre = receptorElement.getAttribute("id"); // El ID es el nombre
                String receptorIp = receptorElement.getAttribute("ip");
                int receptorPuerto = Integer.parseInt(receptorElement.getAttribute("puerto"));
                Contacto receptor = new Contacto(receptorNombre, receptorIp, receptorPuerto);
                // --- FIN DE CAMBIOS ---

                mensajes.add(new Mensaje(msgId, fecha, contenidoCifrado, emisor, receptor));
            }
        }
        conversacion.setMensajes(mensajes);
        return conversacion;
    }

    @Override
    public List<String> listarIdsConversaciones() throws Exception {
        List<String> ids = new ArrayList<>();
        File folder = new File(RUTA_BASE);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().startsWith("conversacion_") && file.getName().endsWith(".xml")) {
                    ids.add(file.getName().replace("conversacion_", "").replace(".xml", ""));
                }
            }
        }
        return ids;
    }

    @Override
    public void guardarConfiguracion(ConfiguracionAplicacion config) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("configuracion");
        doc.appendChild(rootElement);

        rootElement.setAttribute("formatoAlmacenamientoSeleccionado", config.getFormatoAlmacenamientoSeleccionado().name());
        rootElement.setAttribute("rutaAlmacenamientoBase", config.getRutaAlmacenamientoBase());

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(getRutaArchivoConfiguracion()));
        transformer.transform(source, result);
    }

    @Override
    public ConfiguracionAplicacion cargarConfiguracion() throws Exception {
        File file = new File(getRutaArchivoConfiguracion());
        if (!file.exists()) {
            return null;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        Element rootElement = doc.getDocumentElement();
        ConfiguracionAplicacion loadedConfig = ConfiguracionAplicacion.getInstance();

        String formatoStr = rootElement.getAttribute("formatoAlmacenamientoSeleccionado");
        String rutaBase = rootElement.getAttribute("rutaAlmacenamientoBase");

        if (formatoStr != null && !formatoStr.isEmpty()) {
            loadedConfig.setFormatoAlmacenamientoSeleccionado(FormatoAlmacenamiento.valueOf(formatoStr));
        }
        if (rutaBase != null && !rutaBase.isEmpty()) {
            loadedConfig.setRutaAlmacenamientoBase(rutaBase);
        }
        return loadedConfig;
    }
}