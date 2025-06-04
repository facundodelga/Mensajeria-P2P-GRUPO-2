//package org.example.cliente.factory;
//
//
//import org.grupo10.modelo.Turno;
//
//import java.time.LocalDate;
//
//
//public class Main {
//    public static void main(String[] args) {
//        FactorySelector logCreator=new FactorySelector("xml");
//
//        Turno turno=new Turno("1180160688");
//        LocalDate now = LocalDate.now();
//
//        ILogRegistro registrio=logCreator.logClientRegistro();
//        registrio.logToFile(turno, now);
//
//        IPersistenciaConversacion llamados= logCreator.logClientLlamado();
//        llamados.logToFile(turno, 1, now);
//
//        turno=new Turno("345232534");
//        registrio.logToFile(turno, now);
//        llamados.logToFile(turno, 2, now);
//
//    }
//}