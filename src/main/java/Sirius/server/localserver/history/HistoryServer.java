/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.history;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.cismet.cids.utils.MetaClassCacheService;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class HistoryServer extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(HistoryServer.class);

    public static final String JSON_DELETED = "{ DELETED }"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final DBServer server;
    private final transient HistoryExecutor executor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HistoryServer object.
     *
     * @param   server  the {@link DBServer} that is responsible for delivering classes, connections and objects
     *
     * @throws  IllegalArgumentException  if the given <code>DBServer</code> instance is null
     */
    public HistoryServer(final DBServer server) {
        if (server == null) {
            final String message = "given server must not be null"; // NOI18N
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        this.server = server;
        executor = new HistoryExecutor();

        addShutdown(new AbstractShutdownable() {

                @Override
                protected void internalShutdown() throws ServerExitError {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("shutting down HistoryServer"); // NOI18N
                    }

                    executor.shutdown();
                    try {
                        if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                            final String message =
                                "executor did not terminate, history may be incomplete: active tasks: "                  // NOI18N
                                        + executor.getActiveCount()
                                        + " || tasks in queue: "                                                         // NOI18N
                                        + executor.getTaskCount();
                            LOG.error(message);
                            throw new ServerExitError(message);
                        }
                    } catch (final InterruptedException ex) {
                        final String message = "could not await HistoryExecutor termination, history may be incomplete"; // NOI18N
                        LOG.error(message, ex);
                        throw new ServerExitError(message, ex);
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the history of the given object of the given class. The number of historic elements that will be
     * retrieved depends on the given element count and the amount of available historic elements. Resolution strategy:
     *
     * <ul>
     *   <li>elements < 1: order by timestamp</li>
     *   <li>elements > 0: order by timestamp limit <code>elements</code></li>
     * </ul>
     *
     * <p>This operation initialises the history of the object if its class is history enabled and it is not initialised
     * yet. Thus this operation never returns an empty list but always at least one object in case of a history enabled
     * status.</p>
     *
     * @param   classId   the id of the desired class
     * @param   objectId  the id of the object of the desired class
     * @param   usr       the {@link User} that requests the history
     * @param   elements  the number of historic elements to be retrieved or an int < 1 to retrieve all available
     *                    elements
     *
     * @return  the historic objects or <code>null</code> if the class is not history enabled
     *
     * @throws  HistoryException
     *                            <ul>
     *                              <li>if the given <code>User</code> is null</li>
     *                              <li>if the classcache did not provide a class (e.g. because the classid is
     *                                unknown/invalid)</li>
     *                              <li>if the user does not have read permissions for given class</li>
     *                              <li>if any other error occurs</li>
     *                            </ul>
     *
     * @see     ClassAttribute#HISTORY_ENABLED
     * @see     #initHistory(Sirius.server.middleware.types.MetaObject, Sirius.server.newuser.User, java.util.Date)
     */
    public HistoryObject[] getHistory(final int classId, final int objectId, final User usr, final int elements)
            throws HistoryException {
        if (usr == null) {
            final String message = "given user must not be null"; // NOI18N
            LOG.error(message);
            throw new HistoryException(message);
        }

        final Sirius.server.localserver._class.Class clazz;
        try {
            clazz = server.getClassCache().getClass(classId);
        } catch (final Exception ex) {
            final String message = "cannot get class for id: " + classId; // NOI18N
            LOG.error(message, ex);
            throw new HistoryException(message, ex);
        }

        if (clazz == null) {
            final String message = "cannot get class for id: " + classId; // NOI18N
            LOG.error(message);
            throw new HistoryException(message);
        }

        final PermissionHolder permHolder = clazz.getPermissions();
        if (permHolder == null) {
            final String message = "no permissionsholder set for class: " + clazz; // NOI18N
            LOG.error(message);
            throw new HistoryException(message);
        }

        if (!permHolder.hasReadPermission(usr)) {
            final String message = "given user's usergroup has no read permission for class: " + clazz // NOI18N
                        + " || user: " + usr;                                                          // NOI18N
            LOG.warn(message);
            throw new HistoryException(message);
        }

        if (clazz.getClassAttribute(ClassAttribute.HISTORY_ENABLED) != null) {
            final DBConnectionPool conPool = server.getConnectionPool();
            ResultSet set = null;
            try {
                final int expectedElements;
                if (elements < 1) {
                    set = conPool.submitInternalQuery(DBConnection.DESC_FETCH_HISTORY, clazz.getTableName(), objectId);
                    expectedElements = 15;
                } else {
                    set = conPool.submitInternalQuery(
                            DBConnection.DESC_FETCH_HISTORY_LIMIT,
                            clazz.getTableName(),
                            objectId,
                            elements);
                    expectedElements = elements;
                }

                final List<HistoryObject> objects = new ArrayList<HistoryObject>(expectedElements);
                while (set.next()) {
                    final String jsonData = set.getString(1);
                    final Date timestamp = new Date(set.getTimestamp(2).getTime());
                    objects.add(new HistoryObject(clazz, jsonData, timestamp));
                }

                // if objects is empty we have to init the history for the object
                if (objects.isEmpty()) {
                    initHistory(getMetaObject(classId, objectId, usr), usr, new Date());

                    // add the initial object
                    DBConnection.closeResultSets(set);
                    set = conPool.submitInternalQuery(DBConnection.DESC_FETCH_HISTORY_LIMIT, clazz.getTableName(), objectId, 1);
                    set.next();
                    final String jsonData = set.getString(1);
                    final Date timestamp = new Date(set.getTimestamp(2).getTime());
                    objects.add(new HistoryObject(clazz, jsonData, timestamp));
                }

                return objects.toArray(new HistoryObject[objects.size()]);
            } catch (final SQLException e) {
                final String message = "cannot fetch history elements for class: " + clazz; // NOI18N
                LOG.error(message, e);
                throw new HistoryException(message, e);
            } finally {
                DBConnection.closeResultSets(set);
            }
        }

        return null;
    }

    /**
     * Determines if there are any history entries for the given {@link MetaObject}. This operation does not care about
     * the {@link ClassAttribute#HISTORY_ENABLED} flag. It simply looks up whether there are entries in the database or
     * not.<br/>
     * <b>NOTE: This operation does not initialise the history!</b><br/>
     * <br/>
     * <b>IMPORTANT: This operation should not be exposed directly through server api/middleware since it is not
     * protected by permission check</b>
     *
     * @param   mo  the <code>MetaObject</code> to check
     *
     * @return  true if there is at least one historic entry, false otherwise
     *
     * @throws  HistoryException  if the given <code>MetaObject</code> is null or any error occurs during database query
     */
    public boolean hasHistory(final MetaObject mo) throws HistoryException {
        if (mo == null) {
            final String message = "given MetaObject must not be null"; // NOI18N
            LOG.error(message);
            throw new HistoryException(message);
        }

        final String classKey = mo.getMetaClass().getTableName();
        final int objectId = mo.getID();
        ResultSet set = null;
        try {
            set = server.getConnectionPool().submitInternalQuery(DBConnection.DESC_HAS_HISTORY, classKey, objectId);
            // only one result - the count
            set.next();

            final int count = set.getInt(1);

            return count > 0;
        } catch (final SQLException ex) {
            final String message = "cannot determine history status for metaobject: " + mo; // NOI18N
            LOG.error(message, ex);
            throw new HistoryException(message, ex);
        } finally {
            DBConnection.closeResultSets(set);
        }
    }

    /**
     * Classes cannot be resolved by a DBServer instance as long as the Navigator is not connected since it has the only
     * {@link MetaClassCacheService} implementation to date ({@link NavigatorMetaClassService}). This is rather bad
     * design as the server should be able to run independently from the navigator
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     * @param   usr       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  HistoryException  DOCUMENT ME!
     */
    private MetaObject getMetaObject(final int classId, final int objectId, final User usr) throws HistoryException {
        try {
            final MetaObject mo = server.getObject(objectId + "@" + classId, usr);
            if (mo == null) {
                throw new HistoryException("server did not provide metaobject: classId: " + classId // NOI18N
                            + " || objectId: " + objectId // NOI18N
                            + " || usr: " + usr); // NOI18N
            }
            final MetaClass[] allReadableMCs = server.getClasses(usr);

            assert allReadableMCs.length > 0 : "at least the metaclass of the metaobject must be readable"; // NOI18N

            mo.setAllClasses(DomainServerImpl.getClassHashTable(allReadableMCs, allReadableMCs[0].getDomain()));

            return mo;
        } catch (final Exception ex) {
            final String message = "cannot create object for history initialisation: classId: " + classId // NOI18N
                        + " || objectId: " + objectId                                                     // NOI18N
                        + " || usr: " + usr;                                                              // NOI18N
            LOG.error(message, ex);
            throw new HistoryException(message, ex);
        }
    }

    /**
     * Creates an initial entry for the given {@link MetaObject}. Basically it does the same as the
     * {@link #enqueueEntry(Sirius.server.middleware.types.MetaObject, Sirius.server.newuser.User, java.util.Date) }
     * operation except that it checks whether there is an entry already, actually runs the insertion if not and throws
     * an exception if an error occured.
     *
     * @param   mo         the metaobject that shall be historicised
     * @param   usr        the user that implicitely creates the history entry
     * @param   timestamp  the timestamp when the entry is created
     *
     * @throws  HistoryException  if the given <code>MetaObject</code> is null or the given <code>Date</code> is null or
     *                            an error occurred during history insertion
     *
     * @see     #enqueueEntry(Sirius.server.middleware.types.MetaObject, Sirius.server.newuser.User, java.util.Date)
     */
    public void initHistory(final MetaObject mo, final User usr, final Date timestamp) throws HistoryException {
        if ((mo == null) || (timestamp == null)) {
            final String message = "mo or timestamp must not be null: " // NOI18N
                        + "mo: " + mo                                   // NOI18N
                        + " || user: " + usr                            // NOI18N
                        + " || date: " + timestamp;                     // NOI18N
            LOG.error(message);
            throw new HistoryException(message);
        }

        if ((mo.getMetaClass().getClassAttribute(ClassAttribute.HISTORY_ENABLED) != null) && !hasHistory(mo)) {
            final MetaObject dbMo = getMetaObject(mo.getClassID(), mo.getID(), usr);
            final HistoryRunner runner = getRunner(dbMo, usr, timestamp);
            if (LOG.isDebugEnabled()) {
                LOG.debug("init history entry: " + runner); // NOI18N
            }
            runner.run();

            if (runner.executionException != null) {
                throw runner.executionException;
            }
        }
    }

    /**
     * Returns rather fast and only enqueues the entry instead of actually creating the history entry. If the
     * {@link MetaObject}'s {@link MetaClass} is not history enabled at all nothing will be done. If the "anonymous"
     * option is enabled the given user is ignored.<br/>
     * <br/>
     * <b>NOTE: This operation does not initialise the history!</b>
     *
     * @param  mo         the metaobject that shall be historicised
     * @param  user       the user that implicitely creates the history entry
     * @param  timestamp  the timestamp when the entry is created
     *
     * @see    ClassAttribute#HISTORY_ENABLED
     * @see    ClassAttribute#HISTORY_OPTION_ANONYMOUS
     */
    public void enqueueEntry(final MetaObject mo, final User user, final Date timestamp) {
        final HistoryRunner runner = getRunner(mo, user, timestamp);

        if (runner != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enqueue history entry: " + runner); // NOI18N
            }

            executor.execute(runner);
        }
    }

    /**
     * Creates a new {@link HistoryRunner} for the {@link MetaObject} if the object's class has the history enabled
     * attribute set. If the anonymous option is set the given user is ignored.
     *
     * @param   mo         the metaobject that shall be historicised
     * @param   user       the user that implicitely creates the history entry
     * @param   timestamp  the timestamp when the entry is created
     *
     * @return  an initialised <code>HistoryRunner</code> or null if the history enabled attribute is not set
     *
     * @throws  IllegalArgumentException  if the <code>MetaObject</code> or the timestamp is null
     *
     * @see     ClassAttribute#HISTORY_ENABLED
     * @see     ClassAttribute#HISTORY_OPTION_ANONYMOUS
     */
    private HistoryRunner getRunner(final MetaObject mo, final User user, final Date timestamp) {
        if ((mo == null) || (timestamp == null)) {
            throw new IllegalArgumentException("mo or timestamp must not be null: " // NOI18N
                        + "mo: " + mo                // NOI18N
                        + " || user: " + user        // NOI18N
                        + " || date: " + timestamp); // NOI18N
        }

        final ClassAttribute histEnabled = mo.getMetaClass().getClassAttribute(ClassAttribute.HISTORY_ENABLED);
        if (histEnabled == null) {
            return null;
        } else {
            final String anonymous = histEnabled.getOptions().get(ClassAttribute.HISTORY_OPTION_ANONYMOUS);

            final User historyUser;
            if (Boolean.TRUE.toString().equalsIgnoreCase(anonymous)) {
                historyUser = null;
            } else {
                historyUser = user;
            }

            return new HistoryRunner(mo, historyUser, timestamp);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class HistoryRunner implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private final transient MetaObject mo;
        private final transient User user;
        private final transient Date timestamp;

        private transient HistoryException executionException;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new HistoryRunner object.
         *
         * @param  mo         DOCUMENT ME!
         * @param  user       DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public HistoryRunner(final MetaObject mo, final User user, final Date timestamp) {
            this.mo = mo;
            this.user = user;
            this.timestamp = timestamp;
            executionException = null;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("creating history: " + this); // NOI18N
                }

                final String classKey = mo.getMetaClass().getTableName();
                final int objectId = mo.getId();
                final String usrKey = (user == null) ? null : user.getName();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("check for all userGroups");
                }
                // TODO check for all userGroups
                final UserGroup userGroup = user.getUserGroup();
                final String ugKey = (userGroup == null) ? null : userGroup.getName();
                final Timestamp valid_from = new Timestamp(timestamp.getTime());
                final String jsonData = mo.isPersistent() ? mo.getBean().toJSONString(true) : JSON_DELETED;

                final int result = server.getConnectionPool()
                            .submitInternalUpdate(
                                DBConnection.DESC_INSERT_HISTORY_ENTRY,
                                classKey,
                                objectId,
                                usrKey,
                                ugKey,
                                valid_from,
                                jsonData);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("history entry insertion result: " + result);                  // NOI18N
                }
            } catch (final Exception e) {
                executionException = new HistoryException("could not create history entry: " // NOI18N
                                + "mo: " + mo                                                // NOI18N
                                + " || user: " + user                                        // NOI18N
                                + " || date: " + timestamp,                                  // NOI18N
                        e);
            }
        }

        @Override
        public String toString() {
            return "history runner: mo: " + mo + " || user: " + user + " || timestamp: " + timestamp; // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class HistoryExecutor extends ThreadPoolExecutor {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new HistoryExecutor object.
         */
        public HistoryExecutor() {
            super(0, 100, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);

            if (r instanceof HistoryRunner) {
                final HistoryRunner runner = (HistoryRunner)r;
                if (runner.executionException != null) {
                    LOG.error(runner.executionException.getMessage(), runner.executionException);
                }
            }
        }
    }
}
