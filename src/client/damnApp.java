package client;
/*
 * dJC: The dAmn Java Client
 * damnApp.java
 * ©2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The main application class.
 * @version 0.2.2
 * @author MSF
 */
public class damnApp implements ActionListener {
    private JTabbedPane tabbedPane;
    private JPanel serverPage;
    private JTextArea serverTerminal;
    private JFrame frame;
    private JTextField serverCommandField;
    private int connected=0;
    private damnProtocol protocol;
    private Thread socketThread;
    private Runnable commRunnable;
    private damnChatPage dCP;
    private damnProperties prop;
    private damnConfig conf;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem connectItem;
    private JMenuItem disconnectItem;
    private JMenuItem preferencesItem;
    private JMenuItem exitItem;
    private JMenuItem aboutItem;
    
    
    /**
     * damnApp constructor.
     * Initilizes protocol and dCP.
     */
    public damnApp() {
        conf = damnConfig.getInstance();
        protocol = new damnProtocol(this, conf);
        dCP = new damnChatPage(this, protocol);
        prop = new damnProperties(conf);
    }
    
    /**
     * The function for printing information to the server terminal.
     * @param type Indicates the type of message being sent. 0 is for informational. 1 is for outgoing communication and 2 is for incoming.
     * @param data The actual information.
     */
    public synchronized void terminalEcho(int type, String data) {
        if(type == 0) {
            serverTerminal.append("*** " + data + "\n");
        } else if(type == 1) {
            serverTerminal.append(">>> " + data + "\n");
        } else if(type == 2) {
            serverTerminal.append("<<< " + data + "\n");
        }
    }
    
    /**
     * Action handler.
     * This function decides what to do when the user types a server command.
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String parts[];
        if(e.getSource() == serverCommandField) {
            parts = serverCommandField.getText().split(" ");
        } else if(e.getSource() == preferencesItem) {
            prop.showProperties();
            return;
        } else if(e.getSource() == connectItem) {
            terminalEcho(0, "Fetching authtoken, please wait...");
            TokenFetcher tf = new TokenFetcher("www.deviantart.com");
            String authtoken = tf.doTokenFetch(conf.getUser(), conf.getPassword());
            if(authtoken == null) {
                terminalEcho(0, "Error fetching authtoken.");
                terminalEcho(0, "If you know what yours is use /tokenconnect to connect to dAmn.");
                serverCommandField.setText("");
                return;
            }
            serverTerminal.append("*** Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()) + "\n");
            protocol.setUserInfo(conf.getUser(), authtoken);
            commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
            socketThread = new Thread(commRunnable);
            socketThread.start();
            connected = 1;
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
            return;
        } else if(e.getSource() == disconnectItem) {
            if(connected == 1) {
                damnComm dCtmp = (damnComm)commRunnable;
                dCtmp.shutdownComm();
                connected = 0;
                terminalEcho(0, "Disconnected.");
                connectItem.setEnabled(true);
                disconnectItem.setEnabled(false);
            }
            return;
        } else {
            JTextField txtfld = dCP.chatFields.get(dCP.chatFields.indexOf(e.getSource()));
            parts = txtfld.getText().split(" ");
        }
        if(parts[0].equalsIgnoreCase("/connect")) {
            terminalEcho(0, "Fetching authtoken, please wait...");
            TokenFetcher tf = new TokenFetcher("www.deviantart.com");
            String authtoken = tf.doTokenFetch(parts[1], parts[2]);
            if(authtoken == null) {
                terminalEcho(0, "Error fetching authtoken.");
                terminalEcho(0, "If you know what yours is use /tokenconnect to connect to dAmn.");
                serverCommandField.setText("");
                return;
            }
            serverTerminal.append("*** Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()) + "\n");
            protocol.setUserInfo(parts[1], authtoken);
            commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
            socketThread = new Thread(commRunnable);
            socketThread.start();
            connected = 1;
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        } else if(parts[0].equalsIgnoreCase("/tokenconnect")) {
            serverTerminal.append("*** Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()) + "\n");
            protocol.setUserInfo(parts[1], parts[2]);
            commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
            socketThread = new Thread(commRunnable);
            socketThread.start();
            connected = 1;
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        } else if(parts[0].equalsIgnoreCase("/join")) {
            if(connected == 1) {
                protocol.doJoinChannel(parts[1]);
            } else {
                terminalEcho(0, "Gotta connect first dumbass!");
            }
        } else if(parts[0].equalsIgnoreCase("/part")) {
            if(connected == 1) {
                protocol.doPartChannel(parts[1]);
            } else {
                terminalEcho(0, "Gotta connect first you idiot.");
                serverCommandField.setText("");
            }
        } else if(parts[0].equalsIgnoreCase("/disconnect")) {
            if(connected == 1) {
                damnComm dCtmp = (damnComm)commRunnable;
                dCtmp.shutdownComm();
                connected = 0;
                terminalEcho(0, "Disconnected.");
                connectItem.setEnabled(true);
                disconnectItem.setEnabled(false);
            }
        } else if(parts[0].equalsIgnoreCase("/about")) {
            terminalEcho(0, "");
            terminalEcho(0, "dJC: The dAmn Java Client");
            terminalEcho(0, "http://www.sourceforge.net/projects/damnjava");
            terminalEcho(0, "");
            terminalEcho(0, "Written by...");
            terminalEcho(0, "\tMSF - Lead Developer/Project Manager");
            terminalEcho(0, "\tEric Olander - Developer");
            terminalEcho(0, "\tMiklosi Attila - Developer");
            terminalEcho(0, "");
            terminalEcho(0, "If you are interested in getting involved: Let MSF know.");
            terminalEcho(0, "Now back to your regularly scheduled programming...");
            terminalEcho(0, "");
        } else if(parts[0].equalsIgnoreCase("/token")) {
            TokenFetcher tf = new TokenFetcher("www.deviantart.com");

            terminalEcho(0, tf.doTokenFetch(parts[1], parts[2]));
        } else {
            terminalEcho(0, "Unknown Command.");
        }
        serverCommandField.setText("");
    }
    
    /**
     * Creates a chat page.
     * @param chatname The name of the channel the page is being created for.
     * @see client.damnChatPage#addChatPage
     */
    public void createChat(String chatname) {
        dCP.addChatPage(chatname, tabbedPane);
    }
    
