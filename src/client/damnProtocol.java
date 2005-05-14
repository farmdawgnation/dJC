package client;
/*
 * dJC: The dAmn Java Client
 * damnProtocol.java
 * �2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */
import java.io.*;
import java.net.*;
import java.util.regex.*;

/**
 * The interface class for the protocol.
 * @version 0.2.2
 * @author MSF
 */
public class damnProtocol {
    private damnApp dJ;
    damnComm dC;
    private String username;
    private String password;
    
    /**
     * Protocol Interface Constructor
     * @param appObj A reference to the Application's dAmnApp object.
     */
    public damnProtocol(damnApp appObj) {
        dJ = appObj;
    }
    
    /**
     * Sets the user's information in the protocol fields.
     * @param user The username.
     * @param pass The authtoken.
     */
    public void setUserInfo(String user, String pass) {
        username = user;
        password = pass;
    }
    
    /**
     * Builds a dAmn Packet.
     * @param termnewline Indicates wether or not to terminate in a newline
     * @param commadn The command to send.
     * @return The packet data.
     */
    private String buildPacket(int termnewline, String command) {
        String data;
        
        data = command + '\n' + '\0';
        return data;
    }

    /**
     * Builds a dAmn Packet.
     * @param termnewline Indicates wether or not to terminate in a newline
     * @param commadn The command to send.
     * @param args The command arguments.
     * @return The packet data.
     */
    private String buildPacket(int termnewline, String command, String ... args) {
        //TODO: Write buildPacket() method.
        String data;
        
        data = command + '\n';
        
        for(int i=0;i<args.length;i++) {
            //Append args
            data += args[i];
            if(i == args.length && termnewline == 1) {
                data += '\n';
            } else if(i != args.length) {
                data += '\n';
            }
        }
        
        data += '\0';
        return data;
    }
    
    /**
     * Splits a packet up by newlines so it can be parsed.
     * @param data The raw packet data.
     * @return An array of strings. Each string holds a line of data.
     */
    private String[] splitPacket(String data) {
        //TODO: Write parsePacket() method.
        return data.split("\n");
    }
    
