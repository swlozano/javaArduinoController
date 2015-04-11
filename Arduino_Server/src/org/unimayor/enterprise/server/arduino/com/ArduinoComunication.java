/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unimayor.enterprise.server.arduino.com;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.OutputStream;
import java.util.Enumeration;
import org.unimayor.enterprise.server.arduino.controller.GameControl;

/**
 *
 * @author alejandro
 */
public class ArduinoComunication {

    public static final String TURN_Amarillo_OFF = "0";
    public static final String TURN_Amarillo_ON = "1";
    public static final String TURN_Rojo_OFF = "2";
    public static final String TURN_Rojo_ON = "3";
    public static final String ARDUINO_APAGAR_LEDS = "0";
    String[][] pinesArduino = {{"2", "3", "4"}, {"5", "6", "7"}, {"8", "9", "10"}, {"11", "12", "13"}};
    //Variables de conexión
    private OutputStream output = null;
    SerialPort serialPort;
    private final String PUERTO = "/dev/ttyACM0";
    private static final int TIMEOUT = 2000; //Milisegundos
    private static final int DATA_RATE = 9600;
    private static ArduinoComunication instance;

    public static ArduinoComunication getInstance() {
        if (instance == null) {
            instance = new ArduinoComunication();
        }
        return instance;
    }
    
    private ArduinoComunication(){
        
    }

    public void inicializarConexion() {
        CommPortIdentifier puertoID = null;
        Enumeration puertoEnum = CommPortIdentifier.getPortIdentifiers();

        while (puertoEnum.hasMoreElements()) {
            CommPortIdentifier actualPuertoID = (CommPortIdentifier) puertoEnum.nextElement();
            if (PUERTO.equals(actualPuertoID.getName())) {
                puertoID = actualPuertoID;
                break;
            }
        }

        if (puertoID == null) {
            mostrarError("No se puede conectar al puerto");
            //System.exit(Error);
        }

        try {
            serialPort = (SerialPort) puertoID.open(this.getClass().getName(), TIMEOUT);
            //Parámetros puerto serie

            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            output = serialPort.getOutputStream();
        } catch (Exception e) {
            mostrarError(e.getMessage());
            //System.exit(ERROR);

        }
    }

    public void enviarDatos(String datos) {
        try {
            output.write(datos.getBytes());
        } catch (Exception e) {
            mostrarError("ERROR");
            //System.exit(ERROR);
        }
    }
    
    public void enviarDatos(int datos) {
        try {
            output.write(datos);
        } catch (Exception e) {
            mostrarError("ERROR");
            //System.exit(ERROR);
        }
    }
    

    public void prenderMatriz(int[][] matriz) {
        enviarDatos(ARDUINO_APAGAR_LEDS);
        for (int fila = 0; fila < GameControl.filas; fila++) {
            for (int col = 0; col < GameControl.cols; col++) {
                if (matriz[fila][col] == 1) {
                    enviarDatos(pinesArduino[fila][col]);
                }
            }
        }
    }

    public void apagarLeds() {
        enviarDatos(ARDUINO_APAGAR_LEDS);
    }

    private void mostrarError(String mensaje) {
        System.out.println("@@@@@Error " + mensaje);

    }
}
