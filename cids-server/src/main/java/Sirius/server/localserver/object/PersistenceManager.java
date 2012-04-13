/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.Shutdown;
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.sql.DBConnection;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import org.postgis.PGgeometry;

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
    private final transient TransactionHelper transactionHelper;
    private final transient PersistenceHelper persistenceHelper;
    private final Collection<? extends CidsTrigger> allTriggers;
    private final Collection<CidsTrigger> generalTriggers = new ArrayList<CidsTrigger>();
    private final Collection<CidsTrigger> crossDomainTrigger = new ArrayList<CidsTrigger>();
    private final HashMap<CidsTriggerKey, Collection<CidsTrigger>> triggers =
        new HashMap<CidsTriggerKey, Collection<CidsTrigger>>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistenceManager object.
     *
     * @param  dbServer  DOCUMENT ME!
     */
    public PersistenceManager(final DBServer dbServer) {
        this.dbServer = dbServer;

        transactionHelper = new TransactionHelper(dbServer.getActiveDBConnection(), dbServer.getSystemProperties());
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
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * loescht mo und alle Objekte die mo als Attribute hat.
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     */
    public int deleteMetaObject(final User user, final MetaObject mo) throws PersistenceException {
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
                    && (mo.isDummy() || mo.getBean().hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann

            final Collection<CidsTrigger> rightTriggers = getRightTriggers(mo);
            for (final CidsTrigger ct : rightTriggers) {
                ct.beforeDelete(mo.getBean(), user);
            }

            // existiert gar keine
            // sinnvolle bean
            // start transaction
            try {
                transactionHelper.beginWork();
            } catch (final SQLException ex) {
                final String message = "cannot start transaction"; // NOI18N
                LOG.error(message, ex);
                throw new PersistenceException(message, ex);
            }

            PreparedStatement stmt = null;
            try {
                // Mo was created artificially (array holder) so there is no object to delete
                // directly proceed to subObjects
                if (mo == null) {
                    LOG.error("cannot delete MetaObject == null"); // NOI18N
                    return 0;
                }

                if (mo.isDummy()) {
                    return deleteSubObjects(user, mo);
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
                    updateMetaObject(user, mo);
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

                stmt = transactionHelper.getConnection().prepareStatement(paramStmt);
                stmt.setObject(1, mo.getPrimaryKey().getValue());
                // execute deletion and retrieve number of affected objects
                int result = stmt.executeUpdate();

                // now delete all subObjects
                result += deleteSubObjects(user, mo);

                // if the metaobject is deleted it is obviously not persistent anymore
                mo.setPersistent(false);

                transactionHelper.commit();

                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterDelete(mo.getBean(), user);
                }
                return result;
            } catch (final Exception e) {
                final String message = "cannot delete metaobject"; // NOI18N
                LOG.error(message, e);

                try {
                    transactionHelper.rollback();
                } catch (final SQLException ex) {
                    final String error = "cannot rollback transaction, this can cause inconsistent database state"; // NOI18N
                    LOG.error(error, ex);
                    throw new PersistenceException(error, e);
                }

                throw new PersistenceException(message, e);
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
     * Deletes all subobjects of the given MO.
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     */
    private int deleteSubObjects(final User user, final MetaObject mo) throws PersistenceException {
        fixMissingMetaClass(mo);

        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteMetaObject dummy entered discard object insert elements" + mo); // NOI18N
        }

        // initialize number of affected objects
        int count = 0;

        // retrieve number of array elements
        final ObjectAttribute[] oas = mo.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            // delete all referenced Object / array elements
            if (oas[i].referencesObject()) {
                final MetaObject metaObject = (MetaObject)oas[i].getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("try to delete :" + metaObject); // NOI18N
                }

                if ((metaObject != null) && (metaObject.getStatus() == MetaObject.TEMPLATE)) {
                    count += deleteMetaObject(user, metaObject);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("array elements deleted :: " + count); // NOI18N
        }

        return count;
    }

    /**
     * Given metaobject and subobjects will be updated if changed.
     *
     * @param   user  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     *
     * @throws  PersistenceException   Throwable DOCUMENT ME!
     * @throws  IllegalStateException  Exception DOCUMENT ME!
     * @throws  SecurityException      DOCUMENT ME!
     */
    public void updateMetaObject(final User user, final MetaObject mo) throws PersistenceException {
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
                    && (mo.isDummy() || mo.getBean().hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann
            // existiert gar keine sinnvolle
            // bean

            // if Array
            if (mo.isDummy()) {
                updateArrayObjects(user, mo);
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
                            final int key = insertMetaObject(user, subObject);
                            if (subObject.isDummy()) {
                                valueToAdd = mo.getID(); // set value to primary key
                                insertMetaObjectArray(user, subObject);
                            } else {
                                valueToAdd = key;
                            }
                            break;
                        }
                        case MetaObject.TO_DELETE: {
                            deleteMetaObject(user, subObject);
                            valueToAdd = NULL;
                            break;
                        }
                        case MetaObject.NO_STATUS:
                        // fall through because we define no status as modified status
                        case MetaObject.MODIFIED: {
                            updateMetaObject(user, subObject);
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
                    transactionHelper.beginWork();
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

                    stmt = transactionHelper.getConnection().prepareStatement(paramStmt.toString());
                    parameteriseStatement(stmt, values);
                    stmt.executeUpdate();

                    /*
                     * since the meta-jdbc driver is obsolete the index must be refreshed by the server explicitly
                     */

                    transactionHelper.commit();

                    for (final CidsTrigger ct : rightTriggers) {
                        ct.afterUpdate(mo.getBean(), user);
                    }
                } catch (final Exception e) {
                    final String message = "cannot update metaobject"; // NOI18N
                    LOG.error(message, e);

                    try {
                        transactionHelper.rollback();
                    } catch (final SQLException ex) {
                        final String error = "cannot rollback transaction, this can cause inconsistent database state"; // NOI18N
                        LOG.error(error, ex);
                        throw new PersistenceException(error, e);
                    }

                    throw new PersistenceException(message, e);
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
     */
    private void updateArrayObjects(final User user, final MetaObject mo) throws PersistenceException {
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
                        insertMetaObject(user, metaObject);
                        break;
                    }

                    case MetaObject.TO_DELETE: {
                        deleteMetaObject(user, metaObject);
                        break;
                    }

                    case MetaObject.NO_STATUS:
                    case MetaObject.MODIFIED: {
                        updateMetaObject(user, metaObject);
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
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     */
    private void insertMetaObjectArray(final User user, final MetaObject dummy) throws PersistenceException {
        final ObjectAttribute[] oas = dummy.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("insertMO arrayelement " + i); // NOI18N
            }

            final MetaObject arrayElement = (MetaObject)oas[i].getValue();

            final int status = arrayElement.getStatus();

            // entscheide bei MO ob update/delete/insert

            switch (status) {
                case MetaObject.NEW: {
                    // neuer schluessel wird gesetzt
                    insertMetaObject(user, arrayElement);

                    break; // war auskommentiert HELL
                }

                case MetaObject.TO_DELETE: {
                    deleteMetaObject(user, arrayElement);

                    break;
                }

                case MetaObject.NO_STATUS: {
                    break;
                }
                case MetaObject.MODIFIED: {
                    updateMetaObject(user, arrayElement);
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
     * @throws  PersistenceException  Throwable DOCUMENT ME!
     */
    public int insertMetaObject(final User user, final MetaObject mo) throws PersistenceException {
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
                    && (mo.isDummy() || mo.getBean().hasObjectWritePermission(user))) { // wenn mo ein dummy ist dann
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
                        insertMetaObjectArray(user, moAttr);
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
                                        insertMetaObjectArray(user, moAttr);
                                    } else {
                                        objectID = insertMetaObject(user, moAttr);
                                    }
                                    break;
                                }
                                case MetaObject.TO_DELETE: {
                                    objectID = null;
                                    deleteMetaObject(user, moAttr);
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
                transactionHelper.beginWork();

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

                transactionHelper.commit();
                for (final CidsTrigger ct : rightTriggers) {
                    ct.afterInsert(mo.getBean(), user);
                }
            } catch (final SQLException e) {
                final String message = "cannot insert metaobject"; // NOI18N
                LOG.error(message, e);

                try {
                    transactionHelper.rollback();
                } catch (final SQLException ex) {
                    final String error = "cannot rollback transaction, this can cause inconsistent database state"; // NOI18N
                    LOG.error(error, ex);
                    throw new PersistenceException(error, e);
                }

                throw new PersistenceException(message, e);
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
