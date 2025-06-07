package org.example.cliente.factory.json;


import org.example.cliente.factory.ILogRepositorio;
import org.example.cliente.modelo.usuario.Usuario;
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

    public Usuario getCliente(String nombreTotem) {
        Usuario retorno = null;
        boolean encontro = false;

        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(this.filename)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray jsonArray = (JSONArray) jsonObject.get("clientes");

            for (Object obj : jsonArray) {
                JSONObject clienteJSON = (JSONObject) obj;
                String nombre = (String) clienteJSON.get("nombre");
                String ip = (String) clienteJSON.get("ip");
                String puerto = (String) clienteJSON.get("puerto");

                if (nombre.equals(nombreTotem)) {
                    encontro = true;
                    retorno = new Usuario();
                    break;
                }
            }
            return retorno;
        }
    }
}
