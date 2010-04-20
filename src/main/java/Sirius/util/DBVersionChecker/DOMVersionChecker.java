/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * Format des configfiles: Pfad der XML Beschreibung Datenbank Treiber Datenbank Name Benutzer Passwort
 *
 * @version  $Revision$, $Date$
 */
public class DOMVersionChecker implements VersionChecker {

    //~ Instance fields --------------------------------------------------------

    private Connection conn;
    private DatabaseMetaData meta;
    private String xmlFile;
    private SAXBuilder parser;
    private ArrayList differences;

    /**
     * Datenstruktur: Versionsname => map { Tabelle => map { Spalte => map { nullable => "yes" / "no" / "unknown", size
     * => Zahlenwert type => Name des Typ } } }
     *
     * <p>Die Elemente der letzten map (Spalte) sind alle optional, ein Wert von "" wird im Vergleich automatisch als
     * \u00FCbereinstimmend mit der DB angenommen.</p>
     */
    private TreeMap versionMap;

    /** Datenstruktur analog zu einer Version der versionMap, d.h. map { Tabelle => map ... */
    private TreeMap dbStructure;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erstellt eine Instanz anhand eines configfiles der Form: Pfad der XML Beschreibung Datenbank Treiber Datenbank
     * Name Benutzer Passwort
     *
     * @param   configfile  DOCUMENT ME!
     *
     * @throws  DBVersionException  DOCUMENT ME!
     */
    public DOMVersionChecker(String configfile) throws DBVersionException {
        versionMap = new TreeMap();
        dbStructure = new TreeMap();
        differences = new ArrayList();
        parser = new SAXBuilder();

        try {
            BufferedReader cfg = new BufferedReader(new FileReader(configfile));
            xmlFile = cfg.readLine();
            readVersionsXML();
            setDB(cfg.readLine(), cfg.readLine(), cfg.readLine(), cfg.readLine());
        } catch (IOException e) {
            System.err.println(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.DOMVersionChecker(String).error") + e);
            System.exit(1);
        }
    }

    /**
     * Creates a new instance of DOMVersionChecker.
     *
     * @param  xmlFile  DOCUMENT ME!
     * @param  con      DOCUMENT ME!
     */
    public DOMVersionChecker(String xmlFile, Connection con) {
        this.xmlFile = xmlFile;
        versionMap = new TreeMap();
        dbStructure = new TreeMap();
        differences = new ArrayList();
        parser = new SAXBuilder();

        setDB(con);
        readVersionsXML();
    }

    /**
     * Creates a new instance of DOMVersionChecker.
     *
     * @param  xmlFile   DOCUMENT ME!
     * @param  driver    DOCUMENT ME!
     * @param  database  DOCUMENT ME!
     * @param  username  DOCUMENT ME!
     * @param  password  DOCUMENT ME!
     */
    public DOMVersionChecker(String xmlFile, String driver, String database, String username, String password) {
        this.xmlFile = xmlFile;

        versionMap = new TreeMap();
        dbStructure = new TreeMap();
        differences = new ArrayList();
        parser = new SAXBuilder();

        readVersionsXML();
        setDB(driver, database, username, password);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(String[] args) {
        try {
            String nL = System.getProperty("line.separator");//NOI18N
            if (args.length < 2) {
                System.err.println(
                    org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.main(String[]).helpText", new Object[] {nL}) );//NOI18N
                System.exit(0);
            }

            DOMVersionChecker instance = new DOMVersionChecker(args[0]);
            if (args[1].equals("version")) {//NOI18N
                String version = instance.checkVersion();
                if (version != null) {
                    System.out.println(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.main(String[]).foundVersion") + version);
                } else {
                    System.out.println(
                        org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.main(String[])..noProperVersion"));
                }
            } else if (args[1].equals("generiere")) {//NOI18N
                instance.writeVersionXML(instance.xmlFile, args[2]);
            } else if (args[1].equals("vergleiche")) {//NOI18N
                if (instance.compareWithVersion(args[2])) {
                    System.out.println(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.main(String[]).accordanceFound"));
                } else {
                    System.out.println(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.main(String[]).noAccordanceFound"));
                    ArrayList diff = instance.getDifferences();
                    Iterator it = diff.iterator();
                    while (it.hasNext()) {
                        System.out.println(it.next());
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println(e);
        }
    }

    /**
     * Setzt die zu benutzende Datenbank und liest ihre Struktur ein.
     *
     * @param  driver    DOCUMENT ME!
     * @param  database  DOCUMENT ME!
     * @param  username  DOCUMENT ME!
     * @param  password  DOCUMENT ME!
     */
    public void setDB(String driver, String database, String username, String password) {
        try {
            Class.forName(driver).newInstance();
            setDB(DriverManager.getConnection(database, username, password));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Setzt die zu benutzende Datenbank und liest ihre Struktur ein.
     *
     * @param  con  DOCUMENT ME!
     */
    public void setDB(Connection con) {
        conn = con;

        try {
            meta = conn.getMetaData();
            readDBStructure();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Vergleicht nacheinander die vorhandenen Datenmodel-Versionen mit der Struktur der Datenbank, deren Version
     * festgestellt werden soll. Wird eine passende Version gefunden, so wird die Suche abgebrochen und der
     * zugeh\u00F6rige Name der Datenmodel-Version wird geliefert.
     *
     * @return  wenn Versionsckeck erfolgreich: der Name der Datenmodell-Version, sonst null.
     *
     * @throws  DBVersionException  wenn beim checken zu einem Fehler kommt.
     */
    public String checkVersion() throws DBVersionException {
        Set versions = versionMap.keySet();
        Iterator it = versions.iterator();
        while (it.hasNext()) {
            String version = (String)it.next();
            if (compareWithVersion(version)) {
                return version;
            }
        }

        return null;
    }

    /**
     * Vergleicht ob das Datenmodell einer bestimten Version entspricht.
     *
     * @param   version  der Versionsname.
     *
     * @return  true wenn das Datenmodel die Struktur der \u00FCbergebene Version besitzt.
     *
     * @throws  DBVersionException  wenn beim checken zu einem Fehler kommt.
     */
    public boolean compareWithVersion(String version) throws DBVersionException {
        TreeMap tables = (TreeMap)versionMap.get(version);
        if (tables == null) {
            throw new DBVersionException(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).DBVersionException"));//NOI18N
        }

        differences.clear();

        Set dbKeys = dbStructure.keySet();
        Set xmlKeys = tables.keySet();
        Iterator dbIter = dbKeys.iterator();
        Iterator xmlIter = xmlKeys.iterator();

        while (dbIter.hasNext()) { // iterieren \u00FCber alle Tabellen der DB
            String dbTable = (String)dbIter.next();
            String xmlTable;

            if (xmlIter.hasNext()) { // pr\u00FCfen ob es \u00FCberhaupt noch Eintr\u00E4ge im XML gibt
                xmlTable = (String)xmlIter.next();
            } else {
                differences.add(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).differences.tableOnlyInDB", new Object[] {dbTable}));//NOI18N
                continue;
            }

            while (dbTable.compareTo(xmlTable) < 0) { // da beide gleich sortiert sind fehlen Eintr\u00E4ge im XML
                differences.add(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).differences.tableOnlyInDB", new Object[] {dbTable}));//NOI18N
                if (dbIter.hasNext()) {
                    dbTable = (String)dbIter.next();
                } else {
                    break;
                }
            }

            while (dbTable.compareTo(xmlTable) > 0) { // im XML sind Eintr\u00E4ge die nicht in der DB sind
                differences.add(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).differences.TableOnlyInXML", new Object[] {dbTable}));//NOI18N
                if (xmlIter.hasNext()) {
                    xmlTable = (String)xmlIter.next();
                } else {
                    break;
                }
            }

            if (dbTable.compareTo(xmlTable) == 0) { // identische Tabelle in XML und DB, d.h. Spalten vergleichen
                TreeMap dbColumnMap = (TreeMap)dbStructure.get(dbTable);
                TreeMap xmlColumnMap = (TreeMap)tables.get(xmlTable);

                Set dbColumns = dbColumnMap.keySet();
                Set xmlColumns = xmlColumnMap.keySet();
                Iterator dbColumnsIter = dbColumns.iterator();
                Iterator xmlColumnsIter = xmlColumns.iterator();

                while (dbColumnsIter.hasNext()) { // iterieren \u00FCber die Spalten der Tabelle
                    String dbColumn = (String)dbColumnsIter.next();
                    String xmlColumn;

                    if (xmlColumnsIter.hasNext()) {
                        xmlColumn = (String)xmlColumnsIter.next();
                    } else {
                        differences.add(
                            org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).differences.ColumnOnlyInDB", new Object[] {dbTable, dbColumn}));//NOI18N
                        continue;
                    }

                    while (dbColumn.compareTo(xmlColumn) < 0) {
                        differences.add(
                            org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).differences.ColumnOnlyInDB", new Object[] {dbTable, dbColumn}));//NOI18N
                        if (dbColumnsIter.hasNext()) {
                            dbColumn = (String)dbColumnsIter.next();
                        } else {
                            break;
                        }
                    }

                    while (dbColumn.compareTo(xmlColumn) > 0) {
                        differences.add(
                            org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).differences.ColumnOnlyInXML", new Object[] {dbTable, dbColumn}));//NOI18N
                        if (xmlColumnsIter.hasNext()) {
                            xmlColumn = (String)xmlColumnsIter.next();
                        } else {
                            break;
                        }
                    }

                    if (dbColumn.compareTo(xmlColumn) == 0) { // \u00FCbereinstimmender Spaltenname in XML und DB
                        TreeMap dbColumnData = (TreeMap)dbColumnMap.get(dbColumn);
                        TreeMap xmlColumnData = (TreeMap)xmlColumnMap.get(xmlColumn);

                        if (xmlColumnData.get("nullable") != null) {//NOI18N
                            if (
                                ((String)dbColumnData.get("nullable")).compareTo(//NOI18N
                                            xmlColumnData.get("nullable").toString())//NOI18N
                                        != 0) {
                                differences.add(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).nullableDiffers", new Object[] {dbTable, dbColumn, dbColumnData.get("nullable"), xmlColumnData.get("nullable")}));//NOI18N
                            }
                        }

                        if (xmlColumnData.get("size") != null) {//NOI18N
                            if (
                                ((String)dbColumnData.get("size")).compareTo(xmlColumnData.get("size").toString())//NOI18N
                                        != 0) {
                                differences.add(org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).sizeDiffers", new Object[] {dbTable, dbColumn, dbColumnData.get("size"), xmlColumnData.get("size")}));//NOI18N
                            }
                        }

                        if (xmlColumnData.get("type") != null) {//NOI18N
                            if (
                                ((String)dbColumnData.get("type")).compareTo(xmlColumnData.get("type").toString())//NOI18N
                                        != 0) {
                                differences.add(
                                    org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.compareWithVersion(String).typeDiffers", new Object[] {dbTable, dbColumn, dbColumnData.get("type"), xmlColumnData.get("type")}));//NOI18N
                            }
                        }
                    }
                }
            }
        }

        return differences.size() == 0;
    }

    /**
     * Liefert alle Datenmodel-Versionen die in dem Datenmodell-Speichermodul enthalten sind.
     *
     * @return  Bezeichnungen der Datenmodel-Versionenen.
     *
     * @throws  DBVersionException  wenn es zu einem Fehler kommt.
     */
    public String[] getAllVersions() throws DBVersionException {
        return (String[])versionMap.keySet().toArray(new String[0]);
    }

    /**
     * Pr\u00FCft ob eine bestimmte Version in dem Datenmodell-Speichermodul enthalten ist.
     *
     * @param   version  der Versionsname.
     *
     * @return  true wenn die gesuchte Version in dem Datenmodell-Speichermodul enthalten ist, sonst false.
     *
     * @throws  DBVersionException  wenn es zu einem Fehler kommt.
     */
    public boolean versionAvailable(String version) throws DBVersionException {
        Set versions = versionMap.keySet();
        Iterator it = versions.iterator();
        while (it.hasNext()) {
            if (version.compareTo(it.next().toString()) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Speichert die derzeitig ausgew\u00E4hlte Datenbank in eine XML Datei.
     *
     * @param  fileName     Name der Datei in die gespeichert wird
     * @param  versionName  name attribut des version tag, das den Rest umschliesst
     */
    public void writeVersionXML(String fileName, String versionName) {
        try {
            FileWriter xml = new FileWriter(fileName);
            ResultSet tables = meta.getTables(null, null, "%", null);//NOI18N

            xml.write("\t<Version name=\"" + versionName + "\">\n");//NOI18N

            while (tables.next()) {
                if (tables.getString("TABLE_TYPE").compareTo("TABLE") != 0) { // Spezielle Tabellentypen wie views//NOI18N
                                                                              // \u00FCberspringen
                    continue;
                }

                xml.write("\t\t<InternTable name=\"" + tables.getString("TABLE_NAME") + "\">\n");//NOI18N

                ResultSet columns = meta.getColumns(null, null, tables.getString("TABLE_NAME"), "%");//NOI18N

                while (columns.next()) {
                    xml.write("\t\t\t<Column nullable=\"");//NOI18N

                    if (columns.getString("IS_NULLABLE").equals("YES")) {//NOI18N
                        xml.write("true");//NOI18N
                    } else if (columns.getString("IS_NULLABLE").equals("NO")) {//NOI18N
                        xml.write("false");//NOI18N
                    } else { // DB MetaInfo API garantiert keine eindeutige Aussagen
                        xml.write("unknown");//NOI18N
                    }

                    xml.write("\" type=\"" + columns.getString("TYPE_NAME"));//NOI18N
                    xml.write("\" size=\"" + columns.getString("COLUMN_SIZE"));//NOI18N
                    xml.write("\">" + columns.getString("COLUMN_NAME") + "</Column>\n");//NOI18N
                }

                xml.write("\t\t</InternTable>\n");//NOI18N
            }

            xml.write("\t</Version>\n");//NOI18N
            xml.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Gibt die Unterschiede zwischen Datenbank und XML Beschreibung des zuletzt durchgef\u00FChrten Vergleiches
     * zur\u00FCck.
     *
     * @return  Liste von strings mit den Unterschieden
     */
    public ArrayList getDifferences() {
        return differences;
    }

    /**
     * Liest die XML Datei mit den Datenbankbeschreibungen ein und speichert die gefundenen Daten in versionMap.
     */
    private void readVersionsXML() {
        try {
            File test = new File(xmlFile);
            if (!test.exists()) {
                System.out.println(
                    org.openide.util.NbBundle.getMessage(DOMVersionChecker.class, "DOMVersionChecker.readVersionsXML().noXMLFound"));//NOI18N
                return;
            }
            Document xml = parser.build(test);
            Element datamodel = xml.getRootElement();

            List versions = datamodel.getChildren("Version");//NOI18N
            Iterator versionsIter = versions.iterator();
            while (versionsIter.hasNext()) {
                Element version = (Element)versionsIter.next();
                List tables = version.getChildren("InternTable");//NOI18N
                Iterator tablesIter = tables.iterator();
                TreeMap tableMap = new TreeMap();

                while (tablesIter.hasNext()) {
                    Element table = (Element)tablesIter.next();
                    List columns = table.getChildren("Column");//NOI18N
                    Iterator columnsIter = columns.iterator();
                    TreeMap columnMap = new TreeMap();

                    while (columnsIter.hasNext()) {
                        Element column = (Element)columnsIter.next();
                        TreeMap columnData = new TreeMap();
                        columnData.put("nullable", column.getAttributeValue("nullable"));//NOI18N
                        columnData.put("size", column.getAttributeValue("size"));//NOI18N
                        columnData.put("type", column.getAttributeValue("type"));//NOI18N
                        columnMap.put(column.getText(), columnData);
                    }
                    tableMap.put(table.getAttributeValue("name"), columnMap);//NOI18N
                }
                versionMap.put(version.getAttributeValue("name"), tableMap);//NOI18N
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
            ResultSet tables = meta.getTables(null, null, "%", null);//NOI18N

            while (tables.next()) {
                if (tables.getString("TABLE_TYPE").compareTo("TABLE") != 0) {//NOI18N
                    continue;
                }

                ResultSet columns = meta.getColumns(null, null, tables.getString("TABLE_NAME"), "%");//NOI18N
                TreeMap columnsMap = new TreeMap();

                while (columns.next()) {
                    TreeMap columnData = new TreeMap();

                    if (columns.getString("IS_NULLABLE").equals("YES")) {//NOI18N
                        columnData.put("nullable", "true");//NOI18N
                    } else if (columns.getString("IS_NULLABLE").equals("NO")) {//NOI18N
                        columnData.put("nullable", "false");//NOI18N
                    } else {
                        columnData.put("nullable", "unknown");//NOI18N
                    }

                    columnData.put("size", columns.getString("COLUMN_SIZE"));//NOI18N
                    columnData.put("type", columns.getString("TYPE_NAME"));//NOI18N
                    columnsMap.put(columns.getString("COLUMN_NAME"), columnData);//NOI18N
                }
                dbStructure.put(tables.getString("TABLE_NAME"), columnsMap);//NOI18N
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
