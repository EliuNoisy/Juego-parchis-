/**
 * Capa de control del patron MVC
 * Coordina la interaccion entre modelo y vista
 * Implementa la logica del Caso de Uso: Ejercer Turno
 */
package controlador;

import modelo.*;
import vista.PantallaPartida;
import java.util.List;
import java.util.Scanner;

public class ControladorPartida {
    private Partida partida;
    private PantallaPartida vista;
    private Scanner scanner;
    private Ficha ultimaFichaMovida;
    
    /**
     * Constructor del controlador
     * @param partida Modelo de partida
     * @param vista Vista de presentacion
     * @param scanner Scanner para entrada de usuario
     */
    public ControladorPartida(Partida partida, PantallaPartida vista, Scanner scanner) {
        this.partida = partida;
        this.vista = vista;
        this.scanner = scanner;
        this.ultimaFichaMovida = null;
    }
    
    /**
     * Inicia el turno de un jugador
     * Muestra informacion y permite lanzar dado
     */
    public void iniciarTurno() {
        Jugador jugadorActual = partida.getTurnoActual();
        vista.mostrarTurnoActual(jugadorActual);
        
        vista.mostrarMensaje("Presiona ENTER para lanzar el dado");
        scanner.nextLine();
        
        lanzarDado();
    }
    
/**
     * Ejecuta el lanzamiento de dado y logica de movimiento
     * Permite seleccionar ficha y aplicar reglas
     */
    public void lanzarDado() {
        Jugador jugadorActual = partida.getTurnoActual();
        Dado dado = partida.getDado();
        
        int valorDado = dado.lanzar();
        vista.mostrarResultadoDado(valorDado);
        
        List<Ficha> fichasDisponibles = jugadorActual.getFichasDisponibles(valorDado);
        
        if (fichasDisponibles.isEmpty()) {
            vista.mostrarMensaje("No tienes fichas disponibles para mover Pierdes el turno");
            aplicarReglasDelTurno(valorDado);
            return;
        }
        
        vista.mostrarMensaje("Fichas disponibles para mover:");
        for (int i = 0; i < fichasDisponibles.size(); i++) {
            Ficha f = fichasDisponibles.get(i);
            String estado = f.isEnCasa() ? "En casa (saldra a casilla de salida)" : 
                           "Posicion actual: " + f.getPosicion();
            System.out.println((i + 1) + " Ficha " + f.getIdFicha() + " - " + estado);
        }
        
        int seleccion = solicitarSeleccionFicha(fichasDisponibles.size());
        Ficha fichaSeleccionada = fichasDisponibles.get(seleccion - 1);
        
        moverFicha(fichaSeleccionada, valorDado);
        aplicarReglasDelTurno(valorDado);
    }
    
    /**
     * Solicita al usuario seleccionar una ficha
     * Valida que la entrada sea correcta
     * @param maxOpciones Numero maximo de opciones validas
     * @return Indice de la ficha seleccionada (1-maxOpciones)
     */
    private int solicitarSeleccionFicha(int maxOpciones) {
        int seleccion = -1;
        while (seleccion < 1 || seleccion > maxOpciones) {
            System.out.print("\nSelecciona una ficha (1-" + maxOpciones + "): ");
            try {
                seleccion = scanner.nextInt();
                scanner.nextLine();
                if (seleccion < 1 || seleccion > maxOpciones) {
                    System.out.println("Opcion invalida Intenta de nuevo");
                }
            } catch (Exception e) {
                System.out.println("Entrada invalida Ingresa un numero");
                scanner.nextLine();
            }
        }
        return seleccion;
    }
    
