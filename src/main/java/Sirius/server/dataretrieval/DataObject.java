/*
 * DataObject.java
 *
 * Created on 10. September 2003, 10:26
 */

package Sirius.server.dataretrieval;

/**
 * Beschreibt MetaDaten die einer Datenquelle entnommen wurden. Enth\u00E4lt das 
 * serialisierte Objekt von der Datenquelle so wie eine Beschreibung was 
 * mit diesem zu tun ist.
 *
 * @author  awindholz
 */
public class DataObject implements java.io.Serializable {
    
    private byte[] data;
    private String parseInfo;
    private String name = null;
    
    /** Creates a new instance of DataObject 
     */
    public DataObject(byte[] data, String parseInfo, String name) {
        this.data = data;
        this.parseInfo = parseInfo;
        this.name = name;
    }
    
    /**
     * Liefert das Objekt das von der Datenquelle abgefragt wurde.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Liefert eine Beschreibung zum Objekt das von der Datenquelle abgefragt 
     * wurde.
     */
    public String getParseInfo() {
        return parseInfo;
    }
    
    /**
     * Wenn FTP => name der Datei.
     */
    public String getName() {
        return name;
    }
}