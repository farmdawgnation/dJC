package client;
/*
 * dJC: The dAmn Java Client
 * damnUser.java
 * ©2005 The dAmn Java Project
 *
 * This software and its source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */

/**
 * A class to represent a user in a chat room.
 * Used for generating the list of members in a room.
 * @author MSF
 */
public class damnUser {
    String username;
    String symbol;
    String pc;
    
    /**
     * Creates a new instance of damnUser.
     * @param user The username of the new user.
     * @param symb The user's symbol.
     * @param priv The privclass the user is in.
     */
    public damnUser(String user, String symb, String priv) {
        username = user;
        symbol = symb;
        pc = priv;
    }
    
    /**
     * Gets the privilidge class that the user belongs to.
     */
    public String getPc() {
        return pc;
    }
    
    /**
     * Get the formatted version of the username for the member list.
     */
    public String getFormatted() {
        return symbol + username;
    }
    
    /**
     * Get the plain version of the username.
     */
    public String getPlain() {
        return username;
    }
    
}
