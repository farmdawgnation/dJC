/*
 * dJC: The dAmn Java Client
 * damnConfig.java
 * ©2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
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
public final class damnConfig {
    
    private static final String CONFIG_FILE = "config.xml";
    
    private static damnConfig _instance = null;
    
    private final File _configFile;
    private final DocumentBuilder _documentBuilder;
    
    
    private String _user = "";
    private String _password= "";
    private String _host = "";
    private int _port;
    private String _browsercommand = "";
    private int _autorejoin;
    private int _shownotices;
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
     * Gets the current browser command.
     * @return Current browser command.
     */
    public String getBrowsercommand() {
        return _browsercommand;
    }
    
    /**
     * Sets the browser command.
     * @param _browsercommand The browser command to set to.
     */
    public void setBrowsercommand(String _browsercommand) {
        this._browsercommand = _browsercommand;
    }
    
    /**
     * Gets the current auto rejoin setting.
     * @return TRUE if set, FALSE otherwise.
     */
    public boolean getAutorejoin() {
        if(this._autorejoin == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Sets the auto rejoin.
     * @param _autorejoin Must be set to true to enable, false to disable.
     */
    public void setAutorejoin(boolean _autorejoin) {
        if(_autorejoin == true) {
            this._autorejoin = 1;
        } else {
            this._autorejoin = 0;
        }
    }
    
    /**
     * Gets the always show notices setting.
     * @return TRUE if set, FALSE otherwise.
     */
    public boolean getShownotices() {
        if(_shownotices == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Sets the show notices setting.
     * @param _shownotices Must be set to true to enable, false to disable.
     */
    public void setShownotices(boolean _shownotices) {
        if(_shownotices == true) {
            this._shownotices = 1;
        } else {
            this._shownotices = 0;
        }
    }
    
    /**
     * Adds a channel to the configuration
     * @param channel Channel to be added
     */
    public void addChannel(String channel) {
        if ((channel == null) || (channel.length() == 0)) {
            return;
        }
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
        _password = Crypto.decrypt(attributes.getNamedItem("password").getNodeValue());
        _host = attributes.getNamedItem("host").getNodeValue();
        String port = attributes.getNamedItem("port").getNodeValue();
        try {
            _port = Integer.parseInt(port);
        } catch (NumberFormatException ne) {
            throw new InvalidXMLException("bad port "+port);
        }
        _browsercommand = attributes.getNamedItem("browsercommand").getNodeValue();
        String autorejoin = attributes.getNamedItem("autorejoin").getNodeValue();
        try {
            _autorejoin = Integer.parseInt(autorejoin);
        } catch (NumberFormatException ne) {
            throw new InvalidXMLException("bad autorejoin");
        }
        String shownotices = attributes.getNamedItem("shownotices").getNodeValue();
        try {
            _shownotices = Integer.parseInt(shownotices);
        } catch (NumberFormatException ne) {
            throw new InvalidXMLException("bad shownotices");
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
        connection_elem.setAttribute("password", Crypto.encrypt(_password));
        connection_elem.setAttribute("host", _host);
        connection_elem.setAttribute("port", Integer.toString(_port));
        connection_elem.setAttribute("browsercommand", _browsercommand);
        connection_elem.setAttribute("autorejoin", Integer.toString(_autorejoin));
        connection_elem.setAttribute("shownotices", Integer.toString(_shownotices));
        
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
