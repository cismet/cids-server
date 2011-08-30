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

import org.postgis.PGgeometry;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public static final String DEL_ATTR_STRING = "DELETE FROM cs_attr_string "       // NOI18N
                + "WHERE class_id = ? AND object_id = ?";                            // NOI18N
    public static final String DEL_ATTR_MAPPING = "DELETE FROM cs_all_attr_mapping " // NOI18N
                + "WHERE class_id = ? AND object_id = ?";                            // NOI18N
    public static final String INS_ATTR_STRING = "INSERT INTO cs_attr_string "       // NOI18N
                + "(class_id, object_id, attr_id, string_val) VALUES (?, ?, ?, ?)";  // NOI18N
    public static final String INS_ATTR_MAPPING = "INSERT INTO cs_all_attr_mapping " // NOI18N
                + "(class_id, object_id, attr_class_id, attr_object_id) VALUES "     // NOI18N
                + "(?, ?, ?, ?)";                                                    // NOI18N
    public static final String UP_ATTR_STRING = "UPDATE cs_attr_string "             // NOI18N
                + "SET string_val = ? "                                              // NOI18N
                + "WHERE class_id = ? AND object_id = ? AND attr_id = ?";            // NOI18N
    public static final String UP_ATTR_MAPPING = "UPDATE cs_all_attr_mapping "       // NOI18N
                + "SET attr_object_id = ? "                                          // NOI18N
                + "WHERE class_id = ? AND object_id = ? AND attr_class_id = ?";      // NOI18N
    public static final String NULL = "NULL";                                        // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient DBServer dbServer;
    private final transient TransactionHelper transactionHelper;
    private final transient PersistenceHelper persistenceHelper;

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
                    && (mo.isDummy() || mo.getBean().hasObjectWritePermission(user))) {    // wenn mo ein dummy ist dann
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

                // initialise object history if necessary
                initHistory(mo, user);

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

                /*
                 * since the meta-jdbc driver is obsolete the index must be refreshed by the server explicitly
                 */
                deleteIndex(mo);

                // if the metaobject is deleted it is obviously not persistent anymore
                mo.setPersistent(false);

                createHistory(mo, user);

                transactionHelper.commit();

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
     * DOCUMENT ME!
     *
     * @param  mo    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     */
    private void createHistory(final MetaObject mo, final User user) {
        try {
            // immediately returns
            dbServer.getHistoryServer().enqueueEntry(mo, user, new Date());
        } catch (final Exception e) {
            LOG.error("cannot enqueue mo for history creation", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mo    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     */
    private void initHistory(final MetaObject mo, final User user) {
        try {
            dbServer.getHistoryServer().initHistory(mo, user, new Date());
        } catch (final Exception e) {
            LOG.error("cannot initialise history", e); // NOI18N
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

            // initialise object history if necessary
            initHistory(mo, user);

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

                if (value == null) {
                    // delete MetaObject???
                    values.add(NULL);
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
                                values.add(mo.getID()); // set value to primary key
                                insertMetaObjectArray(user, subObject);
                            } else {
                                values.add(key);
                            }
                            break;
                        }
                        case MetaObject.TO_DELETE: {
                            deleteMetaObject(user, subObject);
                            values.add(NULL);
                            break;
                        }
                        case MetaObject.NO_STATUS:
                        // fall through because we define no status as modified status
                        case MetaObject.MODIFIED: {
                            updateMetaObject(user, subObject);
                            values.add(subObject.getID());
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
                        values.add(PostGisGeometryFactory.getPostGisCompliantDbString((Geometry)value));
                    } else {
                        values.add(value);
                    }
                }
                // add update fieldname = ? and add value to valuelist
                paramStmt.append(sep).append(mai.getFieldName()).append(" = ?"); // NOI18N

                ++updateCounter;

                // comma between 'fieldname = ?, ' set in first iteration
                sep = ","; // NOI18N
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
                    updateIndex(mo);

                    createHistory(mo, user);

                    transactionHelper.commit();
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

                /*
                 * since the meta-jdbc driver is obsolete the index must be refreshed by the server explicitly
                 */
                insertIndex(mo);

                createHistory(mo, user);

                transactionHelper.commit();
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
     * mscholl: Deletes the index from cs_attr_string and cs_all_attr_mapping for a given metaobject. If the metaobject
     * does not contain a metaclass it is skipped.
     *
     * @param   mo  the metaobject which will be deleted
     *
     * @throws  SQLException              if an error occurs during index deletion
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void deleteIndex(final MetaObject mo) throws SQLException {
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
            psAttrString = transactionHelper.getConnection().prepareStatement(DEL_ATTR_STRING);
            psAttrMap = transactionHelper.getConnection().prepareStatement(DEL_ATTR_MAPPING);

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
                        // lazily prepare the statement
                        if (psAttrMap == null) {
                            psAttrMap = transactionHelper.getConnection().prepareStatement(UP_ATTR_MAPPING);
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
                            final StringBuilder logMessage = new StringBuilder("Parameterized SQL added to batch: ");
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
                    } else {
                        // lazily prepare the statement
                        if (psAttrString == null) {
                            psAttrString = transactionHelper.getConnection().prepareStatement(UP_ATTR_STRING);
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
     * mscholl: Inserts the index in cs_attr_string and cs_all_attr_mapping for the given metaobject. If the metaobject
     * does not contain a metaclass it is skipped.
     *
     * @param   mo  the metaobject which will be newly created
     *
     * @throws  SQLException              if an error occurs during index insertion
     * @throws  IllegalArgumentException  NullPointerException DOCUMENT ME!
     */
    private void insertIndex(final MetaObject mo) throws SQLException {
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
                        // lazily prepare the statement
                        if (psAttrMap == null) {
                            psAttrMap = transactionHelper.getConnection().prepareStatement(INS_ATTR_MAPPING);
                        }
                        psAttrMap.setInt(1, mo.getClassID());
                        psAttrMap.setInt(2, mo.getID());
                        psAttrMap.setInt(3, mai.getForeignKeyClassId());
                        // if field represents a foreign key the attribute value
                        // is assumed to be a MetaObject
                        final MetaObject value = (MetaObject)attr.getValue();
                        psAttrMap.setInt(4, (value == null) ? -1 : value.getID());
                        psAttrMap.addBatch();
                    } else {
                        // lazily prepare the statement
                        if (psAttrString == null) {
                            psAttrString = transactionHelper.getConnection().prepareStatement(INS_ATTR_STRING);
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
}
