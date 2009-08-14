/*
 * DOMVersionChecker.java
 *
 * Created on 29. April 2003, 11:13
 */

package Sirius.util.DBVersionChecker;

import java.sql.*;
import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.*;

/**
 * Format des configfiles:
 *  Pfad der XML Beschreibung
 *  Datenbank Treiber
 *  Datenbank Name
 *  Benutzer
 *  Passwort
 */
public class DOMVersionChecker implements VersionChecker {
    
    private Connection       conn;
    private DatabaseMetaData meta;
    private String           xmlFile;
    private SAXBuilder       parser;
    private ArrayList        differences;
    
    /** Datenstruktur:
     *    Versionsname => map {
     *                          Tabelle => map {
     *                                           Spalte => map { nullable => "yes" / "no" / "unknown",
     *                                                           size => Zahlenwert
     *                                                           type => Name des Typ } } }
     *
     * Die Elemente der letzten map (Spalte) sind alle optional, ein Wert von "" wird im Vergleich
     * automatisch als \u00FCbereinstimmend mit der DB angenommen.
     */
    private TreeMap          versionMap;
    
    /** Datenstruktur analog zu einer Version der versionMap, d.h. map { Tabelle => map ... */
    private TreeMap          dbStructure;
    
    
    public static void main(String[] args) {
        try {
            String nL = System.getProperty("line.separator");
            if (args.length < 2) {
                System.err.println(
                "Aufruf:" + nL +
                " a) PROG <configfile> generiere <neuer Vesionsname>" + nL +
                " b) PROG <configfile> version" + nL +
                " c) PROG <configfile> vergleiche <Versionsname>" + nL + nL +
                "    Achtung - im Fall a) wird die im configfile angegebene " + nL +
                "    XML Datei neu erzeugt und muss in die Versionsliste " + nL +
                "    aufgenommen werden");
                System.exit(0);
            }
            
            DOMVersionChecker instance = new DOMVersionChecker(args[0]);
            if (args[1].equals("version")) {
                String version = instance.checkVersion();
                if (version != null)
                    System.out.println("Gefundene Version: " + version);
                else
                    System.out.println("F\u00FCr die angegebene Datenbank wurde keine \u00FCbereinstimmende Definition gefunden.");
            }
            else if (args[1].equals("generiere")) {
                instance.writeVersionXML(instance.xmlFile, args[2]);
            }
            else if (args[1].equals("vergleiche")) {
                if (instance.compareWithVersion(args[2]))
                    System.out.println("Vollst\u00E4ndige \u00DCbereinstimmung gefunden.");
                else {
                    System.out.println("Versionen stimmen nicht \u00FCberein:");
                    ArrayList diff = instance.getDifferences();
                    Iterator it = diff.iterator();
                    while (it.hasNext())
                        System.out.println(it.next());
                }
            }
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
    
    /** Erstellt eine Instanz anhand eines configfiles der Form:
     *  Pfad der XML Beschreibung
     *  Datenbank Treiber
     *  Datenbank Name
     *  Benutzer
     *  Passwort
     */
    public DOMVersionChecker(String configfile) throws DBVersionException {
        versionMap   = new TreeMap();
        dbStructure  = new TreeMap();
        differences  = new ArrayList();
        parser       = new SAXBuilder();
        
        try {
            BufferedReader cfg = new BufferedReader(new FileReader(configfile));
            xmlFile = cfg.readLine();
            readVersionsXML();
            setDB(cfg.readLine(), cfg.readLine(), cfg.readLine(), cfg.readLine());
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen des configfiles: " + e);
            System.exit(1);
        }
    }
    
    /** Creates a new instance of DOMVersionChecker */
    public DOMVersionChecker(String xmlFile, String driver, String database, String username, String password) {
        this.xmlFile = xmlFile;
        
        versionMap   = new TreeMap();
        dbStructure  = new TreeMap();
        differences  = new ArrayList();
        parser       = new SAXBuilder();
        
        readVersionsXML();
        setDB(driver, database, username, password);
    }
    
    /** Setzt die zu benutzende Datenbank und liest ihre Struktur ein */
    public void setDB(String driver, String database, String username, String password) {
        try {
            Class.forName(driver).newInstance();
            setDB(DriverManager.getConnection(database, username, password));
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    /** Setzt die zu benutzende Datenbank und liest ihre Struktur ein */
    public void setDB(Connection con) {
        conn = con;
        
        try {
            meta = conn.getMetaData();
            readDBStructure();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    /** Creates a new instance of DOMVersionChecker */
    public DOMVersionChecker(String xmlFile, Connection con) {
        this.xmlFile = xmlFile;
        versionMap   = new TreeMap();
        dbStructure  = new TreeMap();
        differences  = new ArrayList();
        parser       = new SAXBuilder();
        
        setDB(con);
        readVersionsXML();
    }
    
    /**
     * Vergleicht nacheinander die vorhandenen Datenmodel-Versionen mit der
     * Struktur der Datenbank, deren Version festgestellt werden soll. Wird
     * eine passende Version gefunden, so wird die Suche abgebrochen und der
     * zugeh\u00F6rige Name der Datenmodel-Version wird geliefert.
     *
     * @return wenn Versionsckeck erfolgreich: der Name der Datenmodell-Version,
     * sonst null.
     *
     * @throws wenn beim checken zu einem Fehler kommt.
     */
    public String checkVersion() throws DBVersionException {
        Set versions = versionMap.keySet();
        Iterator it = versions.iterator();
        while (it.hasNext()) {
            String version = (String) it.next();
            if (compareWithVersion(version))
                return version;
        }
        
        return null;
    }
    
    /**
     * Vergleicht ob das Datenmodell einer bestimten Version entspricht.
     *
     * @param der Versionsname.
     *
     * @return true wenn das Datenmodel die Struktur der \u00FCbergebene Version
     * besitzt.
     *
     * @throws wenn beim checken zu einem Fehler kommt.
     */
    public boolean compareWithVersion(String version) throws DBVersionException {
        TreeMap tables = (TreeMap) versionMap.get(version);
        if (tables == null)
            throw new DBVersionException("Unbekannte Version");
        
        differences.clear();
        
        Set       dbKeys = dbStructure.keySet();
        Set      xmlKeys =      tables.keySet();
        Iterator  dbIter =  dbKeys.iterator();
        Iterator xmlIter = xmlKeys.iterator();
        
        while (dbIter.hasNext()) { // iterieren \u00FCber alle Tabellen der DB
            String  dbTable = (String)  dbIter.next();
            String xmlTable;
            
            if (xmlIter.hasNext())  // pr\u00FCfen ob es \u00FCberhaupt noch Eintr\u00E4ge im XML gibt
                xmlTable = (String) xmlIter.next();
            else {
                differences.add("Tabelle <" + dbTable + "> in der DB aber nicht im XML");
                continue;
            }
            
            while (dbTable.compareTo(xmlTable) < 0) {  // da beide gleich sortiert sind fehlen Eintr\u00E4ge im XML
                differences.add("Tabelle <" + dbTable + "> in der DB aber nicht im XML");
                if (dbIter.hasNext())
                    dbTable = (String) dbIter.next();
                else
                    break;
            }
            
            while (dbTable.compareTo(xmlTable) > 0) { // im XML sind Eintr\u00E4ge die nicht in der DB sind
                differences.add("Tabelle <" + dbTable + "> im XML aber nicht in der DB");
                if (xmlIter.hasNext())
                    xmlTable = (String) xmlIter.next();
                else
                    break;
            }
            
            if (dbTable.compareTo(xmlTable) == 0) { // identische Tabelle in XML und DB, d.h. Spalten vergleichen
                TreeMap  dbColumnMap = (TreeMap) dbStructure.get(dbTable);
                TreeMap xmlColumnMap = (TreeMap) tables.get(xmlTable);
                
                Set  dbColumns =  dbColumnMap.keySet();
                Set xmlColumns = xmlColumnMap.keySet();
                Iterator  dbColumnsIter =  dbColumns.iterator();
                Iterator xmlColumnsIter = xmlColumns.iterator();
                
                while (dbColumnsIter.hasNext()) { // iterieren \u00FCber die Spalten der Tabelle
                    String  dbColumn = (String) dbColumnsIter.next();
                    String xmlColumn;
                    
                    if (xmlColumnsIter.hasNext())
                        xmlColumn = (String) xmlColumnsIter.next();
                    else {
                        differences.add("Tabelle <" + dbTable + "> Spalte <" + dbColumn + "> in der DB aber nicht im XML");
                        continue;
                    }
                    
                    while (dbColumn.compareTo(xmlColumn) < 0) {
                        differences.add("Tabelle <" + dbTable + "> Spalte <" + dbColumn + "> in der DB aber nicht in XML");
                        if (dbColumnsIter.hasNext())
                            dbColumn = (String) dbColumnsIter.next();
                        else
                            break;
                    }
                    
                    while (dbColumn.compareTo(xmlColumn) > 0) {
                        differences.add("Tabelle <" + dbTable + "> Spalte <" + dbColumn + "> in XML aber nicht in der DB");
                        if (xmlColumnsIter.hasNext())
                            xmlColumn = (String) xmlColumnsIter.next();
                        else
                            break;
                    }
                    
                    if (dbColumn.compareTo(xmlColumn) == 0) { // \u00FCbereinstimmender Spaltenname in XML und DB
                        TreeMap  dbColumnData = (TreeMap)  dbColumnMap.get( dbColumn);
                        TreeMap xmlColumnData = (TreeMap) xmlColumnMap.get(xmlColumn);
                        
                        if (xmlColumnData.get("nullable") != null)
                            if (((String) dbColumnData.get("nullable")).compareTo(xmlColumnData.get("nullable").toString()) != 0)
                                differences.add("Tabelle <" + dbTable + "> Spalte <" + dbColumn + "> nullable db: " + dbColumnData.get("nullable") + " / xml: " + xmlColumnData.get("nullable"));
                        
                        if (xmlColumnData.get("size") != null)
                            if (((String) dbColumnData.get("size")).compareTo(xmlColumnData.get("size").toString()) != 0)
                                differences.add("Tabelle <" + dbTable + "> Spalte <" + dbColumn + "> size db: " + dbColumnData.get("size") + " / xml: " + xmlColumnData.get("size"));
                        
                        if (xmlColumnData.get("type") != null)
                            if (((String) dbColumnData.get("type")).compareTo(xmlColumnData.get("type").toString()) != 0)
                                differences.add("Tabelle <" + dbTable + "> Spalte <" + dbColumn + "> type db: " + dbColumnData.get("type") + " / xml: " + xmlColumnData.get("type"));
                    }
                }
            }
        }
        
        return differences.size() == 0;
    }
    
    /**
     * Liefert alle Datenmodel-Versionen die in dem Datenmodell-Speichermodul
     * enthalten sind.
     *
     * @return Bezeichnungen der Datenmodel-Versionenen.
     *
     * @throws wenn es zu einem Fehler kommt.
     */
    public String[] getAllVersions() throws DBVersionException {
        return (String[]) versionMap.keySet().toArray(new String[0]);
    }
    
    /**
     * Pr\u00FCft ob eine bestimmte Version in dem Datenmodell-Speichermodul
     * enthalten ist.
     *
     * @param der Versionsname.
     *
     * @return true wenn die gesuchte Version in dem Datenmodell-Speichermodul
     * enthalten ist, sonst false.
     *
     * @throws wenn es zu einem Fehler kommt.
     */
    public boolean versionAvailable(String version) throws DBVersionException {
        Set versions = versionMap.keySet();
        Iterator it = versions.iterator();
        while (it.hasNext())
            if (version.compareTo(it.next().toString()) == 0)
                return true;
        
        return false;
    }
    
    
    /**
     * Speichert die derzeitig ausgew\u00E4hlte Datenbank in eine XML Datei
     *
     * @param fileName Name der Datei in die gespeichert wird
     * @param versionName name attribut des version tag, das den Rest umschliesst
     */
    public void writeVersionXML(String fileName, String versionName) {
        try {
            FileWriter xml = new FileWriter(fileName);
            ResultSet tables = meta.getTables(null, null, "%", null);
            
            xml.write("\t<Version name=\"" + versionName + "\">\n");
            
            while (tables.next()) {
                if (tables.getString("TABLE_TYPE").compareTo("TABLE") != 0) // Spezielle Tabellentypen wie views \u00FCberspringen
                    continue;
                
                xml.write("\t\t<InternTable name=\"" + tables.getString("TABLE_NAME") + "\">\n");
                
                ResultSet columns = meta.getColumns(null, null, tables.getString("TABLE_NAME"), "%");
                
                while (columns.next()) {
                    xml.write("\t\t\t<Column nullable=\"");
                    
                    if (columns.getString("IS_NULLABLE").equals("YES"))
                        xml.write("true");
                    else if (columns.getString("IS_NULLABLE").equals("NO"))
                        xml.write("false");
                    else                                // DB MetaInfo API garantiert keine eindeutige Aussagen
                        xml.write("unknown");
                    
                    xml.write("\" type=\"" + columns.getString("TYPE_NAME"));
                    xml.write("\" size=\"" + columns.getString("COLUMN_SIZE"));
                    xml.write("\">" + columns.getString("COLUMN_NAME") + "</Column>\n");
                }
                
                xml.write("\t\t</InternTable>\n");
            }
            
            xml.write("\t</Version>\n");
            xml.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        
    }
    
    /** Gibt die Unterschiede zwischen Datenbank und XML Beschreibung des zuletzt
     *  durchgef\u00FChrten Vergleiches zur\u00FCck.
     *
     * @return Liste von strings mit den Unterschieden
     */
    public ArrayList getDifferences() {
        return differences;
    }
    
    /**
     * Liest die XML Datei mit den Datenbankbeschreibungen ein und speichert die gefundenen
     * Daten in versionMap.
     */
    private void readVersionsXML() {
        try {
            File test = new File(xmlFile);
            if (!test.exists()) {
                System.out.println("Keine XML Informationen zum einlesen gefunden, falls neue erzeugt werden sollen ist dies korrekt.");
                return;
            }
            Document xml = parser.build(test);
            Element datamodel = xml.getRootElement();
            
            List versions = datamodel.getChildren("Version");
            Iterator versionsIter = versions.iterator();
            while (versionsIter.hasNext()) {
                Element version = (Element)versionsIter.next();
                List tables = version.getChildren("InternTable");
                Iterator tablesIter = tables.iterator();
                TreeMap tableMap = new TreeMap();
                
                while (tablesIter.hasNext()) {
                    Element table = (Element) tablesIter.next();
                    List columns = table.getChildren("Column");
                    Iterator columnsIter = columns.iterator();
                    TreeMap columnMap = new TreeMap();
                    
                    while (columnsIter.hasNext()) {
                        Element column = (Element) columnsIter.next();
                        TreeMap columnData = new TreeMap();
                        columnData.put("nullable", column.getAttributeValue("nullable"));
                        columnData.put("size", column.getAttributeValue("size"));
                        columnData.put("type", column.getAttributeValue("type"));
                        columnMap.put(column.getText(), columnData);
                    }
                    tableMap.put(table.getAttributeValue("name"), columnMap);
                }
                versionMap.put(version.getAttributeValue("name"), tableMap);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    /**
     * Liest die Struktur der derzeitig ausgew\u00E4hlten Datenbank in dbStructure ein.
     */
    private void readDBStructure() {
        try {
            ResultSet tables = meta.getTables(null, null, "%", null);
            
            while (tables.next()) {
                if (tables.getString("TABLE_TYPE").compareTo("TABLE") != 0)
                    continue;
                
                ResultSet columns = meta.getColumns(null, null, tables.getString("TABLE_NAME"), "%");
                TreeMap columnsMap = new TreeMap();
                
                while (columns.next()) {
                    TreeMap columnData = new TreeMap();
                    
                    if (columns.getString("IS_NULLABLE").equals("YES"))
                        columnData.put("nullable", "true");
                    else if (columns.getString("IS_NULLABLE").equals("NO"))
                        columnData.put("nullable", "false");
                    else
                        columnData.put("nullable", "unknown");
                    
                    columnData.put("size", columns.getString("COLUMN_SIZE"));
                    columnData.put("type", columns.getString("TYPE_NAME"));
                    columnsMap.put(columns.getString("COLUMN_NAME"), columnData);
                }
                dbStructure.put(tables.getString("TABLE_NAME"), columnsMap);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
