package org.example.cliente.factory.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.example.cliente.factory.IPersistenciaAgenda;
import org.example.cliente.modelo.usuario.Contacto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PersistenciaAgendaXML implements IPersistenciaAgenda {

    private static final String BASE_DIR = "agendas/";
    private final Contacto usuarioDTO;

    public PersistenciaAgendaXML(Contacto usuarioDTO) {
        this.usuarioDTO = usuarioDTO;
    }

    @Override
    public void guardarAgenda(List<Contacto> contactos) {
        File carpeta = new File(BASE_DIR);
        if (!carpeta.exists()) carpeta.mkdirs();

        File archivo = new File(carpeta, usuarioDTO.getNombre() + "_agenda.xml");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            // Nodo raíz
            Element root = doc.createElement("Agenda");
            doc.appendChild(root);

            for (Contacto c : contactos) {
                Element contactoElem = doc.createElement("Contacto");

                Element nombreElem = doc.createElement("Nombre");
                nombreElem.appendChild(doc.createTextNode(c.getNombre()));
                contactoElem.appendChild(nombreElem);

                Element ipElem = doc.createElement("IP");
                ipElem.appendChild(doc.createTextNode(c.getIp()));
                contactoElem.appendChild(ipElem);

                Element puertoElem = doc.createElement("Puerto");
                puertoElem.appendChild(doc.createTextNode(String.valueOf(c.getPuerto())));
                contactoElem.appendChild(puertoElem);

                root.appendChild(contactoElem);
            }

            // Transformar DOM a archivo XML
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource domSource = new DOMSource(doc);
            StreamResult sr = new StreamResult(archivo);

            transformer.transform(domSource, sr);

        } catch (Exception e) {
            System.err.println("Error al guardar la agenda XML: " + e.getMessage());
        }
    }

    @Override
    public List<Contacto> cargarAgenda() {
        List<Contacto> contactos = new ArrayList<>();
        File archivo = new File(BASE_DIR + usuarioDTO.getNombre() + "_agenda.xml");

        if (!archivo.exists()) return contactos;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(archivo);
            doc.getDocumentElement().normalize();

            NodeList listaContactos = doc.getElementsByTagName("Contacto");

            for (int i = 0; i < listaContactos.getLength(); i++) {
                Node node = listaContactos.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    String nombre = elem.getElementsByTagName("Nombre").item(0).getTextContent();
                    String ip = elem.getElementsByTagName("IP").item(0).getTextContent();
                    int puerto = Integer.parseInt(elem.getElementsByTagName("Puerto").item(0).getTextContent());

                    contactos.add(new Contacto(nombre, ip, puerto));
                }
            }

        } catch (Exception e) {
            System.err.println("Error al cargar la agenda XML: " + e.getMessage());
        }

        return contactos;
    }
}
