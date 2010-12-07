/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.history;

import Sirius.server.Shutdown;
import Sirius.server.localserver._class.ClassCache;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final transient DBConnectionPool conPool;
    private final transient ClassCache classCache;
    private final transient ExecutorService executor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HistoryServer object.
     *
     * @param  conPool     DOCUMENT ME!
     * @param  classCache  DOCUMENT ME!
     */
    public HistoryServer(final DBConnectionPool conPool, final ClassCache classCache) {
        this.conPool = conPool;
        this.classCache = classCache;
        executor = Executors.newCachedThreadPool();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     * @param   ug        DOCUMENT ME!
     * @param   elements  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  HistoryException  DOCUMENT ME!
     */
    public HistoryObject[] getHistory(final int classId, final int objectId, final UserGroup ug, final int elements)
            throws HistoryException {
        final Sirius.server.localserver._class.Class clazz;
        try {
            clazz = classCache.getClass(classId);
        } catch (final Exception ex) {
            final String message = "cannot get class for id: " + classId; // NOI18N
            LOG.error(message, ex);
            throw new HistoryException(message, ex);
        }

        if (clazz.getPermissions().hasReadPermission(ug)) {
            final DBConnection con = conPool.getConnection();
            ResultSet set = null;
            try {
                final int expectedElements;
                if (elements < 1) {
                    set = con.submitInternalQuery(
                            DBConnection.DESC_FETCH_HISTORY,
                            classId,
                            objectId);
                    expectedElements = 15;
                } else {
                    set = con.submitInternalQuery(
                            DBConnection.DESC_FETCH_HISTORY_LIMIT,
                            classId,
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
     * DOCUMENT ME!
     *
     * @param   mo         DOCUMENT ME!
     * @param   user       DOCUMENT ME!
     * @param   timestamp  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void enqueueEntry(final MetaObject mo, final User user, final Date timestamp) {
        if ((mo == null) || (timestamp == null)) {
            throw new IllegalArgumentException("mo or timestamp must not be null: " // NOI18N
                        + "mo: " + mo                // NOI18N
                        + " || user: " + user        // NOI18N
                        + " || date: " + timestamp); // NOI18N
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("enqueue history entry: mo: " + mo + " || user: " + user + " || date: " + timestamp); // NOI18N
        }

        executor.execute(new HistoryRunner(mo, user, timestamp));
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
        private final User user;
        private final Date timestamp;

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
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("create history entry: mo: " + mo + " || user: " + user + " || date: " + timestamp); // NOI18N
                }

                final int classId = mo.getClassID();
                final int objectId = mo.getId();
                final Integer usrId = (user == null) ? null : user.getId();
                final Integer ugId = (user == null) ? null : user.getUserGroup().getId();
                final Timestamp valid_from = new Timestamp(timestamp.getTime());
                final String jsonData = mo.isPersistent() ? mo.getBean().toJSONString() : JSON_DELETED;

                final DBConnection con = conPool.getConnection();
                final int result = con.submitInternalUpdate(
                        DBConnection.DESC_INSERT_HISTORY_ENTRY,
                        classId,
                        objectId,
                        usrId,
                        ugId,
                        valid_from,
                        jsonData);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("history entry insertion result: " + result);                                             // NOI18N
                }
            } catch (final Exception e) {
                LOG.error("could not create history entry: mo: " + mo + " || user: " + user + " || date: " + timestamp, // NOI18N
                    e);
            }
        }
    }
}
