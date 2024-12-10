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

import org.openide.util.NbBundle;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

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
    protected Vector memberships;
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
        ConfigAttrStore.initialise(conPool, properties);
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
    public UsersWithMemberships checkForNewUsers() {
        final List newUsers;
        final List newUserGroups;
        final List newMemberships;

        newUsers = new ArrayList(100);
        newUserGroups = new ArrayList(10);
        newMemberships = new ArrayList(100);

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

                    newUsers.add(tmp);
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
                    newUserGroups.add(tmp);
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
                    newMemberships.add(tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            }

            memberTable.close();

            if (!users.containsAll(newUsers) && !memberships.containsAll(newMemberships)) {
                final List<User> allNewUsers = determineNewObjects((List<User>)newUsers, (Vector<User>)users);
                final List<Membership> allNewMemberships = determineNewObjects((List<Membership>)newMemberships,
                        (Vector<Membership>)memberships);
                final UsersWithMemberships newUsersWithMemberships = new UsersWithMemberships(allNewUsers);
                users.addAll(allNewUsers);

                for (final User user : allNewUsers) {
                    for (final Membership membership : allNewMemberships) {
                        if (membership.getUserKey().equals(user.getRegistryKey())) {
                            newUsersWithMemberships.getMemberships().add(membership);
                            memberships.add(membership);
                        }
                    }
                }

                return newUsersWithMemberships;
            }
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR ::  in membership statement" + e.getMessage(), e); // NOI18N
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>       DOCUMENT ME!
     * @param   newList  DOCUMENT ME!
     * @param   oldList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private <T> List<T> determineNewObjects(final List<T> newList, final Vector<T> oldList) {
        final List<T> result = new ArrayList<>();

        for (final T tmp : newList) {
            if (!oldList.contains(tmp)) {
                result.add(tmp);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ServerProperties getProperties() {
        return properties;
    }

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
     * @param   command  DOCUMENT ME!
     * @param   name     DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void execScript(final String command, final String name) throws Exception {
        final Process process = new ProcessBuilder(command.split(" ")).start();

        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            final String output = reader.lines().collect(Collectors.joining());
            LOG.info(String.format("%s:\n%s", name, output));
        }

        final int exitVal = process.waitFor();
        if (exitVal != 0) {
            throw new Exception(String.format("Script '%s' (%s) returned != 0", command, name));
        }
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
        // timestamp erzeugen + csconf + commit
        // success => change + datum

        // the method newPasswordValid throws an exception, if the new password is not valid
        newPasswordValid(newPassword);

        final Timestamp timestamp = new Timestamp(new Date().getTime());

        final String loginName = user.getName();

        final String beforeScript = getProperties().getPasswordchangeTriggerScriptBefore();
        if (beforeScript != null) {
            execScript(beforeScript.replaceAll("\\{user\\}", loginName).replaceAll("\\{password\\}", newPassword)
                        .replaceAll("\\{oldPassword\\}", oldPassword).replaceAll(
                    "\\{time\\}",
                    String.format("%d", timestamp.getTime())),
                "passwordchangeTriggerScriptBefore");
        }
        final boolean success = conPool.submitInternalUpdate(
                DBConnection.DESC_CHANGE_USER_PASSWORD,
                newPassword,
                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(timestamp),
                loginName,
                oldPassword) > 0;

        if (success) {
            final String afterScript = getProperties().getPasswordchangeTriggerScriptAfter();
            if (afterScript != null) {
                execScript(afterScript.replaceAll("\\{user\\}", loginName).replaceAll("\\{password\\}", newPassword)
                            .replaceAll("\\{oldPassword\\}", oldPassword).replaceAll(
                        "\\{time\\}",
                        String.format("%d", timestamp.getTime())),
                    "passwordchangeTriggerScriptAfter");
            }
        }

        return success;
    }

    /**
     * Checks, if the given password is a valid password.
     *
     * @param   password  the new password
     *
     * @return  true, iff the new password is valid
     *
     * @throws  PasswordCheckException  this exception will be thrown, if the new password is not valid
     */
    private boolean newPasswordValid(final String password) throws PasswordCheckException {
        if (password.length() < 8) {
            throw new PasswordCheckException(NbBundle.getMessage(UserStore.class, "UserStore.checkNewPassword.length"));
        }
        boolean digit = false;
        boolean letter = false;
        boolean special = false;

        for (int i = 0; i < password.length(); ++i) {
            final char c = password.charAt(i);
            if (Character.isDigit(c)) {
                digit = true;
            } else if (Character.isAlphabetic(c)) {
                letter = true;
            } else {
                // all characters, which are not a letter or a digit are special characters
                special = true;
            }
        }

        String errors = "";

        if (!digit) {
            errors += NbBundle.getMessage(UserStore.class, "UserStore.checkNewPassword.digit") + "\n";
        }

        if (!letter) {
            errors += NbBundle.getMessage(UserStore.class, "UserStore.checkNewPassword.letter") + "\n";
        }

        if (!special) {
            errors += NbBundle.getMessage(UserStore.class, "UserStore.checkNewPassword.special") + "\n";
        }

        if (!errors.equals("")) {
            if (errors.endsWith("\n")) {
                errors = errors.substring(0, errors.length() - 1);
            }

            throw new PasswordCheckException(errors);
        }

        return true;
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
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public String isUserDeactivated(final User user) throws SQLException {
        ResultSet result = null;
        try {
            result = conPool.submitInternalQuery(
                    DBConnection.IS_USER_DEACTIVATED,
                    user.getName());

            if (result.next()) {
                final String res = result.getString(1);

                if ((res == null) || res.equalsIgnoreCase("false")) {
                    return null;
                } else if (res.equalsIgnoreCase("true")) {
                    final String[] text = getConfigAttrs(user, "deactivation_text");

                    if ((text != null) && (text.length > 0)) {
                        return text[0];
                    } else {
                        return "Dieser Nutzer wurde deaktiviert.";
                    }
                }

                return res;
            }
        } finally {
            DBConnection.closeResultSets(result);
        }

        return null;
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
    public String[] getConfigAttrs(final User user, final String key) throws SQLException {
        synchronized (ConfigAttrStore.getInstance()) {
            if ((user == null) || (key == null)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("user and/or key is null, returning null: user: " + user + " || key: " + key);
                }

                return null;
            }

            if (user.getUserGroup() != null) {
                final UserGroup userGroup = user.getUserGroup();

                // ASSIGNED DIRECTLY TO THE USER
                final String configAttrUser = ConfigAttrStore.getInstance().getConfigAttr(key, user);
                if (configAttrUser != null) {
                    return new String[] { configAttrUser };
                }

                // ASSIGNED TO THE USER@GROUP@DOMAIN
                final String configAttrUserGroup = ConfigAttrStore.getInstance().getConfigAttr(key, user, userGroup);
                if (configAttrUserGroup != null) {
                    return new String[] { configAttrUserGroup };
                }

                // ASSIGNED TO THE GROUP@DOMAIN
                final String configAttrGroup = ConfigAttrStore.getInstance().getConfigAttr(key, userGroup);
                if (configAttrGroup != null) {
                    return new String[] { configAttrGroup };
                }

                // ASSIGNED TO THE DOMAIN
                final String configAttrDomain = ConfigAttrStore.getInstance().getConfigAttr(key, userGroup.getDomain());
                if (configAttrDomain != null) {
                    return new String[] { configAttrDomain };
                }

                return null;
            } else {
                final String userName = user.getName();

                String exemptGroup = null;

                exemptGroup = ConfigAttrStore.getInstance().getExempt(user, key);
                final Set<String> configAttrs = new LinkedHashSet<>();

                // ASSIGNED DIRECTLY TO THE USER
                final String configAttrUser = ConfigAttrStore.getInstance().getConfigAttr(key, user);
                if (configAttrUser != null) {
                    configAttrs.add(configAttrUser);
                }

                final Set<String> domains = new LinkedHashSet<>();
                for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                    if (potentialUserGroup != null) {
                        if ((exemptGroup == null) || (potentialUserGroup.getName().equals(exemptGroup))) {
                            // ASSIGNED TO THE USER@GROUP@DOMAIN
                            final String configAttrUserGroup = ConfigAttrStore.getInstance()
                                        .getConfigAttr(key, user, potentialUserGroup);
                            if (configAttrUserGroup != null) {
                                configAttrs.add(configAttrUserGroup);
                            }

                            // ASSIGNED TO THE GROUP@DOMAIN
                            final String configAttrGroup = ConfigAttrStore.getInstance()
                                        .getConfigAttr(key, potentialUserGroup);
                            if (configAttrGroup != null) {
                                configAttrs.add(configAttrGroup);
                            }

                            // adding domains for later check for domain assignements
                            if (potentialUserGroup.getDomain() != null) {
                                domains.add(potentialUserGroup.getDomain());
                            }
                        }
                    }
                }
                for (final String domain : domains) {
                    final String configAttrDomain = ConfigAttrStore.getInstance().getConfigAttr(key, domain);
                    if (configAttrDomain != null) {
                        configAttrs.add(configAttrDomain);
                    }
                }
                return configAttrs.isEmpty() ? null : configAttrs.toArray(new String[0]);
            }
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
    public synchronized String[] getConfigAttrsOld(final User user, final String key) throws SQLException {
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
            if (getProperties().getServerName().equals(userGroup.getDomain())) {
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class UsersWithMemberships {

        //~ Instance fields ----------------------------------------------------

        private List<User> users = new ArrayList<>();
        private List<Membership> memberships = new ArrayList<>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UsersWithMemberships object.
         *
         * @param  users  DOCUMENT ME!
         */
        public UsersWithMemberships(final List<User> users) {
            this.users = users;
        }

        /**
         * Creates a new UsersWithMemberships object.
         *
         * @param  users        DOCUMENT ME!
         * @param  memberships  DOCUMENT ME!
         */
        public UsersWithMemberships(final List<User> users, final List<Membership> memberships) {
            this.users = users;
            this.memberships = memberships;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the users
         */
        public List<User> getUsers() {
            return users;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  users  the users to set
         */
        public void setUsers(final List<User> users) {
            this.users = users;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the memberships
         */
        public List<Membership> getMemberships() {
            return memberships;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  memberships  the memberships to set
         */
        public void setMemberships(final List<Membership> memberships) {
            this.memberships = memberships;
        }
    }
}
