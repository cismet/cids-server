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


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Iterator;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class PersistenceHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger;
    protected static Class GEOMETRY;

    // static initializer
    static {
        logger = org.apache.log4j.Logger.getLogger(PersistenceHelper.class);
        try {
            GEOMETRY = Class.forName("com.vividsolutions.jts.geom.Geometry"); // NOI18N
        } catch (ClassNotFoundException ex) {
            logger.error(ex);
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
                                || ((value instanceof java.lang.String)
                                    && !((String)value).startsWith("GeometryFromText")) // NOI18N
                                || (value instanceof java.lang.Boolean)
                                || (value instanceof java.lang.Character)));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mai  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.util.MissingResourceException  DOCUMENT ME!
     */
    boolean toBeQuoted(final MemberAttributeInfo mai) throws java.util.MissingResourceException {
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
     * @throws  java.util.MissingResourceException  DOCUMENT ME!
     */
    boolean toBeQuoted(final MemberAttributeInfo mai, final java.lang.Object value)
            throws java.util.MissingResourceException {
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
        // String query = "SELECT MAX(" + key + ") FROM " + tableName;
        final String query = "SELECT NEXTVAL('" + tableName.toUpperCase() + "_SEQ')"; // NOI18N

        // logger.debug("next value key "+query);

        final ResultSet rs = con.createStatement().executeQuery(query);

        if (rs.next()) {
            return (rs.getInt(1));
        } else {
            return 1;
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
            defaultVal = "NULL";                                                                                                                                           // NOI18N
        }
        try {
            if (toBeQuoted(mai, value)) {
                defaultVal = "'" + defaultVal + "'";                                                                                                                       // NOI18N
            }
        } catch (java.util.MissingResourceException e) {
            logger.error(
                "Exception when trying to retrieve list of quoted types insert unsafe therefore default will be set to null (unquoted) this may lead to an SQL-Exception", // NOI18N
                e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("defaultValue :: " + defaultVal);                                                                                                                 // NOI18N
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

            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("primary key ::" + primaryKey); // NOI18N
                }
            }

            if (oa.isPrimaryKey())                                                                      // falls das attribut tats\u00E4chlich pk ist s.o.
            {
                if ((val == null)
                            || ((val != null) && val.toString().trim().equals("-1")                     // NOI18N
                                && (val instanceof java.lang.Integer))) {
                    if (logger != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("primary key is null set val to primaryKey::" + val);          // NOI18N
                        }
                    }
                    oa.setValue(new Integer(primaryKey));
                    val = new Integer(primaryKey);
                } else {                                                                                // val is not null and not -1
                    ;                                                                                   // lass es wie es ist
                }
            } else {
                logger.info(
                    "primary key name :: "                                                              // NOI18N
                            + priK
                            + " :: for class :: "                                                       // NOI18N
                            + metaClass
                            + " :: is ambigious and only one attribute with this name is primary key"); // NOI18N
            }
        }
    }
}
