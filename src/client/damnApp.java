package client;
/*
 * dJC: The dAmn Java Client
 * damnApp.java
 * ©2005 The dAmn Java Project
 *
 * This software and its source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */

import javax.swing.*;

/**
 * The main application class.
 * @version 0.2.2
 * @author MSF
 */
public class damnApp {
    private int connected=0;
    private damnProtocol protocol;
    private Thread socketThread;
    private Runnable commRunnable;
    private damnChatPage dCP;
    private damnProperties prop;
    private damnConfig conf;
    private damnAppGUI dJgui;
    
    
    /**
     * damnApp constructor.
     * Initilizes protocol and dCP.
     */
    public damnApp() {
        conf = damnConfig.getInstance();
        protocol = new damnProtocol(this, conf);
        dCP = new damnChatPage(this, protocol);
        prop = new damnProperties(conf);
        dJgui = new damnAppGUI(this, dCP);
    }
    
    /**
     * The function for printing information to the server terminal.
     * @param type Indicates the type of message being sent. 0 is for informational. 1 is for outgoing communication and 2 is for incoming.
     * @param data The actual information.
     */
    public synchronized void terminalEcho(int type, String data) {
        switch (type) {
            case 0:
                dJgui.getServerTerminal().append("*** " + data + "\n");
                break;
            case 1:
                dJgui.getServerTerminal().append(">>> " + data + "\n");
                break;
            case 2:
                dJgui.getServerTerminal().append("<<< " + data + "\n");
                break;
        }
    }
    
    /**
     * Creates a chat page.
     * @param chatname The name of the channel the page is being created for.
     * @see client.damnChatPage#addChatPage
     */
    public void createChat(String chatname) {
        dCP.addChatPage(chatname, dJgui.getTabbedPane());
    }
    
    /**
     * Deletes a chat page.
     * @param chatname The name of the channel who's page needs to be deleted.
     * @see client.damnChatPage#delChatPage
     */
    public void deleteChat(String chatname) {
        dCP.delChatPage(chatname, dJgui.getTabbedPane());
    }
    
    /**
     * Fetches the chat member list for the specified channel.
     * @param chatname The name of the channel.
     * @return A DefaultListModel object which is the model for the member list.
     */
    public damnChatMemberList getChatMemberList(String chatname) {
        return dCP.getMemberList(chatname);
    }
    
    /**
     * Calls the damnChatPage function to write information to a
     * chat window.
     * @param chatname The name of the channel to send to.
     * @param user The user who sent the message
     * @param message Doh! The message!
     */
    public synchronized void echoChat(String chatname, String user, String message) {
        dCP.echoChat(chatname, user, message);
    }
    
    /**
     * Forwards messages to damnAppGUI handler.
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        dJgui.doServerCommand(e);
    }
    
    /**
     * Calls the damnChatPage function to write information to a chat window.
     * @param chatname The name of the channel to send to.
     * @param message Doh!
     */
    public synchronized void echoChat(String chatname, String message) {
        dCP.echoChat(chatname, message);
    }
    
    /**
     * Connects to dAmn with the specified username and password.
     * @param username The username to connect with, if none is specified the data from the config file will be used.
     * @param password The password to connect with.
     */
    public void connectUserPass(String username, String password) {
        if(username.equalsIgnoreCase("")) {
            username = conf.getUser();
            password = conf.getPassword();
        }
        
        terminalEcho(0, "Fetching authtoken, please wait...");
        TokenFetcher tf = new TokenFetcher("www.deviantart.com");
        String authtoken = tf.doTokenFetch(username, password);
        if(authtoken == null) {
            terminalEcho(0, "Error fetching authtoken.");
            terminalEcho(0, "If you know what yours is use /tokenconnect to connect to dAmn.");
            return;
        }
        terminalEcho(0, "Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()));
        protocol.setUserInfo(username, authtoken);
        commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
        socketThread = new Thread(commRunnable);
        socketThread.start();
        connected = 1;
    }
    
    /**
     * Connects to dAmn with the specified username and authtoken. Both must be specified.
     * @param username The username to connect with.
     * @param authtoken The authtoken to connect with.
     */
    public void connectUserAuth(String username, String authtoken) {
        terminalEcho(0, "Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()) + "\n");
        protocol.setUserInfo(username, authtoken);
        commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
        socketThread = new Thread(commRunnable);
        socketThread.start();
        connected = 1;
    }
    
    /**
     * Disconnects from dAmn.
     */
    public void disconnect() {
        if(connected == 1) {
            damnComm dCtmp = (damnComm)commRunnable;
            dCtmp.shutdownComm();
            connected = 0;
            terminalEcho(0, "Disconnected.");
        }
    }
    
    /**
     * This function tells the properties system to show up.
     */
    public void showProperties() {
        prop.showProperties();
    }
    
    /**
     * Checks to see if we're connected, then joins a channel.
     * @param channel Channel to join.
     */
    public void joinChannel(String channel) {
        if(connected == 1) {
            protocol.doJoinChannel(channel);
        } else {
            terminalEcho(0, "Gotta connect first dumbass!");
        }
    }
    
    /**
     * Checks to see if we are connected, then parts a channel.
     * @param channel Channel to part.
     */
    public void partChannel(String channel) {
        if(connected == 1) {
            protocol.doPartChannel(channel);
        } else {
            terminalEcho(0, "Gotta connect first you idiot.");
        }
    }
    
    /**
     * This function preforms all startup operations for the program.
     */
    public void go() {
        try {
            conf.readConfig();
        } catch (InvalidXMLException ex) {
            ex.printStackTrace();
        } catch (java.io.FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * The starting point for the application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
        damnApp dJ = new damnApp();
        dJ.go();
    }
    
}
