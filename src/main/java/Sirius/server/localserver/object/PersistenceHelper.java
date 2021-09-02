/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Iterator;
import java.util.MissingResourceException;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class PersistenceHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG;
    protected static Class GEOMETRY;

    // static initializer
    static {
        LOG = Logger.getLogger(PersistenceHelper.class);
        try {
            GEOMETRY = Class.forName("com.vividsolutions.jts.geom.Geometry");                                        // NOI18N
        } catch (final ClassNotFoundException ex) {
            final String message = "JTS Geometry class not in classpath, thus cannot reach fully operational state"; // NOI18N
            LOG.fatal(message, ex);
            throw new IllegalStateException(message, ex);
        }
    }

    //~ Instance fields --------------------------------------------------------

    protected DBServer dbServer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistenceHelper object.
     *
     * @param  dbServer  DOCUMENT ME!
     */
    public PersistenceHelper(final DBServer dbServer) {
        this.dbServer = dbServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean toBeQuoted(final java.lang.Object value) {
        if (value == null) {
            return false;
        } else {
            return ((GEOMETRY.isAssignableFrom(value.getClass()) || (value instanceof java.sql.Date)
                                || (value instanceof java.util.Date)
                                || ((value instanceof String)
                                    && !((String)value).toLowerCase().startsWith("ST_GeometryFromText".toLowerCase()) // NOI18N
                                    && !((String)value).startsWith("GeometryFromText")                                // deprecated !
                                )
                                || (value instanceof Boolean)
                                || (value instanceof Character)));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mai  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  MissingResourceException  DOCUMENT ME!
     */
    boolean toBeQuoted(final MemberAttributeInfo mai) throws MissingResourceException {
        final int type = mai.getTypeId();

        final int[] quotedTypes = dbServer.getProperties().getQuotedTypes();

        for (int i = 0; i < quotedTypes.length; i++) {
            if (quotedTypes[i] == type) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mai    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  MissingResourceException  DOCUMENT ME!
     */
    boolean toBeQuoted(final MemberAttributeInfo mai, final java.lang.Object value) throws MissingResourceException {
        boolean q = false;

        q &= toBeQuoted(mai);

        q |= toBeQuoted(value);

        return q;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     * @param   key        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    int getNextID(final String tableName, final String key) throws SQLException {
        final Connection con = dbServer.getActiveDBConnection().getConnection();
        final String query = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getPersistenceHelperNextvalStmt(tableName.toUpperCase());

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            if (rs.next()) {
                return (rs.getInt(1));
            } else {
                return 1;
            }
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * Liefert Key zum Abfragen der MemberAttributInfo aus Hashmap das \u00FCber Fkt MetaClass.getMemberAttributeInfos()
     * geliefert wird.
     *
     * @param   mai    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDefaultValue(final MemberAttributeInfo mai, final java.lang.Object value) {
        String defaultVal = mai.getDefaultValue();

        if (defaultVal == null) {
            defaultVal = "NULL";                                                                                 // NOI18N
        }
        try {
            if (toBeQuoted(mai, value)) {
                defaultVal = "'" + defaultVal + "'";                                                             // NOI18N
            }
        } catch (final MissingResourceException e) {
            LOG.error(
                "Exception when trying to retrieve list of quoted types. Insert unsafe. "                        // NOI18N
                        + "Therefore default will be set to null (unquoted). This may lead to an SQL-Exception", // NOI18N
                e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("defaultValue :: " + defaultVal);                                                          // NOI18N
        }

        return defaultVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mo          DOCUMENT ME!
     * @param  metaClass   DOCUMENT ME!
     * @param  primaryKey  DOCUMENT ME!
     */
    void setPrimaryKey(final MetaObject mo, final MetaClass metaClass, final int primaryKey) {
        // primary key feld der Klasse dieses Objekts
        final String priK = metaClass.getPrimaryKey();

        // theoretisch k\u00F6nnen mehrere Attribute mit dem Namen des primary keys existieren
        final Iterator iter = mo.getAttributeByName(priK, 1).iterator();

        // iteriere \u00FCber primary keys setze auf primaryKey falls value des pk attributs null
        // iterator hier nur f\u00FCr den Fall das mehrere attribute gefunden werden
        if (iter.hasNext()) {
            // attribut (primary key)
            final ObjectAttribute oa = (ObjectAttribute)iter.next();
            java.lang.Object val = oa.getValue();

            if (LOG.isDebugEnabled()) {
                LOG.debug("primary key ::" + primaryKey); // NOI18N
            }

            if (oa.isPrimaryKey())                                                      // falls das attribut
                                                                                        // tats\u00E4chlich pk ist s.o.
            {
                if ((val == null)
                            || ((val != null) && val.toString().trim().equals("-1")     // NOI18N
                                && (val instanceof java.lang.Integer))) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("primary key is null set val to primaryKey::" + val); // NOI18N
                    }
                    oa.setValue(new Integer(primaryKey));
                    val = new Integer(primaryKey);
                } else {                                                                // val is not null and not -1
                    // skip
                    // lass es wie es ist
                }
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        "primary key name :: "        // NOI18N
                                + priK
                                + " :: for class :: " // NOI18N
                                + metaClass
                                + " :: is ambigious and only one attribute with this name is primary key"); // NOI18N
                }
            }
        }
    }
}
