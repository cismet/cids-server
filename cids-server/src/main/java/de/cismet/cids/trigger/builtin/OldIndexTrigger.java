/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger.builtin;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.sql.DBConnection;

import org.openide.util.lookup.ServiceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.trigger.AbstractDBAwareCidsTrigger;
import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsTrigger.class)
public class OldIndexTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger severeIncidence = org.apache.log4j.Logger.getLogger(
            "severe.incidence");
    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            OldIndexTrigger.class);
    public static final String NULL = "NULL";                                       // NOI18N
    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            OldIndexTrigger.class);
    public static final String DEL_ATTR_STRING = "DELETE FROM cs_attr_string "      // NOI18N
                + "WHERE class_id = ? AND object_id = ?";                           // NOI18N
    public static final String DEL_ATTR_MAPPING = "DELETE FROM cs_attr_object "     // NOI18N
                + "WHERE class_id = ? AND object_id = ?";                           // NOI18N
    public static final String INS_ATTR_STRING = "INSERT INTO cs_attr_string "      // NOI18N
                + "(class_id, object_id, attr_id, string_val) VALUES (?, ?, ?, ?)"; // NOI18N
    public static final String INS_ATTR_MAPPING = "INSERT INTO cs_attr_object "     // NOI18N
                + "(class_id, object_id, attr_class_id, attr_object_id) VALUES "    // NOI18N
                + "(?, ?, ?, ?)";                                                   // NOI18N
    public static final String UP_ATTR_STRING = "UPDATE cs_attr_string "            // NOI18N
                + "SET string_val = ? "                                             // NOI18N
                + "WHERE class_id = ? AND object_id = ? AND attr_id = ?";           // NOI18N
    public static final String UP_ATTR_MAPPING = "UPDATE cs_attr_object "           // NOI18N
                + "SET attr_object_id = ? "                                         // NOI18N
                + "WHERE class_id = ? AND object_id = ? AND attr_class_id = ?";     // NOI18N

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        de.cismet.tools.CismetThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        deleteIndex(cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during deleteIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during deleteIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
        de.cismet.tools.CismetThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        insertIndex(cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during insertIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during insertIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        de.cismet.tools.CismetThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        updateIndex(cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during updateIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during updateIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
    }

    @Override
    public void beforeDelete(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public CidsTriggerKey getTriggerKey() {
        return CidsTriggerKey.FORALL;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final CidsTrigger o) {
        return -1;
    }

    /**
     * mscholl: Inserts the index in cs_attr_string and cs_all_attr_mapping for the given metaobject. If the metaobject
     * does not contain a metaclass it is skipped.
     *
     * @param   mo  the metaobject which will be newly created
     *
     * @throws  SQLException              if an error occurs during index insertion
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void insertIndex(final MetaObject mo) throws SQLException {
        final Connection connection = getDbServer().getConnectionPool().getConnection();
        if (mo == null) {
            throw new IllegalArgumentException("MetaObject must not be null"); // NOI18N
        } else if (mo.isDummy()) {
            // don't do anything with a dummy object
            if (LOG.isDebugEnabled()) {
                LOG.debug("insert index for dummy won't be done"); // NOI18N
            }
            return;
        } else if (LOG.isInfoEnabled()) {
            LOG.info("insert index for MetaObject: " + mo);        // NOI18N
        }
        try {
            // we just want to make sure that there is no index present for the
            // given object
            deleteIndex(mo);
        } catch (final SQLException e) {
            LOG.error("could not delete index before insert index", e); // NOI18N
            throw e;
        }
        PreparedStatement psAttrString = null;
        PreparedStatement psAttrMap = null;
        try {
            for (final ObjectAttribute attr : mo.getAttribs()) {
                final MemberAttributeInfo mai = attr.getMai();
                if (mai.isIndexed()) {
                    // set the appropriate param values according to the field
                    // value
                    if (mai.isForeignKey()) {
                        if (mai.isArray()) {
                            attr.getTypeId();
                            String query = "SELECT table_name FROM cs_class where id = "
                                        + attr.getMai().getForeignKeyClassId();
                            final ResultSet rs = connection.createStatement().executeQuery(query);

                            if (rs.next()) {
                                final String foreignTableName = rs.getString(1);
                                query = "SELECT id as id FROM " + foreignTableName + " WHERE "
                                            + mai.getArrayKeyFieldName()
                                            + " =  " + String.valueOf(mo.getID());

                                final ResultSet arrayList = connection.createStatement().executeQuery(query);

                                while (arrayList.next()) {
                                    // lazily prepare the statement
                                    if (psAttrMap == null) {
                                        psAttrMap = connection.prepareStatement(INS_ATTR_MAPPING);
                                    }
                                    psAttrMap.setInt(1, mo.getClassID());
                                    psAttrMap.setInt(2, mo.getID());
                                    psAttrMap.setInt(3, mai.getForeignKeyClassId());
                                    psAttrMap.setInt(4, arrayList.getInt(1));
                                    psAttrMap.addBatch();
                                }

                                arrayList.close();
                            }
                            rs.close();
                        } else {
                            // lazily prepare the statement
                            if (psAttrMap == null) {
                                psAttrMap = connection.prepareStatement(INS_ATTR_MAPPING);
                            }
                            psAttrMap.setInt(1, mo.getClassID());
                            psAttrMap.setInt(2, mo.getID());
                            psAttrMap.setInt(3, mai.getForeignKeyClassId());
                            // if field represents a foreign key the attribute value
                            // is assumed to be a MetaObject
                            final MetaObject value = (MetaObject)attr.getValue();
                            psAttrMap.setInt(4, (value == null) ? -1 : value.getID());
                            psAttrMap.addBatch();
                        }
                    } else {
                        // lazily prepare the statement
                        if (psAttrString == null) {
                            psAttrString = connection.prepareStatement(INS_ATTR_STRING);
                        }
                        psAttrString.setInt(1, mo.getClassID());
                        psAttrString.setInt(2, mo.getID());
                        psAttrString.setInt(3, mai.getId());
                        // interpret the fields value as a string
                        psAttrString.setString(4, (attr.getValue() == null) ? NULL : String.valueOf(attr.getValue()));
                        psAttrString.addBatch();
                    }
                }
            }

            // execute the batches if there are indexed fields
            if (psAttrString != null) {
                final int[] strRows = psAttrString.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int insertCount = 0;
                    for (final int row : strRows) {
                        insertCount += row;
                    }
                    LOG.debug("cs_attr_string: inserted " + insertCount + " rows");      // NOI18N
                }
            }
            if (psAttrMap != null) {
                final int[] mapRows = psAttrMap.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int insertCount = 0;
                    for (final int row : mapRows) {
                        insertCount += row;
                    }
                    LOG.debug("cs_all_attr_mapping: inserted " + insertCount + " rows"); // NOI18N
                }
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not insert index for object '"                                    // NOI18N
                        + mo.getID()
                        + "' of class '"                                                 // NOI18N
                        + mo.getClass()
                        + "'",                                                           // NOI18N
                e);
            throw e;
        } finally {
            DBConnection.closeStatements(psAttrString, psAttrMap);
        }
    }

    /**
     * mscholl: Updates the index of cs_attr_string and cs_all_attr_mapping for the given metaobject. Update for a
     * certain attribute will only be done if the attribute is changed.
     *
     * @param   mo  the metaobject which will be updated
     *
     * @throws  SQLException              if an error occurs during index update
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void updateIndex(final MetaObject mo) throws SQLException {
        final Connection connection = getDbServer().getConnectionPool().getConnection();

        if (mo == null) {
            throw new IllegalArgumentException("MetaObject must not be null"); // NOI18N
        } else if (mo.isDummy()) {
            // don't do anything with a dummy object
            if (LOG.isDebugEnabled()) {
                LOG.debug("update index for dummy won't be done"); // NOI18N
            }
            return;
        } else if (LOG.isInfoEnabled()) {
            LOG.info("update index for MetaObject: " + mo);        // NOI18N
        }
        PreparedStatement psAttrString = null;
        PreparedStatement psAttrMap = null;
        try {
            for (final ObjectAttribute attr : mo.getAttribs()) {
                final MemberAttributeInfo mai = attr.getMai();
                if (mai.isIndexed() && attr.isChanged()) {
                    // set the appropriate param values according to the field
                    // value
                    if (mai.isForeignKey()) {
                        if (mai.isArray()) {
                            attr.getTypeId();
                            String query = "SELECT table_name FROM cs_class where id = "
                                        + attr.getMai().getForeignKeyClassId();
                            final ResultSet rs = connection.createStatement().executeQuery(query);

                            if (rs.next()) {
                                final String foreignTableName = rs.getString(1);
                                query = "SELECT id as id FROM " + foreignTableName + " WHERE "
                                            + mai.getArrayKeyFieldName()
                                            + " =  " + String.valueOf(mo.getID());

                                final ResultSet arrayList = connection.createStatement().executeQuery(query);

                                while (arrayList.next()) {
                                    // lazily prepare the statement
                                    if (psAttrMap == null) {
                                        psAttrMap = connection.prepareStatement(UP_ATTR_MAPPING);
                                    }
                                    psAttrMap.setInt(1, arrayList.getInt(1));
                                    psAttrMap.setInt(2, mo.getClassID());
                                    psAttrMap.setInt(3, mo.getID());
                                    psAttrMap.setInt(4, mai.getForeignKeyClassId());
                                    psAttrMap.addBatch();
                                }

                                arrayList.close();
                            }
                            rs.close();
                        } else {
                            // lazily prepare the statement
                            if (psAttrMap == null) {
                                psAttrMap = connection.prepareStatement(UP_ATTR_MAPPING);
                            }
                            // if field represents a foreign key the attribute value
                            // is assumed to be a MetaObject
                            final MetaObject value = (MetaObject)attr.getValue();
                            psAttrMap.setInt(1, (value == null) ? -1 : value.getID());
                            psAttrMap.setInt(2, mo.getClassID());
                            psAttrMap.setInt(3, mo.getID());
                            psAttrMap.setInt(4, mai.getForeignKeyClassId());
                            psAttrMap.addBatch();
                            if (LOG.isDebugEnabled()) {
                                final StringBuilder logMessage = new StringBuilder(
                                        "Parameterized SQL added to batch: ");
                                logMessage.append(UP_ATTR_MAPPING);
                                logMessage.append('\n');
                                logMessage.append("attr_obj_id: ");
                                logMessage.append(String.valueOf((value == null) ? -1 : value.getID()));
                                logMessage.append("class_id: ");
                                logMessage.append(String.valueOf(mo.getClassID()));
                                logMessage.append("object_id: ");
                                logMessage.append(String.valueOf(mo.getID()));
                                logMessage.append("attr_class_id: ");
                                logMessage.append(String.valueOf(mai.getForeignKeyClassId()));
                                LOG.debug(logMessage.toString());
                            }
                        }
                    } else {
                        // lazily prepare the statement
                        if (psAttrString == null) {
                            psAttrString = connection.prepareStatement(UP_ATTR_STRING);
                        }
                        // interpret the fields value as a string
                        psAttrString.setString(1, (attr.getValue() == null) ? NULL : String.valueOf(attr.getValue()));
                        psAttrString.setInt(2, mo.getClassID());
                        psAttrString.setInt(3, mo.getID());
                        psAttrString.setInt(4, mai.getId());
                        psAttrString.addBatch();
                        if (LOG.isDebugEnabled()) {
                            final StringBuilder logMessage = new StringBuilder("Parameterized SQL added to batch: ");
                            logMessage.append(UP_ATTR_MAPPING);
                            logMessage.append('\n');
                            logMessage.append("attr_obj_id: ");
                            logMessage.append(String.valueOf(attr.getValue()));
                            logMessage.append("class_id: ");
                            logMessage.append(String.valueOf(mo.getClassID()));
                            logMessage.append("object_id: ");
                            logMessage.append(String.valueOf(mo.getID()));
                            logMessage.append("attr_class_id: ");
                            logMessage.append(String.valueOf(mai.getId()));
                            LOG.debug(logMessage.toString());
                        }
                    }
                }
            }

            // execute the batches if there are indexed fields
            if (psAttrString != null) {
                final int[] strRows = psAttrString.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int updateCount = 0;
                    for (final int row : strRows) {
                        updateCount += row;
                    }
                    LOG.debug("cs_attr_string: updated " + updateCount + " rows");      // NOI18N
                }
            }
            if (psAttrMap != null) {
                final int[] mapRows = psAttrMap.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int updateCount = 0;
                    for (final int row : mapRows) {
                        updateCount += row;
                    }
                    LOG.debug("cs_all_attr_mapping: updated " + updateCount + " rows"); // NOI18N
                }
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not insert index for object '"                                   // NOI18N
                        + mo.getID()
                        + "' of class '"                                                // NOI18N
                        + mo.getClass()
                        + "'",                                                          // NOI18N
                e);
            // TODO: consider to wrap exception
            throw e;
        } finally {
            DBConnection.closeStatements(psAttrString, psAttrMap);
        }
    }

    /**
     * mscholl: Deletes the index from cs_attr_string and cs_all_attr_mapping for a given metaobject. If the metaobject
     * does not contain a metaclass it is skipped.
     *
     * @param   mo  the metaobject which will be deleted
     *
     * @throws  SQLException              if an error occurs during index deletion
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void deleteIndex(final MetaObject mo) throws SQLException {
        final Connection connection = getDbServer().getConnectionPool().getConnection();

        if (mo == null) {
            throw new IllegalArgumentException("MetaObject must not be null"); // NOI18N
        } else if (mo.isDummy()) {
            // don't do anything with a dummy object
            if (LOG.isDebugEnabled()) {
                LOG.debug("delete index for dummy won't be done"); // NOI18N
            }
            return;
        } else if (LOG.isInfoEnabled()) {
            LOG.info("delete index for MetaObject: " + mo);        // NOI18N
        }
        PreparedStatement psAttrString = null;
        PreparedStatement psAttrMap = null;
        try {
            // prepare the update statements
            psAttrString = connection.prepareStatement(DEL_ATTR_STRING);
            psAttrMap = connection.prepareStatement(DEL_ATTR_MAPPING);

            // set the appropriate param values
            psAttrString.setInt(1, mo.getClassID());
            psAttrString.setInt(2, mo.getID());
            psAttrMap.setInt(1, mo.getClassID());
            psAttrMap.setInt(2, mo.getID());

            // execute the deletion
            final int strRows = psAttrString.executeUpdate();
            final int mapRows = psAttrMap.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("cs_attr_string: deleted " + strRows + " rows");      // NOI18N
                LOG.debug("cs_all_attr_mapping: deleted " + mapRows + " rows"); // NOI18N
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not delete index for object '"                           // NOI18N
                        + mo.getID()
                        + "' of class '"                                        // NOI18N
                        + mo.getClass()
                        + "'",                                                  // NOI18N
                e);
            // TODO: consider to wrap exception
            throw e;
        } finally {
            DBConnection.closeStatements(psAttrString, psAttrMap);
        }
    }
}
