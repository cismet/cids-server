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
import Sirius.server.sql.SQLTools;

import org.openide.util.lookup.ServiceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

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
public class IndexTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger severeIncidence = org.apache.log4j.Logger.getLogger(
            "severe.incidence");
    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            IndexTrigger.class);
    public static final String NULL = "NULL"; // NOI18N
    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            IndexTrigger.class);

    //~ Instance fields --------------------------------------------------------

    private List<CidsBean> beansToCheck = new ArrayList<CidsBean>();
    private List<CidsBeanInfo> beansToUpdate = new ArrayList<CidsBeanInfo>();
    private Connection con = null;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        de.cismet.tools.CismetThreadPool.executeSequentially(new Runnable() {

                @Override
                public void run() {
                    try {
                        final Connection connection = getLongtermConnection();
                        deleteIndex(connection, cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during deleteIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during deleteIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
        beansToCheck.add(cidsBean);
        de.cismet.tools.CismetThreadPool.executeSequentially(new Runnable() {

                @Override
                public void run() {
                    try {
                        final Connection connection = getLongtermConnection();
                        insertIndex(connection, cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during insertIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during insertIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        beansToCheck.add(cidsBean);
        // The triggers, which update the index should be executed sequentially, because
        // during the execution of the deleteMetaObject method, the updateMetaObject method can
        // be executed and this leads to a race condition between the
        // delete trigger and the update trigger
        de.cismet.tools.CismetThreadPool.executeSequentially(new Runnable() {

                @Override
                public void run() {
                    try {
                        final Connection connection = getLongtermConnection();
                        deleteIndex(connection, cidsBean.getMetaObject());
                        insertIndex(connection, cidsBean.getMetaObject());
//                        updateIndex(connection, cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during updateIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during updateIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
    }

    @Override
    public void beforeDelete(final CidsBean cidsBean, final User user) {
        // The object is deleted from the database after afterDelete trigger, so the
        // dependencies must be determined in the beforeDelete trigger.
        try {
            final Connection connection = getConnection();
            final List<CidsBeanInfo> beanInfo = getDependentBeans(connection, cidsBean.getMetaObject());
            addAll(beansToUpdate, beanInfo);
        } catch (SQLException sQLException) {
            log.error("Error during beforeDelete " + cidsBean.getMOString(), sQLException);
            severeIncidence.error("Error during beforeDelete " + cidsBean.getMOString(), sQLException);
        }
    }

    @Override
    public void beforeInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeUpdate(final CidsBean cidsBean, final User user) {
        // In the afterUpdate trigger, the object possibly references to other objects than now, so
        // the dependend objects must be also determined in the beforeUpdate trigger
        try {
            final Connection connection = getConnection();
            final List<CidsBeanInfo> beanInfo = getDependentBeans(connection, cidsBean.getMetaObject());
            addAll(beansToUpdate, beanInfo);
        } catch (SQLException sQLException) {
            log.error("Error during beforeDelete " + cidsBean.getMOString(), sQLException);
            severeIncidence.error("Error during beforeDelete " + cidsBean.getMOString(), sQLException);
        }
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
     * Determines all cids beans, the given meta object references to within an one to many relation.
     *
     * @param   connection  The connection to the database
     * @param   mo          the meta object to check
     *
     * @return  all master objects of the given meta object
     *
     * @throws  SQLException              DOCUMENT ME!
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private List<CidsBeanInfo> getDependentBeans(final Connection connection, final MetaObject mo) throws SQLException {
        final List<CidsBeanInfo> dependentBeans = new ArrayList<CidsBeanInfo>();

        if (mo == null) {
            throw new IllegalArgumentException("MetaObject must not be null"); // NOI18N
        } else if (mo.isDummy()) {
            // don't do anything with a dummy object
            if (LOG.isDebugEnabled()) {
                LOG.debug("insert index for dummy won't be done"); // NOI18N
            }
            return dependentBeans;
        }

        final String query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                    .getIndexTriggerSelectClassIdForeignKeyStmt((-1) * mo.getClassID());
        final ResultSet masterClasses = connection.createStatement().executeQuery(query);

        while (masterClasses.next()) {
            final int classId = masterClasses.getInt(1);

            final String fieldQuery = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                        .getIndexTriggerSelectFieldStmt(mo.getClassID(), classId);
            final ResultSet field = connection.createStatement().executeQuery(fieldQuery);

            if (field.next()) {
                final String fieldName = field.getString(1);
                final String idQuery = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                            .getIndexTriggerSelectObjFieldStmt(fieldName, mo.getMetaClass().getTableName(), mo.getID());
                final ResultSet oid = connection.createStatement().executeQuery(idQuery);

                if (oid.next()) {
                    final int id = oid.getInt(1);
                    final CidsBeanInfo beanInfo = new CidsBeanInfo();
                    beanInfo.setObjectId(id);
                    beanInfo.setClassId(classId);
                    dependentBeans.add(beanInfo);
                }

                oid.close();
            }

            field.close();
        }
        masterClasses.close();
        return dependentBeans;
    }

    /**
     * mscholl: Inserts the index in cs_attr_string and cs_all_attr_mapping for the given metaobject. If the metaobject
     * does not contain a metaclass it is skipped.
     *
     * @param   connection  DOCUMENT ME!
     * @param   mo          the metaobject which will be newly created
     *
     * @throws  SQLException              if an error occurs during index insertion
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void insertIndex(final Connection connection, final MetaObject mo) throws SQLException {
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
            deleteIndex(connection, mo);
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
                        if (mai.getForeignKeyClassId() < 0) {
                            final String backreferenceQuery = SQLTools.getStatements(dbServer.getProperties()
                                                .getInteralDialect())
                                        .getIndexTriggerSelectBackReferenceStmt(Math.abs(mai.getForeignKeyClassId()),
                                            mai.getClassId());
                            final ResultSet backreferenceRs = connection.createStatement()
                                        .executeQuery(backreferenceQuery);

                            if (backreferenceRs.next()) {
                                final String backreferenceField = backreferenceRs.getString(1);
                                backreferenceRs.close();
                                String query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                                            .getIndexTriggerSelectTableNameByClassIdStmt(Math.abs(
                                                    attr.getMai().getForeignKeyClassId()));
                                final ResultSet rs = connection.createStatement().executeQuery(query);

                                if (rs.next()) {
                                    final String foreignTableName = rs.getString(1);
                                    query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                                                .getIndexTriggerSelectFKIdStmt(
                                                        foreignTableName,
                                                        backreferenceField,
                                                        mo.getID());

                                    final ResultSet arrayList = connection.createStatement().executeQuery(query);

                                    while (arrayList.next()) {
                                        // lazily prepare the statement
                                        if (psAttrMap == null) {
                                            psAttrMap = connection.prepareStatement(SQLTools.getStatements(
                                                        getDbServer().getProperties().getInteralDialect())
                                                            .getIndexTriggerInsertAttrObjectStmt());
                                        }
                                        psAttrMap.setInt(1, mo.getClassID());
                                        psAttrMap.setInt(2, mo.getID());
                                        psAttrMap.setInt(3, Math.abs(mai.getForeignKeyClassId()));
                                        psAttrMap.setInt(4, arrayList.getInt(1));
                                        psAttrMap.addBatch();
                                    }

                                    arrayList.close();
                                }
                                rs.close();
                            } else {
                                LOG.error("Cannot fill index table properly, because the backreference was not found.");
                            }
                        } else if (mai.isArray()) {
                            attr.getTypeId();
                            String query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                                        .getIndexTriggerSelectTableNameByClassIdStmt(attr.getMai()
                                            .getForeignKeyClassId());
                            final ResultSet rs = connection.createStatement().executeQuery(query);

                            if (rs.next()) {
                                final String foreignTableName = rs.getString(1);
                                query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                                            .getIndexTriggerSelectFKIdStmt(
                                                    foreignTableName,
                                                    mai.getArrayKeyFieldName(),
                                                    mo.getID());

                                final ResultSet arrayList = connection.createStatement().executeQuery(query);

                                while (arrayList.next()) {
                                    // lazily prepare the statement
                                    if (psAttrMap == null) {
                                        psAttrMap = connection.prepareStatement(SQLTools.getStatements(
                                                    getDbServer().getProperties().getInteralDialect())
                                                        .getIndexTriggerInsertAttrObjectStmt());
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
                                psAttrMap = connection.prepareStatement(SQLTools.getStatements(
                                            getDbServer().getProperties().getInteralDialect())
                                                .getIndexTriggerInsertAttrObjectStmt());
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
                            psAttrString = connection.prepareStatement(SQLTools.getStatements(
                                        getDbServer().getProperties().getInteralDialect())
                                            .getIndexTriggerInsertAttrObjectStmt());
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
                if (mo.getMetaClass().isIndexed()) {
                    updateDerivedIndex(connection, mo);
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
     * @param       connection  DOCUMENT ME!
     * @param       mo          the metaobject which will be updated
     *
     * @throws      SQLException              if an error occurs during index update
     * @throws      IllegalArgumentException  NullPointerException DOCUMENT ME!
     *
     * @deprecated  This method does not work properly. If you have a class with multiple subclasses of the same type
     *              which are in the search index, the update command will modify all cs_attr_object entries of the same
     *              type with the same update command, so that all entries have the value that was set by the last
     *              update command.
     */
    private void updateIndex(final Connection connection, final MetaObject mo) throws SQLException {
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
        PreparedStatement psAttrMapArray = null;

        try {
            for (final ObjectAttribute attr : mo.getAttribs()) {
                final MemberAttributeInfo mai = attr.getMai();
                if (mai.isIndexed() && attr.isChanged()) {
                    // set the appropriate param values according to the field
                    // value
                    if (mai.isForeignKey()) {
                        if (mai.isArray()) {
                            attr.getTypeId();
                            String query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                                        .getIndexTriggerSelectTableNameByClassIdStmt(attr.getMai()
                                            .getForeignKeyClassId());
                            final ResultSet rs = connection.createStatement().executeQuery(query);

                            if (rs.next()) {
                                final String foreignTableName = rs.getString(1);
                                query = SQLTools.getStatements(dbServer.getProperties().getInteralDialect())
                                            .getIndexTriggerSelectFKIdStmt(
                                                    foreignTableName,
                                                    mai.getArrayKeyFieldName(),
                                                    mo.getID());

                                final ResultSet arrayList = connection.createStatement().executeQuery(query);
                                final PreparedStatement psAttrMapDelArray = connection.prepareStatement(
                                        SQLTools.getStatements(getDbServer().getProperties().getInteralDialect())
                                                    .getIndexTriggerDeleteAttrObjectArrayStmt());

                                psAttrMapDelArray.setInt(1, mo.getClassID());
                                psAttrMapDelArray.setInt(2, mo.getID());
                                psAttrMapDelArray.setInt(3, mai.getForeignKeyClassId());

                                psAttrMapDelArray.executeUpdate();

                                while (arrayList.next()) {
                                    // lazily prepare the statement
                                    if (psAttrMapArray == null) {
                                        psAttrMapArray = connection.prepareStatement(SQLTools.getStatements(
                                                    getDbServer().getProperties().getInteralDialect())
                                                        .getIndexTriggerInsertAttrObjectStmt());
                                    }
                                    psAttrMapArray.setInt(1, mo.getClassID());
                                    psAttrMapArray.setInt(2, mo.getID());
                                    psAttrMapArray.setInt(3, mai.getForeignKeyClassId());
                                    psAttrMapArray.setInt(4, arrayList.getInt(1));
                                    psAttrMapArray.addBatch();
                                }

                                arrayList.close();
                            }
                            rs.close();
                        } else {
                            // lazily prepare the statement
                            if (psAttrMap == null) {
                                psAttrMap = connection.prepareStatement(SQLTools.getStatements(
                                            getDbServer().getProperties().getInteralDialect())
                                                .getIndexTriggerUpdateAttrObjectStmt());
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
                                logMessage.append(SQLTools.getStatements(
                                        getDbServer().getProperties().getInteralDialect())
                                            .getIndexTriggerUpdateAttrObjectStmt());
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
                            psAttrString = connection.prepareStatement(SQLTools.getStatements(
                                        getDbServer().getProperties().getInteralDialect())
                                            .getIndexTriggerUpdateAttrStringStmt());
                        }
                        // interpret the fields value as a string
                        psAttrString.setString(1, (attr.getValue() == null) ? NULL : String.valueOf(attr.getValue()));
                        psAttrString.setInt(2, mo.getClassID());
                        psAttrString.setInt(3, mo.getID());
                        psAttrString.setInt(4, mai.getId());
                        psAttrString.addBatch();
                        if (LOG.isDebugEnabled()) {
                            final StringBuilder logMessage = new StringBuilder("Parameterized SQL added to batch: ");
                            logMessage.append(SQLTools.getStatements(
                                    getDbServer().getProperties().getInteralDialect())
                                        .getIndexTriggerUpdateAttrStringStmt());
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
                    LOG.debug("cs_attr_string: updated " + updateCount + " rows");                  // NOI18N
                }
            }
            if (psAttrMap != null) {
                final int[] mapRows = psAttrMap.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int updateCount = 0;
                    for (final int row : mapRows) {
                        updateCount += row;
                    }
                    LOG.debug("cs_attr_object(complex objects): updated " + updateCount + " rows"); // NOI18N
                }
            }
            if (psAttrMapArray != null) {
                final int[] mapRows = psAttrMapArray.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int updateCount = 0;
                    for (final int row : mapRows) {
                        updateCount += row;
                    }
                    LOG.debug("cs_attr_object(Array-Entries): updated " + updateCount + " rows");   // NOI18N
                }
            }
            if (mo.getMetaClass().isIndexed()) {
                updateDerivedIndex(connection, mo);
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not insert index for object '"                                               // NOI18N
                        + mo.getID()
                        + "' of class '"                                                            // NOI18N
                        + mo.getClass()
                        + "'",                                                                      // NOI18N
                e);
            // TODO: consider to wrap exception
            throw e;
        } finally {
            DBConnection.closeStatements(psAttrString, psAttrMap);
        }
    }

    /**
     * Updates the table cs_attr_object_derived.
     *
     * @param   connection  the connection to the database
     * @param   mo          the object that should be updated
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void updateDerivedIndex(final Connection connection, final MetaObject mo) throws SQLException {
        updateDerivedIndex(connection, mo.getClassID(), mo.getID());
    }

    /**
     * Updates the table cs_attr_object_derived.
     *
     * @param   connection  the connection to the database
     * @param   classId     the class id of the oject that should be updated
     * @param   objectId    the object id of the oject that should be updated
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void updateDerivedIndex(final Connection connection, final int classId, final int objectId)
            throws SQLException {
        final PreparedStatement psDeleteAttrMapDerive = connection.prepareStatement(SQLTools.getStatements(
                    getDbServer().getProperties().getInteralDialect()).getIndexTriggerDeleteAttrObjectDerivedStmt());
        final PreparedStatement psInsertAttrMapDerive = connection.prepareStatement(SQLTools.getStatements(
                    getDbServer().getProperties().getInteralDialect()).getIndexTriggerInsertAttrObjectDerivedStmt());

        psDeleteAttrMapDerive.setInt(1, classId);
        psDeleteAttrMapDerive.setInt(2, objectId);
        psInsertAttrMapDerive.setInt(1, classId);
        psInsertAttrMapDerive.setInt(2, objectId);
        final int del = psDeleteAttrMapDerive.executeUpdate();
        final int ins = psInsertAttrMapDerive.executeUpdate();
        if (LOG.isDebugEnabled()) {
            LOG.debug("cs_attr_object_derived: updated. deleted:" + del + ", inserted:" + ins); // NOI18N
        }
    }

    /**
     * mscholl: Deletes the index from cs_attr_string and cs_all_attr_mapping for a given metaobject. If the metaobject
     * does not contain a metaclass it is skipped.
     *
     * @param   connection  DOCUMENT ME!
     * @param   mo          the metaobject which will be deleted
     *
     * @throws  SQLException              if an error occurs during index deletion
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void deleteIndex(final Connection connection, final MetaObject mo) throws SQLException {
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
        PreparedStatement psAttrDerive = null;
        try {
            // prepare the update statements
            psAttrString = connection.prepareStatement(SQLTools.getStatements(
                        getDbServer().getProperties().getInteralDialect()).getIndexTriggerDeleteAttrStringObjectStmt());
            psAttrMap = connection.prepareStatement(SQLTools.getStatements(
                        getDbServer().getProperties().getInteralDialect()).getIndexTriggerDeleteAttrObjectObjectStmt());
            psAttrDerive = connection.prepareStatement(SQLTools.getStatements(
                        getDbServer().getProperties().getInteralDialect())
                            .getIndexTriggerDeleteAttrObjectDerivedStmt());
            // set the appropriate param values
            psAttrString.setInt(1, mo.getClassID());
            psAttrString.setInt(2, mo.getID());
            psAttrMap.setInt(1, mo.getClassID());
            psAttrMap.setInt(2, mo.getID());
            psAttrDerive.setInt(1, mo.getClassID());
            psAttrDerive.setInt(2, mo.getID());

            // execute the deletion
            final int strRows = psAttrString.executeUpdate();
            final int mapRows = psAttrMap.executeUpdate();
            final int mapDeriveRows = psAttrDerive.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("cs_attr_string: deleted " + strRows + " rows");                   // NOI18N
                LOG.debug("cs_all_attr_mapping: deleted " + mapRows + " rows");              // NOI18N
                LOG.debug("cs_all_attr_mapping_derive: deleted " + mapDeriveRows + " rows"); // NOI18N
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not delete index for object '"                                        // NOI18N
                        + mo.getID()
                        + "' of class '"                                                     // NOI18N
                        + mo.getClass()
                        + "'",                                                               // NOI18N
                e);
            // TODO: consider to wrap exception
            throw e;
        } finally {
            DBConnection.closeStatements(psAttrString, psAttrMap);
        }
    }

    @Override
    public void afterCommittedInsert(final CidsBean cidsBean, final User user) {
        de.cismet.tools.CismetThreadPool.executeSequentially(new Runnable() {

                @Override
                public void run() {
                    try {
                        final Connection connection = getLongtermConnection();
                        insertIndex(connection, cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during insertIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during insertIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
        updateAllDependentBeans();
    }

    @Override
    public void afterCommittedUpdate(final CidsBean cidsBean, final User user) {
        de.cismet.tools.CismetThreadPool.executeSequentially(new Runnable() {

                @Override
                public void run() {
                    try {
                        // Some times, the master object is not updates, but only the detail objects.
                        // In this case, the index of the master object should be also updated.
                        final Connection connection = getLongtermConnection();
                        deleteIndex(connection, cidsBean.getMetaObject());
                        insertIndex(connection, cidsBean.getMetaObject());
                    } catch (SQLException sQLException) {
                        log.error("Error during updateIndex " + cidsBean.getMOString(), sQLException);
                        severeIncidence.error("Error during updateIndex " + cidsBean.getMOString(), sQLException);
                    }
                }
            });
        updateAllDependentBeans();
    }

    @Override
    public void afterCommittedDelete(final CidsBean cidsBean, final User user) {
        updateAllDependentBeans();
    }

    /**
     * Updates the index of the master objects (master in an one to many relation).
     */
    private void updateAllDependentBeans() {
        final List<CidsBeanInfo> beansToUpdateTmp = new ArrayList<CidsBeanInfo>(beansToUpdate);
        final List<CidsBean> beansToCheckTmp = new ArrayList<CidsBean>(beansToCheck);
        beansToUpdate.clear();
        beansToCheck.clear();

        de.cismet.tools.CismetThreadPool.executeSequentially(new Runnable() {

                @Override
                public void run() {
                    try {
                        final Connection connection = getLongtermConnection();
                        for (final CidsBean bean : beansToCheckTmp) {
                            final List<CidsBeanInfo> beanInfo = getDependentBeans(
                                    connection,
                                    bean.getMetaObject());
                            addAll(beansToUpdateTmp, beanInfo);
                        }

                        for (final CidsBeanInfo beanInfo : beansToUpdateTmp) {
                            connection.createStatement()
                                    .execute(
                                        SQLTools.getStatements(getDbServer().getProperties().getInteralDialect())
                                            .getIndexTriggerSelectReindexPureStmt(
                                                beanInfo.getClassId(),
                                                beanInfo.getObjectId()));
                            updateDerivedIndex(connection, beanInfo.getClassId(), beanInfo.getObjectId());
                        }
                    } catch (SQLException sQLException) {
                        log.error("Error during updateAllDependentBeans ", sQLException);
                        severeIncidence.error("Error during updateAllDependentBeans ", sQLException);
                    } finally {
                        releaseConnection();
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private synchronized Connection getLongtermConnection() throws SQLException {
        return getConnection(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private synchronized Connection getConnection() throws SQLException {
        return getDbServer().getConnectionPool().getConnection(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   longterm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private synchronized Connection getConnection(final boolean longterm) throws SQLException {
        if ((con == null) || con.isClosed()) {
            if ((con != null) && con.isClosed()) {
                getDbServer().getConnectionPool().releaseDbConnection(con);
            }

            con = getDbServer().getConnectionPool().getConnection(longterm);
        }

        return con;
    }

    /**
     * DOCUMENT ME!
     */
    private synchronized void releaseConnection() {
        if (con != null) {
            getDbServer().getConnectionPool().releaseDbConnection(con);
            con = null;
        }
    }

    /**
     * add all elements from to list toAdd to the list base, if they are not already contained in the list base.
     *
     * @param  base   the list to fill
     * @param  toAdd  the elements to add
     */
    private void addAll(final List<CidsBeanInfo> base, final List<CidsBeanInfo> toAdd) {
        for (final CidsBeanInfo tmp : toAdd) {
            if (!base.contains(tmp)) {
                base.add(tmp);
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Contains all information, which are required to identify a cids bean.
     *
     * @version  $Revision$, $Date$
     */
    private class CidsBeanInfo {

        //~ Instance fields ----------------------------------------------------

        private int objectId;
        private int classId;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the objectId
         */
        public int getObjectId() {
            return objectId;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  objectId  the objectId to set
         */
        public void setObjectId(final int objectId) {
            this.objectId = objectId;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the classId
         */
        public int getClassId() {
            return classId;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  classId  the classId to set
         */
        public void setClassId(final int classId) {
            this.classId = classId;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof CidsBeanInfo) {
                return (((CidsBeanInfo)obj).classId == this.classId) && (((CidsBeanInfo)obj).objectId == this.objectId);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = (37 * hash) + this.objectId;
            hash = (37 * hash) + this.classId;
            return hash;
        }
    }
}
