package client;
/*
 * dJC: The dAmn Java Client
 * damnChatPage.java
 * �2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.lang.*;
import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.html.*;

/**
 * This is the class which manages the chat pages.
 * @version 0.2.2
 * @author MSF
 */
public class damnChatPage implements ActionListener, HyperlinkListener, KeyListener {
    private ArrayList<JPanel> chatPages;
    private ArrayList<JEditorPane> chatTerminals;
    private ArrayList<JScrollPane> chatScrollPanes;
    ArrayList<JTextField> chatFields;
    private ArrayList<String> channelList;
    private ArrayList<damnChatMemberList> chatMemberLists;
    private damnProtocol dP;
    private damnApp dJ;
    private String awaymsg;

    /**
     * Initilizes an instance of damnChatPage.
     * @param app A reference to the damnApp object.
     * @param protocol A reference to the damnProtocol object.
     */
    public damnChatPage(damnApp app, damnProtocol protocol) {
        dP = protocol;
        dJ = app;
        chatPages = new ArrayList<JPanel>();
        chatTerminals = new ArrayList<JEditorPane>();
        chatScrollPanes = new ArrayList<JScrollPane>();
        chatFields = new ArrayList<JTextField>();
        channelList = new ArrayList<String>();
        chatMemberLists = new ArrayList<damnChatMemberList>();
        awaymsg = null;
    }
    
