package client;
/*
 * dJC: The dAmn Java Client
 * damnChatPage.java
 * ©2005 The dAmn Java Project
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
 * @author MSF
 */
public class damnChatPage implements ActionListener, HyperlinkListener, KeyListener, ChangeListener {
    private JTabbedPane tabbedPane;
    private ArrayList<JPanel> chatPages;
    private ArrayList<JEditorPane> chatTitles;
    private ArrayList<JEditorPane> chatTopics;
    private ArrayList<JEditorPane> chatTerminals;
    private ArrayList<JScrollPane> chatScrollPanes;
    ArrayList<JTextField> chatFields;
    private ArrayList<String> channelList;
    private ArrayList<damnChatMemberList> chatMemberLists;
    private damnProtocol dP;
    private damnApp dJ;
    private String awaymsg;
    private String defaultHtml;
    private int searchSet = 0;

    /**
     * Initilizes an instance of damnChatPage.
     * @param app A reference to the damnApp object.
     * @param protocol A reference to the damnProtocol object.
     */
    public damnChatPage(damnApp app, damnProtocol protocol) {
        dP = protocol;
        dJ = app;
        chatPages = new ArrayList<JPanel>();
        
        dP.setChatPage(this);
        
        chatTerminals = new ArrayList<JEditorPane>();
        chatTitles = new ArrayList<JEditorPane>();
        chatTopics = new ArrayList<JEditorPane>();
        chatScrollPanes = new ArrayList<JScrollPane>();
        chatFields = new ArrayList<JTextField>();
        channelList = new ArrayList<String>();
        chatMemberLists = new ArrayList<damnChatMemberList>();
        awaymsg = null;
        defaultHtml = new String("<html><head><style type=\"text/css\">\n a { color:#222222 } \n td.tn { margin-right:2px; margin-left: 2px; margin-top:2px; margin-bottom:2px; } </style>"+
                "</head><body></body></html><html><head><style type=\"text/css\">\n a { color:#222222 } \n td.tn { margin-right:2px; margin-left: 2px; margin-top:2px; margin-bottom:2px; } </style>"+
                "</head><body>");
    }
    
    /**
     * Assigns the tabbed pane to the damnChatPage object.
     * @param paneObj The pane to assign.
     */
    public void setPane(JTabbedPane paneObj) {
        tabbedPane = paneObj;
        tabbedPane.addChangeListener(this);
    }
    
