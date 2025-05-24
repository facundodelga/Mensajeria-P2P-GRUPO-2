package org.example.cliente.factory.xml;

import org.grupo10.factory.ILogLlamados;
import org.grupo10.modelo.Turno;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class XmlLlamados implements ILogLlamados {
    @Override
    public void logToFile(Turno turno, int boxNumber, LocalDate date) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;

            File file = new File("logLlamado.xml");
            if (file.exists()) {
                doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
            } else {
                doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("logs");
                doc.appendChild(rootElement);
            }

            Element logElement = doc.createElement("log");
            Element typeElement = doc.createElement("type");
            typeElement.appendChild(doc.createTextNode("Call"));
            logElement.appendChild(typeElement);

            Element clienteElement = doc.createElement("cliente");
            clienteElement.appendChild(doc.createTextNode(turno.getDni()));
            logElement.appendChild(clienteElement);

            Element boxElement = doc.createElement("box");
            boxElement.appendChild(doc.createTextNode(Integer.toString(boxNumber)));
            logElement.appendChild(boxElement);

            Element dateElement = doc.createElement("date");
            dateElement.appendChild(doc.createTextNode(date.toString()));
            logElement.appendChild(dateElement);

            doc.getDocumentElement().appendChild(logElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (ParserConfigurationException | IOException | TransformerException | org.xml.sax.SAXException e) {
            e.printStackTrace();
        }
    }
}
