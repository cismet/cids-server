/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ResultSetToCSV.java
 *
 * Created on 17. M\u00E4rz 2004, 11:38
 */
package Sirius.server.dataretrieval;

import java.io.*;

import java.sql.*;

/**
 * Konvertiert ResultSet zu CSV. Erste Zeile beinhaltet die Spaltennamen, restlichen die Daten. Standarddelimiter ist
 * ",". Es wird keine \u00FCberpr\u00FCfung vorgenommen auf vorhandensein eines Delimiter-Zeichens in ausgelesenen
 * werten.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class CSVTool {

    //~ Instance fields --------------------------------------------------------

    private String delimiter_ = ",";
    private boolean withColNames_ = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Delim = ","
     */
    public CSVTool() {
    }

    /**
     * Creates a new instance of ResultSetToCSV.
     *
     * @param  delimiter  DOCUMENT ME!
     */
    public CSVTool(String delimiter) {
        delimiter_ = delimiter;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  withColNames  DOCUMENT ME!
     */
    public void insertColumnNames(boolean withColNames) {
        withColNames_ = withColNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rs       DOCUMENT ME!
     * @param   pWriter  DOCUMENT ME!
     *
     * @throws  IOException   DOCUMENT ME!
     * @throws  SQLException  DOCUMENT ME!
     */
    public void toCSV(ResultSet rs, PrintWriter pWriter) throws IOException, SQLException {
        writeResult(rs, pWriter, delimiter_);
    }

    /**
     * Schreibt Daten des \u00DCbergebenen ResultSet-Objektes in \u00FCbergebenen PrintWriter-Objekt.
     *
     * @param   resultSet  DOCUMENT ME!
     * @param   pWriter    DOCUMENT ME!
     * @param   delimiter  DOCUMENT ME!
     *
     * @throws  IOException   DOCUMENT ME!
     * @throws  SQLException  DOCUMENT ME!
     */
    private void writeResult(ResultSet resultSet, PrintWriter pWriter, String delimiter) throws IOException,
        SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();

        int columnCount = rsmd.getColumnCount();

        for (int column = 1; column < columnCount; column++) {
            pWriter.print(rsmd.getColumnName(column) + delimiter);
        }

        pWriter.println(rsmd.getColumnName(columnCount));

        while (resultSet.next()) {
            writeRow(resultSet, pWriter, delimiter, columnCount);
        }

        pWriter.flush();
    }

    /**
     * Schreibt nur eine Zeile in den PrintWriter.
     *
     * @param   resultSet    DOCUMENT ME!
     * @param   pWriter      DOCUMENT ME!
     * @param   delimiter    DOCUMENT ME!
     * @param   columnCount  DOCUMENT ME!
     *
     * @throws  IOException   DOCUMENT ME!
     * @throws  SQLException  DOCUMENT ME!
     */
    private void writeRow(ResultSet resultSet, PrintWriter pWriter, String delimiter, int columnCount)
        throws IOException, SQLException {
        String wert;

        for (int i = 1; i < columnCount; i++) {
            wert = resultSet.getString(i);

            if (!resultSet.wasNull()) {
                pWriter.print(wert.trim());
            }

            pWriter.print(",");
        }

        wert = resultSet.getString(columnCount);

        if (!resultSet.wasNull()) {
            pWriter.print(wert.trim());
        }

        pWriter.println("");
    }
}