    /**
     * Adds a Chat Page
     * @param chatname The name of the channel to add a page for.
     * @param tabbedPane A reference to the Application's Tabbed Pane.
     */
    public void addChatPage(int priv, String chatname) {
        JPanel chatPage = new JPanel(new BorderLayout(5,5));
        if(priv == 0) {
            chatPage.setName("<html><body><font color=\"black\">#" + chatname + "</font></body></html>");
        } else if(priv == 1) {
            chatPage.setName("<html><body><font color=\"black\">" + chatname + "</font></body></html>");
        }
        
        JEditorPane chatTitle = new JEditorPane();
        chatTitle.setEditable(false);
        chatTitle.setContentType("text/html");
        chatTitle.addHyperlinkListener(this);
        chatPage.add(chatTitle, BorderLayout.PAGE_START);
        
        JPanel topicTerminalPanel = new JPanel(new BorderLayout(5,5));
        
        JEditorPane chatTopic = new JEditorPane();
        chatTopic.setEditable(false);
        chatTopic.setContentType("text/html");
        chatTopic.addHyperlinkListener(this);
        topicTerminalPanel.add(chatTopic, BorderLayout.PAGE_START);
        
        JEditorPane chatTerminal = new JEditorPane();
        chatTerminal.setEditable(false);
        chatTerminal.setContentType("text/html");
        chatTerminal.setText(defaultHtml);
        chatTerminal.addHyperlinkListener(this);
        JScrollPane chatScrollPane = new JScrollPane(chatTerminal, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setAutoscrolls(true);
        topicTerminalPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        chatPage.add(topicTerminalPanel, BorderLayout.CENTER);
        
        JTextField chatField = new JTextField(20);
        chatField.setFocusTraversalKeysEnabled(false);
        chatField.addActionListener(this);
        chatField.addKeyListener(this);
        chatPage.add(chatField, BorderLayout.PAGE_END);
        
        damnChatMemberList memberList = new damnChatMemberList();
        memberList.generateHtml();
        JScrollPane memberListScrollPane = new JScrollPane(memberList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPage.add(memberListScrollPane, BorderLayout.LINE_END);
        if(priv == 1) {
            memberListScrollPane.setVisible(false);
        }
        
        tabbedPane.add(chatPage);
        
        chatPages.add(chatPage);
        chatTitles.add(chatTitle);
        chatTopics.add(chatTopic);
        chatTerminals.add(chatTerminal);
        chatScrollPanes.add(chatScrollPane);
        chatFields.add(chatField);
        chatMemberLists.add(memberList);
        if(priv == 1) {
            channelList.add("pchat:" + chatname);
        } else {
            channelList.add(chatname);
        }
    }
    
    /** 
     * Deletes a Chat Page
     * @param chatname The name of the channel to delete the page for.
     * @param tabbedPane a reference to the application's Tabbed Pane.
     */
    public void delChatPage(String chatname) {
        int index = findPages(chatname);
        
        tabbedPane.remove(chatPages.get(index));
        
        channelList.remove(index);
        chatMemberLists.remove(index);
        chatFields.remove(index);
        chatTerminals.remove(index);
        chatTopics.remove(index);
        chatTitles.remove(index);
        chatPages.remove(index);
    }
    
    /** 
     * Another standard issue message handler.
     */
    public void actionPerformed(ActionEvent e) {
        JTextField chatField = chatFields.get(chatFields.indexOf(e.getSource()));
        if(chatField.getText().startsWith("/") && chatField.getText().startsWith("/me ") == false
                && chatField.getText().startsWith("/topic ") == false && chatField.getText().startsWith("/title ") == false
                && chatField.getText().startsWith("/kick ") == false && chatField.getText().startsWith("/admin ") == false
                && chatField.getText().equalsIgnoreCase("/clear") == false && chatField.getText().startsWith("/promote ") == false
                && chatField.getText().startsWith("/demote ") == false) {
            dJ.actionPerformed(e);
        } else {
            if(chatField.getText().startsWith("/topic ") || chatField.getText().startsWith("/title ")) {
                String channel = channelList.get(chatFields.indexOf(e.getSource()));
                String comparts[] = chatField.getText().split(" ", 2);
                dP.doSet(channel, comparts[0].substring(1), comparts[1]);
            } else if(chatField.getText().startsWith("/kick ")) {
                String channel = channelList.get(chatFields.indexOf(e.getSource()));
                String comparts[] = chatField.getText().split(" ", 3);
                if(comparts.length == 3) {
                    dP.doKick(channel, comparts[1], comparts[2]);
                } else {
                    dP.doKick(channel, comparts[1], " ");
                }
            } else if(chatField.getText().startsWith("/admin ")) {
                String channel = channelList.get(chatFields.indexOf(e.getSource()));
                String parts[] = chatField.getText().split(" ", 2);
                dP.doAdmin(channel, parts[1]);
            } else if(chatField.getText().equalsIgnoreCase("/clear")) {
                chatTerminals.get(chatFields.indexOf(e.getSource())).setText(defaultHtml);
            } else if(chatField.getText().startsWith("/promote ")) {
                String channel = channelList.get(chatFields.indexOf(e.getSource()));
                String parts[] = chatField.getText().split(" ");
                if(parts.length > 2) {
                    dP.doPromote(channel, parts[1], parts[2]);
                } else if(parts.length == 2) {
                    dP.doPromote(channel, parts[1], "");
                } else {
                    dJ.terminalEcho(0, "promote: Not enough arguments.");
                }
            } else if(chatField.getText().startsWith("/demote ")) {
                String channel = channelList.get(chatFields.indexOf(e.getSource()));
                String parts[] = chatField.getText().split(" ");
                if(parts.length > 2) {
                    dP.doDemote(channel, parts[1], parts[2]);
                } else if(parts.length == 2) {
                    dP.doDemote(channel, parts[1], "");
                } else {
                    dJ.terminalEcho(0, "demote: Not enough arguments.");
                }
            } else {
                String channel = channelList.get(chatFields.indexOf(e.getSource()));
                if(channel.startsWith("pchat:")) {
                    dP.doSendPMessage(channel.split(":")[1], chatField.getText());
                } else {
                    dP.doSendMessage(channel, chatField.getText());
                }
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
            if(message.toLowerCase().indexOf(dP.getUser().toLowerCase()) != -1) highLight = "bgcolor=\"#BBC2BB\"";
            
            if(message.toLowerCase().startsWith(dP.getUser().toLowerCase())) sendAwayAlert(channel);
            
            String style = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr "+highLight+"><td valign=\"middle\" nowrap>";
            String styleEnd = "</table>";

            insertHTML(chatTerminal, style+"&lt;<B>"+ user + "</B>&gt;&nbsp;<td valign=\"middle\">" +message+ styleEnd, chatTerminal.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if(tabbedPane.getSelectedIndex() != findPages(channel)+1 && message.indexOf(dP.getUser()) == -1) {
            tabbedPane.setTitleAt(findPages(channel)+1, tabbedPane.getTitleAt(findPages(channel)+1).replace("<font color=\"black\">", "<font color=\"blue\">"));
        }
        
        if(tabbedPane.getSelectedIndex() != findPages(channel)+1 && message.indexOf(dP.getUser()) != -1) {
            tabbedPane.setTitleAt(findPages(channel)+1, tabbedPane.getTitleAt(findPages(channel)+1).replace("<font color=\"black\">", "<font color=\"red\">"));
            tabbedPane.setTitleAt(findPages(channel)+1, tabbedPane.getTitleAt(findPages(channel)+1).replace("<font color=\"blue\">", "<font color=\"red\">"));
        }
        
        chatTerminal.setCaretPosition(chatTerminal.getDocument().getLength());

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
            if(message.toLowerCase().indexOf(dP.getUser().toLowerCase()) != -1 && !message.toLowerCase().startsWith("*** " + dP.getUser().toLowerCase())) {
                highLight =  "bgcolor=\"#BBC2BB\"";
                sendAwayAlert(channel);
            }

            String style = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr "+highLight+"><td valign=\"middle\">";
            String styleEnd = "</table>";

            insertHTML(chatTerminal, style+message+styleEnd, chatTerminal.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if(tabbedPane.getSelectedIndex() != findPages(channel)+1 && message.indexOf(dP.getUser()) == -1) {
            tabbedPane.setTitleAt(findPages(channel)+1, tabbedPane.getTitleAt(findPages(channel)+1).replace("<font color=\"black\">", "<font color=\"blue\">"));
        }
        
        if(tabbedPane.getSelectedIndex() != findPages(channel)+1 && message.indexOf(dP.getUser()) != -1) {
            tabbedPane.setTitleAt(findPages(channel)+1, tabbedPane.getTitleAt(findPages(channel)+1).replace("<font color=\"black\">", "<font color=\"red\">"));
            tabbedPane.setTitleAt(findPages(channel)+1, tabbedPane.getTitleAt(findPages(channel)+1).replace("<font color=\"blue\">", "<font color=\"red\">"));
        }
        
        chatTerminal.setCaretPosition(chatTerminal.getDocument().getLength());
        
    }
    
    /**
     * Locates the chat page object for a specified channel.
     * @param channel The name of the channel.
     * @return The index of the chat page in the list. Returns -1 if no such page exists.
     */
    public int findPages(String channel) {
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
            if(chatPages.get(i).getName().indexOf("IdleRPG") == -1)
                dP.doSendMessage(channelList.get(i),  "/me is away: " + message);
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
            if(chatPages.get(i).getName().indexOf("IdleRPG") == -1)
                dP.doSendMessage(channelList.get(i), "/me is back.");
        }
    }
    
    /**
     * Will send the away alert if the away state is enabled.
     * @param channel The Channel to send the alert to.
     */
    public void sendAwayAlert(String channel) {
        if(awaymsg != null && !channel.equalsIgnoreCase("IdleRPG")) {
            dP.doSendMessage(channel, "/me is away: " + awaymsg);
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        if(hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                if(hyperlinkEvent.getURL().toString() != null) {
                    Runtime.getRuntime().exec(dJ.browserCommand() + " " + hyperlinkEvent.getURL().toString());
                } else {
                    System.out.println("NULL URL!");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void keyPressed(KeyEvent keyEvent) {
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
        damnChatMemberList memList = chatMemberLists.get(chatFields.indexOf(keyEvent.getSource()));
        if(keyEvent.getKeyChar() == '\t') {
            JTextField field = chatFields.get(chatFields.indexOf(keyEvent.getSource()));
            
            String[] parts = field.getText().split(" ");
            
            String result = memList.searchAgent(parts[parts.length-1]);
            field.setText("");
            
            for(int i=0;i<parts.length;i++) {
                if(i < parts.length-1) {
                    field.setText(field.getText() + parts[i] + " ");
                } else {
                    if(result != null) {
                        field.setText(field.getText() + result);
                    } else {
                        field.setText(field.getText() + parts[i]);
                    }
                }
            }
        } else {
            memList.searchAgentReset();
        }
    }

    public void stateChanged(ChangeEvent changeEvent) {
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).replace("<font color=\"blue\">", "<font color=\"black\">"));
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).replace("<font color=\"red\">", "<font color=\"black\">"));
    }
    
    /**
     * Sets the selected tab.
     * @param index Index of the tab to select.
     */
    public void changeSelectedTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }
    
    /**
     * Set the chat title.
     * @param channel The channel to change the title for.
     * @param title The title for the channel.
     */
    public void setChannelTitle(String channel, String title) {
        int index = this.findPages(channel);
        
        JEditorPane chatTitle = chatTitles.get(index);
        
        try {
            chatTitle.setText(defaultHtml);
            insertHTML(chatTitle, title, chatTitle.getDocument().getLength());
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        
        if(title.equals("")) {
            chatTitle.setVisible(false);
        } else {
            chatTitle.setVisible(true);
        }
    }
    
    /**
     * Set the channel topic.
     * @param channel The channel to change the topic for.
     * @param topic The topic for the channel.
     */
    public void setChannelTopic(String channel, String topic) {
        int index = this.findPages(channel);
        
        JEditorPane chatTopic = chatTopics.get(index);
        
        try {
            chatTopic.setText(defaultHtml);
            insertHTML(chatTopic, topic, chatTopic.getDocument().getLength());
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        
        if(topic.equals("")) {
            chatTopic.setVisible(false);
        } else {
            chatTopic.setVisible(true);
        }
    }
}
