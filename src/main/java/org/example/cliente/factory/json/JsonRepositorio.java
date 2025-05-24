package org.example.cliente.factory.json;

import org.grupo10.exception.ClienteNoExistenteException;
import org.grupo10.factory.ILogRepositorio;
import org.grupo10.modelo.Cliente;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class JsonRepositorio implements ILogRepositorio {
    private String filename;

    @Override
    public void readRepo( ) throws FileNotFoundException {
        String currentDir = System.getProperty("user.dir");
        try {
            FileReader fr = new FileReader(currentDir+"/colasdeespera/src/org/grupo10/sistema_servidor/repo.json");
            fr.close();
            this.filename = currentDir+"/colasdeespera/src/org/grupo10/sistema_servidor/repo.json";
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cliente getCliente(String dniTotem) throws ClienteNoExistenteException, FileNotFoundException {
        Cliente retorno = null;
        boolean encontro = false;

        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(this.filename)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray jsonArray = (JSONArray) jsonObject.get("clientes");

            for (Object obj : jsonArray) {
                JSONObject clienteJSON = (JSONObject) obj;
                String nombre = (String) clienteJSON.get("nombre");
                String dni = (String) clienteJSON.get("dni");
                int prioridad = ((Long) clienteJSON.get("prioridad")).intValue();
                LocalDate fechaNacimiento = LocalDate.parse((String) clienteJSON.get("fechaNacimiento"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (dni.equals(dniTotem)) {
                    encontro = true;
                    retorno = new Cliente(dni, nombre, prioridad, fechaNacimiento);
                    break;
                }
            }

            if (!encontro) {
                throw new ClienteNoExistenteException();
            }

        } catch (IOException | ParseException e) {
            throw new FileNotFoundException("Hubo un error al leer el repositorio de clientes.");
        }
        return retorno;
    }
}
