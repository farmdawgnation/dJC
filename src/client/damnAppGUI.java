/*
 * dJC: The dAmn Java Client
 * damnAppGUI.java
 * ©2005 The dAmn Java Project
 *
 * This software and its source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and its source. Thank you.
 */

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This is the application GUI Class.
 * @author MSF
 */
public class damnAppGUI extends JFrame {
    private damnApp dJ;
    private damnChatPage dCP;
    private AwaySetter aws;
    private JTabbedPane tabbedPane;
    private JPanel serverPage;
    private JTextArea serverTerminal;
    private JTextField serverCommandField;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenu awayMenu;
    private JMenuItem connectItem;
    private JMenuItem disconnectItem;
    private JMenuItem preferencesItem;
    private JMenuItem exitItem;
    private JMenuItem aboutItem;
    private JMenuItem goAwayItem;
    private JMenuItem comeBackItem;
    
    /**
     * Constructs the damnAppGUI Object.
     * @param appObj A reference to damnApp.
     */
    public damnAppGUI(damnApp appObj, damnChatPage chtPageObj) {
        dJ = appObj;
        dCP = chtPageObj;
        damnShowInterface();
        aws = new AwaySetter(dCP, this);
    }
    
    /**
     * Constructs and shows the interface for the application.
     */
    private void damnShowInterface() {
        setTitle("dJC: The dAmn Java Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        menuBar = new JMenuBar();
        
        //File Menu
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        //Away Menu
        awayMenu = new JMenu("Away");
        menuBar.add(awayMenu);
        
        //Help Menu
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        
        //File Menu Items
        connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doConnect();
            }
        });
        fileMenu.add(connectItem);
        disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doDisconnect();
            }
        });
        disconnectItem.setEnabled(false);
        fileMenu.add(disconnectItem);
        preferencesItem = new JMenuItem("Preferences");
        preferencesItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dJ.showProperties();
            }
        });
        fileMenu.add(preferencesItem);
        exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);
        
        //Away Menu Items
        goAwayItem = new JMenuItem("Go Away");
        goAwayItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aws.show();
            }
        });
        awayMenu.add(goAwayItem);
        comeBackItem = new JMenuItem("Come Back");
        comeBackItem.setEnabled(false);
        comeBackItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dCP.unsetAway();
                awayStat(false);
            }
        });
        awayMenu.add(comeBackItem);
            
        
        //Help Menu Items
        aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);
        
        //Set the Menu Bar
        setJMenuBar(menuBar);
        
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
        serverCommandField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doServerCommand(evt);
            }
        });
        serverPage.add(serverCommandField, BorderLayout.PAGE_END);
        
        tabbedPane.add(serverPage);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(panel);
        
        pack();
        setVisible(true);
    }
    
    /**
     * This function decides what to do when the user types a server command.
     */
    public void doServerCommand(ActionEvent e) {
        String parts[];
        if(e.getSource() == serverCommandField) {
            parts = serverCommandField.getText().split(" ");
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
        } else if(parts[0].equalsIgnoreCase("/away")) {
            String[] peices;
            if(e.getSource() == serverCommandField) {
                peices = serverCommandField.getText().split(" ", 2);
            } else {
                JTextField txtfld = dCP.chatFields.get(dCP.chatFields.indexOf(e.getSource()));
                peices = txtfld.getText().split(" ", 2);
            }
            dCP.setAway(peices[1]);
        } else if(parts[0].equalsIgnoreCase("/back")) {
            dCP.unsetAway();
        } else {
            dJ.terminalEcho(0, "Unknown Command.");
        }
        serverCommandField.setText("");
    }
    
    /**
     * Handle connect 
     */
    private void doConnect() {
        dJ.connectUserPass("", "");
        connectItem.setEnabled(false);
        disconnectItem.setEnabled(true);
    }
    
    /**
     * Handle disconnect 
     */
    private void doDisconnect() {
        connectItem.setEnabled(true);
        disconnectItem.setEnabled(false);
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
    
    /**
     * Enable/Disable the Away menu options.
     * @param away Wether or not to be away.
     */
    public void awayStat(boolean away) {
        if(away) {
            comeBackItem.setEnabled(true);
            goAwayItem.setEnabled(false);
        } else {
            comeBackItem.setEnabled(false);
            goAwayItem.setEnabled(true);
        }
    }
}
