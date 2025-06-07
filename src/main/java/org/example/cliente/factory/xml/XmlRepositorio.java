package org.example.cliente.factory.xml;

import org.grupo10.exception.ClienteNoExistenteException;
import org.grupo10.factory.ILogRepositorio;
import org.grupo10.modelo.Cliente;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class XmlRepositorio implements ILogRepositorio {
    private  String filename;
    File file;
    @Override
    public void readRepo( ) throws FileNotFoundException {
            String currentDir = System.getProperty("user.dir");
        try {
            file = new File(currentDir+"/colasdeespera/src/org/grupo10/sistema_servidor/repo.xml");
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            this.filename = filename;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        }
    }

    @Override
    public Cliente getCliente(String dniTotem) throws ClienteNoExistenteException, FileNotFoundException {
        Cliente retorno = null;
        boolean encontro = false;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("cliente");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element clienteElement = (Element) nodeList.item(i);
                String nombre = clienteElement.getElementsByTagName("nombre").item(0).getTextContent();
                String dni = clienteElement.getElementsByTagName("dni").item(0).getTextContent();
                int prioridad = Integer.parseInt(clienteElement.getElementsByTagName("prioridad").item(0).getTextContent());
                LocalDate fechaNacimiento = LocalDate.parse(clienteElement.getElementsByTagName("fechaNacimiento").item(0).getTextContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (dni.equals(dniTotem)) {
                    encontro = true;
                    retorno = new Cliente(dni, nombre, prioridad, fechaNacimiento);
                    break;
                }
            }

            if (!encontro) {
                throw new ClienteNoExistenteException();
            }

        } catch (ParserConfigurationException | IOException | org.xml.sax.SAXException e) {
            throw new FileNotFoundException("Hubo un error al leer el repositorio de clientes.");
        }
        return retorno;
    }
}
