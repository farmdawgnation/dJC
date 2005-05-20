/*
 * dJC: The dAmn Java Client
 * damnAppGUI.java
 * ©2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This is the application GUI Class.
 * @author MSF
 */
public class damnAppGUI implements ActionListener {
    private damnApp dJ;
    private damnChatPage dCP;
    private JTabbedPane tabbedPane;
    private JPanel serverPage;
    private JTextArea serverTerminal;
    private JFrame frame;
    private JTextField serverCommandField;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem connectItem;
    private JMenuItem disconnectItem;
    private JMenuItem preferencesItem;
    private JMenuItem exitItem;
    private JMenuItem aboutItem;
    
    /**
     * Constructs the damnAppGUI Object.
     * @param appObj A reference to damnApp.
     */
    public damnAppGUI(damnApp appObj, damnChatPage chtPageObj) {
        dJ = appObj;
        dCP = chtPageObj;
    }
    
    /**
     * Constructs and shows the interface for the application.
     */
    public void damnShowInterface() {
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
        serverTerminal.setEditable(false);
        JScrollPane serverScrollPane = new JScrollPane(serverTerminal, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        serverPage.add(serverScrollPane, BorderLayout.CENTER);
        
        serverTerminal.setText("dJC: The dAmn Java Client\nVersion 0.3\n©2005 The dAmn Java Project\nType '/about' for more info.'\n");
        
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
     * Action handler.
     * This function decides what to do when the user types a server command.
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String parts[];
        if(e.getSource() == serverCommandField) {
            parts = serverCommandField.getText().split(" ");
        } else if(e.getSource() == preferencesItem) {
            dJ.showProperties();
            return;
        } else if(e.getSource() == connectItem) {
            dJ.connectUserPass("", "");
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
            return;
        } else if(e.getSource() == disconnectItem) {
            connectItem.setEnabled(true);
            disconnectItem.setEnabled(false);
            return;
        } else {
            JTextField txtfld = dCP.chatFields.get(dCP.chatFields.indexOf(e.getSource()));
            parts = txtfld.getText().split(" ");
        }
        if(parts[0].equalsIgnoreCase("/connect")) {
            dJ.connectUserPass(parts[1], parts[2]);
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        } else if(parts[0].equalsIgnoreCase("/tokenconnect")) {
            dJ.connectUserAuth(parts[1], parts[2]);
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        } else if(parts[0].equalsIgnoreCase("/join")) {
            dJ.joinChannel(parts[1]);
        } else if(parts[0].equalsIgnoreCase("/part")) {
            dJ.partChannel(parts[1]);
        } else if(parts[0].equalsIgnoreCase("/disconnect")) {
            dJ.disconnect();
            connectItem.setEnabled(true);
            disconnectItem.setEnabled(false);
        } else if(parts[0].equalsIgnoreCase("/about")) {
            dJ.terminalEcho(0, "");
            dJ.terminalEcho(0, "dJC: The dAmn Java Client");
            dJ.terminalEcho(0, "http://www.sourceforge.net/projects/damnjava");
            dJ.terminalEcho(0, "");
            dJ.terminalEcho(0, "Written by...");
            dJ.terminalEcho(0, "\tMSF - Lead Developer/Project Manager");
            dJ.terminalEcho(0, "\tEric Olander - Developer");
            dJ.terminalEcho(0, "\tMiklosi Attila - Developer");
            dJ.terminalEcho(0, "");
            dJ.terminalEcho(0, "If you are interested in getting involved: Let MSF know.");
            dJ.terminalEcho(0, "Now back to your regularly scheduled programming...");
            dJ.terminalEcho(0, "");
        } else if(parts[0].equalsIgnoreCase("/token")) {
            TokenFetcher tf = new TokenFetcher("www.deviantart.com");

            dJ.terminalEcho(0, tf.doTokenFetch(parts[1], parts[2]));
        } else {
            dJ.terminalEcho(0, "Unknown Command.");
        }
        serverCommandField.setText("");
    }
    
    /**
     * Gets the tabbed pane.
     * @returns A reference to the tabbed pane.
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
    
    /**
     * Get the server terminal.
     * @returns A reference to the server terminal.
     */
    public JTextArea getServerTerminal() {
        return serverTerminal;
    }
}
