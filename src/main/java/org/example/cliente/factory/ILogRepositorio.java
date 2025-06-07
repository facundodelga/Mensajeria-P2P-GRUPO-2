package org.example.cliente.factory;

import org.example.cliente.modelo.usuario.Usuario; // ¡Importante! Usar Usuario
import java.io.FileNotFoundException;

public interface ILogRepositorio {
    void readRepo() throws FileNotFoundException;
    Usuario getUsuario(String dniTotem); // Cambiado para Usuario
}