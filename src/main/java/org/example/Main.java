package org.example;

import org.example.controlador.Controlador;
import org.example.sistema.Sistema;
import org.example.usuario.Usuario;
import org.example.vista.Vista;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Controlador c =new Controlador(new Vista());


    }
}