/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * JDBCProto.java
 *
 * Created on 8. September 2003, 13:32
 */
package Sirius.server.dataretrieval;

import java.io.*;

import java.sql.*;

import java.util.*;

import Sirius.util.*;

import Sirius.server.middleware.types.*;

import Sirius.metajdbc.driver.*;

import org.apache.log4j.*;

/**
 * Liest MetaDataObject, erstellt eine Verbindung zum Datenbank-Server und liefert das zwischengespeicherte
 * ResultSet-Objekt (als Byte-Array in einem DataObject) zur\u00FCck. Bildet ein Dataretrieval-Protokol. * Bezeichnung
 * dieses Protokols ist "jdbc". Die Gr\u00F6sse des Caches wird aus der BackingStrore abgefragt, wenn dort keine Daten
 * vorhanden sind wird standardwert von 0,5 MB genommen. Das Backing Store kann \u00FCber main dieser Klasse gesetzt
 * werden.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class JDBCProto implements MetaObjectProto {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger logger = Logger.getLogger(JDBCProto.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Wenn keine Daten \u00FCber maximale gr\u00F6sse in der BackingStore gesetzt oder ein fehler passiert wird die
     * Gr\u00F6se auf 0,5 MB gesetzt.
     */
    public JDBCProto() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String qualifiedKey() {
        return "/Sirius/dataretrieval/JDBCProto";  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getMaxCacheSize() {
        int maxCacheSize = 512000;

        try {
            String size = Integer.toString(maxCacheSize);
            size = PreferencesTool.getPreference(true, qualifiedKey(), size);
            maxCacheSize = Integer.parseInt(size);
            if(logger.isInfoEnabled())
                logger.info("maxCacheSize was set to " + maxCacheSize + ".");  // NOI18N
        } catch (Exception e) {
            String warn = "Maximum size of cached result set is set to "  // NOI18N
                + maxCacheSize + ". Original message: "  // NOI18N
                + System.getProperty("line.separator") + e.getMessage();  // NOI18N

            logger.warn(warn, e);
        }

        return maxCacheSize;
    }

    public DataObject getDataObject(MetaObject metaDataObject) throws DataRetrievalException {
        ProtoDelegator protoDelegator = new ProtoDelegator(logger);
        DataObject ret = null;
        Properties info = new Properties();

        MetaObject param_mo = protoDelegator.getParameterMO(metaDataObject);

        // URL url_tmp = protoDelegator.getURL(param_mo);

        String url = protoDelegator.getURL(param_mo);

        // String url = url_tmp.toString();

        logger.warn("url: " + url);  // NOI18N

        // String url = (String)protoDelegator.getSingleValue(metaDataObject, "URL");
        String driver = (String)protoDelegator.getSingleValue(param_mo, "driver");  // NOI18N
        String login = (String)protoDelegator.getSingleValue(param_mo, "login");  // NOI18N
        String password = (String)protoDelegator.getSingleValue(param_mo, "password");  // NOI18N
        String query = (String)protoDelegator.getSingleValue(param_mo, "statement");  // NOI18N

        // url = Parametrizer.parametrize(url, metaDataObject);

        query = Parametrizer.parametrize(query, metaDataObject);

        try {
            Connection con;
            Class.forName(driver.trim());

            // DriverManager.setLogWriter(new PrintWriter(System.err));
            // con = DriverManager.getConnection(url, info);
            con = DriverManager.getConnection(url, login, password);

            PreparedStatement stmt = con.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();

            String name = metaDataObject.getName();
            if ((name == null) || (name.length() == 0)) {
                name = String.valueOf(System.currentTimeMillis());
            }

            return createDataObject(rs, query, name);
        } catch (SQLException e) {
            String message = "Error occurs during attempt to call up a objekt "  // NOI18N
                + " of the database: Original message: " + e.getMessage();  // NOI18N

            String debugMessage = message + " Gesendeter Statement: " + query;  // NOI18N

            DataRetrievalException newEx = new DataRetrievalException(message, e);
            if (logger.isDebugEnabled()) {
                // logger.error(message);
                logger.debug(debugMessage, e);
            }

            throw newEx;
        } catch (ClassNotFoundException e) {
            String message = "Error occurs during attempt to call up a objekt "  // NOI18N
                + " of the database: The indicated driver \""  // NOI18N
                + driver + "\" was not found.";  // NOI18N

            throw new DataRetrievalException(message, e, logger);
        }
    }

    /**
     * Speichert das \u00FCbergebene ResultSet zwischen und liefert dieses in serialisierter Form in einem DataObjekt
     * zur\u00FCck.
     *
     * @param   rs     das ReultSet das serialisiert werden soll.
     * @param   query  die query die an Datenbank abgesetzt wurde, durch welche dieses ResultSet entstanden ist.
     * @param   name   DOCUMENT ME!
     *
     * @return  Objekt vom typ DataObject das enth\u00E4lt das Serialisierte ResultSet Objekt, sowie eine textuelle
     *          kurzbeschreibung in einem String.
     *
     * @throws  DataRetrievalException      DOCUMENT ME!
     * @throws  ResultSetTooLargeException  DOCUMENT ME!
     */
    protected DataObject createDataObject(ResultSet rs, String query, String name) throws DataRetrievalException,
        ResultSetTooLargeException {
        try {
            CachedRS cRS = new CachedRS();
            // cRS.populate(rs);
            cRS.populate(getMaxCacheSize(), rs);

            String do_name = name + ".rs";  // NOI18N

            return new DataObject(cRS.serialize(cRS), "Deserialisieren zu ResultSet.", do_name);  // NOI18N

//            return new DataObject(new byte[0], "tescht");
        } catch (IOException e) {
            String message = "Error occurs during attempt to serialise a objekt."  // NOI18N
                + " Original message: " + e.getMessage();  // NOI18N
            throw new DataRetrievalException(message, e, logger);
        } catch (SQLException e) {
            String message = "Database error occurs, during attempt to call up"  // NOI18N
                + " a objekt drom database. Original message: " + e.getMessage();  // NOI18N

            throw new DataRetrievalException(message, e, logger);
        } catch (Sirius.metajdbc.driver.ResultSetTooLargeException e) {
            throw new ResultSetTooLargeException(e, logger);
        }
    }

    /**
     * Wenn es sich bei \u00FCbergebenem Parameter um instantion dieser Klasse handelt wird true geliefert.
     *
     * @return  true wenn o eine instantion dieser Klasse ist.
     */
/*    public boolean equals(Object o) {
        return (o instanceof JDBCProto);
    }*/

    /**
     * Bezeichner f\u00FCr diesen Protokol.
     *
     * @return  jdbc;
     */
    public String getDataSourceClass() {
        return "jdbc";  // NOI18N
    }

    /**
     * Liefert den ersten gefundenen MetaAttribut mit dem angegebenem Namen im aktuell gesetztem MetaAttribut. Keine
     * Rekursion.
     *
     * @param  args  DOCUMENT ME!
     *
     * @name   Name des Attributes.
     */
/*    protected ObjectAttribute getSingleAttribute(MetaObject metaObject, String name) {
        ObjectAttribute[] mas = metaObject.getAttributes();

        for(int i = 0; i < mas.length; i++) {
            if(mas[i].getName().equals(name))
                return mas[i];
        }
        return null;
    }*/

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            System.exit(-1);
        }
        try {
            String intValue = new Integer(args[0]).toString();
            PreferencesTool.setPreference(true, qualifiedKey(), intValue);
            System.out.println("Daten in Backingstore geschrieben: "  // NOI18N
                + "(" + qualifiedKey() + ", " + args[0] + ")");  // NOI18N
        } catch (NumberFormatException e) {
            usage();
            System.exit(-2);
        } catch (Exception e) {
        }
    }
    /**
     * DOCUMENT ME!
     */
    private static void usage() {
        String usage = JDBCProto.class.getName() + " <maxCacheSize>";  // NOI18N
        System.out.println("Usage:");  // NOI18N
        System.out.println("java " + usage);  // NOI18N
    }
} // class end
// -------------------------------------------------------------------------- //
/*    static {
        MetaObjectProtoMgr.register(new JDBCProto());
    }*/

/**
 * Diese Klasse Testet ob es sich bei dem \u00FCbergebenem String um eine URL
 * f\u00FCr JDBC-Treiber handelt. Akzeptiert Strings die mit "jdbc:" beginnen.
 * Caseunsensitiv.
 *
 * @param url der URL-String dass auf Protokol \u00FCberpr\u00FCft werden soll.
 */
/*    public boolean acceptsURL(String url) {

        int minLength = 5;
        String correctProtoString = "jdbc:";

        if(url.length() < minLength) return false;

        String protoString = url.substring(0, minLength);

        return protoString.equalsIgnoreCase(correctProtoString);
    }*/
