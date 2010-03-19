/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DataObject.java
 *
 * Created on 10. September 2003, 10:26
 */
package Sirius.server.dataretrieval;

/**
 * Beschreibt MetaDaten die einer Datenquelle entnommen wurden. Enth\u00E4lt das serialisierte Objekt von der
 * Datenquelle so wie eine Beschreibung was mit diesem zu tun ist.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class DataObject implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    private byte[] data;
    private String parseInfo;
    private String name = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of DataObject.
     *
     * @param  data       DOCUMENT ME!
     * @param  parseInfo  DOCUMENT ME!
     * @param  name       DOCUMENT ME!
     */
    public DataObject(byte[] data, String parseInfo, String name) {
        this.data = data;
        this.parseInfo = parseInfo;
        this.name = name;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Liefert das Objekt das von der Datenquelle abgefragt wurde.
     *
     * @return  DOCUMENT ME!
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Liefert eine Beschreibung zum Objekt das von der Datenquelle abgefragt wurde.
     *
     * @return  DOCUMENT ME!
     */
    public String getParseInfo() {
        return parseInfo;
    }

    /**
     * Wenn FTP => name der Datei.
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }
}
