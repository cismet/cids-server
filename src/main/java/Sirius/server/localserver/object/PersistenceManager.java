/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.PreparableStatement;
import Sirius.server.sql.SQLTools;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.beans.PropertyVetoException;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;
import de.cismet.cids.trigger.DBAwareCidsTrigger;

import de.cismet.commons.utils.StackUtils;

/**
 * DOCUMENT ME!
 *
 * @author   sascha.schlobinski@cismet.de
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class PersistenceManager extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(PersistenceManager.class);
    private static final transient Logger LOG_PERFORMANCE = Logger.getLogger("PersistPerformance");
    public static final String NULL = "NULL"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient DBServer dbServer;
    private final transient PersistenceHelper persistenceHelper;
    private final Collection<? extends CidsTrigger> allTriggers;
    private final Collection<CidsTrigger> generalTriggers = new ArrayList<CidsTrigger>();
    private final Collection<CidsTrigger> crossDomainTrigger = new ArrayList<CidsTrigger>();
    private final HashMap<CidsTriggerKey, Collection<CidsTrigger>> triggers =
        new HashMap<CidsTriggerKey, Collection<CidsTrigger>>();
    private final transient ThreadLocal<TransactionHelper> local;
    private final transient ComboPooledDataSource pool;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistenceManager object.
     *
     * @param   dbServer  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public PersistenceManager(final DBServer dbServer) {
        this.dbServer = dbServer;

        persistenceHelper = new PersistenceHelper(dbServer);
        final Lookup.Result<CidsTrigger> result = Lookup.getDefault().lookupResult(CidsTrigger.class);
        allTriggers = result.allInstances();
        for (final CidsTrigger t : allTriggers) {
            if (t instanceof DBAwareCidsTrigger) {
                if (CidsTriggerKey.ALL.toLowerCase().equals(t.getTriggerKey().getDomain())
                            || DomainServerImpl.getServerProperties().getServerName().toLowerCase().equals(
                                t.getTriggerKey().getDomain())) {
                    ((DBAwareCidsTrigger)t).setDbServer(dbServer);
                }
            }
            if (triggers.containsKey(t.getTriggerKey())) {
                final Collection<CidsTrigger> c = triggers.get(t.getTriggerKey());
                assert (c != null);
                c.add(t);
            } else {
                final Collection<CidsTrigger> c = new ArrayList<>();
                c.add(t);
                triggers.put(t.getTriggerKey(), c);
            }
        }

        try {
            pool = new ComboPooledDataSource();
            pool.setDriverClass(dbServer.getSystemProperties().getJDBCDriver());
            pool.setJdbcUrl(dbServer.getSystemProperties().getDbConnectionString());
            pool.setUser(dbServer.getSystemProperties().getDbUser());
            pool.setPassword(dbServer.getSystemProperties().getDbPassword());

            pool.setMinPoolSize(5);
            pool.setAcquireIncrement(5);
            pool.setMaxPoolSize(dbServer.getSystemProperties().getPoolSize());
        } catch (PropertyVetoException ex) {
            throw new IllegalStateException("pool could not be initialized", ex);
        }

        local = new ThreadLocal<TransactionHelper>() {

                @Override
                protected TransactionHelper initialValue() {
                    try {
                        return new TransactionHelper(pool.getConnection());
                    } catch (final SQLException ex) {
                        throw new IllegalStateException("transactionhelper could not be created", ex);
                    }
                }
            };

        addShutdown(new AbstractShutdownable() {

                @Override
                protected void internalShutdown() throws ServerExitError {
                    try {
                        DataSources.destroy(pool);
                    } catch (final SQLException ex) {
                        throw new ServerExitError("cannot bring down c3p0 pool cleanly", ex); // NOI18N
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private long startPerformanceMeasurement() {
        if (LOG_PERFORMANCE.isDebugEnabled()) {
            try {
                return System.currentTimeMillis();
            } catch (final Throwable t) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("error while doing performance measurement", t);
                }
            }
        }
        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name    DOCUMENT ME!
     * @param  mo      DOCUMENT ME!
     * @param  before  DOCUMENT ME!
     */
    private void stopPerformanceMeasurement(final String name, final MetaObject mo, final long before) {
        if (LOG_PERFORMANCE.isDebugEnabled()) {
            try {
                final long time = (System.currentTimeMillis() - before);
                if (LOG_PERFORMANCE.isDebugEnabled()) {
                    LOG_PERFORMANCE.debug("PerformanceTest: " + time + "ms |  " + name + " | MetaClass: "
                                + mo.getMetaClass().getName() + " | MetaObject: " + mo.getId());
                }
            } catch (final Throwable t) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("error while doing performance measurement", t);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     */
    public int insertMetaObject(final User user, final MetaObject mo) throws PersistenceException {
        try {
            long before = 0;

            before = startPerformanceMeasurement();
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            final TransactionHelper transactionHelper = local.get();
            transactionHelper.beginWork();
            final int rtn = insertMetaObjectWithoutTransaction(user, mo);
            transactionHelper.commit();
            stopPerformanceMeasurement("insertMetaObject - insertMetaObjectWithoutTransaction", mo, before);

            before = startPerformanceMeasurement();
            for (final CidsTrigger ct : rightTriggers) {
                ct.afterCommittedInsert(mo.getBean(), user);
            }
            stopPerformanceMeasurement("insertMetaObject - afterCommittedInsert", mo, before);

            return rtn;
        } catch (final Exception e) {
            final String message = "cannot insert metaobject"; // NOI18N
            LOG.error(message, e);
            rollback();
            throw new PersistenceException(message, e);
        } finally {
            local.get().close();
            local.remove();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     */
    public void updateMetaObject(final User user, final MetaObject mo) throws PersistenceException, SQLException {
        if (!(mo instanceof LightweightMetaObject)) {
            try {
                long before = 0;

                before = startPerformanceMeasurement();
                final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
                final TransactionHelper transactionHelper = local.get();
                transactionHelper.beginWork();
                updateMetaObjectWithoutTransaction(user, mo);
                transactionHelper.commit();
                stopPerformanceMeasurement("updateMetaObject - updateMetaObjectWithoutTransaction", mo, before);

                before = startPerformanceMeasurement();
                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterCommittedUpdate(mo.getBean(), user);
                }
                stopPerformanceMeasurement("updateMetaObject - afterCommittedUpdate", mo, before);
            } catch (final Exception e) {
                final String message = "cannot update metaobject"; // NOI18N
                LOG.error(message, e);
                rollback();
                throw new PersistenceException(message, e);
            } finally {
                local.get().close();
                local.remove();
            }
        } else {
            LOG.info("The object that should be updated is a lightweight object. So the update is ignored.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     */
    public int deleteMetaObject(final User user, final MetaObject mo) throws PersistenceException {
        long before = 0;
        before = startPerformanceMeasurement();
        try {
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            final TransactionHelper transactionHelper = local.get();
            transactionHelper.beginWork();
            final int rtn = deleteMetaObjectWithoutTransaction(user, mo);
            transactionHelper.commit();
            stopPerformanceMeasurement("deleteMetaObject - deleteMetaObjectWithoutTransaction", mo, before);

            before = startPerformanceMeasurement();
            for (final CidsTrigger ct : rightTriggers) {
                ct.afterCommittedDelete(mo.getBean(), user);
            }
            stopPerformanceMeasurement("deleteMetaObject - afterCommittedDelete", mo, before);
            return rtn;
        } catch (final Exception e) {
            final String message = "cannot delete metaobject"; // NOI18N
            LOG.error(message, e);
            rollback();
            throw new PersistenceException(message, e);
        } finally {
            local.get().close();
            local.remove();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     */
    private void rollback() throws PersistenceException {
        try {
            final TransactionHelper transactionHelper = local.get();
            transactionHelper.rollback();
        } catch (final SQLException ex) {
            final String error = "cannot rollback transaction, this can cause inconsistent database state"; // NOI18N
            LOG.error(error, ex);
            throw new PersistenceException(error, ex);
        }
    }

    /**
     * loescht mo und alle Objekte die mo als Attribute hat.
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     * @throws  SecurityException     DOCUMENT ME!
     */
    private int deleteMetaObjectWithoutTransaction(final User user, final MetaObject mo) throws PersistenceException,
        SQLException {
        long before = 0;
        before = startPerformanceMeasurement();
        fixMissingMetaClass(mo);
        stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - fixMissingMetaClass", mo, before);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "deleteMetaObject entered "            // NOI18N
                        + mo
                        + "status :"                   // NOI18N
                        + mo.getStatus()
                        + " of class:"                 // NOI18N
                        + mo.getClassID()
                        + " isDummy(ArrayContainer) :" // NOI18N
                        + mo.isDummy());
        }

        if (dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(
                        user)
                    && (mo.isDummy() || mo.hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann

            before = startPerformanceMeasurement();
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            for (final CidsTrigger ct : rightTriggers) {
                ct.beforeDelete(mo.getBean(), user);
            }
            stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - beforeDeleteTriggers", mo, before);

            before = startPerformanceMeasurement();
            PreparedStatement stmt = null;
            try {
                // Mo was created artificially (array holder) so there is no object to delete
                // directly proceed to subObjects
                if (mo == null) {
                    LOG.error("cannot delete MetaObject == null"); // NOI18N
                    return 0;
                }

                final ObjectAttribute[] allAttributes = mo.getAttribs();
                boolean deeper = false;
                for (final ObjectAttribute oa : allAttributes) {
                    if (oa.isChanged()) {
                        deeper = true;
                        break;
                    }
                }
                stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - checkForDeeper", mo, before);

                before = startPerformanceMeasurement();
                if (deeper) {
                    updateMetaObjectWithoutTransaction(user, mo);
                }
                stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - updateDeeper", mo, before);

                before = startPerformanceMeasurement();
                // retrieve the metaObject's class
                final Sirius.server.localserver._class.Class c = dbServer.getClass(user, mo.getClassID());
                // get Tablename from class
                final String tableName = c.getTableName();
                // get primary Key from class
                final String pk = c.getPrimaryKey();
                // add tablename and whereclause to the delete statement

                final String paramStmt = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                    .getDialect())
                            .getPersistenceManagerDeleteFromStmt(tableName, pk);

                if (LOG.isDebugEnabled()) {
                    final StringBuilder logMessage = new StringBuilder("Parameterized SQL: ");
                    logMessage.append(paramStmt);
                    logMessage.append('\n');
                    logMessage.append("Primary key: ");
                    logMessage.append(String.valueOf(mo.getPrimaryKey().getValue()));
                    LOG.debug(logMessage.toString());
                }

                final TransactionHelper transactionHelper = local.get();
                stmt = transactionHelper.getConnection().prepareStatement(paramStmt);
                stmt.setObject(1, mo.getPrimaryKey().getValue());
                // execute deletion and retrieve number of affected objects
                int result = stmt.executeUpdate();

                stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - executeUpdate", mo, before);

                before = startPerformanceMeasurement();
                // now delete all array entries
                for (final ObjectAttribute oa : allAttributes) {
                    final java.lang.Object value = oa.getValue();
                    if (value instanceof MetaObject) {
                        final MetaObject arrayMo = (MetaObject)value;
                        // 1-n kinder löschen
                        if (oa.isVirtualOneToManyAttribute()) {
                            result += deleteOneToManyChildsWithoutTransaction(user, arrayMo);
                        }
                        // n-m kinder löschen
                        if (oa.isArray()) {
                            result += deleteArrayEntriesWithoutTransaction(user, arrayMo);
                        }
                    }
                }

                stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - deleteAllArrayEntries", mo, before);

                before = startPerformanceMeasurement();
                // if the metaobject is deleted it is obviously not persistent anymore
                mo.setPersistent(false);

                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterDelete(mo.getBean(), user);
                }
                stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - afterDeleteTriggers", mo, before);
                return result;
            } finally {
                before = startPerformanceMeasurement();
                DBConnection.closeStatements(stmt);
                stopPerformanceMeasurement("deleteMetaObjectWithoutTransaction - closeStatements", mo, before);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "'"                                        // NOI18N
                            + user
                            + "' is not allowed to delete MO " // NOI18N
                            + mo.getID()
                            + "."                              // NOI18N
                            + mo.getClassKey(),
                    StackUtils.getDebuggingThrowable());
            }
            // TODO: shouldn't that return -1 or similar to indicate that nothing has been done?
            throw new SecurityException("not allowed to delete meta object"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   arrayMo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException          DOCUMENT ME!
     * @throws  PersistenceException  DOCUMENT ME!
     */
    private int deleteOneToManyChildsWithoutTransaction(final User user, final MetaObject arrayMo) throws SQLException,
        PersistenceException {
        long before = 0;
        before = startPerformanceMeasurement();
        fixMissingMetaClass(arrayMo);
        stopPerformanceMeasurement("deleteOneToManyChildsWithoutTransaction - fixMissingMetaClass", arrayMo, before);

        if (!arrayMo.isDummy()) {
            LOG.error("deleteOneToManyEntries on a metaobject that is not a dummy");
            // TODO maybe better throw an exception ?
            return 0;
        }

        int result = 0;
        for (final ObjectAttribute oa : arrayMo.getAttribs()) {
            final MetaObject childMO = (MetaObject)oa.getValue();
            result += deleteMetaObjectWithoutTransaction(user, childMO);
        }
        return result;
    }

    /**
     * Deletes all link-entries of the array dummy-object.
     *
     * @param   user     DOCUMENT ME!
     * @param   arrayMo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private int deleteArrayEntriesWithoutTransaction(final User user, final MetaObject arrayMo) throws SQLException {
        long before = 0;
        before = startPerformanceMeasurement();
        fixMissingMetaClass(arrayMo);
        stopPerformanceMeasurement("deleteArrayEntriesWithoutTransaction - fixMissingMetaClass", arrayMo, before);

        if (!arrayMo.isDummy()) {
            LOG.error("deleteArrayEntries on a metaobject that is not a dummy");
        }

        // initialize number of affected objects
        PreparedStatement stmt = null;

        try {
            before = startPerformanceMeasurement();
            final String tableName = arrayMo.getMetaClass().getTableName();
            final String arrayKeyFieldName = arrayMo.getReferencingObjectAttribute().getMai().getArrayKeyFieldName();
            final String paramStmt = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                .getDialect())
                        .getPersistenceManagerDeleteFromStmt(tableName, arrayKeyFieldName);

            final TransactionHelper transactionHelper = local.get();
            stmt = transactionHelper.getConnection().prepareStatement(paramStmt);
            stmt.setObject(1, arrayMo.getId());
            // execute deletion and retrieve number of affected objects
            final int result = stmt.executeUpdate();

            stopPerformanceMeasurement("deleteArrayEntriesWithoutTransaction - executeUpdate", arrayMo, before);

            if (LOG.isDebugEnabled()) {
                LOG.debug("array elements deleted :: " + result); // NOI18N
            }

            return result;
        } finally {
            before = startPerformanceMeasurement();
            DBConnection.closeStatements(stmt);
            stopPerformanceMeasurement("deleteArrayEntriesWithoutTransaction - closeStatements", arrayMo, before);
        }
    }

    /**
     * Given metaobject and subobjects will be updated if changed.
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @throws  PersistenceException   Throwable DOCUMENT ME!
     * @throws  SQLException           DOCUMENT ME!
     * @throws  IllegalStateException  Exception DOCUMENT ME!
     * @throws  SecurityException      DOCUMENT ME!
     */
    private void updateMetaObjectWithoutTransaction(final User user, final MetaObject mo) throws PersistenceException,
        SQLException {
        long before = 0;
        before = startPerformanceMeasurement();
        fixMissingMetaClass(mo);
        stopPerformanceMeasurement("updateMetaObjectWithoutTransaction - fixMissingMetaClass", mo, before);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "updateMetaObject entered "            // NOI18N
                        + mo
                        + "status :"                   // NOI18N
                        + mo.getStatus()
                        + " of class:"                 // NOI18N
                        + mo.getClassID()
                        + " isDummy(ArrayContainer) :" // NOI18N
                        + mo.isDummy());               // NOI18N
        }
        if (dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(
                        user)
                    && (mo.isDummy() || mo.hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann
            // existiert gar keine sinnvolle
            // bean
            before = startPerformanceMeasurement();

            // if Array
            if (mo.isDummy()) {
                updateArrayObjectsWithoutTransaction(user, mo);
                return;
            }
            stopPerformanceMeasurement(
                "updateMetaObjectWithoutTransaction - updateArrayObjectsWithoutTransaction",
                mo,
                before);

            before = startPerformanceMeasurement();
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            stopPerformanceMeasurement("updateMetaObjectWithoutTransaction - getRightTriggers", mo, before);
            before = startPerformanceMeasurement();
            for (final CidsTrigger ct : rightTriggers) {
                final long beforeFine = startPerformanceMeasurement();
                ct.beforeUpdate(mo.getBean(), user);
                stopPerformanceMeasurement("beforeUpdate " + ct.toString(), mo, beforeFine);
            }
            stopPerformanceMeasurement("updateMetaObjectWithoutTransaction - beforeUpdateTriggers", mo, before);

            // variables for sql statement
            // retrieve class object
            final MetaClass metaClass = dbServer.getClass(mo.getClassID());
            // retrieve object attributes
            final ObjectAttribute[] mAttr = mo.getAttribs();
            MemberAttributeInfo mai;
            // counts fields to update, if 0 no update will be done at all
            int updateCounter = 0;
            final List<String> fieldNames = new ArrayList<String>();
            final ArrayList values = new ArrayList(mAttr.length);
            final TransactionHelper transactionHelper = local.get();
            // iterate over all attributes
            for (int i = 0; i < mAttr.length; ++i) {
                // if it is not changed, skip and proceed
                if (!mAttr[i].isChanged()) {
                    continue;
                }
                mai = mAttr[i].getMai();
                if (mai == null) {
                    throw new IllegalStateException("MAI not found: " + mAttr[i].getName()); // NOI18N
                }
                if (mai.isExtensionAttribute()) {
                    // extension attributes should be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(mAttr[i] + "is an extension attribute -> ignored");
                    }

                    continue;
                }
                // fieldname is now known, find value now
                final java.lang.Object value = mAttr[i].getValue();

                java.lang.Object valueToAdd = NULL;
                if (value == null) {
                    // delete MetaObject???
                    valueToAdd = NULL;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("valueString set to '" + NULL + "' as value of attribute was null"); // NOI18N
                    }
                } else if (value instanceof MetaObject) {
                    final MetaObject subObject = (MetaObject)value;
                    // CUD for the subobject
                    switch (subObject.getStatus()) {
                        case MetaObject.NEW: {
                            // set new key
                            final int key = insertMetaObjectWithoutTransaction(user, subObject);
                            if (subObject.isDummy()) {
                                valueToAdd = mo.getID(); // set value to primary key
                                insertMetaObjectArrayWithoutTransaction(user, subObject);
                            } else {
                                valueToAdd = key;
                            }
                            break;
                        }
                        case MetaObject.TO_DELETE: {
                            deleteMetaObjectWithoutTransaction(user, subObject);
                            valueToAdd = NULL;
                            break;
                        }
                        case MetaObject.NO_STATUS: {
                            if (subObject.isDummy()) {
                                updateMetaObjectWithoutTransaction(user, subObject);
                            }
                            valueToAdd = subObject.getID();
                            break;
                        }
                        // fall through because we define no status as modified status
                        case MetaObject.MODIFIED: {
                            updateMetaObjectWithoutTransaction(user, subObject);
                            valueToAdd = subObject.getID();

                            break;
                        }
                        default: {
                            // should never occur
                            // TODO: consider to LOG fatal!
                            LOG.error(
                                "error updating subobject '"   // NOI18N
                                        + subObject
                                        + "' of attribute "    // NOI18N
                                        + mai.getFieldName()
                                        + ": invalid status: " // NOI18N
                                        + subObject.getStatus());
                            // TODO: throw illegalstateexception ?
                        }
                    }
                } else {
                    before = startPerformanceMeasurement();
                    if (PersistenceHelper.GEOMETRY.isAssignableFrom(value.getClass())) {
                        valueToAdd = SQLTools.getGeometryFactory(Lookup.getDefault().lookup(DialectProvider.class)
                                            .getDialect()).getDbString((Geometry)value);
                    } else {
                        valueToAdd = value;
                    }
                    stopPerformanceMeasurement(
                        "updateMetaObjectWithoutTransaction - getDbObject",
                        mo,
                        before);
                }
                if (!mAttr[i].isVirtualOneToManyAttribute()) {
                    values.add(valueToAdd);

                    fieldNames.add(mai.getFieldName());
                    ++updateCounter;
                }
            }

            if (updateCounter > 0) {
                PreparedStatement stmt = null;
                try {
                    // statment done, just append the where clause using the object's primary key
                    values.add(Integer.valueOf(mo.getID()));

                    PreparableStatement paramStmt = null;
                    if (metaClass.getAttribs() != null) {
                        for (final ClassAttribute ca : metaClass.getAttribs()) {
                            if (ca.getName().equals("customUpdateStmt")) {
                                paramStmt = PreparableStatement.fromString((String)ca.getValue());
                                break;
                            }
                        }
                    }
                    if (paramStmt == null) {
                        paramStmt = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                            .getDialect())
                                    .getPersistenceManagerUpdateStmt(metaClass.getTableName(),
                                            metaClass.getPrimaryKey(),
                                            fieldNames.toArray(new String[fieldNames.size()]));
                    }

                    if (LOG.isDebugEnabled()) {
                        final StringBuilder logMessage = new StringBuilder("Parameterized SQL: ");
                        logMessage.append(paramStmt);
                        logMessage.append('\n');
                        final int i = 1;
                        for (final java.lang.Object value : values) {
                            if (i > 1) {
                                logMessage.append("; ");
                            }
                            logMessage.append(i);
                            logMessage.append(". parameter: ");
                            logMessage.append(value.toString());
                        }
                        LOG.debug(logMessage.toString(), new Exception());
                    }

                    before = startPerformanceMeasurement();

                    paramStmt.setObjects(values.toArray());
                    stmt = paramStmt.parameterise(transactionHelper.getConnection());
                    stmt.executeUpdate();
                    stopPerformanceMeasurement("updateMetaObjectWithoutTransaction - executeUpdate", mo, before);

                    /*
                     * since the meta-jdbc driver is obsolete the index must be refreshed by the server explicitly
                     */

                    before = startPerformanceMeasurement();
                    for (final CidsTrigger ct : rightTriggers) {
                        ct.afterUpdate(mo.getBean(), user);
                    }
                    stopPerformanceMeasurement("updateMetaObjectWithoutTransaction - afterUpdateTriggers", mo, before);
                } finally {
                    before = startPerformanceMeasurement();
                    DBConnection.closeStatements(stmt);
                    stopPerformanceMeasurement("updateMetaObjectWithoutTransaction - closeStatements", mo, before);
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "'"                                                // NOI18N
                            + user
                            + "' is not allowed to update MetaObject " // NOI18N
                            + mo.getID()
                            + "."                                      // NOI18N
                            + mo.getClassKey(),
                    StackUtils.getDebuggingThrowable());
            }
            throw new SecurityException("not allowed to update meta object"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stmt    DOCUMENT ME!
     * @param   values  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private PreparedStatement parameteriseStatement(final PreparedStatement stmt, final List values)
            throws SQLException {
        final ParameterMetaData metaData = stmt.getParameterMetaData();
        for (int i = 0; i < values.size(); ++i) {
            final int type = metaData.getParameterType(i + 1);
            if (NULL.equals(values.get(i))) {
                stmt.setNull(i + 1, type);
            } else {
                stmt.setObject(i + 1, values.get(i), type);
            }
        }

        return stmt;
    }

    /**
     * Processes all array elements.
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     */
    private void updateArrayObjectsWithoutTransaction(final User user, final MetaObject mo) throws PersistenceException,
        SQLException {
        long before = 0;
        before = startPerformanceMeasurement();
        fixMissingMetaClass(mo);
        stopPerformanceMeasurement("updateArrayObjectsWithoutTransaction - fixMissingMetaClass", mo, before);

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateArrayObjects called for: " + mo); // NOI18N
        }

        final ObjectAttribute[] oas = mo.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            if (oas[i].referencesObject()) {
                final MetaObject metaObject = (MetaObject)oas[i].getValue();
                final int status = metaObject.getStatus();

                switch (status) {
                    case MetaObject.NEW: {
                        // arraykey need not to be process
                        if (oas[i].isVirtualOneToManyAttribute()) {
                            final int masterClassId = oas[i].getClassID();
                            String backlinkMasterProperty = null;
                            for (final ObjectAttribute oaBacklink : metaObject.getAttribs()) {
                                if (oaBacklink.getMai().getForeignKeyClassId() == masterClassId) {
                                    backlinkMasterProperty = oaBacklink.getMai().getFieldName();
                                    break;
                                }
                            }
                            if (backlinkMasterProperty != null) {
                                metaObject.getAttributeByFieldName(backlinkMasterProperty)
                                        .setValue(oas[i].getParentObject());
                            } else {
                                LOG.error(
                                    "Der Backlink konnte nicht gesetzt werden, da in der Masterklasse das Attribut "
                                            + backlinkMasterProperty
                                            + " nicht gefunden werden konnte.");
                            }
                        }
                        insertMetaObjectWithoutTransaction(user, metaObject);
                        break;
                    }

                    case MetaObject.TO_DELETE: {
                        deleteMetaObjectWithoutTransaction(user, metaObject);
                        break;
                    }

                    case MetaObject.NO_STATUS: {
                        break;
                    }
                    case MetaObject.MODIFIED: {
                        updateMetaObjectWithoutTransaction(user, metaObject);
                        break;
                    }

                    default: {
                        // should never occur
                        // TODO: consider LOG fatal
                        LOG.error(
                            "error for array element "
                                    + metaObject
                                    + " has invalid status ::"
                                    + status); // NOI18N
                        // TODO: throw illegalstateexception?
                    }
                }
            } else {
                LOG.warn("ArrayElement is no MetaObject and won't be inserted"); // NOI18N
            }
        }

        // key references for array are set by client
        // TODO: why does the client set them?
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user   DOCUMENT ME!
     * @param   dummy  DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     */
    private void insertMetaObjectArrayWithoutTransaction(final User user, final MetaObject dummy)
            throws PersistenceException, SQLException {
        insertMetaObjectArrayWithoutTransaction(user, dummy, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user   DOCUMENT ME!
     * @param   dummy  DOCUMENT ME!
     * @param   fk     DOCUMENT ME!
     *
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     */
    private void insertMetaObjectArrayWithoutTransaction(final User user, final MetaObject dummy, final int fk)
            throws PersistenceException, SQLException {
        final ObjectAttribute[] oas = dummy.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("insertMO arrayelement " + i); // NOI18N
            }

            final MetaObject arrayElement = (MetaObject)oas[i].getValue();
//                oas[i].setParentObject(dummy.getReferencingObjectAttribute().getParentObject());

            final int status = arrayElement.getStatus();

            // entscheide bei MO ob update/delete/insert

            switch (status) {
                case MetaObject.NEW: {
//                    if (oas[i].isVirtualOneToManyAttribute()) {
//                        final int masterClassId = oas[i].getClassID();
//                        String backlinkMasterProperty = null;
//                        for (final ObjectAttribute oaBacklink : arrayElement.getAttribs()) {
//                            if (oaBacklink.getMai().getForeignKeyClassId() == masterClassId) {
//                                backlinkMasterProperty = oaBacklink.getName();
//                                break;
//                            }
//                        }
//                        if (backlinkMasterProperty != null) {
//                            arrayElement.getAttributeByFieldName(backlinkMasterProperty)
//                                    .setValue(oas[i].getParentObject().getReferencingObjectAttribute()
//                                        .getParentObject());
//                        } else {
//                            LOG.error(
//                                "Der Backlink konnte nicht gesetzt werden, da in der Masterklasse das Attribut "
//                                        + backlinkMasterProperty
//                                        + " nicht gefunden werden konnte.");
//                        }
//                    }

                    // neuer schluessel wird gesetzt
                    insertMetaObjectWithoutTransaction(user, arrayElement, fk);

                    break; // war auskommentiert HELL
                }

                case MetaObject.TO_DELETE: {
                    deleteMetaObjectWithoutTransaction(user, arrayElement);

                    break;
                }

                case MetaObject.NO_STATUS: {
                    break;
                }
                case MetaObject.MODIFIED: {
                    updateMetaObjectWithoutTransaction(user, arrayElement);
                    break;
                }
                default: {
                    break;
                }
            }

            // this causes no problem as it is never on the top level (-1 != object_id:-)
            // die notwendigen schl\u00FCsselbeziehungen werden im client gesetzt???
            // TODO: is that the case? if so, consider refactoring
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     */
    private int insertMetaObjectWithoutTransaction(final User user, final MetaObject mo) throws PersistenceException,
        SQLException {
        return insertMetaObjectWithoutTransaction(user, mo, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     * @param   fk    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     * @throws  SQLException          DOCUMENT ME!
     */
    private int insertMetaObjectWithoutTransaction(final User user, final MetaObject mo, final int fk)
            throws PersistenceException, SQLException {
        long before = 0;
        before = startPerformanceMeasurement();
        fixMissingMetaClass(mo);
        stopPerformanceMeasurement("insertMetaObjectWithoutTransaction - fixMissingMetaClass", mo, before);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "insertMetaObject entered "            // NOI18N
                        + mo
                        + "status :"                   // NOI18N
                        + mo.getStatus()
                        + " of class:"                 // NOI18N
                        + mo.getClassID()
                        + " isDummy(ArrayContainer) :" // NOI18N
                        + mo.isDummy());               // NOI18N
        }
        mo.forceStatus(MetaObject.NO_STATUS);
        if (dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(user)
                    && (mo.isDummy() || mo.hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann
            // existiert gar keine sinnvolle
            // bean won't insert history
            // here since we assume that the
            // object to be inserted is new

            before = startPerformanceMeasurement();
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            for (final CidsTrigger ct : rightTriggers) {
                ct.beforeInsert(mo.getBean(), user);
            }
            stopPerformanceMeasurement("insertMetaObjectWithoutTransaction - beforeInsertTriggers", mo, before);

            // class of the new object
            final MetaClass metaClass = dbServer.getClass(mo.getClassID());

            final ObjectAttribute[] mAttr = mo.getAttribs();

            // retrieve new ID to be used as primarykey for the new object
            final int rootPk;
            if (mo.getID() < 0) {
                try {
                    rootPk = persistenceHelper.getNextID(metaClass.getTableName(), metaClass.getPrimaryKey());
                } catch (final SQLException ex) {
                    final String message = "cannot fetch next id for metaclass: " + metaClass; // NOI18N
                    LOG.error(message, ex);
                    throw new PersistenceException(message, ex);
                }

                // set the new primary key as value of the primary key attribute
                for (final ObjectAttribute maybePK : mAttr) {
                    if (maybePK.isPrimaryKey()) {
                        maybePK.setValue(rootPk);
                    }
                }

                // set object's id
                mo.setID(rootPk);
            } else {
                rootPk = mo.getID();
            }

            try {
                mo.getBean().setProperty(mo.getPrimaryKey().getName().toLowerCase(), rootPk);
            } catch (final Exception ex) {
                LOG.warn("id Property could not be set", ex);
            }

            // initialis all array attributes with the value of the primary key
            mo.setArrayKey2PrimaryKey();

            final ArrayList<MetaObject> virtual1toMChildren = new ArrayList<MetaObject>();

            final ArrayList values = new ArrayList(mAttr.length);
            final List<String> fieldNames = new ArrayList<String>();
            final TransactionHelper transactionHelper = local.get();
            // iterate all attributes to create insert statement
            for (int i = 0; i < mAttr.length; i++) {
                // attribute value
                final java.lang.Object value = mAttr[i].getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        "mAttr["                    // NOI18N
                                + i
                                + "].getName() of " // NOI18N
                                + mo.getClassKey()
                                + ": "              // NOI18N
                                + mAttr[i].getName());
                }
                if (mAttr[i].isVirtualOneToManyAttribute()) {
                    if (value == null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(mAttr[i] + "is virtual one to many attribute and has no values -> ignored");
                        }
                    } else {
                        final MetaObject moAttr = (MetaObject)value;
//                        insertMetaObjectArray(user, moAttr, rootPk);
                        virtual1toMChildren.add(moAttr);
                    }
                    continue;
                }

                final MemberAttributeInfo mai = mAttr[i].getMai();
                // if object does not have mai it cannot be inserted
                if (mai == null) {
                    final String message = ("MAI not found: " + mAttr[i].getName()); // NOI18N
                    throw new IllegalStateException(message);
                }

                if (mai.isExtensionAttribute()) {
                    // extension attributes should be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(mAttr[i] + "is an extension attribute -> ignored");
                    }

                    continue;
                }

                fieldNames.add(mai.getFieldName());
                if (!mAttr[i].referencesObject()) // does not reference object, so it does not have key
                {
                    if (value == null) {
                        // use defaultvalue
                        values.add(persistenceHelper.getDefaultValue(mai));
                    } else {
                        before = startPerformanceMeasurement();
                        if (PersistenceHelper.GEOMETRY.isAssignableFrom(value.getClass())) {
                            values.add(SQLTools.getGeometryFactory(
                                    Lookup.getDefault().lookup(DialectProvider.class).getDialect()).getDbString(
                                    (Geometry)value));
                        } else {
                            values.add(value);
                        }
                        stopPerformanceMeasurement(
                            "insertMetaObjectWithoutTransaction - getDbObject",
                            mo,
                            before);
                    }
                } else if (!mAttr[i].isPrimaryKey()) { // references metaobject

                    final MetaObject moAttr = (MetaObject)value;

                    if ((fk > -1)
                                && (mAttr[i].getMai().getForeignKeyClassId()
                                    == mo.getReferencingObjectAttribute().getParentObject()
                                    .getReferencingObjectAttribute().getClassID())) {
                        values.add(fk);
                    } else {
                        try {
                            // recursion
                            if (value != null) {
                                final int status = moAttr.getStatus();
                                Integer objectID = moAttr.getID();
                                switch (status) {
                                    case MetaObject.NEW: {
                                        if (moAttr.isDummy()) {
                                            objectID = mo.getID();
                                            // jt ids still to be made
                                            insertMetaObjectArrayWithoutTransaction(user, moAttr);
                                        } else {
                                            objectID = insertMetaObjectWithoutTransaction(user, moAttr);
                                        }
                                        break;
                                    }
                                    case MetaObject.TO_DELETE: {
                                        objectID = null;
                                        deleteMetaObjectWithoutTransaction(user, moAttr);
                                        break;
                                    }
                                    case MetaObject.MODIFIED: {
                                        updateMetaObjectWithoutTransaction(user, moAttr);
                                        break;
                                    }
                                    default: {
                                        // NOP
                                    }
                                }
                                // foreign key will be set
                                if (status == MetaObject.TEMPLATE) {
                                    values.add(NULL);
                                } else {
                                    values.add(objectID);
                                }
                            } else if (mAttr[i].isArray()) {
                                values.add(rootPk);
                            } else {
                                values.add(NULL);
                            }
                        } catch (final Exception e) {
                            final String error = "interrupted insertMO recursion moAttr::" + moAttr + " MAI" + mai; // NOI18N
                            LOG.error(error, e);
                            throw new PersistenceException(error, e);
                        }
                    }
                }
            }

            if (!mo.isDummy()) {
                // set params and execute stmt
                PreparedStatement stmt = null;
                try {
                    before = startPerformanceMeasurement();

                    PreparableStatement paramStmt = null;
                    if (metaClass.getAttribs() != null) {
                        for (final ClassAttribute ca : metaClass.getAttribs()) {
                            if (ca.getName().equals("customInsertStmt")) {
                                paramStmt = PreparableStatement.fromString((String)ca.getValue());
                                break;
                            }
                        }
                    }
                    if (paramStmt == null) {
                        paramStmt = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                            .getDialect())
                                    .getPersistenceManagerInsertStmt(metaClass.getTableName(),
                                            fieldNames.toArray(new String[fieldNames.size()]));
                    }

                    paramStmt.setObjects(values.toArray());
                    stmt = paramStmt.parameterise(transactionHelper.getConnection());
                    if (LOG.isDebugEnabled()) {
                        final StringBuilder logMessage = new StringBuilder("Parameterized SQL: ");
                        logMessage.append(paramStmt);
                        logMessage.append('\n');
                        final int i = 1;
                        for (final java.lang.Object value : values) {
                            if (i > 1) {
                                logMessage.append("; ");
                            }
                            logMessage.append(i);
                            logMessage.append(". parameter: ");
                            logMessage.append(value.toString());
                        }
                        LOG.debug(logMessage.toString(), new Exception());
                    }
                    stmt.executeUpdate();
                    stopPerformanceMeasurement("insertMetaObjectWithoutTransaction - executeUpdate", mo, before);

                    for (final MetaObject vChild : virtual1toMChildren) {
                        insertMetaObjectArrayWithoutTransaction(user, vChild, rootPk);
                    }

                    before = startPerformanceMeasurement();
                    for (final CidsTrigger ct : rightTriggers) {
                        ct.afterInsert(mo.getBean(), user);
                    }
                    stopPerformanceMeasurement("insertMetaObjectWithoutTransaction - afterInsertTriggers", mo, before);
                } finally {
                    before = startPerformanceMeasurement();
                    DBConnection.closeStatements(stmt);
                    stopPerformanceMeasurement("insertMetaObjectWithoutTransaction - closeStatements", mo, before);
                }
            }

            return rootPk;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "'"                                        // NOI18N
                            + user
                            + "' is not allowed to insert MO " // NOI18N
                            + mo.getID()
                            + "."                              // NOI18N
                            + mo.getClassKey(),                // NOI18N
                    StackUtils.getDebuggingThrowable());
            }
            throw new SecurityException("not allowed to insert meta object"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<CidsTrigger> getRightTriggers(final MetaObject mo) {
        assert (mo != null);
        final ArrayList<CidsTrigger> list = new ArrayList<CidsTrigger>();
        final String domain = mo.getMetaClass().getDomain().toLowerCase();
        final String table = mo.getMetaClass().getTableName().toLowerCase();

        final Collection<CidsTrigger> listForAll = triggers.get(CidsTriggerKey.FORALL);
        final Collection<CidsTrigger> listAllTablesInOneDomain = triggers.get(new CidsTriggerKey(
                    domain,
                    CidsTriggerKey.ALL));
        final Collection<CidsTrigger> listOneTableInAllDomains = triggers.get(new CidsTriggerKey(
                    CidsTriggerKey.ALL,
                    table));
        final Collection<CidsTrigger> listExplicitTableInDomain = triggers.get(new CidsTriggerKey(domain, table));

        if (listForAll != null) {
            list.addAll(triggers.get(CidsTriggerKey.FORALL));
        }
        if (listAllTablesInOneDomain != null) {
            list.addAll(triggers.get(new CidsTriggerKey(domain, CidsTriggerKey.ALL)));
        }
        if (listOneTableInAllDomains != null) {
            list.addAll(triggers.get(new CidsTriggerKey(CidsTriggerKey.ALL, table)));
        }
        if (listExplicitTableInDomain != null) {
            list.addAll(triggers.get(new CidsTriggerKey(domain, table)));
        }

        return list;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mo  DOCUMENT ME!
     */
    private void fixMissingMetaClass(final MetaObject mo) {
        if (mo.getMetaClass() == null) {
            mo.setMetaClass(new MetaClass(
                    dbServer.getClassCache().getClass(mo.getClassID()),
                    mo.getDomain()));
        }
    }
}
