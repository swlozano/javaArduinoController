/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unimayor.enterprise.server.arduino.com;

import org.unimayor.enterprise.server.arduino.data.Constantes;
import org.unimayor.enterprise.server.arduino.data.ObjSerializado;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.unimayor.enterprise.server.arduino.ServidorBuzzMe;
import org.unimayor.enterprise.server.arduino.data.duino.ObjSerializadoArduino;


import org.unimayor.enterprise.server.arduino.data.Usuario;
import org.unimayor.enterprise.server.arduino.data.duino.UsuarioArduinoSerial;
import org.unimayor.enterprise.server.arduino.data.UsuarioSerial;
import org.unimayor.enterprise.server.arduino.facade.FacadeBuzzMe;

/**
 *
 * @author alejandro.lozano
 */
public class ChatConexion extends Thread {

    protected ConcurrentHashMap<String, ChatConexion> clientesConectados;
    protected FacadeBuzzMe facade;
    protected ObjectInputStream entrada = null;
    protected ObjectOutputStream salida = null;
    protected String pin = "";
    private UsuarioSerial usuarioLocal;
    private UsuarioArduinoSerial usuarioArduinoSerial;
    Socket socketCliente;
    Thread hiloEsperaArduino;

    public ChatConexion(Socket socketCliente, ServidorBuzzMe servidor) {
        this.socketCliente = socketCliente;
        this.clientesConectados = servidor.clientesConectados;
        //this.facade = servidor.facade;
        usuarioArduinoSerial = new UsuarioArduinoSerial();
        usuarioArduinoSerial.setPin(clientesConectados.size() + 1 + "");
        try {
            entrada = new ObjectInputStream(this.socketCliente.getInputStream());
            salida = new ObjectOutputStream(this.socketCliente.getOutputStream());
            enviarObjeto(new ObjSerializadoArduino(usuarioArduinoSerial, Constantes.ARDUINO_CMD_GET_PIN));
            //enviarObjeto(new ObjSerializadoArduino(null, Constantes.ARDUINO_CMD_GET_PIN));
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.socketCliente.close();
            } catch (IOException e2) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("Nuevo cliente agregado");
        this.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ObjSerializadoArduino chatObjSerie = (ObjSerializadoArduino) entrada.readObject();
                interpreteComandos(chatObjSerie);
                if (chatObjSerie.getComando() == Constantes.COMMAND_SALIR) {
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ChatConexion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatConexion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                socketCliente.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            } finally {
                socketCliente = null;
                //ArrayList<UsuarioSerial> usuarios = this.facade.getContactos(this.pin, clientesConectados.keySet());
                eliminaCliente();
                //notificarDesConexion(usuarios);
            }
        }
    }

    private void interpreteComandos(ObjSerializado os) {
        switch (os.getComando()) {
            case Constantes.COMMAND_REGISTRAR:
                //Llamamos al facade y registramos el contacto
                String pin = this.facade.registrarUsuario(os.getUsuarioCliente());
                ObjSerializado os1 = new ObjSerializado();
                if (!pin.equals("")) {
                    this.pin = pin;
                    incorporaCliente(this.pin, this);
                    os1.registrarPin(pin);
                    enviarObjeto(os1);
                    this.usuarioLocal = new UsuarioSerial(pin, os.getUsuarioCliente().getNick(), Constantes.ESTADO_DISPONIBLE, "");
                } else {
                    os1.ingresar("", "");
                    enviarObjeto(os1);
                }
                break;
            case Constantes.COMMAND_INGRESAR:
                boolean loginOk = false;
                ObjSerializado objSerializado;
                objSerializado = new ObjSerializado();
                if (!os.getUsuarioCliente().getPin().trim().equals("")) {
                    Usuario us = this.facade.login(os.getUsuarioCliente().getPin(), os.getUsuarioCliente().getPassword());
                    if (us != null) {
                        this.usuarioLocal = new UsuarioSerial(us);
                        objSerializado.setUsuarioCliente(this.usuarioLocal);
                        loginOk = true;
                        this.pin = os.getUsuarioCliente().getPin();
                        ArrayList<UsuarioSerial> usuarios = this.facade.getContactos(os.getUsuarioCliente().getPin(), clientesConectados.keySet());
                        if (usuarios != null && !usuarios.isEmpty()) {
                            objSerializado.ingresar(usuarios, true);
                            notificarCambioEstado(this.usuarioLocal.getEstado(), "", usuarios);
                            //notificarConexion(usuarios);
                        } else {
                            objSerializado.ingresar(null, true);
                        }

                        ArrayList<UsuarioSerial> contactosToAccept = this.facade.getContactosToAccept(this.pin);
                        objSerializado.setContactosToAccept(contactosToAccept);

                        incorporaCliente(this.pin, this);
                    }
                }
                if (!loginOk) {
                    objSerializado.ingresar(null, false);
                }
                enviarObjeto(objSerializado);
                break;
            case Constantes.COMMAND_ADD_CONTACTO:
                if (!os.getUsuarioCliente().getPin().trim().equals("")) {
                    boolean response = this.facade.addContacto(this.pin, os.getUsuarioCliente().getPin());
                    if (response) {
                        notificarPeticion(os.getUsuarioCliente().getPin());
                    }
                }
                break;
            case Constantes.COMMAND_ACCEPT_CONTACTO:
                ArrayList<UsuarioSerial> contactosConfirmados = this.facade.confirmarContactos(this.pin, os.getContactosToAccept());
                if (contactosConfirmados != null && !contactosConfirmados.isEmpty()) {
                    notificarConexion(contactosConfirmados);
                    notificarNewContactsAdd(contactosConfirmados);
                }
                break;
            case Constantes.COMMAND_INI_CONVERSACION:
                this.usuarioLocal.setEstado(Constantes.ESTADO_EN_CHARLA);
                String sendTo = os.getUsuarioCliente().getPin();
                iniciarCharla(sendTo);
                notificarCambioEstado(Constantes.ESTADO_NO_DISPONIBLE, sendTo);
                break;

            case Constantes.COMMAND_ACEPTAR_CHARLA:
                this.usuarioLocal.setEstado(Constantes.ESTADO_EN_CHARLA);
                String pinAceptado = os.getUsuarioCliente().getPin();
                aceptarCharla(pinAceptado);
                notificarCambioEstado(Constantes.ESTADO_EN_CHARLA, pinAceptado);
                break;

            case Constantes.COMMAND_CAMBIAR_ESTADO:
                byte est = os.getUsuarioCliente().getEstado();
                if (est != Constantes.ESTADO_EN_CHARLA) {
                    this.facade.cambiarEstado(this.pin, est);
                }
                notificarCambioEstado(est);
                this.usuarioLocal.setEstado(est);
                break;

            case Constantes.COMMAND_MENSAJE:
                enviarMsg(os.getUsuarioCliente().getPin(), os.getMensaje());
                break;

            case Constantes.COMMAND_ABANDONAR_CHARLA:
                Usuario usuario = this.facade.getUsuarioByPin(this.pin);
                if (usuario != null) {
                    this.usuarioLocal.setEstado(usuario.getEstado());
                    notificarAbandonoCharla(os.getUsuarioCliente().getPin(), usuario.getEstado());
                    notificarCambioEstado(usuario.getEstado(), os.getUsuarioCliente().getPin());
                }
                break;

            case Constantes.COMMAND_BLOQUEAR:
                boolean[] resBlock = this.facade.bloquearUsuario(this.pin, os.getUsuarioCliente().getPin());
                if (resBlock[0]) {
                    notificarBloqueo(os.getUsuarioCliente().getPin(), resBlock[1]);
                }
                break;

            case Constantes.COMMAND_AUDIO:
                enviarAudio(os);
                break;

            case Constantes.COMMAND_MSG_OK:
                notificarMsgOk(os.getUsuarioCliente().getPin(), os.getMensaje());
                break;




        }
    }

    private void interpreteComandos(ObjSerializadoArduino osArduino) {
        switch (osArduino.getComando()) {
            //ARDUINO CASOS
            case Constantes.ARDUINO_CMD_NICK:
                //Guarda el nombre enviado por el dispositivo movil
                setNickOnUsuario(osArduino.getUsuarioArduinoSerial());
                break;

            case Constantes.ARDUINO_CMD_NUEVO_JUEGO:
                //Genera un nuevo juego
                ObjSerializadoArduino objSerializadoArduino = new ObjSerializadoArduino();
                objSerializadoArduino.setArrayJuegos(ServidorBuzzMe.generarTableros(5));
                objSerializadoArduino.setComando(Constantes.ARDUINO_CMD_NUEVO_JUEGO);
                enviarObjeto(objSerializadoArduino);
                break;

            case Constantes.ARDUINO_CMD_SIGUIENTE_TABLERO:
                //ACTUALIZA EL CIRCUITO EN ARDUINO
                int[][] siguienteTablero = ServidorBuzzMe.gameControl.getSiguienteTablero();
                siguienteTablero[0][0] = 13;
                if (siguienteTablero != null) {
                    ArduinoComunication.getInstance().prenderMatriz(siguienteTablero);
                    hiloEsperaArduino = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                hiloEsperaArduino.sleep(10000);
                                ObjSerializadoArduino objSerializadoArduino1 = new ObjSerializadoArduino();
                                objSerializadoArduino1.setComando(Constantes.ARDUINO_CMD_SIGUIENTE_TABLERO);
                                enviarObjeto(objSerializadoArduino1);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ChatConexion.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    hiloEsperaArduino.start();
                } else {
                    //Notifiacr fin de juego
                    ArduinoComunication.getInstance().apagarLeds();
                    ObjSerializadoArduino objSerializadoArduino1 = new ObjSerializadoArduino();
                    objSerializadoArduino1.setComando(Constantes.ARDUINO_CMD_FIN_JUEGO);
                    enviarObjeto(objSerializadoArduino1);
                }

                break;
            case Constantes.ARDUINO_CMD_PRENDER_PIN:
                ArduinoComunication.getInstance().enviarDatos(osArduino.getUsuarioArduinoSerial().getPin());
                break;
        }
    }

    private void setNickOnUsuario(UsuarioArduinoSerial usuarioArduinoSerial1) {
        this.usuarioArduinoSerial.setNick(usuarioArduinoSerial1.getNick());
        this.clientesConectados.put(usuarioArduinoSerial1.getNick(), this);
    }

    private void enviarAudio(ObjSerializado os) {
        String key = os.getUsuarioCliente().getPin();
        if (clientesConectados.containsKey(key)) {
            ChatConexion chatCx = clientesConectados.get(key);
            chatCx.enviarObjeto(os);
        }
    }

    private void notificarAbandonoCharla(String pin, byte estado) {
        if (clientesConectados.containsKey(pin)) {

            UsuarioSerial serial = new UsuarioSerial();
            serial.setPin(this.pin);
            serial.setEstado(estado);

            ObjSerializado serializado = new ObjSerializado();
            serializado.setComando(Constantes.COMMAND_ABANDONAR_CHARLA);
            serializado.setUsuarioCliente(serial);

            ChatConexion chatCx = clientesConectados.get(pin);
            chatCx.enviarObjeto(serializado);
        }
    }

    private void enviarMsg(String pinToSend, String msg) {
        if (clientesConectados.containsKey(pinToSend)) {
            UsuarioSerial usuarioSerial = new UsuarioSerial();
            usuarioSerial.setPin(this.pin);
            ObjSerializado objSerializado = new ObjSerializado();
            objSerializado.setComando(Constantes.COMMAND_MENSAJE);
            objSerializado.setMensaje(msg);
            objSerializado.setUsuarioCliente(usuarioSerial);
            ChatConexion chatCx = clientesConectados.get(pinToSend);
            chatCx.enviarObjeto(objSerializado);
        }
    }

    private void notificarMsgOk(String pinToSend, String idMsg) {
        if (clientesConectados.containsKey(pinToSend)) {
            UsuarioSerial usuarioSerial = new UsuarioSerial();
            usuarioSerial.setPin(this.pin);

            ObjSerializado objSerializado = new ObjSerializado();
            objSerializado.setComando(Constantes.COMMAND_MSG_OK);
            objSerializado.setUsuarioCliente(usuarioSerial);
            objSerializado.setMensaje(idMsg);

            ChatConexion chatCx = clientesConectados.get(pinToSend);
            chatCx.enviarObjeto(objSerializado);
        }
    }

    private void notificarCambioEstado(byte estado) {
        notificarCambioEstado(estado, "");
    }

    private void notificarCambioEstado(byte estado, String pinDiscriminado) {
        ArrayList<UsuarioSerial> usus = this.facade.getContactos(pin, clientesConectados.keySet());
        notificarCambioEstado(estado, pinDiscriminado, usus);
    }

    private void notificarCambioEstado(byte estado, String pinDiscriminado, ArrayList<UsuarioSerial> contactos) {
        ArrayList<UsuarioSerial> usus = contactos;
        if (usus != null && !usus.isEmpty()) {
            UsuarioSerial us;

            UsuarioSerial usToSend = new UsuarioSerial();
            usToSend.setPin(this.pin);
            usToSend.setEstado(estado);

            ObjSerializado objSerializado = new ObjSerializado();
            objSerializado.setComando(Constantes.COMMAND_CAMBIAR_ESTADO);
            objSerializado.setUsuarioCliente(usToSend);

            ChatConexion chatCX;

            for (int i = 0; i < usus.size(); i++) {
                us = usus.get(i);
                if (clientesConectados.containsKey(us.getPin()) && !us.getPin().equals(pinDiscriminado) && us.getEstado() != Constantes.ESTADO_DESCONECTADO) {
                    chatCX = clientesConectados.get(us.getPin());
                    chatCX.enviarObjeto(objSerializado);
                }
            }
        }
    }

    private void notificarBloqueo(String pinBlock, boolean bloqueado) {
        if (clientesConectados.containsKey(pinBlock)) {
            if (this.usuarioLocal.getEstado() != Constantes.ESTADO_DESCONECTADO) {
                byte estado = 0;
                UsuarioSerial usuarioSerial = new UsuarioSerial();

                if (bloqueado) {
                    usuarioSerial.setEstado(Constantes.ESTADO_DESCONECTADO);
                    estado = Constantes.ESTADO_DESCONECTADO;
                } else {
                    usuarioSerial.setEstado(this.usuarioLocal.getEstado());
                }
                usuarioSerial.setPin(this.pin);

                ObjSerializado objSerializado = new ObjSerializado();
                objSerializado.setComando(Constantes.COMMAND_CAMBIAR_ESTADO);
                objSerializado.setUsuarioCliente(usuarioSerial);

                ChatConexion chatCx = clientesConectados.get(pinBlock);
                chatCx.enviarObjeto(objSerializado);

                /**
                 * Notifica al usuario quien desbloquea el estado del usuario
                 * desbloqueado
                 *
                 */
                if (!bloqueado) {
                    estado = chatCx.usuarioLocal.getEstado();
                }

                UsuarioSerial us = new UsuarioSerial();
                us.setPin(pinBlock);
                us.setEstado(estado);
                us.setBloqueado(bloqueado);

                ObjSerializado os = new ObjSerializado();
                os.setComando(Constantes.COMMAND_CAMBIAR_ESTADO);
                os.setUsuarioCliente(us);

                enviarObjeto(os);

            }
        }
    }

    synchronized public void notificarNewContactsAdd(ArrayList<UsuarioSerial> contactosConfirmados) {
        String key;
        UsuarioSerial uses;
        for (int i = 0; i < contactosConfirmados.size(); i++) {
            uses = contactosConfirmados.get(i);
            key = uses.getPin();
        }
        ObjSerializado objSerializado = new ObjSerializado();
        objSerializado.setComando(Constantes.COMMAND_NEW_CONTACTS_ADD);
        objSerializado.setContactos(contactosConfirmados);
        enviarObjeto(objSerializado);
    }

    synchronized public void enviarObjeto(ObjSerializado objSerializado) {
        try {
            salida.writeObject(objSerializado);
            salida.flush();
        } catch (IOException ex) {
            Logger.getLogger(ChatConexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void notificarNuevoJuego(ObjSerializadoArduino objSerializadoArduino) {
        for (Map.Entry<String, ChatConexion> entry : clientesConectados.entrySet()) {
            ChatConexion chatConexion = entry.getValue();
            chatConexion.enviarObjeto(objSerializadoArduino);
        }
    }

    synchronized public void enviarObjeto(ObjSerializadoArduino objSerializadoArduino) {
        try {
            salida.writeObject(objSerializadoArduino);
            salida.flush();
        } catch (IOException ex) {
            Logger.getLogger(ChatConexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Notifica al socketCliente que un usuario desea hablar con el
     */
    public void iniciarCharla(String pin) {
        if (clientesConectados.containsKey(pin)) {
            ChatConexion chatCx = clientesConectados.get(pin);
            if (chatCx.usuarioLocal.getEstado() == Constantes.ESTADO_DISPONIBLE) {
                UsuarioSerial us = new UsuarioSerial();
                us.setPin(this.pin);
                ObjSerializado objMsg = new ObjSerializado();
                objMsg.setComando(Constantes.COMMAND_INI_CONVERSACION);
                objMsg.setUsuarioCliente(us);
                chatCx.enviarObjeto(objMsg);
            }
        }
    }

    public void notificarConexion(ArrayList<UsuarioSerial> usuarios) {

        if (usuarios == null || usuarios.isEmpty()) {
            return;
        } else {
            Usuario usByPin = this.facade.getUsuarioByPin(this.pin);
            if (usByPin == null || usByPin.getEstado() == Constantes.ESTADO_DESCONECTADO) {
                return;
            } else {
                usuarioLocal.setEstado(usByPin.getEstado());
            }
        }

        String key;
        ChatConexion chatCx;

        ObjSerializado os;
        os = new ObjSerializado();
        os.setComando(Constantes.COMMAND_USUARIO_CONECTADO);
        os.setUsuarioCliente(this.usuarioLocal);

        for (int i = 0; i < usuarios.size(); i++) {
            key = usuarios.get(i).getPin();
            if (clientesConectados.containsKey(key)) {
                chatCx = clientesConectados.get(key);
                chatCx.enviarObjeto(os);
            }
        }
    }

    synchronized public void notificarDesConexion(ArrayList<UsuarioSerial> usuarios) {
        if (usuarioLocal.getEstado() != Constantes.ESTADO_DESCONECTADO) {
            notificarCambioEstado(Constantes.ESTADO_DESCONECTADO);
        }
        /*String key;
         ChatConexion chatCx;
         ObjSerializado os;
         if (usuarios != null && !usuarios.isEmpty()) {
         UsuarioSerial usToSend = new UsuarioSerial();
         usToSend.setPin(this.pin);
         usToSend.setEstado(Constantes.ESTADO_DESCONECTADO);
         os = new ObjSerializado();
         os.setComando(Constantes.COMMAND_CAMBIAR_ESTADO);
         os.setUsuarioCliente(usToSend);
         for (int i = 0; i < usuarios.size(); i++) {
         key = usuarios.get(i).getPin();
         if (clientesConectados.containsKey(key)) {
         chatCx = clientesConectados.get(key);
         chatCx.enviarObjeto(os);
         }
         }
         }*/
    }

    synchronized public void incorporaCliente(String pin,
            ChatConexion cnx) {
        if (!clientesConectados.containsKey(pin)) {
            clientesConectados.put(pin, cnx);
        }
    }

    synchronized public void eliminaCliente() {
        clientesConectados.remove(usuarioArduinoSerial.getPin());
    }

    synchronized public void notificarPeticion(String pinContacto) {
        if (clientesConectados.containsKey(pinContacto)) {
            try {
                ObjSerializado objSerializado = new ObjSerializado();
                objSerializado.setComando(Constantes.COMMAND_ACCEPT_CONTACTO);
                objSerializado.setUsuarioCliente(usuarioLocal);
                ChatConexion cx = clientesConectados.get(pinContacto);
                cx.salida.writeObject(objSerializado);
            } catch (IOException ex) {
                Logger.getLogger(ChatConexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void aceptarCharla(String pinAceptado) {
        if (clientesConectados.containsKey(pinAceptado)) {
            ChatConexion chatCx = clientesConectados.get(pinAceptado);
            if (chatCx.usuarioLocal.getEstado() == Constantes.ESTADO_DISPONIBLE) {
                UsuarioSerial usu = new UsuarioSerial();
                usu.setPin(pin);
                ObjSerializado objSerializado = new ObjSerializado();
                objSerializado.setComando(Constantes.COMMAND_ACEPTAR_CHARLA);
                objSerializado.setUsuarioCliente(usu);
                chatCx.enviarObjeto(objSerializado);
                notificarCambioEstado(Constantes.ESTADO_EN_CHARLA);
            } else {
                //se le responde al socketCliente que la persona no acepta
            }
        }
    }
}
