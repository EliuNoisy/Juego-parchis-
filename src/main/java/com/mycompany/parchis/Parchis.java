/**
 * Clase principal del juego Parchis Star
 * Implementa el Caso de Uso 6: Ejercer Turno
 * Version simplificada para 2 jugadores en consola
 */
package com.mycompany.parchis;

import modelo.*;
import vista.PantallaPartida;
import controlador.ControladorPartida;
import java.util.Scanner;

public class Parchis {
    /**
     * Metodo principal que inicia el juego
     * Configura 2 jugadores y ejecuta el ciclo de turnos
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PantallaPartida vista = new PantallaPartida();
        
        // Pantalla de bienvenida
        System.out.println("\n============================================================");
        System.out.println("|                                                          |");
        System.out.println("|            * PARCHIS STAR *                              |");
        System.out.println("|            Version Consola                               |");
        System.out.println("|                                                          |");
        System.out.println("============================================================");
        
        // Crear partida
        Partida partida = new Partida(1);
        
        System.out.println("=== CONFIGURACION DE JUGADORES ===\n");
        
        // Configurar Jugador 1
        System.out.print("Ingresa el nombre del Jugador 1: ");
        String nombre1 = scanner.nextLine();
        Jugador jugador1 = new Jugador(1, nombre1, "Amarillo");
        partida.agregarJugador(jugador1);
        
        // Configurar Jugador 2
        System.out.print("Ingresa el nombre del Jugador 2: ");
        String nombre2 = scanner.nextLine();
        Jugador jugador2 = new Jugador(2, nombre2, "Azul");
        partida.agregarJugador(jugador2);
        
        // Iniciar partida
        partida.iniciarPartida();
        
        // Crear controlador MVC
        ControladorPartida controlador = new ControladorPartida(partida, vista, scanner);
        
        // Variables de control del ciclo de juego
        boolean juegoActivo = true;
        int turnosJugados = 0;
        final int MAX_TURNOS = 100;
        
        System.out.println("\nPresiona ENTER para comenzar");
        scanner.nextLine();
        
        // Ciclo principal del juego
        while (juegoActivo && turnosJugados < MAX_TURNOS) {
            vista.mostrarTablero(partida);
            controlador.iniciarTurno();
            
            if (controlador.verificarFinPartida()) {
                juegoActivo = false;
            }
            
            turnosJugados++;
        }
        
        // Finalizar por limite de turnos
        if (turnosJugados >= MAX_TURNOS) {
            System.out.println("\n=== JUEGO FINALIZADO (Limite de turnos) ===");
            partida.finalizarPartida();
        }
        
        scanner.close();
        System.out.println("\nGracias por jugar Parchis Star");
    }
}