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
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.sql.DBConnection;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import org.postgis.PGgeometry;

import java.beans.PropertyVetoException;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;
import de.cismet.cids.trigger.DBAwareCidsTrigger;

import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;

import de.cismet.tools.CurrentStackTrace;

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
                ((DBAwareCidsTrigger)t).setDbServer(dbServer);
            }
            if (triggers.containsKey(t.getTriggerKey())) {
                final Collection<CidsTrigger> c = triggers.get(t.getTriggerKey());
                assert (c != null);
                c.add(t);
            } else {
                final Collection<CidsTrigger> c = new ArrayList<CidsTrigger>();
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
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  DOCUMENT ME!
     */
    public int insertMetaObject(final User user, final MetaObject mo) throws PersistenceException {
        try {
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            final TransactionHelper transactionHelper = local.get();
            transactionHelper.beginWork();
            final int rtn = insertMetaObjectWithoutTransaction(user, mo);
            transactionHelper.commit();

            for (final CidsTrigger ct : rightTriggers) {
                ct.afterCommittedInsert(mo.getBean(), user);
            }

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
                final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
                final TransactionHelper transactionHelper = local.get();
                transactionHelper.beginWork();
                updateMetaObjectWithoutTransaction(user, mo);
                transactionHelper.commit();

                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterCommittedUpdate(mo.getBean(), user);
                }
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
        try {
            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            final TransactionHelper transactionHelper = local.get();
            transactionHelper.beginWork();
            final int rtn = deleteMetaObjectWithoutTransaction(user, mo);
            transactionHelper.commit();

            for (final CidsTrigger ct : rightTriggers) {
                ct.afterCommittedDelete(mo.getBean(), user);
            }

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
        fixMissingMetaClass(mo);

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

        if (
            dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(
                        user.getUserGroup())
                    && (mo.isDummy() || mo.hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann

            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            for (final CidsTrigger ct : rightTriggers) {
                ct.beforeDelete(mo.getBean(), user);
            }

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

                if (deeper) {
                    updateMetaObjectWithoutTransaction(user, mo);
                }

                // intitialize UserGroup
                UserGroup ug = null;

                // retrieve userGroup is user is not null
                if (user != null) {
                    ug = user.getUserGroup();
                }
                // retrieve the metaObject's class
                final Sirius.server.localserver._class.Class c = dbServer.getClass(ug, mo.getClassID());
                // get Tablename from class
                final String tableName = c.getTableName();
                // get primary Key from class
                final String pk = c.getPrimaryKey();
                // add tablename and whereclause to the delete statement
                final String paramStmt = "DELETE FROM " + tableName + " WHERE " + pk + " = ?"; // NOI18N+

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

                // if the metaobject is deleted it is obviously not persistent anymore
                mo.setPersistent(false);

                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterDelete(mo.getBean(), user);
                }
                return result;
            } finally {
                DBConnection.closeStatements(stmt);
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
                    new CurrentStackTrace());
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
        fixMissingMetaClass(arrayMo);

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
        fixMissingMetaClass(arrayMo);

        if (!arrayMo.isDummy()) {
            LOG.error("deleteArrayEntries on a metaobject that is not a dummy");
        }

        // initialize number of affected objects
        PreparedStatement stmt = null;

        try {
            final String tableName = arrayMo.getMetaClass().getTableName();
            final String arrayKeyFieldName = arrayMo.getReferencingObjectAttribute().getMai().getArrayKeyFieldName();
            final String paramStmt = "DELETE FROM " + tableName + " WHERE " + arrayKeyFieldName + " = ?"; // NOI18N+

            final TransactionHelper transactionHelper = local.get();
            stmt = transactionHelper.getConnection().prepareStatement(paramStmt);
            stmt.setObject(1, arrayMo.getId());
            // execute deletion and retrieve number of affected objects
            final int result = stmt.executeUpdate();

            if (LOG.isDebugEnabled()) {
                LOG.debug("array elements deleted :: " + result); // NOI18N
            }

            return result;
        } finally {
            DBConnection.closeStatements(stmt);
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
        fixMissingMetaClass(mo);

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
        if (
            dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(
                        user.getUserGroup())
                    && (mo.isDummy() || mo.hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann
            // existiert gar keine sinnvolle
            // bean

            // if Array
            if (mo.isDummy()) {
                updateArrayObjectsWithoutTransaction(user, mo);
                return;
            }

            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            for (final CidsTrigger ct : rightTriggers) {
                ct.beforeUpdate(mo.getBean(), user);
            }
            // variables for sql statement
            final StringBuffer paramStmt = new StringBuffer("UPDATE "); // NOI18N
            String sep = "";                                            // NOI18N
            // retrieve class object
            final MetaClass metaClass = dbServer.getClass(mo.getClassID());
            // add table name and set clause
            paramStmt.append(metaClass.getTableName()).append(" SET "); // NOI18N
            // retrieve object attributes
            final ObjectAttribute[] mAttr = mo.getAttribs();
            MemberAttributeInfo mai;
            // counts fields to update, if 0 no update will be done at all
            int updateCounter = 0;
            final ArrayList values = new ArrayList(mAttr.length);
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
                        case MetaObject.NO_STATUS:
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
                    // TODO: try to convert JTS GEOMETRY to PGgeometry directly
                    if (PersistenceHelper.GEOMETRY.isAssignableFrom(value.getClass())) {
                        valueToAdd = PostGisGeometryFactory.getPostGisCompliantDbString((Geometry)value);
                    } else {
                        valueToAdd = value;
                    }
                }
                if (!mAttr[i].isVirtualOneToManyAttribute()) {
                    values.add(valueToAdd);

                    // add update fieldname = ? and add value to valuelist
                    paramStmt.append(sep).append(mai.getFieldName()).append(" = ?"); // NOI18N
                    ++updateCounter;

                    // comma between 'fieldname = ?, ' set in first iteration
                    sep = ","; // NOI18N
                }
            }

            if (updateCounter > 0) {
                PreparedStatement stmt = null;
                try {
                    // statment done, just append the where clause using the object's primary key
                    paramStmt.append(" WHERE ").append(metaClass.getPrimaryKey()).append(" = ?"); // NOI18N
                    values.add(Integer.valueOf(mo.getID()));

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
                        LOG.debug(logMessage.toString());
                    }

                    final TransactionHelper transactionHelper = local.get();
                    stmt = transactionHelper.getConnection().prepareStatement(paramStmt.toString());
                    parameteriseStatement(stmt, values);
                    stmt.executeUpdate();

                    /*
                     * since the meta-jdbc driver is obsolete the index must be refreshed by the server explicitly
                     */

                    for (final CidsTrigger ct : rightTriggers) {
                        ct.afterUpdate(mo.getBean(), user);
                    }
                } finally {
                    DBConnection.closeStatements(stmt);
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
                    new CurrentStackTrace());
            }
            throw new SecurityException("not allowed to insert meta object"); // NOI18N
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
            } else if (type == Types.OTHER) {
                // assume PGgeometry as String
                stmt.setObject(i + 1, new PGgeometry(String.valueOf(values.get(i))));
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
        fixMissingMetaClass(mo);

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
                                    backlinkMasterProperty = oaBacklink.getName();
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

                    case MetaObject.NO_STATUS:
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
        fixMissingMetaClass(mo);

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

        if (
            dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(
                        user.getUserGroup())
                    && (mo.isDummy() || mo.hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann
            // existiert gar keine sinnvolle
            // bean won't insert history
            // here since we assume that the
            // object to be inserted is new

            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            for (final CidsTrigger ct : rightTriggers) {
                ct.beforeInsert(mo.getBean(), user);
            }

            final StringBuffer paramSql = new StringBuffer("INSERT INTO "); // NOI18N
            // class of the new object
            final MetaClass metaClass = dbServer.getClass(mo.getClassID());
            paramSql.append(metaClass.getTableName()).append(" ("); // NOI18N

            // retrieve new ID to be used as primarykey for the new object
            final int rootPk;
            try {
                rootPk = persistenceHelper.getNextID(metaClass.getTableName(), metaClass.getPrimaryKey());
            } catch (final SQLException ex) {
                final String message = "cannot fetch next id for metaclass: " + metaClass; // NOI18N
                LOG.error(message, ex);
                throw new PersistenceException(message, ex);
            }

            final ObjectAttribute[] mAttr = mo.getAttribs();
            // set the new primary key as value of the primary key attribute
            for (final ObjectAttribute maybePK : mAttr) {
                if (maybePK.isPrimaryKey()) {
                    maybePK.setValue(rootPk);
                }
            }
            // set object's id
            mo.setID(rootPk);
            // initialis all array attributes with the value of the primary key
            mo.setArrayKey2PrimaryKey();

            final ArrayList<MetaObject> virtual1toMChildren = new ArrayList<MetaObject>();

            final ArrayList values = new ArrayList(mAttr.length);
            String sep = ""; // NOI18N
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
                // add fieldname of this attribute to statement
                paramSql.append(sep).append(mai.getFieldName());
                if (!mAttr[i].referencesObject()) // does not reference object, so it does not have key
                {
                    if (value == null) {
                        // use defaultvalue
                        values.add(persistenceHelper.getDefaultValue(mai, value));
                    } else {
                        // TODO: try to convert JTS GEOMETRY to PGgeometry directly
                        if (PersistenceHelper.GEOMETRY.isAssignableFrom(value.getClass())) {
                            values.add(PostGisGeometryFactory.getPostGisCompliantDbString((Geometry)value));
                        } else {
                            values.add(value);
                        }
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
                                    case MetaObject.MODIFIED:
                                    // NOP
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

                // after the first iteration set the seperator to comma
                sep = ", "; // NOI18N
            }
            // finalise param stmt
            sep = "";                             // NOI18N
            paramSql.append(") VALUES (");        // NOI18N
            for (int i = 0; i < values.size(); ++i) {
                paramSql.append(sep).append('?'); // NOI18N
                sep = ", ";                       // NOI18N
            }
            paramSql.append(')');                 // NOI18N

            // set params and execute stmt
            PreparedStatement stmt = null;
            try {
                final TransactionHelper transactionHelper = local.get();
                stmt = transactionHelper.getConnection().prepareStatement(paramSql.toString());
                if (LOG.isDebugEnabled()) {
                    final StringBuilder logMessage = new StringBuilder("Parameterized SQL: ");
                    logMessage.append(paramSql);
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
                    LOG.debug(logMessage.toString());
                }
                stmt = parameteriseStatement(stmt, values);
                stmt.executeUpdate();

                for (final MetaObject vChild : virtual1toMChildren) {
                    insertMetaObjectArrayWithoutTransaction(user, vChild, rootPk);
                }

                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterInsert(mo.getBean(), user);
                }
            } finally {
                DBConnection.closeStatements(stmt);
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
                    new CurrentStackTrace());
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
            mo.setMetaClass(new MetaClass(dbServer.getClassCache().getClass(mo.getClassID()), mo.getDomain()));
        }
    }
}
