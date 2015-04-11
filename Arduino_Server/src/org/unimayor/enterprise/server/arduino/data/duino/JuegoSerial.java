/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unimayor.enterprise.server.arduino.data.duino;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author alejandro
 */
public class JuegoSerial implements  Serializable{
     
     private static final long serialVersionUID = 5243709329518371219L;
     private int[][] tablero;
     private int numeroLedsPrendidos;

    public JuegoSerial(int[][] tablero, int numeroLedsPrendidos) {
        this.tablero = tablero;
        this.numeroLedsPrendidos = numeroLedsPrendidos;
    }

     
    /**
     * @return the numeroLedsPrendidos
     */
    public int getNumeroLedsPrendidos() {
        return numeroLedsPrendidos;
    }

    /**
     * @param numeroLedsPrendidos the numeroLedsPrendidos to set
     */
    public void setNumeroLedsPrendidos(int numeroLedsPrendidos) {
        this.numeroLedsPrendidos = numeroLedsPrendidos;
    }

    /**
     * @return the tablero
     */
    public int[][] getTablero() {
        return tablero;
    }

    /**
     * @param tablero the tablero to set
     */
    public void setTablero(int[][] tablero) {
        this.tablero = tablero;
    }
     
     
}
