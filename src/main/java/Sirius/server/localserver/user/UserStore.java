/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.user;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.newuser.Membership;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.ExceptionHandler;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   sascha.schlobinski@cismet.de
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class UserStore extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserStore.class);

    //~ Instance fields --------------------------------------------------------

    protected DBConnectionPool conPool;
    protected Vector users;
    protected Vector userGroups;
    // protected Hashtable userGroupHash;
    protected Vector memberships;
    // protected Hashtable membershipHash;// by userIDplusLsName
    protected ServerProperties properties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserStore object.
     *
     * @param  conPool     DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    public UserStore(final DBConnectionPool conPool, final ServerProperties properties) {
        this.conPool = conPool;
        this.properties = properties;
        users = new Vector(100, 100);
        userGroups = new Vector(10, 10);
        // userGroupHash = new Hashtable(25);
        memberships = new Vector(100, 100);
        // membershipHash = new Hashtable(101);

        try {
            final ResultSet userTable = conPool.submitInternalQuery(DBConnection.DESC_GET_ALL_USERS, new Object[0]);

            // --------------------load users--------------------------------------------------

            while (userTable.next()) {
                try {
                    final User tmp = new User(
                            userTable.getInt("id"),                   // NOI18N
                            userTable.getString("login_name").trim(), // NOI18N
                            properties.getServerName(),
                            userTable.getBoolean("administrator"));   // NOI18N

                    users.addElement(tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            }

            userTable.close();

            // --------------------load userGroups--------------------------------------------------

            final ResultSet userGroupTable = conPool.submitInternalQuery(
                    DBConnection.DESC_GET_ALL_USERGROUPS,
                    new Object[0]);

            while (userGroupTable.next()) {
                try {
                    String domain = userGroupTable.getString("domain_name"); // NOI18N
                    if ("LOCAL".equals(domain)) {                            // NOI18N
                        domain = properties.getServerName();
                    }

                    final UserGroup tmp = new UserGroup(
                            userGroupTable.getInt("id"),             // NOI18N
                            userGroupTable.getString("name").trim(), // NOI18N
                            domain,
                            userGroupTable.getString("descr"),
                            userGroupTable.getInt("prio"));          // NOI18N
                    userGroups.addElement(tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            }

            
            userGroupTable.close();

            // --------------------load memberships--------------------------------------------------

            final ResultSet memberTable = conPool.submitInternalQuery(
                    DBConnection.DESC_GET_ALL_MEMBERSHIPS,
                    new Object[0]);

            while (memberTable.next()) {
                try {
                    final String lsName = properties.getServerName();

                    final String login = memberTable.getString("login_name");
                    final String ug = memberTable.getString("ug");

                    String ugDomain = memberTable.getString("ugDomain"); // NOI18N

                    if ((ugDomain == null) || ugDomain.equalsIgnoreCase("local")) { // NOI18N
                        ugDomain = lsName;
                    }

                    final String usrDomain = lsName;

                    final Membership tmp = new Membership(login, usrDomain, ug, ugDomain);
                    memberships.addElement(tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            }

            memberTable.close();

            addShutdown(new AbstractShutdownable() {

                    @Override
                    protected void internalShutdown() throws ServerExitError {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("shutting down UserStore"); // NOI18N
                        }

                        users.clear();
                        userGroups.clear();
                        memberships.clear();
                    }
                });
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR ::  in membership statement" + e.getMessage(), e); // NOI18N
        }
    }                                                                                // end Konstruktor

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector getUsers() {
        return users;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector getUserGroups() {
        return userGroups;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector getMemberships() {
        return memberships;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws Exception {
        final java.lang.Object[] params = new java.lang.Object[3];

        params[0] = newPassword;
        params[1] = user.getName();
        params[2] = oldPassword;

        if (conPool.submitInternalUpdate(DBConnection.DESC_CHANGE_USER_PASSWORD, params) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    // FIXME: WHATS THE PURPOSE OF THIS IMPL???
    public boolean validateUser(final User user) {
        return true;
    }

    /**
     * --------------------------------------------------------------------------
     *
     * @param   user      DOCUMENT ME!
     * @param   password  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public boolean validateUserPassword(final User user, final String password) throws SQLException {
        ResultSet result = null;
        try {
            // TODO: should username and password be trimmed?
            result = conPool.submitInternalQuery(
                    DBConnection.DESC_VERIFY_USER_PW,
                    user.getName(),
                    password);
            return result.next() && (result.getInt(1) == 1);
        } finally {
            DBConnection.closeResultSets(result);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   key   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public synchronized String[] getConfigAttrs(final User user, final String key) throws SQLException {
        if ((user == null) || (key == null)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("user and/or key is null, returning null: user: " + user + " || key: " + key);
            }

            return null;
        }

        ResultSet keyIdSet = null;
        int keyId = -1;
        try {
            keyIdSet = conPool.submitInternalQuery(DBConnection.DESC_FETCH_CONFIG_ATTR_KEY_ID, key);
            if (keyIdSet.next()) {
                keyId = keyIdSet.getInt(1);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("key not present: " + key); // NOI18N
                }

                return null;
            }
        } finally {
            DBConnection.closeResultSets(keyIdSet);
        }

        assert keyId > 0 : "invalid key id"; // NOI18N

        final UserGroup userGroup = user.getUserGroup();
        if (userGroup != null) {
            final String configAttr = getConfigAttrForUserGroup(keyId, user, userGroup);
            return (configAttr != null) ? new String[] { configAttr } : null;
        } else {
            final ResultSet exemptGroupValueSet = conPool.submitInternalQuery(
                    DBConnection.DESC_FETCH_CONFIG_ATTR_EXEMPT_VALUE,
                    user.getId(),
                    keyId);
            final int groupId;
            if (exemptGroupValueSet.next()) {
                groupId = exemptGroupValueSet.getInt(1);
            } else {
                groupId = -1;
            }

            final Set<String> configAttrs = new LinkedHashSet<>();
            for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                final String configAttr = getConfigAttrForUserGroup(keyId, user, potentialUserGroup);
                if (groupId < 0) {
                    if (configAttr != null) {
                        configAttrs.add(configAttr);
                    }
                } else if (potentialUserGroup.getId() == groupId) {
                    configAttrs.add(configAttr);
                }
            }
            return configAttrs.isEmpty() ? null : configAttrs.toArray(new String[0]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   keyId      DOCUMENT ME!
     * @param   user       DOCUMENT ME!
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private String getConfigAttrForUserGroup(final int keyId, final User user, final UserGroup userGroup)
            throws SQLException {
        ResultSet userValueSet = null;
        ResultSet ugValueSet = null;
        ResultSet domainValueSet = null;

        try {
            final String userName = user.getName();
            final String userGroupName = userGroup.getName();
            final String domain;
            if (properties.getServerName().equals(userGroup.getDomain())) {
                domain = "LOCAL"; // NOI18N
            } else {
                domain = userGroup.getDomain();
            }

            final String value;
            userValueSet = conPool.submitInternalQuery(
                    DBConnection.DESC_FETCH_CONFIG_ATTR_USER_VALUE,
                    userName,
                    userGroupName,
                    domain,
                    keyId);
            if (userValueSet.next()) {
                value = userValueSet.getString(1);
            } else {
                ugValueSet = conPool.submitInternalQuery(
                        DBConnection.DESC_FETCH_CONFIG_ATTR_UG_VALUE,
                        userGroupName,
                        domain,
                        keyId);
                if (ugValueSet.next()) {
                    value = ugValueSet.getString(1);
                } else {
                    domainValueSet = conPool.submitInternalQuery(
                            DBConnection.DESC_FETCH_CONFIG_ATTR_DOMAIN_VALUE,
                            domain,
                            keyId);
                    if (domainValueSet.next()) {
                        value = domainValueSet.getString(1);
                    } else {
                        value = null;
                    }
                }
            }

            return value;
        } finally {
            DBConnection.closeResultSets(userValueSet, ugValueSet, domainValueSet);
        }
    }
}
