/*
 * dJC: The dAmn Java Client
 * damnChatMemberList.java
 * ©2005 The dAmn Java Project
 *
 * This software and its source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */

package client;

import java.util.ArrayList;
import javax.swing.JEditorPane;

/**
 * Represents a chat member list.
 * @author MSF
 */
public class damnChatMemberList extends JEditorPane {
    private ArrayList<damnUser> users;
    private ArrayList<String> privclasses;
    
    /** Creates a new instance of damnChatMemberList */
    public damnChatMemberList() {
        users = new ArrayList<damnUser>();
        privclasses = new ArrayList<String>();
        setContentType("text/html");
        setEditable(false);
    }
    
    /**
     * Adds a user to the internal list.
     * @param username The username to add.
     * @param symbol The user's symbol.
     * @param pc The user's Privclass. (String Form)
     */
    public void addUser(String username, String symbol, String pc) {
        users.add(new damnUser(username, symbol, pc));
    }
    
    /**
     * Deletes a user from the internal list.
     * @param username The username to delete.
     */
    public void delUser(String username) {
        int i = findUser(username);
        if(i != -1) {
            users.remove(i);
        }
    }
    
    /**
     * Adds a privclass.
     * @param pc Privclass to add.
     */
    public void addPc(String pc) {
        privclasses.add(pc);
    }
    
    /**
     * Finds a user with the specified username in the internal list.
     * @param username The username to locate.
     * @returns The index of the user in the internal list.
     */
    private int findUser(String username) {
        for(int i=0;i < users.size();i++) {
            if(users.get(i).getPlain().equalsIgnoreCase(username)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Checks to see wether the specified privclass has any members.
     */
    private boolean classHasMembers(String pc) {
        for(int i=0;i < users.size(); i++) {
            if(users.get(i).getPc().equalsIgnoreCase(pc)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generates the HTML for the member list.
     */
    public void generateHtml() {
        String code = new String("<html><body><table width=\"100%\">");
        
        for(int a=0;a < privclasses.size(); a++) {
            if(classHasMembers(privclasses.get(a))) {
                code += "<tr><th align=\"left\">" + privclasses.get(a) + "</th></tr>";
                for(int i=0;i < users.size();i++) {
                    if(users.get(i).getPc().equalsIgnoreCase(privclasses.get(a))) {
                        code += "<tr><td>" + users.get(i).getFormatted() + "</td></tr>";
                    }
                }
            }
        }
        
        code += "</table></body></html>";
        
        setText(code);
    }
}