    /**
     * The message handler.
     * @param data The data to handle.
     * @param dC The reference to the Application's damnComm object.
     */
    public void handleMessage(String data, damnComm dC) {
        String[] tmpBox;
        tmpBox = splitPacket(data);
        dJ.terminalEcho(1, tmpBox[0]);
        
        try {
            if(tmpBox[0].equalsIgnoreCase("ping")) {
                dC.writeData(buildPacket(1, "pong"));
            } else if(tmpBox[0].startsWith("login ")) {
                String[] event = tmpBox[1].split("=");
                if(event[1].equalsIgnoreCase("ok")) {
                    dJ.terminalEcho(0, "Login Successful");
                } else {
                    dJ.terminalEcho(0, "Login Unsuccessful, please close connection and try again.");
                }
            } else if(tmpBox[0].startsWith("recv chat:")) {
                if(tmpBox[2].equalsIgnoreCase("msg main")) {
                    tmpBox[5] = processTablumps(tmpBox[5]);
                    String[] fromtxt = tmpBox[3].split("=");
                    String[] infotext = tmpBox[0].split(":");
                    dJ.echoChat(infotext[1], fromtxt[1], tmpBox[5]);
                } else if(tmpBox[2].equalsIgnoreCase("action main")) {
                    tmpBox[5] = processTablumps(tmpBox[5]);
                    String[] fromtext = tmpBox[3].split("=");
                    String[] infotext = tmpBox[0].split(":");
                    dJ.echoChat(infotext[1], "*** " + fromtext[1] + " " + tmpBox[5]);
                } else if(tmpBox[2].startsWith("join ")) {
                    String[] whoisit = tmpBox[2].split(" ");
                    String[] infotext= tmpBox[0].split(":");
                    dJ.echoChat(infotext[1], "*** " + whoisit[1] + " has joined.");
                    dJ.getChatMemberList(infotext[1]).addElement(whoisit[1]);
                } else if(tmpBox[2].startsWith("part ")) {
                    String[] whoisit = tmpBox[2].split(" ");
                    String[] infotext= tmpBox[0].split(":");
                    dJ.echoChat(infotext[1], "*** " + whoisit[1] + " has left.");
                    dJ.getChatMemberList(infotext[1]).remove(dJ.searchList(infotext[1], whoisit[1]));
                } else if(tmpBox[2].startsWith("kicked ")) {
                    String[] whoisit = tmpBox[2].split(" ");
                    String[] kicker = tmpBox[3].split("=");
                    String[] infotext = tmpBox[0].split(":");
                    dJ.echoChat(infotext[1], "*** " + whoisit[1] + "has been kicked by " + kicker[1] + " ** " + tmpBox[6]);
                    dJ.getChatMemberList(infotext[1]).remove(dJ.searchList(infotext[1], whoisit[1]));
                }
            } else if(tmpBox[0].startsWith("join chat:")) {
                String linea[] = tmpBox[0].split(":");
                String lineb[] = tmpBox[1].split("=");

                if(lineb[1].equalsIgnoreCase("ok")) {
                    dJ.createChat(linea[1]);
                    dJ.terminalEcho(0, "Successfully joined #" + linea[1]);
                } else if(lineb[1].equalsIgnoreCase("chatroom doesn't exist")) {
                    dJ.terminalEcho(0, "Chat room #" + linea[1] + " does not exist.");
                }
            } else if(tmpBox[0].startsWith("part chat:")) {
                String linea[] = tmpBox[0].split(":");
                String lineb[] = tmpBox[1].split("=");

                if(lineb[1].equalsIgnoreCase("ok")) {
                    dJ.deleteChat(linea[1]);
                    dJ.terminalEcho(0, "Successfully parted #" + linea[1]);
                } else {
                    dJ.terminalEcho(0, "Unreconized Error: " + lineb[1]);
                }
            } else if(tmpBox[0].startsWith("property chat:")) {
                String linea[] = tmpBox[0].split(":");
                String lineb[] = tmpBox[1].split("=");

                if(lineb[1].equalsIgnoreCase("members")) {
                    String[] propertysplit = data.split("\n\n");
                    for(int i=1; i<propertysplit.length; i++) {
                        String[] dataSplit = propertysplit[i].split("\n");
                        String[] linec = dataSplit[0].split(" ");
                        dJ.getChatMemberList(linea[1]).addElement(linec[1]);
                    }
                } else if(lineb[1].equalsIgnoreCase("topic")) {
                    if(tmpBox.length >= 5) {
                        tmpBox[5] = processTablumps(tmpBox[5]);
                        dJ.echoChat(linea[1], "*** Topic for #" + linea[1] + ": " + tmpBox[5]);
                    }
                } else if(lineb[1].equalsIgnoreCase("title")) {
                    if(tmpBox.length >= 5) {
                        tmpBox[5] = processTablumps(tmpBox[5]);
                        dJ.echoChat(linea[1], "*** Title for #" + linea[1] + ": " + tmpBox[5]);
                    }
                } 
            } else if(tmpBox[0].startsWith("property login:")) {
                    dJ.terminalEcho(1, "User infos: ");
                    for (int i=0; i<8; i++)
                        dJ.terminalEcho(1, tmpBox[i+1]);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends the authientication messages.
     */
    public void doHandshake() {
        //Initial Handshake
        dC.writeData(buildPacket(1, "dAmnClient 0.2", "agent=dJC-0.2"));
        dJ.terminalEcho(1, "dAmnClient 0.2....");
        dC.writeData(buildPacket(1, "login " + username, "pk=" + password));
        dJ.terminalEcho(1, "login...");
    }
    
    /**
     * Joins a channel.
     * @param channel The channel to join.
     */
    public void doJoinChannel(String channel) {
        dC.writeData(buildPacket(1, "join chat:" + channel));
        dJ.terminalEcho(1, "join #" + channel + "...");
    }
    
    /**
     * Parts from a channel.
     * @param channel The channel to part from.
     */
    public void doPartChannel(String channel) {
        dC.writeData(buildPacket(1, "part chat:" + channel));
        dJ.terminalEcho(1, "part #" + channel + "...");
    }
    
    /**
     * Sends a message to a channel.
     * @param channel The channel the message should be sent to.
     * @param message The message.
     */
    public void doSendMessage(String channel, String message) {
        if(message.startsWith("/me ")) {
            String[] pieces = message.split("/me ");
            dC.writeData(buildPacket(0, "send chat:" + channel, "", "action main", "", pieces[1]));
        } else {
            dC.writeData(buildPacket(0, "send chat:" + channel, "", "msg main", "", message));
        }
    }
    
    /**
     * Sets a property in the channel.
     * @param channel The channel for which the property will be set.
     * @param property The channel property to be set.
     * @param value The value to give the property.
     */
    public void doSet(String channel, String property, String value) {
        dC.writeData(buildPacket(0, "set chat:" + channel, "p=" + property, "", value));
    }
    
    /**
     * Kicks a user from the specified channel.
     * @param channel The channel from which to kick.
     * @param user The username to kick.
     * @param reason The reason for the kick.
     */
    public void doKick(String channel, String user, String reason) {
        dC.writeData(buildPacket(0, "kick chat:" + channel, "u=" + user, "", reason));
    }
    
     /**
     * Gets user information.
     * @param user The username to get info from.
     * @param property The property.
     */
    public void doGetUserInformation(String user) {
        dC.writeData(buildPacket(0, "get login:" + user, "p=info"));
    }
    
     /**
     * Promote a user to a privilege class
     * @param channel The channel for which the property will be set
     * @param user The username to promote privilege to
     * @param privClass The promoted privilege class 
     */
    public void doPromote(String channel, String user, String privClass) {
               
        dC.writeData(buildPacket(0, "send chat:" + channel, "", "promote "+user, "pc="+privClass));
    }
    
     /**
     * Demote a user to a privilege class
     * @param channel The channel for which the property will be set
     * @param user The username to demtoe privilege to
     * @param privClass The destination privilege class 
     */
    public void doDemote(String channel, String user, String privClass) {
        dC.writeData(buildPacket(0, "send chat:" + channel, "", "demote "+user, "pc="+privClass));
    }
    
    
    
    /**
     * Processes Tablumps sent from the server.
     * @param rawdata The message the user sent.
     * @return The HTML Formatted message. Tablump free!
     */
    private String processTablumps(String rawdata) {
        //Emoticons
        Pattern thePattern;
        Matcher theMatcher;
        
        if(rawdata.contains("&emote\t")) {
            thePattern = Pattern.compile("&emote\t([0-9A-Za-z:=()\\|]+)\t([0-9]+)\t([0-9]+)\t([0-9A-Za-z=:!'() \\|]+)\t([0-9a-z/=.]+)\t");
            theMatcher = thePattern.matcher(rawdata);
            rawdata = theMatcher.replaceAll("<img src=\"http://e.deviantart.com/emoticons/$5\" alt=\"$4\">");
        }

        if(rawdata.contains("&a\t")) {
            thePattern = Pattern.compile("&a\t([0-9A-Za-z:.()/\\|]+)\t\t([0-9A-Za-z:!'() \\|]+)*(&/a\t)*");
            theMatcher = thePattern.matcher(rawdata);
            rawdata = theMatcher.replaceAll("<a href=\"$1\">$2</a>");
        }

        if(rawdata.contains("&link\t")) {

            thePattern = Pattern.compile("&link\t([0-9A-Za-z:.()/\\|]+)\t([a-zA-Z]+)*(&(\t))*");
            theMatcher = thePattern.matcher(rawdata);
            rawdata = theMatcher.replaceAll("<a href=\"$1\">$1</a>");
        }
        
       
        //Formatting
        thePattern = Pattern.compile("&([a-zA-Z])\t([a-zA-Z0-9]+)&/([a-zA-Z])\t");
        theMatcher = thePattern.matcher(rawdata);
        rawdata = theMatcher.replaceAll("<$1>$2</$3>");
        
        if(rawdata.contains("&sub\t")) {
            thePattern = Pattern.compile("&sub\t([^&]+)(&/sub\t)*");
            theMatcher = thePattern.matcher(rawdata);
            rawdata = theMatcher.replaceAll("<font size=-1>$1</font>");
        }

        
        //:dev...:
        if(rawdata.contains("&dev")) {
            thePattern = Pattern.compile("&dev\t([~!@$%^*=])\t([A-Za-z0-9]+)\t");
            theMatcher = thePattern.matcher(rawdata);
            rawdata = theMatcher.replaceAll("<a href=\"http://$2.deviantart.com\">$1$2</a>");
        }
        
        rawdata = rawdata.replaceAll("\t", "(t)");
        
        return rawdata;
    }
}
