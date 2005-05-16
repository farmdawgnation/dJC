/*
 * damnConfig.java
 *
 * Created on May 13, 2005, 7:10 AM
 */

package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements a singleton which contains the dAmn client configuration information.
 * This information is stored in the config.xml file located in the same directory as the client jar.
 *
 * @author Eric Olander
 */
public class damnConfig {
    
    private static final String CONFIG_FILE = "config.xml";
    
    private static damnConfig _instance = null;
    
    private final File _configFile;
    private final DocumentBuilder _documentBuilder;
    
    
    private String _user = "";
    private String _password= "";
    private String _host = "";
    private int _port;
    private final Set<String> _channels = new HashSet<String>();
    
    /** Creates a new instance of damnConfig */
    private damnConfig() {
        String configFileName = System.getProperty("user.dir") + System.getProperty("file.separator") + CONFIG_FILE;
        _configFile = new File(configFileName);
        try {
            _documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            throw new Error("Unable to create an XML document parser.");
        }
    }
    
    /**
     * Gets the handle to the damnConfig singleton instance
     * @return damnConfig singleton instance handle
     */
    public static damnConfig getInstance() {
        if (_instance == null) {
            _instance = new damnConfig();
        }
        return _instance;
    }
    
    /**
     * Gets the user's dAmn login id
     * @return current configured login id
     */
    public String getUser() {
        return _user;
    }
    
    /**
     * Sets the user's dAmn login id
     * @param _user user's login id
     */
    public void setUser(String _user) {
        this._user = _user;
    }
    
    /**
     * Gets the user's password
     * @return current configured password
     */
    public String getPassword() {
        return _password;
    }
    
    /**
     * Sets the user's password
     * @param _password user's password
     */
    public void setPassword(String _password) {
        this._password = _password;
    }
    
    /**
     * Gets the host to connect to
     * @return current configured host
     */
    public String getHost() {
        return _host;
    }
    
    /**
     * Sets the host to connect to
     * @param _host hostname or IP address
     */
    public void setHost(String _host) {
        this._host = _host;
    }
    
    /**
     * Gets the current configured port
     * @return current configured port
     */
    public int getPort() {
        return _port;
    }
    
    /**
     * Sets the port to connect to
     * @param _port port on the host server
     */
    public void setPort(int _port) {
        this._port = _port;
        String port = Integer.toString(_port);
    }
    
    /**
     * Adds a channel to the configuration
     * @param channel Channel to be added
     */
    public void addChannel(String channel) {
        _channels.add(channel);
    }
    
    /**
     * Remove a channel from the configuration
     * @param channel Channel to be removed
     */
    public void deleteChannel(String channel) {
        _channels.remove(channel);
    }
    
    /**
     * Clears all of the channels out of the configuration.
     */
    public void clearChannels() {
        _channels.clear();
    }
    
    /**
     * Gets the configured channels 
     * @return String array of channels
     */
    public String[] getChannels() {
        return (String[])_channels.toArray(new String[_channels.size()]);
    }
    
    /**
     * Reads the configuration from the config.xml file in the current directory
     * @throws java.io.FileNotFoundException config file does not exist
     * @throws client.InvalidXMLException unable to parse the config file
     */
    public void readConfig() throws FileNotFoundException, InvalidXMLException {
        Document configDoc;
        Element configRoot;
        
        if (!_configFile.exists()) {
            throw new FileNotFoundException();
        }
        try {
            configDoc = _documentBuilder.parse(_configFile);
            configRoot = configDoc.getDocumentElement();
        } catch (Exception e) {
            throw new InvalidXMLException(e);
        }
        
        Node configNode = configRoot.getChildNodes().item(1);
        NamedNodeMap attributes = configNode.getAttributes();
        
        _user = attributes.getNamedItem("user").getNodeValue();
        _password = attributes.getNamedItem("password").getNodeValue();
        _host = attributes.getNamedItem("host").getNodeValue();
        String port = attributes.getNamedItem("port").getNodeValue();
        try {
            _port = Integer.parseInt(port);
        } catch (NumberFormatException ne) {
            throw new InvalidXMLException("bad port "+port);
        }
        _channels.clear();
        NodeList childList = configNode.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            NamedNodeMap attrs = childList.item(i).getAttributes();
            if (attrs != null) {
                _channels.add(attrs.getNamedItem("id").getNodeValue());
            }
        }
    }
    
    /**
     * Writes the current configuration to the config.xml file in the current directory
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.FileNotFoundException 
     */
    public void writeConfig() throws TransformerException, FileNotFoundException {
        Document reqDoc = _documentBuilder.newDocument();
        Element root = reqDoc.createElement("config");
        
        Element connection_elem = reqDoc.createElement("connection");
        connection_elem.setAttribute("user", _user);
        connection_elem.setAttribute("password", _password);
        connection_elem.setAttribute("host", _host);
        connection_elem.setAttribute("port", Integer.toString(_port));
        
        Iterator<String> iter = _channels.iterator();
        while (iter.hasNext()) {
            Element channel_elem = reqDoc.createElement("channel");
            channel_elem.setAttribute("id", iter.next());
            connection_elem.appendChild(channel_elem);
        }
        root.appendChild(connection_elem);
        reqDoc.appendChild(root);
        
        Transformer xform = TransformerFactory.newInstance().newTransformer();
        FileOutputStream fileOut = new FileOutputStream(_configFile);
        
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.transform(new DOMSource(reqDoc), new StreamResult(fileOut));
    }
}
