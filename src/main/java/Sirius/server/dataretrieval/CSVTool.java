/*
 * ResultSetToCSV.java
 *
 * Created on 17. M\u00E4rz 2004, 11:38
 */

package Sirius.server.dataretrieval;

import java.io.*;
import java.sql.*;

/**
 * Konvertiert ResultSet zu CSV. Erste Zeile beinhaltet die Spaltennamen, restlichen die Daten.
 * Standarddelimiter ist ",". Es wird keine \u00FCberpr\u00FCfung vorgenommen auf vorhandensein 
 * eines Delimiter-Zeichens in ausgelesenen werten.
 *
 * @author  awindholz
 */
public class CSVTool {
    
    private String delimiter_ = ",";
    private boolean withColNames_ = true;
    
    /**
     * Delim = ","
     */
    public CSVTool() {}
    
    /** Creates a new instance of ResultSetToCSV */
    public CSVTool(String delimiter) {
        delimiter_ = delimiter;
    }
    
    public void insertColumnNames(boolean withColNames) {
        withColNames_ = withColNames;
    }
    
    public void toCSV(ResultSet rs, PrintWriter pWriter) throws IOException, SQLException {
        writeResult(rs, pWriter, delimiter_);
    }
    
    /**
     * Schreibt Daten des \u00DCbergebenen ResultSet-Objektes in \u00FCbergebenen
     * PrintWriter-Objekt.
     */
    private void writeResult(ResultSet resultSet, PrintWriter pWriter, String delimiter)
    throws IOException, SQLException {
        
        ResultSetMetaData rsmd = resultSet.getMetaData();
        
        int columnCount = rsmd.getColumnCount();
        
        for(int column = 1; column < columnCount; column++) {
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
     */
    private void writeRow(ResultSet resultSet, PrintWriter pWriter, String delimiter, int columnCount)
    throws IOException, SQLException {
        
        String wert;
        
        for(int i = 1; i < columnCount; i++) {
            
            wert = resultSet.getString(i);
            
            if(!resultSet.wasNull()) {
                
                pWriter.print(wert.trim());
            }
            
            pWriter.print(",");
        }
        
        wert = resultSet.getString(columnCount);
        
        if(!resultSet.wasNull()) {
            
            pWriter.print(wert.trim());
        }
        
        pWriter.println("");
    }
}