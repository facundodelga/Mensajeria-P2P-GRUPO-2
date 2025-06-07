package org.example.cliente.factory.txt;

import org.grupo10.factory.ILogLlamados;
import org.grupo10.modelo.Turno;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class TxtLlamados implements ILogLlamados {
    @Override
    public void logToFile(Turno turno, int boxNumber, LocalDate date) {
        try (FileWriter writer = new FileWriter("logLlamado.txt", true)) {
            writer.write("Llamada - Cliente: " + turno.getDni() +  ", Box: " + boxNumber + ", Date: " + date.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
