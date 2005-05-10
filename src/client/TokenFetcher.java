/*
 * TokenFetcher.java
 *
 * Created on May 10, 2005, 5:36 PM
 */

package client;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.regex.*;

public class TokenFetcher {
    private Socket sock;
    private PrintWriter writer;
    private BufferedReader reader;
    private String host;
    private int port;
    
    /** Creates a new instance of TokenFetcher */
    public TokenFetcher(String commHost) {
        host = commHost;
        port = 80;
    }
    
    public String doTokenFetch(String username, String password) {
        String message;
        String authtoken = null;
        
        if(username.equalsIgnoreCase("") || password.equalsIgnoreCase("")) {
            return null;
        }
        
        String info = new String("username=" + username + "&password=" + password + "&reusetoken=1");
        
        String payload = new String("POST /users/login HTTP/1.1\nHost: www.deviantart.com\nUser-Agent: dAmnJavaFetcher/1.0\nAccept:text/html\nCookie: skipintro=1\nContent-Type: application/x-www-form-urlencoded\nContent-Length: " + info.length() + "\n\n" + info);
        
        try {
            sock = new Socket(host, port);
            writer = new PrintWriter(sock.getOutputStream());
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            
            writer.write(payload);
            writer.flush();
            
            while((message = reader.readLine()) != null) {
                if(message.startsWith("Set-Cookie: ")) {
                    Pattern thePattern = Pattern.compile("Set-Cookie: userinfo=([A-Za-z%]+)");
                    Matcher theMatcher = thePattern.matcher(message);
                    message = theMatcher.replaceAll("$1");
                    
                    String[] boxes = message.split("%3B");
                    System.out.println(boxes.length);
                    String[] moreBoxes = boxes[9].split("%22");
                    authtoken = moreBoxes[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return authtoken;
    }
    
}
