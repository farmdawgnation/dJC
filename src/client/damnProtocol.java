package client;
/*
 * dJC: The dAmn Java Client
 * damnProtocol.java
 * ?2005 The dAmn Java Project
 *
 * This software and its source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */
import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The interface class for the protocol.
 * @author MSF
 */
public class damnProtocol {
    private damnApp dJ;
    private damnComm dC;
    private damnConfig conf;
    private damnChatPage dCP;
    private int pendingData;
    private String username;
    private String password;
    private String[] whoisInfo;
    boolean whoisInfoReady;
    boolean whoisBadUsername;
    private HashMap<String, String> damnPacket;
    
   
    /**
     * Protocol Interface Constructor
     * @param appObj A reference to the Application's dAmnApp object.
     */
    public damnProtocol(damnApp appObj, damnConfig configObj) {
        dJ = appObj;
        conf = configObj;
        whoisInfo = new String[12];
        whoisInfoReady = false;
        whoisBadUsername = false;
        damnPacket = new HashMap<String, String>();
    }
    
    /**
     * Sets damnComm.
     * @param commObj The the damnComm object to link.
     */
    public void setComm(damnComm commObj) {
        dC = commObj;
    }
    
    /**
     * Sets damnChatPage object.
     * @param cpObj The damnChatPage object to link.
     */
    public void setChatPage(damnChatPage cpObj) {
        dCP = cpObj;
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
     * Gets the username for the protocol.
     * @returns The username.
     */
    public String getUser() {
        return username;
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
        StringBuffer data;
        
        data = new StringBuffer(command + '\n');
        
        for (int i=0; i<args.length; i++) {
            //Append args
            data.append(args[i]);
            if(i == args.length - 1 && termnewline == 0) {
                break;
            } else if(i != args.length) {
                data.append('\n');
            }
        }
        
        data.append('\0');
        return data.toString();
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
     * Maps the packet for use from the damnPacket object.
     * @param data The raw packet data.
     */
    private void mapPacket(String data) {
        String[] splitPacket = data.split("\n");
        
        for(int i=0;i<splitPacket.length;i++) {
            if(i == 0) {
                damnPacket.put("command", splitPacket[0]);
            } else {
                if(damnPacket.get("command").startsWith("property ") && damnPacket.containsKey("p") &&
                        (damnPacket.get("p").equals("members") || damnPacket.get("p").equals("privclasses"))) {
                    String[] propPacket = data.split("\n\n", 2);
                    damnPacket.put("value", propPacket[1]);
                    break;
                } else if(damnPacket.get("command").startsWith("property ") && damnPacket.containsKey("p") &&
                        damnPacket.containsKey("by") && damnPacket.containsKey("ts") &&
                        (damnPacket.get("p").equals("topic") || damnPacket.get("p").equals("title"))) {
                    String[] propPacket = data.split("\n\n", 2);
                    damnPacket.put("value", propPacket[1]);
                    break;
                } else if(damnPacket.get("command").startsWith("recv ") && !damnPacket.containsKey("type") &&
                        !splitPacket[i].equalsIgnoreCase("")) {
                    damnPacket.put("type", splitPacket[i]);
                } else if(damnPacket.containsKey("type") && damnPacket.containsKey("p")) {
                    if(damnPacket.get("type").equals("admin show")) {
                        String[] showPacket = data.split("\n\n", 3);
                        damnPacket.put("value", showPacket[2]);
                        break;
                    }
                } else {
                    if((!splitPacket[i].equalsIgnoreCase("") && splitPacket[i].indexOf("=") == -1) ||
                            (splitPacket[0].startsWith("recv ") && damnPacket.containsKey("type") && damnPacket.containsKey("from"))) {
                        damnPacket.put("value", splitPacket[i]);
                    } else if(!splitPacket[i].equalsIgnoreCase("") && splitPacket[i].indexOf("=") != -1) {
                        String[] property = splitPacket[i].split("=", 2);
                        damnPacket.put(property[0], property[1]);
                    }
                }
            }
        }
    }
    
    /**
     * The message handler.
     * @param data The data to handle.
     * @param dC The reference to the Application's damnComm object.
     */
    public void handleMessage(String data, damnComm dC) {
        mapPacket(data);
        //dJ.terminalEcho(1, tmpBox[0]);
        
        try {
            if(damnPacket.get("command").equalsIgnoreCase("ping")) {
                dC.writeData(buildPacket(1, "pong"));
            } else if(damnPacket.get("command").startsWith("login ")) {
                if(damnPacket.get("e").equalsIgnoreCase("ok")) {
                    dJ.terminalEcho(0, "Login Successful");
                    doMassJoin(conf.getChannels());
                } else {
                    dJ.terminalEcho(0, "Login Unsuccessful, please close connection and try again.");
                }
            } else if(damnPacket.get("command").startsWith("recv ")) {
                if(damnPacket.get("type").equalsIgnoreCase("msg main")) {
                    String value = processTablumps(damnPacket.get("value"));
                    String fromtxt = damnPacket.get("from");
                    String infotext = damnPacket.get("command").split(":")[1];
                    if(infotext.equalsIgnoreCase(username) && damnPacket.get("command").split(":").length == 3) {
                        infotext = damnPacket.get("command").split(":")[2];
                    }
                    if(damnPacket.get("command").split(" ")[1].startsWith("pchat:")) {
                        dCP.echoChat("pchat:" + infotext, fromtxt, value);
                    } else {
                        dCP.echoChat(infotext, fromtxt, value);
                    }
                } else if(damnPacket.get("type").equalsIgnoreCase("action main")) {
                    String value = processTablumps(damnPacket.get("value"));
                    String fromtext = damnPacket.get("from");
                    String infotext = damnPacket.get("command").split(":")[1];
                    if(infotext == getUser() && damnPacket.get("command").split(":").length == 3) {
                        infotext = damnPacket.get("command").split(":")[2];
                    }
                    if(damnPacket.get("command").split(" ")[1].startsWith("pchat:")) {
                        dCP.echoChat("pchat:" + infotext, "*** " + fromtext + " " + value);
                    } else {
                        dCP.echoChat(infotext, "*** " + fromtext + " " + value);
                    }
                } else if(damnPacket.get("type").startsWith("join ")) {
                    String whoisit = damnPacket.get("type").split(" ")[1];
                    String show = damnPacket.get("s");
                    String privclass = damnPacket.get("pc");
                    String symbol = damnPacket.get("symbol");
                    String infotext = damnPacket.get("command").split(":")[1];
                    if(damnPacket.get("command").split(":").length == 3) {
                        if(infotext.equalsIgnoreCase(getUser())) infotext = damnPacket.get("command").split(":")[2];
                        infotext = "pchat:" + infotext;
                        dCP.echoChat(infotext, "** " + whoisit + " has joined.");
                    } else {
                        if(show.equalsIgnoreCase("1") || conf.getShownotices()) {
                            dCP.echoChat(infotext, "** " + whoisit + " has joined.");
                        }
                        dCP.getMemberList(infotext).addUser(whoisit, symbol, privclass);
                        dCP.getMemberList(infotext).generateHtml();
                    }
                } else if(damnPacket.get("type").startsWith("part ")) {
                    String whoisit = damnPacket.get("type").split(" ")[1];
                    String show = damnPacket.get("s");
                    String reason = new String();
                    if(damnPacket.containsKey("r")) {
                        reason = damnPacket.get("r");
                    }
                    String infotext = damnPacket.get("command").split(":")[1];
                    if(damnPacket.get("command").split(":").length == 3) {
                        if(infotext.equalsIgnoreCase(getUser())) infotext = damnPacket.get("command").split(":")[2];
                        infotext = "pchat:" + infotext;
                        if(!damnPacket.containsKey("r")) {
                            dCP.echoChat(infotext, "** " + whoisit + " has left.");
                        } else {
                            dCP.echoChat(infotext, "** " + whoisit + " has left. [" + reason + "]");
                        }
                    } else {
                        if(show.equals("1") || conf.getShownotices()) {
                            if(!damnPacket.containsKey("r")) {
                                dCP.echoChat(infotext, "** " + whoisit + " has left.");
                            } else {
                                dCP.echoChat(infotext, "** " + whoisit + " has left. [" + reason + "]");
                            }
                        }
                        dCP.getMemberList(infotext).delUser(whoisit);
                        dCP.getMemberList(infotext).generateHtml();
                    }
                } else if(damnPacket.get("type").startsWith("kicked ")) {
                    String whoisit = damnPacket.get("type").split(" ")[1];
                    String kicker = damnPacket.get("by");
                    String infotext = damnPacket.get("command").split(":")[1];
                    dCP.echoChat(infotext, processTablumps("<b>** " + whoisit + " has been kicked by " + kicker + " ** " + damnPacket.get("value") + "</b>"));
                    dCP.getMemberList(infotext).delUser(whoisit);
                    dCP.getMemberList(infotext).generateHtml();
                } else if(damnPacket.get("type").startsWith("privchg ")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String who = damnPacket.get("type").split(" ")[1];
                    String bywho = damnPacket.get("by");
                    String newclass = damnPacket.get("pc");
                    
                    dCP.getMemberList(channel).setClass(who, newclass);
                    dCP.getMemberList(channel).generateHtml();
                    dCP.echoChat(channel, "<b>** " + who + " has been made a member of " + newclass + " by " + bywho + " **</b>");
                } else if(damnPacket.get("type").startsWith("admin update")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String prop = damnPacket.get("p");
                    String who = damnPacket.get("by");
                    String name = damnPacket.get("name");
                    String privs = damnPacket.get("privs");
                    
                    dCP.echoChat(channel, "<b>** " + prop + " " + name + " has been updated by " + who + " with: " + privs + " **</b>");
                } else if(damnPacket.get("type").equalsIgnoreCase("admin show")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    dCP.echoChat(channel, "<b>** Administrative Information: "+ damnPacket.get("p") + " **</b>");
                    
                    String[] dataLines = damnPacket.get("value").split("\n");

                    for(int i=0;i<dataLines.length;i++) {
                        dCP.echoChat(channel, "<b>" + dataLines[i] + "</b>");
                    }

                    dCP.echoChat(channel, "<b>** End Administrative Information **</b>");
                } else if(damnPacket.get("type").equalsIgnoreCase("admin create")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String who = damnPacket.get("by");
                    String name = damnPacket.get("name");
                    String privs = damnPacket.get("privs");
                    
                    dCP.echoChat(channel, "<b>** Priviledge class " + name + " has been created by " + who + " with privs: " + privs + " **</b>");
                } else if(damnPacket.get("type").equalsIgnoreCase("admin remove")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String who = damnPacket.get("by");
                    String name = damnPacket.get("name");
                    
                    dCP.echoChat(channel, "<b>** Priviledge class " + name + " has been removed by " + who + " **</b>");
                } else if(damnPacket.get("type").equalsIgnoreCase("admin privclass")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String prop = damnPacket.get("p");
                    String event = damnPacket.get("e");
                    String command = damnPacket.get("value");
                    
                    dCP.echoChat(channel, "<b><em>(admin " + prop + " error): " + event + " (" + command + ")</em></b>");
                }
            } else if(damnPacket.get("command").startsWith("join ")) {
                if(damnPacket.get("command").split(" ")[1].startsWith("chat")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String event = damnPacket.get("e");

                    if(event.equalsIgnoreCase("ok")) {
                        if(!dJ.chatExists(channel)) {
                            dCP.addChatPage(0, channel);
                            pendingData += 2;
                        }
                        dJ.terminalEcho(0, "Successfully joined #" + channel);
                    } else if(event.equalsIgnoreCase("chatroom doesn't exist")) {
                        dJ.terminalEcho(0, "Chat room does not exist.");
                    } else if(event.equalsIgnoreCase("not privileged")) {
                        if(dJ.chatExists(channel)) {
                            dCP.echoChat(channel, "Join Error: Not Privileged");
                        } else {
                            dJ.terminalEcho(0, "Join Error (#" + channel + "): Not Privileged");
                        }
                    }
                } else if(damnPacket.get("command").split(" ")[1].startsWith("pchat")) {
                    String channel = damnPacket.get("command").split(":")[1];
                    String event = damnPacket.get("e");
                    
                    if(channel.equalsIgnoreCase(getUser())) {
                        channel = damnPacket.get("command").split(":")[2];
                    }
                    
                    if(event.equalsIgnoreCase("ok")) {
                        pendingData += 2;
                        dCP.addChatPage(1, channel);
                        dJ.terminalEcho(0, "Now chatting with " + channel);
                    } else if(event.equalsIgnoreCase("chatroom doesn't exist")) {
                        dJ.terminalEcho(0, "Chat room does not exist.");
                    }
                }
            } else if(damnPacket.get("command").startsWith("part ")) {
                int pchat = 0;
                if(damnPacket.get("command").split(" ")[1].startsWith("pchat:"))
                    pchat = 1;
                
                String channel = damnPacket.get("command").split(":")[1];
                if(pchat == 1 && channel == this.getUser()) {
                    channel = damnPacket.get("command").split(":")[2];
                }
                String event = damnPacket.get("e");
                String linec;
                if(damnPacket.containsKey("r")) {
                    linec = damnPacket.get("r");
                } else {
                    linec = null;
                }

                if(event.equalsIgnoreCase("ok")) {
                    if(pchat == 0) {
                        dCP.delChatPage(channel);
                        if(linec == null) {
                            dJ.terminalEcho(0, "Successfully parted #" + channel);
                        } else {
                            dJ.terminalEcho(0, "Successfully parted #" + channel + " [" + linec + "]");
                        }
                    } else {
                        if(channel.equalsIgnoreCase(getUser())) channel = damnPacket.get("command").split(":")[2];
                        dCP.delChatPage("pchat:" + channel);
                        if(linec == null) {
                            dJ.terminalEcho(0, "No longer talking with " + channel);
                        } else {
                            dJ.terminalEcho(0, "No longer talking with " + channel + " [" + linec + "]");
                        }
                    }
                } else if(event.equalsIgnoreCase("not joined")) {
                    if(dJ.chatExists(channel)) dCP.delChatPage(channel);
                    if(dJ.chatExists("pchat:" + channel)) dCP.delChatPage("pchat:" + channel); 
                } else {
                    dJ.terminalEcho(0, "Unreconized Error: " + event);
                }
            } else if(damnPacket.get("command").startsWith("kicked chat:")) {
                String channel = damnPacket.get("command").split(":")[1];
                String who = damnPacket.get("by");
                String value = damnPacket.get("value");
                
                if(!conf.getAutorejoin() || (damnPacket.containsKey("r") && damnPacket.get("r").equals("not privileged"))) {
                    if(damnPacket.containsKey("r")) {
                        dCP.echoChat(channel, "<b>You have been kicked from #" + channel + " by " + who + " * " + processTablumps(value) + "</b>");
                    } else {
                        dCP.echoChat(channel, "<b>You have been kicked from #" + channel + " by " + who + " * </b>");
                    }
                } else {
                    if(damnPacket.containsKey("r")) {
                        dCP.echoChat(channel, "<b>You have been kicked from #" + channel + " by " + who + " * " + processTablumps(value) + "</b>");
                    } else {
                        dCP.echoChat(channel, "<b>You have been kicked from #" + channel + " by " + who + " * </b>");
                    }
                    doJoinChannel(channel);
                    dCP.echoChat(channel, "Attempting to rejoin...");
                }
            } else if(damnPacket.get("command").startsWith("property chat:") || damnPacket.get("command").startsWith("property pchat:")) {
                String channel = damnPacket.get("command").split(":")[1];
                String property = damnPacket.get("p");
                int pchat = 0;
                
                if(damnPacket.get("command").split(" ")[1].startsWith("pchat:")) {
                    pchat = 1;
                    if(channel.equalsIgnoreCase(this.getUser())) channel = damnPacket.get("command").split(":")[2];
                }

                if(property.equalsIgnoreCase("members")) {
                    String[] propertysplit = damnPacket.get("value").split("\n\n");
                    if(pchat == 0) {
                        dCP.getMemberList(channel).clearUsers();
                    }
                    for(int i=0; i<propertysplit.length; i++) {
                        String[] dataSplit = propertysplit[i].split("\n");
                        String[] linec = dataSplit[0].split(" ");
                        String[] privclass = dataSplit[1].split("=");
                        String[] symbol = dataSplit[3].split("=", 2);
                        if(pchat == 0) {
                            dCP.getMemberList(channel).addUser(linec[1], symbol[1], privclass[1]);
                            dCP.getMemberList(channel).generateHtml();
                        } else {
                            symbol = dataSplit[2].split("=", 2);
                            dCP.echoChat("pchat:" + channel, "** " + symbol[1] + linec[1] + " is in the room.");
                        }
                    }
                } else if(property.equalsIgnoreCase("privclasses")) {
                    String[] privclasses = damnPacket.get("value").split("\n");
                    dCP.getMemberList(channel).clearPcl();
                    for(int i=0;i < privclasses.length; i++) {
                        String[] classdata = privclasses[i].split(":");
                        dCP.getMemberList(channel).addPc(classdata[1]);
                    }
                } else if(property.equalsIgnoreCase("topic")) {
                    String who;
                    String when;
                    if(damnPacket.containsKey("by") && damnPacket.containsKey("ts")) {
                        who = damnPacket.get("by");
                        when = damnPacket.get("ts");
                    } else {
                        who = "null";
                        when = "never set";
                    }
                    
                    if(damnPacket.get("command").split(" ")[1].startsWith("pchat:")) {
                        if(channel == this.getUser()) channel = damnPacket.get("command").split(":")[2];
                        channel = "pchat:" + channel;
                    }
                    
                    if(damnPacket.containsKey("value")) {
                        dCP.setChannelTopic(channel, processTablumps(damnPacket.get("value")));
                    } else {
                        dCP.setChannelTopic(channel, "");
                    }

                    if(pendingData == 0) {
                        dCP.echoChat(channel, "<b>** Topic was set by " + who + " (" + when + ") **</b>");
                    } else {
                        pendingData--;
                    }
                } else if(property.equalsIgnoreCase("title")) {
                    String who;
                    String when;
                    if(damnPacket.containsKey("by") && damnPacket.containsKey("ts")) {
                        who = damnPacket.get("by");
                        when = damnPacket.get("ts");
                    } else {
                        who = "null";
                        when = "never set";
                    }
                    
                    if(damnPacket.get("command").split(" ")[1].startsWith("pchat:")) {
                        if(channel == this.getUser()) channel = damnPacket.get("command").split(":")[2];
                        channel = "pchat:" + channel;
                    }
                    
                    if(damnPacket.containsKey("value")) {
                        dCP.setChannelTitle(channel, processTablumps(damnPacket.get("value")));
                    } else {
                        dCP.setChannelTitle(channel, "");
                    }

                    if(pendingData == 0) {
                        dCP.echoChat(channel, "<b>** Title was set by " + who + " (" + when + ") **</b>");
                    } else {
                        pendingData--;
                    }
                } 
            } else if(damnPacket.get("command").startsWith("property login:")) {
                    //dJ.terminalEcho(1, "User infos: ");
                    String[] tmpBox = data.split("\n");
                    for (int i=0; i<11; i++) {
                        whoisInfo[i] = tmpBox[i+1];
                        dJ.terminalEcho(1, tmpBox[i+1]);
                    }
                    whoisInfoReady = true;
            } else if(damnPacket.get("command").startsWith("get login:")) {
                    dJ.terminalEcho(0, "Whois Information Error: " + damnPacket.get("e"));
                    whoisBadUsername = true;
            } else if(damnPacket.get("command").equalsIgnoreCase("disconnect")) {
                if(damnPacket.containsKey("e")) {
                    dJ.terminalEcho(0, "You have been disconnected. [" + damnPacket.get("e") + "]");
                } else {
                    dJ.terminalEcho(0, "You have been disconnected. [no reason given]");
                }
                dJ.disconnect();
            } else if(damnPacket.get("command").equalsIgnoreCase("dAmnServer 0.2")) {
                dJ.terminalEcho(0, "Server OK.");
            } else {
                dJ.terminalEcho(0, "Unknown Data. Echoing Raw Packet.");
                dJ.terminalEcho(0, "-------------------------------------");
                String[] tmpBox = data.split("\n");
                for(int i=0;i<tmpBox.length;i++)
                    dJ.terminalEcho(1, tmpBox[i]);
                dJ.terminalEcho(0, "-------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
            dJ.terminalEcho(0, "dJC has hit an error reading packets: " + e.getMessage());
        }
        
        damnPacket.clear();
    }
    
    /**
     * Sends the authientication messages.
     */
    public void doHandshake() {
        //Initial Handshake
        dC.writeData(buildPacket(1, "dAmnClient 0.2", "agent=dJC-" + dJ.getVersion()));
        dJ.terminalEcho(0, "Sending Version Information...");
        dC.writeData(buildPacket(1, "login " + username, "pk=" + password));
        dJ.terminalEcho(0, "Sending Login...");
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
     * Implements the mass join feature. Used for autojoin.
     * @param channels The channels to join.
     */
    public void doMassJoin(String[] channels) {
        for(int i=0;i < channels.length;i++) {
            doJoinChannel(channels[i]);
        }
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
     * Sends a message to a private chat.
     * @param user The user who you are chatting with.
     * @param message The message.
     */
    public void doSendPMessage(String user, String message) {
        String[] pchatusrs;
        pchatusrs = new String[2];
        pchatusrs[0] = new String(user);
        pchatusrs[1] = new String(getUser());
        
        Arrays.sort(pchatusrs);
        Arrays.sort(pchatusrs, new Comparator<String>() {
            public int compare(String usra, String usrb) {
                if(usra.compareToIgnoreCase(usrb) == 0) {
                    return 0;
                } else if(usra.compareToIgnoreCase(usrb) < 0) {
                    return -1;
                } else if(usra.compareToIgnoreCase(usrb) > 0) {
                    return 1;
                }
                return 0;
            }
        });
        
        if(message.startsWith("/me ")) {
            String[] pieces = message.split("/me ");
            dC.writeData(buildPacket(0, "send pchat:" + pchatusrs[0] + ":" + pchatusrs[1], "", "action main", "", pieces[1]));
        } else {
            dC.writeData(buildPacket(0, "send pchat:" + pchatusrs[0] + ":" + pchatusrs[1], "", "msg main", "", message));
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
        whoisInfoReady = false;
        whoisBadUsername = false;
        dC.writeData(buildPacket(1, "get login:" + user, "p=info"));
    }
    
    /**
     * Returns the raw user information data given by the chat server
     * whoisInfoReady must be true for this method to work
     */
    public String[] whoisData()
    {
        if (whoisInfoReady) {
            whoisInfoReady = false;
            return whoisInfo;
        } else return null;
    }
    
     /**
     * Promote a user to a privilege class
     * @param channel The channel for which the property will be set
     * @param user The username to promote privilege to
     * @param privClass The promoted privilege class 
     */
    public void doPromote(String channel, String user, String privClass) {
        dC.writeData(buildPacket(0, "send chat:" + channel, "", "promote "+user, "", privClass));
    }
    
     /**
     * Demote a user to a privilege class
     * @param channel The channel for which the property will be set
     * @param user The username to demtoe privilege to
     * @param privClass The destination privilege class 
     */
    public void doDemote(String channel, String user, String privClass) {
        dC.writeData(buildPacket(0, "send chat:" + channel, "", "demote "+user, "", privClass));
    }
    
    /**
     * Issues an administrative command.
     * @param channel The channel to send the command to.
     * @param command The command to send to the channel.
     */
    public void doAdmin(String channel, String command) {
        dC.writeData(buildPacket(0, "send chat:" + channel, "", "admin", "", command));
    }
    
    /**
     * Kills a user off damn. This is an MN@ command.... Thanks to bzed!
     * @param user The user to kill.
     * @param conn The connection to kill. Zero for all.
     * @param reason The reason for killing the user.
     */
    public void doKill(String user, String conn, String reason) {
        dC.writeData(buildPacket(0, "kill login:" + user, "conn=" + conn, reason));
    }
    
    /**
     * Starts a private chat with the specified user.
     * @param user The user to chat with.
     */
    public void doJoinPrivateChat(String user) {
        String[] pchatusrs;
        pchatusrs = new String[2];
        pchatusrs[0] = new String(user);
        pchatusrs[1] = new String(getUser());
        
        Arrays.sort(pchatusrs);
        Arrays.sort(pchatusrs, new Comparator<String>() {
            public int compare(String usra, String usrb) {
                if(usra.compareToIgnoreCase(usrb) == 0) {
                    return 0;
                } else if(usra.compareToIgnoreCase(usrb) < 0) {
                    return -1;
                } else if(usra.compareToIgnoreCase(usrb) > 0) {
                    return 1;
                }
                return 0;
            }
        });
        
        dC.writeData(buildPacket(1, "join pchat:" + pchatusrs[0] + ":" + pchatusrs[1]));
    }
    
    /**
     * Parts a private chat with the specified user.
     * @param user The user to chat with.
     */
    public void doPartPrivateChat(String user) {
        String[] pchatusrs;
        pchatusrs = new String[2];
        pchatusrs[0] = new String(user);
        pchatusrs[1] = new String(getUser());
        
        Arrays.sort(pchatusrs);
        Arrays.sort(pchatusrs, new Comparator<String>() {
            public int compare(String usra, String usrb) {
                if(usra.compareToIgnoreCase(usrb) == 0) {
                    return 0;
                } else if(usra.compareToIgnoreCase(usrb) < 0) {
                    return -1;
                } else if(usra.compareToIgnoreCase(usrb) > 0) {
                    return 1;
                }
                return 0;
            }
        });
        
        dC.writeData(buildPacket(1, "part pchat:" + pchatusrs[0] + ":" + pchatusrs[1]));
    }
    
    /**
     * Processes Tablumps sent from the server.
     * @param rawdata The message the user sent.
     * @return The HTML Formatted message. Tablump free!
     */
    private String processTablumps(String rawdata) {
        Pattern thePattern;
        Matcher theMatcher;
        
        //Formatting
        rawdata = rawdata.replaceAll("&([biu])\t", "<$1>");
        rawdata = rawdata.replaceAll("&/([biu])\t", "</$1>");
        
        rawdata = rawdata.replace("&sub\t","<sub>");
        rawdata = rawdata.replace("&/sub\t","</sub>");
        // superscript
        rawdata = rawdata.replace("&sup\t","<sup>");
        rawdata = rawdata.replace("&/sup\t","</sup>");
        // strike
        rawdata = rawdata.replace("&s\t","<del>");
        rawdata = rawdata.replace("&/s\t","</del>");
        // paragraph
        rawdata = rawdata.replace("&p\t","<p>");
        rawdata = rawdata.replace("&/p\t","</p>");
        // break
        rawdata = rawdata.replace("&br\t","<br>");
        // code
        rawdata = rawdata.replace("&code\t","<code>");
        rawdata = rawdata.replace("&/code\t","</code>");
        // bcode
        rawdata = rawdata.replace("&bcode\t","<pre><code>");
        rawdata = rawdata.replace("&/bcode\t","</code></pre>");
        //li
        rawdata = rawdata.replace("&li\t","<li>");
        rawdata = rawdata.replace("&/li\t","</li>");
        //ul
        rawdata = rawdata.replace("&ul\t","<ul>");
        rawdata = rawdata.replace("&/ul\t","</ul>");
        //ol
        rawdata = rawdata.replace("&ol\t","<ol>");
        rawdata = rawdata.replace("&/ol\t","</ol>");
        
        // Emoticons
        if(rawdata.indexOf("&emote\t") != -1) {
            thePattern = Pattern.compile("&emote\t([^\t]+)\t([0-9]+)\t([0-9]+)\t([^\t]*)\t([^\t]+)\t");
            theMatcher = thePattern.matcher(rawdata);
            rawdata = theMatcher.replaceAll("<img width=\"$2\" height=\"$3\" src=\"http://e.deviantart.com/emoticons/$5\" alt=\"$4\">");
        }

         // Thumbnails
        if(rawdata.indexOf("&thumb\t") != -1) {
            thePattern = Pattern.compile("&thumb\t(\\d+)\t([^\t]*)\t([^\t]*)\t(\\d+)x(\\d+)\t(\\d+)\t([^\t]+)\t([^\t]*)\t");
            theMatcher = thePattern.matcher(rawdata);
            
            while (theMatcher.find()) {
                String url = theMatcher.group(7);
                Pattern p = Pattern.compile("fs(\\d):");
                Matcher m = p.matcher( url );
                url = m.replaceAll("fs$1.deviantart.com/");
                
                int Width = Integer.parseInt( theMatcher.group(4) );
                int Height = Integer.parseInt( theMatcher.group(5) );
                int nw, nh;
                
                if (Width>100) {
                    //http://www.deviantart.com/view/15696906
                    if (Width>Height) { nw = 100;  nh = 100 * Height / Width; }
                    else { nh = 100; nw = 100 * Width / Height; }
                    String link = "<a href=\"http://www.deviantart.com/view/$1\">";
                    rawdata = theMatcher.replaceFirst("<td class=\"tn\"><a href=\"http://www.deviantart.com/view/$1\"><img src=\"http://tn$6.deviantart.com/100/"+url+"\" width=\""+nw+"\" height=\""+nh+"\" border=\"0\" ></a></td>");
                } else {
                    rawdata = theMatcher.replaceFirst("<a href=\"http://www.deviantart.com/view/$1\"><img src=\"http://"+url+"\" width=\""+Width+"\" height=\""+Height+"\" border=\"0\"></a>");
                }
                
                theMatcher = thePattern.matcher(rawdata);
            }
        }
        
        
        while (rawdata.indexOf("&avatar\t") != -1) {
            thePattern = Pattern.compile("&avatar\t([^\t]+)\t(\\d+)\t");
            theMatcher = thePattern.matcher(rawdata);
            while (theMatcher.find()) {
                String name = theMatcher.group(1).toLowerCase();
                String[] types = {"gif","gif","jpg"};
                int type = Integer.parseInt(theMatcher.group(2));
                if (type > 0)
                    rawdata = theMatcher.replaceFirst("<a href=\"http://"+name+".deviantart.com/\"><img height=\"50\" width=\"50\" border=\"0\" src=\"http://a.deviantart.com/avatars/"+name.charAt(0)+"/"+name.charAt(1)+"/"+name+"."+types[type]+"\"></a>");
                else
                    rawdata = theMatcher.replaceFirst("<a href=\"http://" + name + ".deviantart.com/\"><img height=\"50\" width=\"50\" border=\"0\" src=\"http://a.deviantart.com/avatars/default.gif\"></a>");
                
                theMatcher = thePattern.matcher(rawdata);
            }
        }
      
        // Anchor
        // &a/thttp://photography.deviantart.com/t/tphotography.deviantart.com&/a
        
        if(rawdata.indexOf("&a\t") != -1) 
            rawdata = rawdata.replaceAll("&a\t([^\t]+)\t([^\t]*)\t([^&]*?)&/a\t",  "<a href=\"$1\" title=\"$2\">$2$3</a>");
        
        // Links
        if(rawdata.indexOf("&link\t") != -1) {
            // link no description
            rawdata = rawdata.replaceAll("&link\t([^\t]+)\t&\t","<a href=\"$1\" title=\"$1\">[link]</a>");
            // link with description
            rawdata = rawdata.replaceAll("&link\t([^\t]+)\t([^\t]+)\t&\t","<a href=\"$1\" title=\"$1\">$2</a>");         
       }
        
        //:dev...:
        if(rawdata.indexOf("&dev") != -1) rawdata = rawdata.replaceAll("&dev\t([^\t])\t([^\t]+)\t",
                "<a href=\"http://$2.deviantart.com\">$1$2</a>");
        
        rawdata = rawdata.replaceAll("\t", "(t)");

        return rawdata;
    }
}
