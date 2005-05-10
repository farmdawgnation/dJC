package client;
 /*
 * dJC: The dAmn Java Client
 * damnComm.java
 * ©2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */
import java.io.*;
import java.net.*;
import java.nio.*;

/**
 * This is the class which manages the communications thread.
 * @version 0.2
 * @author MSF
 */
public class damnComm implements Runnable {
    private Socket sock;
    private PrintWriter writer;
    private BufferedReader reader;
    private damnProtocol dP;
    private String host;
    private int port;
    private int commActive = 0;
    private String message;
    private int dataa;
    
    /**
     * This is the damnComm constructor.
     * You must give it a damnProtocol object so it can build
     * and parse the packets.
     * @param protocolObj A reference to teh damnProtocl Object for the application.
     * @param hostStr Connection host.
     * @param portNum The port to connect to.
     */
    public damnComm(damnProtocol protocolObj, String hostStr, int portNum) {
        dP = protocolObj;
        dP.dC = this;
        host = hostStr;
        port = portNum;
    }
    
    /**
     * This is the run() method for the communication thread.
     * It will connect to the server upon start and enter the listen loop.
     * All incoming data will be reffered to the damnProtocol object for managing.
     */
    public void run() {
        try {
            sock = new Socket(host, port);
            writer = new PrintWriter(sock.getOutputStream());
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            commActive = 1;
            
            dP.doHandshake();
            
            while(commActive == 1) {
                while((dataa = reader.read()) != -1) {
                    //System.out.println(message);
                    if((char)dataa != '\0') {
                        if(message != null) {
                            message += (char)dataa;
                        } else {
                            message = message.format("%c", dataa);
                        }
                    } else {
                        break;
                    }
                }
                dP.handleMessage(message, this);
                message = null;
            }
            
            sock.close();
            sock = null;
            writer = null;
            streamReader = null;
            reader = null;
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * This is the function to write data to the connection.
     * Is syncronized to be thread-safe.
     * @param data The data to write to the connection.
     */
    public synchronized void writeData(String data) {
        try {
            writer.write(data);
            writer.flush();
        } catch (Exception ex) {ex.printStackTrace();}
    }
    
    /**
     * This function will shutdown the thread by setting commActive to
     * zero. It is synchronized just to be on the safe side.
     */
    public synchronized void shutdownComm() {
        commActive = 0;
    }
}
