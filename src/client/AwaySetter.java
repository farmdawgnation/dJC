/*
 * AwaySetter.java
 *
 * Created on May 25, 2005, 3:20 PM
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
