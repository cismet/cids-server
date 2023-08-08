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
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;
import Sirius.server.sql.ServerSQLStatements;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        final List<CidsBeanInfo> dependentBeans = new ArrayList<>();

        if (mo == null) {
            throw new IllegalArgumentException("MetaObject must not be null"); // NOI18N
        } else if (mo.isDummy()) {
            // don't do anything with a dummy object
            if (LOG.isDebugEnabled()) {
                LOG.debug("insert index for dummy won't be done"); // NOI18N
            }
            return dependentBeans;
        }

        final ServerSQLStatements statements = getSQLStatements();
        try(final ResultSet masterClasses = connection.createStatement().executeQuery(
                            statements.getIndexTriggerSelectClassIdForeignKeyStmt(
                                mo.getMetaClass().getTableName(),
                                true))) {
            while (masterClasses.next()) {
                final String classKey = masterClasses.getString(1);

                try(final ResultSet field = connection.createStatement().executeQuery(
                                    statements.getIndexTriggerSelectFieldStmt(
                                        mo.getMetaClass().getTableName(),
                                        classKey))) {
                    if (field.next()) {
                        final String fieldName = field.getString(1);
                        try(final ResultSet oid = connection.createStatement().executeQuery(
                                            statements.getIndexTriggerSelectObjFieldStmt(
                                                fieldName,
                                                mo.getMetaClass().getTableName(),
                                                mo.getID()))) {
                            if (oid.next()) {
                                final int id = oid.getInt(1);
                                final CidsBeanInfo beanInfo = new CidsBeanInfo();
                                beanInfo.setObjectId(id);
                                beanInfo.setClassKey(classKey);
                                dependentBeans.add(beanInfo);
                            }
                        }
                    }
                }
            }
        }
        return dependentBeans;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ServerSQLStatements getSQLStatements() {
        return SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect());
    }

    /**
     * mscholl: Inserts the index in cs_attr_string and cs_attr_object for the given metaobject. If the metaobject does
     * not contain a metaclass it is skipped.
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
            final ServerSQLStatements statements = getSQLStatements();
            final int metaObjectId = mo.getID();
            final String metaClassKey = mo.getMetaClass().getTableName();

            for (final ObjectAttribute attr : mo.getAttribs()) {
                final MemberAttributeInfo mai = attr.getMai();
                if (mai.isIndexed()) {
                    // set the appropriate param values according to the field
                    // value
                    if (mai.isForeignKey()) {
                        final int foreignClassId = Math.abs(mai.getForeignKeyClassId());
                        try(final ResultSet rs = connection.createStatement().executeQuery(
                                            statements.getIndexTriggerSelectTableNameByClassIdStmt(foreignClassId))) {
                            if (rs.next()) {
                                final String foreignTableName = rs.getString(1);

                                if (mai.getForeignKeyClassId() < 0) {
                                    try(final ResultSet backreferenceRs = connection.createStatement().executeQuery(
                                                        statements.getIndexTriggerSelectBackReferenceStmt(
                                                            foreignClassId,
                                                            mai.getClassId()))) {
                                        if (backreferenceRs.next()) {
                                            final String backreferenceField = backreferenceRs.getString(1);
                                            try(final ResultSet arrayList = connection.createStatement().executeQuery(
                                                                statements.getIndexTriggerSelectFKIdStmt(
                                                                    foreignTableName,
                                                                    backreferenceField,
                                                                    metaObjectId))) {
                                                while (arrayList.next()) {
                                                    // lazily prepare the statement
                                                    if (psAttrMap == null) {
                                                        psAttrMap = connection.prepareStatement(
                                                                statements.getIndexTriggerInsertAttrObjectStmt());
                                                    }
                                                    final int attrObjectId = arrayList.getInt(1);

                                                    psAttrMap.setString(1, metaClassKey);
                                                    psAttrMap.setInt(2, metaObjectId);
                                                    psAttrMap.setString(3, foreignTableName);
                                                    psAttrMap.setInt(4, attrObjectId);
                                                    psAttrMap.addBatch();
                                                }
                                            }
                                        }
                                    }
                                } else if (mai.isArray()) {
                                    try(final ResultSet arrayList = connection.createStatement().executeQuery(
                                                        statements.getIndexTriggerSelectFKIdStmt(
                                                            foreignTableName,
                                                            mai.getArrayKeyFieldName(),
                                                            metaObjectId))) {
                                        while (arrayList.next()) {
                                            // lazily prepare the statement
                                            if (psAttrMap == null) {
                                                psAttrMap = connection.prepareStatement(
                                                        statements.getIndexTriggerInsertAttrObjectStmt());
                                            }
                                            psAttrMap.setString(1, metaClassKey);
                                            psAttrMap.setInt(2, metaObjectId);
                                            psAttrMap.setString(3, foreignTableName);
                                            psAttrMap.setInt(4, arrayList.getInt(1));
                                            psAttrMap.addBatch();
                                        }
                                    }
                                } else {
                                    // lazily prepare the statement
                                    if (psAttrMap == null) {
                                        psAttrMap = connection.prepareStatement(
                                                statements.getIndexTriggerInsertAttrObjectStmt());
                                    }
                                    psAttrMap.setString(1, metaClassKey);
                                    psAttrMap.setInt(2, metaObjectId);
                                    psAttrMap.setString(3, foreignTableName);
                                    // if field represents a foreign key the attribute value
                                    // is assumed to be a MetaObject
                                    final MetaObject value = (MetaObject)attr.getValue();
                                    psAttrMap.setInt(4, (value == null) ? -1 : value.getID());
                                    psAttrMap.addBatch();
                                }
                            }
                        }
                    } else {
                        // lazily prepare the statement
                        if (psAttrString == null) {
                            psAttrString = connection.prepareStatement(SQLTools.getStatements(
                                        Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                                            .getIndexTriggerInsertAttrStringStmt());
                        }
                        psAttrString.setString(1, metaClassKey);
                        psAttrString.setInt(2, metaObjectId);
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
                    LOG.debug("cs_attr_string: inserted " + insertCount + " rows"); // NOI18N
                }
            }
            if (psAttrMap != null) {
                final int[] mapRows = psAttrMap.executeBatch();
                if (LOG.isDebugEnabled()) {
                    int insertCount = 0;
                    for (final int row : mapRows) {
                        insertCount += row;
                    }
                    LOG.debug("cs_attr_object: inserted " + insertCount + " rows"); // NOI18N
                }
            }
            if (mo.getMetaClass().isIndexed()) {
                updateDerivedIndex(connection, mo);
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not insert index for object '"                               // NOI18N
                        + mo.getID()
                        + "' of class '"                                            // NOI18N
                        + mo.getClass()
                        + "'",                                                      // NOI18N
                e);
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
        updateDerivedIndex(connection, mo.getMetaClass().getTableName(), mo.getID());
    }

    /**
     * Updates the table cs_attr_object_derived.
     *
     * @param   connection  the connection to the database
     * @param   classKey    the class id of the oject that should be updated
     * @param   objectId    the object id of the oject that should be updated
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void updateDerivedIndex(final Connection connection, final String classKey, final int objectId)
            throws SQLException {
        final ServerSQLStatements statements = getSQLStatements();
        final PreparedStatement psDeleteAttrMapDerive = connection.prepareStatement(
                statements.getIndexTriggerDeleteAttrObjectDerivedStmt());
        final PreparedStatement psInsertAttrMapDerive = connection.prepareStatement(
                statements.getIndexTriggerInsertAttrObjectDerivedStmt());

        psDeleteAttrMapDerive.setString(1, classKey);
        psDeleteAttrMapDerive.setInt(2, objectId);
        psInsertAttrMapDerive.setString(1, classKey);
        psInsertAttrMapDerive.setInt(2, objectId);
        psInsertAttrMapDerive.setString(3, classKey);
        psInsertAttrMapDerive.setInt(4, objectId);
        psInsertAttrMapDerive.setString(5, classKey);
        psInsertAttrMapDerive.setInt(6, objectId);
        final int del = psDeleteAttrMapDerive.executeUpdate();
        final int ins = psInsertAttrMapDerive.executeUpdate();
        if (LOG.isDebugEnabled()) {
            LOG.debug("cs_attr_object_derived: updated. deleted:" + del + ", inserted:" + ins); // NOI18N
        }
    }

    /**
     * mscholl: Deletes the index from cs_attr_string and cs_attr_object for a given metaobject. If the metaobject does
     * not contain a metaclass it is skipped.
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
        final Integer metaObjectId = mo.getID();
        final String metaClassKey = mo.getMetaClass().getTableName();
        final ServerSQLStatements statements = getSQLStatements();
        try(final PreparedStatement psAttrString = connection.prepareStatement(
                            statements.getIndexTriggerDeleteAttrStringObjectStmt());
                    final PreparedStatement psAttrMap = connection.prepareStatement(
                            statements.getIndexTriggerDeleteAttrObjectObjectStmt());
                    final PreparedStatement psAttrDerive = connection.prepareStatement(
                            statements.getIndexTriggerDeleteAttrObjectDerivedStmt());
            ) {
            // set the appropriate param values
            psAttrString.setString(1, metaClassKey);
            psAttrString.setInt(2, metaObjectId);
            psAttrMap.setString(1, metaClassKey);
            psAttrMap.setInt(2, metaObjectId);
            psAttrDerive.setString(1, metaClassKey);
            psAttrDerive.setInt(2, metaObjectId);

            // execute the deletion
            final int strRows = psAttrString.executeUpdate();
            final int mapRows = psAttrMap.executeUpdate();
            final int mapDeriveRows = psAttrDerive.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("cs_attr_string: deleted " + strRows + " rows");               // NOI18N
                LOG.debug("cs_attr_object: deleted " + mapRows + " rows");               // NOI18N
                LOG.debug("cs_attr_object_derived: deleted " + mapDeriveRows + " rows"); // NOI18N
            }
        } catch (final SQLException e) {
            LOG.error(
                "could not delete index for object '"                                    // NOI18N
                        + metaObjectId
                        + "' of class '"                                                 // NOI18N
                        + metaClassKey
                        + "'",                                                           // NOI18N
                e);
            // TODO: consider to wrap exception
            throw e;
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
                        final CidsBeanInfo info = new CidsBeanInfo();
                        info.setClassKey(cidsBean.getMetaObject().getMetaClass().getTableName());
                        info.setObjectId(cidsBean.getMetaObject().getID());
                        beansToUpdate.add(info);
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
        final CidsBeanInfo info = new CidsBeanInfo();
        info.setClassKey(cidsBean.getMetaObject().getMetaClass().getTableName());
        info.setObjectId(cidsBean.getMetaObject().getID());
        beansToUpdate.add(info);
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
        final List<CidsBeanInfo> beansToUpdateTmp = new ArrayList<>(beansToUpdate);
        final List<CidsBean> beansToCheckTmp = new ArrayList<>(beansToCheck);
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

                            final CidsBeanInfo info = new CidsBeanInfo();
                            info.setClassKey(bean.getMetaObject().getMetaClass().getTableName());
                            info.setObjectId(bean.getMetaObject().getId());
                            beansToUpdateTmp.add(info);
                        }

                        for (final CidsBeanInfo beanInfo : beansToUpdateTmp) {
                            connection.createStatement()
                                    .execute(
                                        SQLTools.getStatements(
                                            Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                                            .getIndexTriggerSelectReindexPureStmt(
                                                beanInfo.getClassKey(),
                                                beanInfo.getObjectId()));
                            updateDerivedIndex(connection, beanInfo.getClassKey(), beanInfo.getObjectId());
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
        private String classKey;

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
         * @return  the classKey
         */
        public String getClassKey() {
            return classKey;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  classKey  the classKey to set
         */
        public void setClassKey(final String classKey) {
            this.classKey = classKey;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof CidsBeanInfo) {
                return (((CidsBeanInfo)obj).classKey == this.classKey)
                            && (((CidsBeanInfo)obj).objectId == this.objectId);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = (37 * hash) + this.objectId;
            hash = (37 * hash) + Objects.hashCode(this.classKey);
            return hash;
        }
    }
}
