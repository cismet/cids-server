/*
 * JDBC_XMLProto.java
 *
 * Created on 18. September 2003, 10:05
 */

package Sirius.server.dataretrieval;

import java.io.*;
import java.sql.*;
import Sirius.server.middleware.types.*;
import org.apache.log4j.*;

/**
 * Stellt Verbindung zur Datenbank, setzt darauf das parametrisierte
 * Statement ab, holt das ResultSet konvertiert es zu einem XML-File dessen
 * Inhalt anschliessend serialisiert wird. Der XML-File wird in Tempor\u00E4rem
 * Verzeichnis des Betriebssystem abgelegt. Der Name der Datei wird zusammen
 * gesetzt aus praefix "jdbc" einer Ordnungszahl und suffix ".xml".
 * Die Ordnungszahl wird bestimmt durch die h\u00F6chste Ordnungszahl + 1 aus den
 * Dateien die der oben beschriebenen Signatur entsprechen und sich aktuell
 * im Tempor\u00E4ren Verzeichnis befinden. Die Datei wird nach dem sie
 * serialisiert wurde standardm\u00E4sig gel\u00F6scht, dies kann verhindert werden in dem
 * man. Die Objekte werden \u00FCber getObject(..) Funktion aus dem Original-ResultSet
 * in gecacheten unver\u00E4ndert \u00FCbernommen, wenn der zuletzt eingelesene Wert
 * SQL-NULL war(resultSet.wasNull() = true) so wird in XML-File ein String 
 * "SQL.NuLL" als Wert geschrieben. Bildet ein Dataretrieval-Protokol. 
 * Bezeichnung dieses Protokols ist "jdbc_xml".
 * 
 *
 * @author  awindholz
 */
public class JDBC_XMLProto extends JDBCProto {
    
    private static Logger logger = Logger.getLogger(JDBC_XMLProto.class);
    public static final String SQL_NULL = "SQL.NuLL";
    public boolean deleteOnExit = true;
    
    /** Creates a new instance of JDBC_XMLProto 
     * Defaultm\u00E4sig wird die Datei nach dem sie serialisiert wurde gel\u00F6scht.
     */
    public JDBC_XMLProto() {
    }
    
