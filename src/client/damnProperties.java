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
 *
 * @author MSF
 */
public class damnProperties implements ActionListener {
    private JFrame frame;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel serverLabel;
    private JTextField serverField;
    private JLabel portLabel;
    private JTextField portField;
    private JLabel autojoinLabel;
    private JTextField autojoinField;
    private JButton saveButton;
    private JButton cancelButton;
    private damnConfig conf;
    
    /** Creates a new instance of damnProperties */
    public damnProperties(damnConfig dconf) {
        frame = new JFrame("dJC User Preferences");
        frame.setResizable(false);
        usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        passwordField.setEchoChar('*');
        serverLabel = new JLabel("Server:");
        serverField = new JTextField(20);
        portLabel = new JLabel("Port:");
        portField = new JTextField(20);
        autojoinLabel = new JLabel("Auto-Joins:");
        autojoinField = new JTextField(20);
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        setupProperties();
        conf = dconf;
    }
    
    /** Displays the new window. */
    public void setupProperties() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.LINE_AXIS));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        panel.add(usernamePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.LINE_AXIS));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        panel.add(passwordPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel serverPanel = new JPanel();
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.LINE_AXIS));
        serverPanel.add(serverLabel);
        serverPanel.add(serverField);
        panel.add(serverPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel portPanel = new JPanel();
        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.LINE_AXIS));
        portPanel.add(portLabel);
        portPanel.add(portField);
        panel.add(portPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel autojoinPanel = new JPanel();
        autojoinPanel.setLayout(new BoxLayout(autojoinPanel, BoxLayout.LINE_AXIS));
        autojoinPanel.add(autojoinLabel);
        autojoinPanel.add(autojoinField);
        panel.add(autojoinPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        frame.getContentPane().add(panel);
        frame.pack();
    }

    public void showProperties() {
        usernameField.setText(conf.getUser());
        passwordField.setText(conf.getPassword());
        serverField.setText(conf.getHost());
        portField.setText(String.format("%d", conf.getPort()));
        
        String[] channels = conf.getChannels();
        String autojoinval = new String();
        for(int i=0; i<channels.length; i++) {
            if(i == 0) {
                autojoinval = channels[i];
            } else {
                autojoinval += "," + channels[i];
            }
        }
        
        autojoinField.setText(autojoinval);
        
        frame.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == saveButton) {
            conf.setHost(serverField.getText());
            conf.setPort(3900);
            conf.setUser(usernameField.getText());
            conf.setPassword(passwordField.getText());
            
            conf.clearChannels();
            String[] channels = autojoinField.getText().replaceAll(" ", "").split(",");
            for(int i=0;i < channels.length;i++) {
                conf.addChannel(channels[i]);
            }
            
            try {
                conf.writeConfig();
            } catch(javax.xml.transform.TransformerException ex) {
                ex.printStackTrace();
            } catch(java.io.FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        frame.setVisible(false);
    }
    
}
