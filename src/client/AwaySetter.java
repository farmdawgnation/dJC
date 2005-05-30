/*
 * dJC: The dAmn Java Client
 * AwaySetter.java
 * ©2005 The dAmn Java Project
 *
 * This software and its source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Class to give a graphical frontend to setting away.
 * @author MSF
 */
public class AwaySetter implements ActionListener {
    private damnChatPage dCP;
    private damnAppGUI dJgui;
    private JFrame frame;
    private JTextField awayField;
    private JButton goAway;
    private JButton cancel;
    
    public AwaySetter(damnChatPage cpObj, damnAppGUI guiObj) {
        dCP = cpObj;
        dJgui = guiObj;
        
        frame = new JFrame("Away Message");
        frame.setResizable(false);
        frame.setLayout(new FlowLayout());
        
        awayField = new JTextField(20);
        goAway = new JButton("Go Away");
        goAway.addActionListener(this);
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        
        frame.getContentPane().add(awayField);
        frame.getContentPane().add(goAway);
        frame.getContentPane().add(cancel);
        frame.pack();
    }
    
    public void show() {
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == goAway) {
            dJgui.awayStat(true);
            dCP.setAway(awayField.getText());
        }
        awayField.setText("");
        frame.setVisible(false);
    }
}