    /**
     * Adds a Chat Page
     * @param chatname The name of the channel to add a page for.
     * @param tabbedPane A reference to the Application's Tabbed Pane.
     */
    public void addChatPage(String chatname, JTabbedPane tabbedPane) {
        JPanel chatPage = new JPanel(new BorderLayout(5,5));
        chatPage.setName("#" + chatname);
        
        JEditorPane chatTerminal = new JEditorPane();

        
        //chatTerminal.setLineWrap(true);
        chatTerminal.setEditable(false);
        chatTerminal.setContentType("text/html");
        chatTerminal.setText("<html><head><style type=\"text/css\">\n a { color:#222222 } \n td.tn { margin-right:2px; margin-left: 2px; margin-top:2px; margin-bottom:2px; } </style>"+
                "</head><body></body></html>");
        chatTerminal.addHyperlinkListener(this);
        JScrollPane chatScrollPane = new JScrollPane(chatTerminal, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setAutoscrolls(true);
        chatPage.add(chatScrollPane, BorderLayout.CENTER);
        
        JTextField chatField = new JTextField(20);
        chatField.addActionListener(this);
        chatField.addKeyListener(this);
        chatPage.add(chatField, BorderLayout.PAGE_END);
        
        damnChatMemberList memberList = new damnChatMemberList();
        memberList.generateHtml();
        JScrollPane memberListScrollPane = new JScrollPane(memberList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPage.add(memberListScrollPane, BorderLayout.LINE_END);
        
        tabbedPane.add(chatPage);
        
        chatPages.add(chatPage);
        chatTerminals.add(chatTerminal);
        chatScrollPanes.add(chatScrollPane);
        chatFields.add(chatField);
        chatMemberLists.add(memberList);
        channelList.add(chatname);
    }
    
    /** 
     * Deletes a Chat Page
     * @param chatname The name of the channel to delete the page for.
     * @param tabbedPane a reference to the application's Tabbed Pane.
     */
    public void delChatPage(String chatname, JTabbedPane tabbedPane) {
        int index = findPages(chatname);
        
        tabbedPane.remove(chatPages.get(index));
        
        channelList.remove(index);
        chatMemberLists.remove(index);
        chatFields.remove(index);
        chatTerminals.remove(index);
        chatPages.remove(index);
    }
    
    /** 
     * Another standard issue message handler.
     */
    public void actionPerformed(ActionEvent e) {
        JTextField chatField = chatFields.get(chatFields.indexOf(e.getSource()));
        if(chatField.getText().startsWith("/") && chatField.getText().startsWith("/me ") == false
                && chatField.getText().startsWith("/topic ") == false && chatField.getText().startsWith("/title ") == false
                && chatField.getText().startsWith("/kick ") == false && chatField.getText().startsWith("/admin ") == false) {
            dJ.actionPerformed(e);
        } else {
            if(chatField.getText().startsWith("/topic ") || chatField.getText().startsWith("/title ")) {
                String channel = chatPages.get(chatFields.indexOf(e.getSource())).getName();
                String comparts[] = chatField.getText().split(" ", 2);
                dP.doSet(channel.substring(1), comparts[0].substring(1), comparts[1]);
            } else if(chatField.getText().startsWith("/kick ")) {
                String channel = chatPages.get(chatFields.indexOf(e.getSource())).getName();
                String comparts[] = chatField.getText().split(" ", 3);
                if(comparts[2] != null) {
                    dP.doKick(channel.substring(1), comparts[1], comparts[2]);
                } else {
                    dP.doKick(channel.substring(1), comparts[1], " ");
                }
            } else if(chatField.getText().startsWith("/admin ")) {
                String channel = chatPages.get(chatFields.indexOf(e.getSource())).getName();
                String parts[] = chatField.getText().split(" ", 2);
                dP.doAdmin(channel.substring(1), parts[1]);
            } else {
                JPanel chatPage = chatPages.get(chatFields.indexOf(e.getSource()));
                dP.doSendMessage(chatPage.getName().substring(1), chatField.getText());
            }
        }
        if(!chatField.getText().equalsIgnoreCase("You are away - you must unset away before you can talk.")) {
            chatField.setText("");
        }
    }

    /**
     * Inserts a line into the HTML doucment
     * @param editor the JEditorPane component
     * @param html the line to be inserted
     * @param location the valid location in the document, where to insert
     */
    private void insertHTML(JEditorPane editor, String html, int location)
            throws Exception {
        HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
        Document doc = editor.getDocument();
        StringReader reader = new StringReader(html);
        kit.read(reader, doc, location);
    }
    
    /**
     * Writes a user message to a chat page.
     * @param channel The name of the channel the message is going to.
     * @param user The user the message is coming from.
     * @param message The message.
     */
    public synchronized void echoChat(String channel, String user, String message) {
        JEditorPane chatTerminal = chatTerminals.get(findPages(channel));
        try {
            String highLight = "";
            if(message.toLowerCase().contains(dP.getUser().toLowerCase())) highLight = "bgcolor=\"#BBC2BB\"";
            
            if(message.toLowerCase().startsWith(dP.getUser().toLowerCase())) sendAwayAlert(channel);
            
            String style = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr "+highLight+"><td valign=\"middle\" nowrap>";
            String styleEnd = "</table>";

            insertHTML(chatTerminal, style+"&lt;<B>"+ user + "</B>&gt;&nbsp;<td valign=\"middle\">" +message+ styleEnd, chatTerminal.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        chatTerminal.setCaretPosition(chatTerminal.getDocument().getLength());
//        chatTerminal.invalidate();

    }
    
    
    /**
     * Writes a misc message to a chat page.
     * @param channel The name of the channel the message is going to.
     * @param message The message.
     */
    public synchronized void echoChat(String channel, String message) {
        JEditorPane chatTerminal = chatTerminals.get(findPages(channel));
        try {
            String highLight = "";
            if(message.toLowerCase().contains(dP.getUser().toLowerCase()) && !message.toLowerCase().startsWith("*** " + dP.getUser().toLowerCase())) {
                highLight =  "bgcolor=\"#BBC2BB\"";
                sendAwayAlert(channel);
            }

            String style = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr "+highLight+"><td valign=\"middle\">";
            String styleEnd = "</table>";

            insertHTML(chatTerminal, style+message+styleEnd, chatTerminal.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        chatTerminal.setCaretPosition(chatTerminal.getDocument().getLength());
//        chatTerminal.invalidate();
        
    }
    
    /**
     * Locates the chat page object for a specified channel.
     * @param channel The name of the channel.
     * @return The index of the chat page in the list. Returns -1 if no such page exists.
     */
    private int findPages(String channel) {
        String select;
        for(int i=0; i<channelList.size(); i++) {
            select = channelList.get(i);
            if(select.equalsIgnoreCase(channel)) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Locates a member list.
     * @param channel The channel to locate a list for.
     * @return A DefaultListModel object for the member list.
     */
    public damnChatMemberList getMemberList(String channel) {
        return chatMemberLists.get(findPages(channel));
    }
    
    /**
     * Sets the object to away mode.
     * @param message The away message.
     */
    public void setAway(String message) {
        awaymsg = message;
        for(int i=0;i <chatFields.size();i++) {
            chatFields.get(i).setText("You are away - you must unset away before you can talk.");
            chatFields.get(i).setEditable(false);
            dP.doSendMessage(chatPages.get(i).getName().substring(1),  "/me is away: " + message);
        }
    }
    
    /**
     * Unsets the away mode.
     */
    public void unsetAway() {
        awaymsg = null;
        for(int i=0;i<chatFields.size();i++) {
            chatFields.get(i).setText("");
            chatFields.get(i).setEditable(true);
            dP.doSendMessage(chatPages.get(i).getName().substring(1), "/me is back.");
        }
    }
    
    /**
     * Will send the away alert if the away state is enabled.
     * @param channel The Channel to send the alert to.
     */
    public void sendAwayAlert(String channel) {
        if(awaymsg != null) {
            dP.doSendMessage(channel, "/me is away: " + awaymsg);
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        if(hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            /*try {
                Runtime.getRuntime().exec("config.xml");
            } catch(IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    public void keyPressed(KeyEvent keyEvent) {
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
        if(keyEvent.getKeyChar() == '\t') {
            System.out.println("TAB");
        }
    }
}
