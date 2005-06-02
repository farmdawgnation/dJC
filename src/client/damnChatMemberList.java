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
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JEditorPane;

/**
 * Represents a chat member list.
 * @author MSF
 */
public class damnChatMemberList extends JEditorPane {
    private ArrayList<damnUser> users;
    private ArrayList<String> privclasses;
    private ArrayList<String> searchResults;
    private int searchIndex = -1;
    private int searchSet = 0;
    
    /** Creates a new instance of damnChatMemberList */
    public damnChatMemberList() {
        users = new ArrayList<damnUser>();
        privclasses = new ArrayList<String>();
        searchResults = new ArrayList<String>();
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
     * Creates an alphabetical list of the members.
     * @param userlist A damnUser array containing the list of members.
     * @returns The sorted damnUser array.
     */
    private damnUser[] sortUsers() {
        Comparator<damnUser> dUc = new Comparator<damnUser>() {
            public int compare(damnUser usra, damnUser usrb) {
                if(usra.getPlain().equalsIgnoreCase(usrb.getPlain())) {
                    return 0;
                } else if(usra.getPlain().compareToIgnoreCase(usrb.getPlain()) < 0) {
                    return -1;
                } else if(usra.getPlain().compareToIgnoreCase(usrb.getPlain()) > 0) {
                    return 1;
                }
                return 0;
            }
        };
        
        damnUser[] userArr = users.toArray(new damnUser[users.size()]);
        Arrays.sort(userArr, dUc);
        return userArr;
    }
    
    /**
     * Generates the HTML for the member list.
     */
    public void generateHtml() {
        StringBuffer code = new StringBuffer("<html><body><table width=\"100%\">");
        
        damnUser[] userArr = sortUsers();
        
        for(int a=0;a < privclasses.size(); a++) {
            if(classHasMembers(privclasses.get(a))) {
                code.append("<tr><th align=\"left\">" + privclasses.get(a) + "</th></tr>");
                for(int i=0;i < userArr.length;i++) {
                    if(userArr[i].getPc().equalsIgnoreCase(privclasses.get(a))) {
                        code.append("<tr><td>" + userArr[i].getFormatted() + "</td></tr>");
                    }
                }
            }
        }
        
        code.append("</table></body></html>");
        
        setText(code.toString());
    }
    
    /**
     * Clears the privclass list.
     */
    public void clearPcl() {
        privclasses.clear();
    }
    
    /**
     * Clears the users list.
     */
    public void clearUsers() {
        users.clear();
    }
    
    /**
     * Runs a search of the member list.
     * Results are stored internally.
     * @param searchString The start of the username that we are searching for.
     */
    private void searchUsers(String searchstring) {
        damnUser[] userArr = sortUsers();
        int itemsFound = 0;
        searchResults.clear();
        for(int i=0;i < userArr.length;i++) {
            if(userArr[i].getPlain().startsWith(searchstring)) {
                searchResults.add(userArr[i].getPlain());
                itemsFound = 1;
            }
        }
        
        if(itemsFound == 1) {
            searchIndex = 0;
        } else {
            searchIndex = -1;
        }
    }
    
    /**
     * Fetches the next item from a search.
     * @returns The next item from the last search.
     */
    private String fetchResult() {
        if(searchIndex == -1) {
            return null;
        } else {
            if(searchIndex > searchResults.size() - 1) {
                searchIndex = 0;
            }
            String result = searchResults.get(searchIndex);
            searchIndex++;
            return result;
        }
    }
    
    /**
     * Controls the search functions as needed.
     * @param searchstring The string to search by.
     * @returns The first result on the first call, and the following results on subsequent calls.
     */
    public String searchAgent(String searchstring) {
        if(searchSet == 0) {
            searchSet = 1;
            searchUsers(searchstring);
            return fetchResult();
        } else {
            return fetchResult();
        }
    }
    
    /**
     * Resets the search agent.
     */
    public void searchAgentReset() {
        searchSet = 0;
    }
    
    /**
     * Promotes/Demotes the specified user to the specified class.
     * @param username The user to promote.
     * @param privclass The class to promote the user to.
     */
    public void setClass(String username, String privclass) {
        for(int i=0;i<users.size();i++) {
            if(users.get(i).getPlain().equalsIgnoreCase(username)) {
                users.get(i).setPc(privclass);
            }
        }
    }
}