    /**
     * Mueve una ficha en el tablero
     * Maneja sacar ficha de casa y movimientos normales
     * @param ficha Ficha a mover
     * @param pasos Numero de casillas a avanzar
     */
    public void moverFicha(Ficha ficha, int pasos) {
        Jugador jugadorActual = partida.getTurnoActual();
        Tablero tablero = partida.getTablero();
        ReglasJuego reglas = partida.getReglas();
        
        // REGLA: Sacar ficha con 5
        if (ficha.isEnCasa() && reglas.verificarSacarFichaConCinco(pasos)) {
            ficha.setEnCasa(false);
            int posicionSalida = obtenerPosicionSalida(jugadorActual.getColor());
            ficha.setPosicion(posicionSalida);
            
            Casilla casillaSalida = tablero.getCasilla(posicionSalida);
            casillaSalida.agregarFicha(ficha);
            
            vista.mostrarMensaje("Ficha sacada de casa a la posicion " + posicionSalida);
            ultimaFichaMovida = ficha;
            
        } else if (!ficha.isEnCasa()) {
            // Mover ficha normal
            tablero.moverFicha(ficha, pasos);
            vista.actualizarFicha(ficha);
            ultimaFichaMovida = ficha;
        }
        
        aplicarReglasDelJuego(ficha);
    }
    
    /**
     * Obtiene la posicion de salida segun el color del jugador
     * Amarillo: 5, Azul: 22, Rojo: 39, Verde: 56
     * @param color Color del jugador
     * @return Numero de casilla de salida
     */
    private int obtenerPosicionSalida(String color) {
        switch (color.toLowerCase()) {
            case "amarillo": return 5;
            case "azul": return 22;
            case "rojo": return 39;
            case "verde": return 56;
            default: return 0;
        }
    }
    
    /**
     * Aplica las reglas del juego despues de un movimiento
     * Delega en el modelo de reglas
     * @param ficha Ficha que se movio
     */
    public void aplicarReglasDelJuego(Ficha ficha) {
        ReglasJuego reglas = partida.getReglas();
        Tablero tablero = partida.getTablero();
        Jugador jugadorActual = partida.getTurnoActual();
        
        reglas.aplicar(jugadorActual, ficha, tablero);
    }
    
    /**
     * Aplica reglas del turno: turno extra o cambio de jugador
     * Maneja penalizacion de tres 6 seguidos
     * @param valorDado Valor obtenido en el dado
     */
    private void aplicarReglasDelTurno(int valorDado) {
        ReglasJuego reglas = partida.getReglas();
        
        // REGLA: Turno extra con 6
        if (reglas.verificarTurnoExtra(valorDado)) {
            partida.incrementarContadorSeis();
            
            // REGLA: Tres 6 seguidos - penalizacion
            if (reglas.verificarTresSeisSeguidos(partida.getContadorSeis())) {
                vista.mostrarMensaje("TRES 6 SEGUIDOS La ultima ficha movida regresa a casa");
                if (ultimaFichaMovida != null && !ultimaFichaMovida.isEnMeta()) {
                    ultimaFichaMovida.regresarACasa();
                    Tablero tablero = partida.getTablero();
                    Casilla casilla = tablero.getCasilla(ultimaFichaMovida.getPosicion());
                    if (casilla != null) {
                        casilla.removerFicha(ultimaFichaMovida);
                    }
                }
                partida.reiniciarContadorSeis();
                partida.cambiarTurno();
            } else {
                vista.mostrarMensaje("Sacaste 6 Tienes un turno extra");
                vista.mostrarMensaje("Presiona ENTER para continuar");
                scanner.nextLine();
                iniciarTurno();
            }
        } else {
            partida.reiniciarContadorSeis();
            partida.cambiarTurno();
        }
    }
    
    /**
     * Verifica si algun jugador gano la partida
     * Condicion: tener las 4 fichas en meta
     * @return true si hay ganador, false si no
     */
    public boolean verificarFinPartida() {
        for (Jugador j : partida.getJugadores()) {
            int fichasEnMeta = 0;
            for (Ficha f : j.getFichas()) {
                if (f.isEnMeta()) {
                    fichasEnMeta++;
                }
            }
            if (fichasEnMeta == 4) {
                vista.mostrarMensaje(j.getNombre().toUpperCase() + " HA GANADO");
                partida.finalizarPartida();
                return true;
            }
        }
        return false;
    }
}