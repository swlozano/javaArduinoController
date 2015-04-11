package org.unimayor.enterprise.server.arduino.data.duino;

import java.io.Serializable;
import java.util.ArrayList;
import org.unimayor.enterprise.server.arduino.data.Constantes;

/**
 *
 * @author alejandro.lozano
 */
public class ObjSerializadoArduino implements Serializable {

    private static final long serialVersionUID = -4793345147041142999L;
    private UsuarioArduinoSerial usuarioArduinoSerial;
    private byte comando;
    private byte numeroTableros;
    private ArrayList<JuegoSerial> arrayJuegos = new ArrayList<JuegoSerial>();

    public ObjSerializadoArduino(UsuarioArduinoSerial usuarioArduinoSerial, byte comando) {
        this.usuarioArduinoSerial = usuarioArduinoSerial;
        this.comando = comando;
    }
          

    public ObjSerializadoArduino() {
    }


    public void salir() {
        this.setComando(Constantes.COMMAND_SALIR);
    }

    /**
     * @return the usuarioArduinoSerial
     */
    public UsuarioArduinoSerial getUsuarioArduinoSerial() {
        return usuarioArduinoSerial;
    }

    /**
     * @param usuarioArduinoSerial the usuarioArduinoSerial to set
     */
    public void setUsuarioArduinoSerial(UsuarioArduinoSerial usuarioArduinoSerial) {
        this.usuarioArduinoSerial = usuarioArduinoSerial;
    }

    /**
     * @return the comando
     */
    public byte getComando() {
        return comando;
    }

    /**
     * @param comando the comando to set
     */
    public void setComando(byte comando) {
        this.comando = comando;
    }

    /**
     * @return the numeroTableros
     */
    public byte getNumeroTableros() {
        return numeroTableros;
    }

    /**
     * @param numeroTableros the numeroTableros to set
     */
    public void setNumeroTableros(byte numeroTableros) {
        this.numeroTableros = numeroTableros;
    }

    /**
     * @return the arrayJuegos
     */
    public ArrayList<JuegoSerial> getArrayJuegos() {
        return arrayJuegos;
    }

    /**
     * @param arrayJuegos the arrayJuegos to set
     */
    public void setArrayJuegos(ArrayList<JuegoSerial> arrayJuegos) {
        this.arrayJuegos = arrayJuegos;
    }

}
