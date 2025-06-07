package org.example.cliente.factory.txt;

import org.grupo10.exception.ClienteNoExistenteException;
import org.grupo10.factory.ILogRepositorio;
import org.grupo10.modelo.Cliente;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TxtRepositorio implements ILogRepositorio {
    private String filename;
    FileReader file;
    @Override
    public void readRepo() throws FileNotFoundException {
        String currentDir = System.getProperty("user.dir");
        try {
            file = new FileReader(currentDir+"/colasdeespera/src/org/grupo10/sistema_servidor/repo.txt");
            file.close();
            this.filename = currentDir+"/colasdeespera/src/org/grupo10/sistema_servidor/repo.txt";
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

        try (FileReader fr = new FileReader(this.filename);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            while ((line = br.readLine()) != null) {
                String nombre = line; // Lee el nombre
                if ((line = br.readLine()) != null) {
                    String[] datos = line.split(" "); // Lee la línea con el DNI y otros datos
                    String dni = datos[0];
                    int prioridad = Integer.parseInt(datos[1]);
                    LocalDate fechaNacimiento = LocalDate.parse(datos[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    if (dni.equals(dniTotem)) {
                        System.out.println();
                        encontro = true;
                        retorno = new Cliente(dni, nombre, prioridad, fechaNacimiento);
                        // Salir del bucle si el cliente se encuentra
                    }
                }
            }

            if (!encontro) {
                throw new ClienteNoExistenteException();
            }

        } catch (IOException e) {
            throw new FileNotFoundException("Hubo un error al leer el repositorio de clientes.");
        }
        return retorno;
    }
}