    /**
     * Deletes a chat page.
     * @param chatname The name of the channel who's page needs to be deleted.
     * @see client.damnChatPage#delChatPage
     */
    public void deleteChat(String chatname) {
        dCP.delChatPage(chatname, tabbedPane);
    }
    
    /**
     * Fetches the chat member list for the specified channel.
     * @param chatname The name of the channel.
     * @return A DefaultListModel object which is the model for the member list.
     */
    public DefaultListModel getChatMemberList(String chatname) {
        return dCP.getMemberList(chatname);
    }
    
    /**
     * Searches for the index number of a specified member in a chat room.
     * Used for removing members from the list if they've left the room.
     * @param chatname The name of the channel where the member should be.
     * @param username The username to look for.
     * @return The index of the member that you're looking for. It will give a -1 if no such member was found.
     */
    public int searchList(String chatname, String username) {
        DefaultListModel list = dCP.getMemberList(chatname);
        for(int i=0;i < list.size(); i++) {
            if(list.get(i).equals(username)) {
                return i;
            }
        }
        
        System.out.println("searchList() Fail!");
        return -1;
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
     * Calles the damnChatPage function to write preformatted information to a
     * chat window.
     * @param chatname The name of the channel to which to send the message.
     * @param message The message to write.
     */
    public synchronized void echoChat(String chatname, String message) {
        dCP.echoChat(chatname, message);
    }
    
    /**
     * Constructs and shows the interface for the application.
     */
    private void damnShowInterface() {
        frame = new JFrame("dJC: The dAmn Java Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        menuBar = new JMenuBar();
        
        //File Menu
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        //Help Menu
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        
        //File Menu Items
        connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(this);
        fileMenu.add(connectItem);
        disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(this);
        disconnectItem.setEnabled(false);
        fileMenu.add(disconnectItem);
        preferencesItem = new JMenuItem("Preferences");
        preferencesItem.addActionListener(this);
        fileMenu.add(preferencesItem);
        exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);
        
        //Help Menu Items
        aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);
        
        //Set the Menu Bar
        frame.setJMenuBar(menuBar);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new java.awt.Dimension(800, 600));
        serverPage = new JPanel(new BorderLayout(5, 5));
        serverPage.setName("Server");
        
        serverTerminal = new JTextArea(5, 20);
        serverTerminal.setLineWrap(true);
        JScrollPane serverScrollPane = new JScrollPane(serverTerminal, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        serverPage.add(serverScrollPane, BorderLayout.CENTER);
        
        serverTerminal.setText("dJC: The dAmn Java Client\nVersion 0.2.2\n©2005 The dAmn Java Project\nType '/about' for more info.'\n");
        
        serverCommandField = new JTextField(20);
        serverCommandField.addActionListener(this);
        serverPage.add(serverCommandField, BorderLayout.PAGE_END);
        
        tabbedPane.add(serverPage);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        
        frame.pack();
        frame.setVisible(true);
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
        
        damnShowInterface();
    }
    
    /**
     * The starting point for the application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) { }
                damnApp dJ = new damnApp();
                dJ.go();
            }
        });
    }
    
}
