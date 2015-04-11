package org.unimayor.enterprise.server.arduino.data.duino;

import java.io.Serializable;


/**
 *
 * @author alejandro.lozano
 */
public class UsuarioArduinoSerial implements Serializable {
    
    private static final long serialVersionUID = 6792723495615930717L;
    private String pin;
    private String nick;
 

    public UsuarioArduinoSerial() {
        
    }

       
    public UsuarioArduinoSerial(String pin, String nick) {
        this.pin = pin;
        this.nick = nick;
    }
    
   
       
    /**
     * @return the pin
     */
    public String getPin() {
        return pin;
    }

    /**
     * @param pin the pin to set
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     * @return the nick
     */
    public String getNick() {
        return nick;
    }

    /**
     * @param nick the nick to set
     */
    public void setNick(String nick) {
        this.nick = nick;
    }
        
}