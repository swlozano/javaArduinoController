/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unimayor.enterprise.server.arduino.controller;

import java.util.ArrayList;
import org.unimayor.enterprise.server.arduino.data.duino.JuegoSerial;

/**
 *
 * @author alejandro
 */
public class GameControl {

    private int[] numerosGenerados = new int[10];
    public static int filas = 4;
    public static int cols = 3;
    private int contadorTableros = 0;
    private ArrayList<JuegoSerial> tableros;
    private int tablerosAJugar=5;
    //Un metodo que permite generar el juego
    //EL juego se puede generar segun el numero de leds que se quieren prender
    //El numero de leds existentes

    public ArrayList<JuegoSerial> generarJuego(int numeroTableros) {
        tableros = new ArrayList<JuegoSerial>();

        int contNumLeds = 3;
        for (int i = 0; i < numeroTableros; i++) {
            tableros.add(new JuegoSerial(generarTablero(numeroTableros), contNumLeds));
            if (contNumLeds <= 10) {
                contNumLeds++;
            }
        }
        return getTableros();
    }

    private int[][] generarTablero(int numeroLeds) {
        int[][] matrizColores = new int[filas][cols];
        for (int i = 0; i < numeroLeds; i++) {
            int numAleatorio = generarAleatorio();
            setValueHightOnMatriz(matrizColores, numAleatorio);
        }
        return matrizColores;
    }

    private int[][] setValueHightOnMatriz(int[][] matrizColores, int posicion) {
        int contPosiciones = 0;
        boolean romper = false;

        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < cols; col++) {
                if (posicion == contPosiciones) {
                    matrizColores[fila][col] = 1;
                    romper = true;
                    break;
                }
                contPosiciones++;
            }
            if (romper) {
                break;
            }
        }

        return matrizColores;
    }

    private int generarAleatorio() {
        int numAleatorio = 0;
        boolean numEsDiferente = true;
        do {
            numAleatorio = (int) Math.floor(Math.random() * 12 + 1);
            for (int i = 0; i < numerosGenerados.length; i++) {
                numEsDiferente = true;
                int j = numerosGenerados[i];
                if (numAleatorio == j) {
                    numEsDiferente = false;
                    break;
                }
            }
        } while (!numEsDiferente);

        return numAleatorio - 1;
    }

    public int[][] getSiguienteTablero() {
        int[][] tablero = null;
        if (contadorTableros < tablerosAJugar) {
            tablero = getTableros().get(contadorTableros).getTablero();
            contadorTableros++;
        }else{
            contadorTableros=0;
        } 
        return tablero;
    }

    /**
     * @return the contadorTableros
     */
    public int getContadorTableros() {
        return contadorTableros;
    }

    /**
     * @param contadorTableros the contadorTableros to set
     */
    public void setContadorTableros(int contadorTableros) {
        this.contadorTableros = contadorTableros;
    }

    /**
     * @return the tableros
     */
    public ArrayList<JuegoSerial> getTableros() {
        return tableros;
    }

    /**
     * @param tableros the tableros to set
     */
    public void setTableros(ArrayList<JuegoSerial> tableros) {
        this.tableros = tableros;
    }
}
