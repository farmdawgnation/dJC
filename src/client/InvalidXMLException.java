/*
 * InvalidXMLException.java
 *
 * Created on May 13, 2005, 7:37 AM
 */

package client;

/**
 * Exception for errors in parsing the configuration XML file
 *
 * @author Eric Olander
 */
public class InvalidXMLException extends Exception {
    
    /** Creates a new instance of InvalidXMLException */
    public InvalidXMLException(Throwable e) {
        super(e);
    }
    
     public InvalidXMLException(String msg) {
        super(msg);
    }
}
