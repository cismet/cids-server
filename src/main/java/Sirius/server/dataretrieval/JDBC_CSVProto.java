/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * JDBC_CSVProto.java
 *
 * Created on 18. September 2003, 10:05
 */
package Sirius.server.dataretrieval;

import java.io.*;

import java.sql.*;

import Sirius.server.middleware.types.*;

import org.apache.log4j.*;

/**
 * Stellt Verbindung zur Datenbank, setzt darauf das parametrisierte Statement ab, holt das ResultSet konvertiert es zu
 * einem CSV-File dessen Inhalt anschliessend serialisiert wird. Der CSV-File wird in Tempor\u00E4rem Verzeichnis des
 * Betriebssystem abgelegt. Der Name der Datei wird zusammen gesetzt aus praefix "jdbc" einer Ordnungszahl und suffix
 * ".csv". Die Ordnungszahl wird bestimmt durch die h\u00F6chste Ordnungszahl + 1 aus den Dateien die der oben
 * beschriebenen Signatur entsprechen und sich aktuell im Tempor\u00E4ren Verzeichnis befinden. Die Datei wird nach dem
 * sie serialisiert wurde standardm\u00E4sig gel\u00F6scht, dies kann verhindert werden in dem man. Bezeichnung dieses
 * Protokols ist "jdbc_csv".
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class JDBC_CSVProto extends JDBC_XMLProto {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger logger = Logger.getLogger(JDBC_CSVProto.class);

    //~ Instance fields --------------------------------------------------------

    public boolean deleteOnExit = true;
    public CSVTool csvTool;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of JDBC_CSVProto Defaultm\u00E4sig wird die Datei nach dem sie serialisiert wurde
     * gel\u00F6scht.
     */
    public JDBC_CSVProto() {
        csvTool = new CSVTool();
    }

    /**
     * Creates a new instance of JDBC_CSVProto Defaultm\u00E4sig wird die Datei nach dem sie serialisiert wurde
     * gel\u00F6scht.
     *
     * @param  delimiter  DOCUMENT ME!
     */
    public JDBC_CSVProto(String delimiter) {
        csvTool = new CSVTool(delimiter);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Beeinflust das l\u00F6schverhalten nach dem die Datei serialisiert wurde.
     *
     * @param  deleteOnExit  wenn true wird die datei nach dem serialisieren gel\u00F6scht, soll dies verhindert werden
     *                       dann sollte false gesetzt werden.
     */
    public void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }

    /**
     * Konvertiert das \u00FCbergebene ResultSet zu einem CSV-File und liefert dieses File serialisiert in einem
     * DataObjekt zur\u00FCck.
     *
     * @param   rs     das ReultSet das zu CSV verarbeiter werden soll.
     * @param   query  die query die an Datenbank abgesetzt wurde, durch welche dieses ResultSet entstanden ist.
     * @param   name   DOCUMENT ME!
     *
     * @return  Objekt vom typ DataObject das enth\u00E4lt das Serialisierte CSV-Dokument mit ResultSet daten, sowie
     *          eine textuelle kurzbeschreibung in einem String.
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    protected DataObject createDataObject(ResultSet rs, String query, String name) throws DataRetrievalException {
        try {
            File csvFile = createFile(new JdbcCSVFileFilter(), "csv");
            PrintWriter pWriter = new PrintWriter(new FileWriter(csvFile));
            if (deleteOnExit) {
                csvFile.deleteOnExit();
            }

            csvTool.toCSV(rs, pWriter);

            String info = "CSV-File with struktur of the delivered ResultSet. "
                + "Contents of the file as byte sequence.";

            String do_name = name + ".csv";

            return new DataObject(toBytes(csvFile), info, do_name);
        } catch (Exception e) {
            // Noch nicht iO
            String message = "Error occurs during creating an CSV-File, "
                + "from determined ResultSet. Original message: " + e.getMessage();
            throw new DataRetrievalException(message, e, logger);
        }
    }

    /**
     * Bezeichner f\u00FCr diesen Protokol.
     *
     * @return  jdbc_CSV;
     */
    public String getDataSourceClass() {
        return "jdbc_csv";
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Filtert alle Dateien aus die nicht die Signatur besitzen: Prefix "jdbc" postfix ".csv".
     *
     * @version  $Revision$, $Date$
     */
    class JdbcCSVFileFilter implements FilenameFilter {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new JdbcCSVFileFilter object.
         */
        JdbcCSVFileFilter() {
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Tests if a specified file should be included in a file list.
         *
         * @param   dir   the directory in which the file was found.
         * @param   name  the name of the file.
         *
         * @return  <code>true</code> if and only if the name should be included in the file list; <code>false</code>
         *          otherwise.
         */
        public boolean accept(File dir, String name) {
            if (name.startsWith("jdbc")
                        && name.endsWith(".csv")) {
                return true;
            }

            return false;
        }
    }
}