    /**
     * Beeinflust das l\u00F6schverhalten nach dem die Datei serialisiert wurde.
     * 
     * @param wenn true wird die datei nach dem serialisieren gel\u00F6scht, soll
     * dies verhindert werden dann sollte false gesetzt werden.
     */
    public void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }
    
    /**
     * Konvertiert das \u00FCbergebene ResultSet zu einem XML-File und liefert dieses
     * File serialisiert in einem DataObjekt zur\u00FCck.
     *
     * @param rs das ReultSet das zu XML verarbeiter werden soll.
     * @param query die query die an Datenbank abgesetzt wurde, durch welche
     * dieses ResultSet entstanden ist.
     *
     * @return Objekt vom typ DataObject das enth\u00E4lt das Serialisierte 
     * XML-Dokument mit ResultSet daten, sowie eine textuelle kurzbeschreibung 
     * in einem String.
     */
    protected DataObject createDataObject(ResultSet rs, String query)
    throws DataRetrievalException {
        try {
            File xmlFile = createFile(new JdbcXmlFileFilter(), "xml");
            PrintWriter pWriter = new PrintWriter(new FileWriter(xmlFile));
            if(deleteOnExit) {
                xmlFile.deleteOnExit();
            }
            
            writeResult(rs, pWriter);
            
            String info = "XML-File with struktur of the delivered ResultSet. " +
            "Contents of the file as byte sequence..";
            
            String do_name = System.currentTimeMillis() + ".xml";
            
            return new DataObject(toBytes(xmlFile), info, do_name);
        } catch(Exception e) {
            // Noch nicht iO
            String message = "Error occurs during creating an XML document, " +
            "from determined ResultSet. Original message:" + e.getMessage();
            throw new DataRetrievalException(message, e, logger);
        }
    }
    
    /**
     * Der XML-File wird in Tempor\u00E4rem
     * Verzeichnis des Betriebssystem abgelegt. Der Name der Datei wird zusammen
     * gesetzt aus praefix "jdbc" einer Ordnungszahl und suffix .<erweiterung>.
     * Die Ordnungszahl wird bestimmt durch die h\u00F6chste Ordnungszahl + 1 aus den
     * Dateien die der oben beschriebenen Signatur entsprechen und sich aktuell
     * im Tempor\u00E4ren Verzeichnis befinden. Die Dateien werden nach dem sie
     * serialisiert wurden gel\u00F6scht.
     *
     * @param erweiterung Erweiterung die f\u00FCr Dateien benutzt werden soll.
     * @param filter Filter der alle Dateien ausser mit \u00FCbergebener Erweiterung 
     * ausfilltert.
     */
    synchronized protected File createFile(FilenameFilter filter, String erweiterung) throws IOException {
        File file;
        String tmpPath = System.getProperty("java.io.tmpdir") +
        System.getProperty("file.separator");
        
        int count = 0;
        
        File dir = new File(tmpPath);
        String[] files = dir.list(filter);
        String zahl;
        int maxZahl = 0, tmp;
        
        for(int i = 0; i < files.length; i++) {
            // der Teil des Namen mit der Zahl.
            zahl = files[i].substring(4, files[i].length() - 4);
            
            try {
                
                tmp = Integer.parseInt(zahl);
                maxZahl = (tmp < maxZahl) ? maxZahl : tmp;
                
            } catch(NumberFormatException e) {
                // passt schon
            }
        }
        
        String fileName = "jdbc" + ++maxZahl + "." + erweiterung;
        file = new File(tmpPath + fileName);
        
        logger.debug("Anlegen der Datei f\u00FCr " + erweiterung + "-ResultSet: " + tmpPath + fileName);
        
        return file;
    }
    
    /**
     * Schreibt Daten des \u00DCbergebenen ResultSet-Objektes in \u00FCbergebenen 
     * PrintWriter-Objekt.
     * <RESULT><br>
     *<br>
     *   <ROW><br>
     *<br>
     *     <FIELD name="Spaltenname 1">wert11</FIELD><br>
     *     <FIELD name="Spaltenname 1">wert12</FIELD><br>
     *<br>
     *   </ROW><br>
     *<br>
     *   <ROW><br>
     *
     *     <FIELD name="Spaltenname 1">wert21</FIELD><br>
     *     <FIELD name="Spaltenname 1">wert22</FIELD><br>
     *<br>
     *   </ROW><br>
     *<br>
     * </RESULT><br><br>
     */
    private void writeResult(ResultSet resultSet, PrintWriter pWriter)
    throws IOException, SQLException {
        pWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pWriter.println();
        pWriter.println("<RESULT>");
        
        while (resultSet.next()) {
            writeRow(resultSet, pWriter);
        }
        
        pWriter.println("</RESULT>");
        pWriter.flush();
    }
    
    /**
     * Schreibt nur eine Zeile in den PrintWriter.
     */
    private void writeRow(ResultSet resultSet, PrintWriter pWriter)
    throws IOException, SQLException {
        
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        
        pWriter.println("\t<ROW>");
        
        for(int i = 1; i <= columnCount; i++) {
            
            pWriter.print("\t\t<FIELD name='" + rsmd.getColumnName(i).trim() + "'>");
            String wert = resultSet.getString(i);
            if(resultSet.wasNull()) {
                pWriter.print(SQL_NULL);
            } else {
                pWriter.print(wert.trim());
            }
            
            pWriter.println("</FIELD>");
        }
        
        pWriter.println("\t</ROW>");
        pWriter.println("");
    }
    
    /**
     * Bezeichner f\u00FCr diesen Protokol.
     *
     * @return jdbc_xml;
     */
    public String getDataSourceClass() {
        return "jdbc_xml";
    }
    
    /**
     * Serialisiert den Inhalt der \u00FCbergebenen Datei.
     */
    protected static byte[] toBytes(File toByteArray) throws IOException {
        
        int cacheSize = 4096;
        int count;
        byte[] cache = new byte[cacheSize];
        
        BufferedInputStream bis =
        new BufferedInputStream(
        new FileInputStream(toByteArray));
        
        ByteArrayOutputStream baos =
        new ByteArrayOutputStream(cacheSize);
        int i = 0;
        while ((count = bis.read(cache, 0, cacheSize)) >= 0) {
            baos.write(cache, 0, count);
            i+=count;
//logger.info(Integer.toString((i) / 1048546));
        }

// logger.info("FERTIG");
        baos.close();
        bis.close();
        
        return  baos.toByteArray();
    }
    
    /**
     * Filtert alle Dateien aus die nicht die Signatur besitzen: Prefix "jdbc"
     * postfix ".xml".
     */
    class JdbcXmlFileFilter implements FilenameFilter {
        
        JdbcXmlFileFilter() { }
        
        /** Tests if a specified file should be included in a file list.
         *
         * @param   dir    the directory in which the file was found.
         * @param   name   the name of the file.
         * @return  <code>true</code> if and only if the name should be
         * included in the file list; <code>false</code> otherwise.
         *
         */
        public boolean accept(File dir, String name) {
            
            if(name.startsWith("jdbc") &&
            name.endsWith(".xml")) { return true; }
            
            return false;
        }
    }
}