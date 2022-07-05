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

        try (
            final ResultSet userTable = conPool.submitInternalQuery(DBConnection.DESC_GET_ALL_USERS, new Object[0]);
            final ResultSet userGroupTable = conPool.submitInternalQuery(DBConnection.DESC_GET_ALL_USERGROUPS, new Object[0]);
            final ResultSet memberTable = conPool.submitInternalQuery(DBConnection.DESC_GET_ALL_MEMBERSHIPS, new Object[0]);
        ) {

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

            // --------------------load userGroups--------------------------------------------------

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

            // --------------------load memberships--------------------------------------------------            

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
        try (final ResultSet result = conPool.submitInternalQuery(
                    DBConnection.DESC_VERIFY_USER_PW,
                    user.getName(),
                    password);) {
            // TODO: should username and password be trimmed?
            return result.next() && (result.getInt(1) == 1);
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

        if (user.getUserGroup() != null) {
            final UserGroup userGroup = user.getUserGroup();
           
            // ASSIGNED DIRECTLY TO THE USER
            final String configAttrUser = getConfigAttr(key, user);
            if (configAttrUser != null) {
                return new String[] { configAttrUser };
            }

            // ASSIGNED TO THE USER@GROUP@DOMAIN
            final String configAttrUserGroup = getConfigAttr(key, user, userGroup);
            if (configAttrUserGroup != null) {
                return new String[] { configAttrUserGroup };
            }

            // ASSIGNED TO THE GROUP@DOMAIN
            final String configAttrGroup = getConfigAttr(key, userGroup);
            if (configAttrGroup != null) {
                return new String[] { configAttrGroup };
            }

            // ASSIGNED TO THE DOMAIN
            final String configAttrDomain = getConfigAttr(key, userGroup.getDomain());
            if (configAttrDomain != null) {
                return new String[] { configAttrDomain };
            }
            
            return null;
        } else {
            final String userName = user.getName();
            
            int groupId = -1;
            try (final ResultSet exemptGroupValueSet = conPool.submitInternalQuery(
                    DBConnection.DESC_FETCH_CONFIG_ATTR_EXEMPT_VALUE,
                    userName,
                    key)) {
                if (exemptGroupValueSet.next()) {
                    groupId = exemptGroupValueSet.getInt(1);
                }
            }

            final Set<String> configAttrs = new LinkedHashSet<>();

            // ASSIGNED DIRECTLY TO THE USER
            final String configAttrUser = getConfigAttr(key, user);
            if (configAttrUser != null) {
                configAttrs.add(configAttrUser);                
            }
            
            final Set<String> domains = new LinkedHashSet<>();
            for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                if (potentialUserGroup != null) {
                    if (groupId < 0 || potentialUserGroup.getId() == groupId) {
                        
                        // ASSIGNED TO THE USER@GROUP@DOMAIN
                        final String configAttrUserGroup = getConfigAttr(key, user, potentialUserGroup);
                        if (configAttrUserGroup != null) {
                            configAttrs.add(configAttrUserGroup);
                        }
                        
                        // ASSIGNED TO THE GROUP@DOMAIN
                        final String configAttrGroup = getConfigAttr(key, potentialUserGroup);
                        if (configAttrGroup != null) {
                            configAttrs.add(configAttrGroup);
                        }

                        // adding domains for later check for domain assignements
                        if (potentialUserGroup.getDomain() != null)  {
                            domains.add(potentialUserGroup.getDomain());
                        }
                    }
                }
            }
            for (final String domain : domains) {
                final String configAttrDomain = getConfigAttr(key, domain);
                if (configAttrDomain != null) {
                    configAttrs.add(configAttrDomain);
                }
            }
            return configAttrs.isEmpty() ? null : configAttrs.toArray(new String[0]);
        }
    }

    /**
     * ASSIGNED DIRECTLY TO THE USER
     * 
     * @param key
     * @param user
     * @return
     * @throws SQLException 
     */
    private String getConfigAttr(final String key, final User user)
            throws SQLException {
        final String userName = user.getName();

        try (final ResultSet userValueSet = conPool.submitInternalQuery(
                DBConnection.DESC_FETCH_CONFIG_ATTR_USER_VALUE,
                userName,
                key)) {
            if (userValueSet.next()) {
                return userValueSet.getString(1);
            }
        }
        return null;
    }
    
    
    /**
     * ASSIGNED TO THE USER@GROUP@DOMAIN
     * 
     * @param key
     * @param user
     * @param userGroup
     * @return
     * @throws SQLException 
     */
    private String getConfigAttr(final String key, final User user, final UserGroup userGroup) throws SQLException {         
        if (user != null && userGroup != null) {
            final String userName = user.getName();
            final String userGroupName = userGroup.getName();                                
            final String domain = userGroup.getDomain();
            
            if (properties.getServerName().equals(domain)) { // CHECKING NOW FOR BOTH ASSIGNEMENTS: LOCAL AND <SERVER_NAME>                                   
                final String configAttr = getConfigAttr(key, userName, userGroupName, "LOCAL");
                if (configAttr != null) return configAttr;            
            }
            
            final String configAttr = getConfigAttr(key, userName, userGroupName, domain);
            if (configAttr != null) return configAttr;            
        }
        return null;
    }
    
    private String getConfigAttr(final String key, final String userName, final String userGroupName, final String domain) throws SQLException {         
        try (final ResultSet userValueSet = conPool.submitInternalQuery(
                DBConnection.DESC_FETCH_CONFIG_ATTR_USER_AND_GROUP_VALUE,
                userName,
                userGroupName,
                domain,
                key)) {
            if (userValueSet.next()) {
                return userValueSet.getString(1);
            }
        }
        return null;
    }
    
    /**
     * ASSIGNED TO THE GROUP@DOMAIN
     * 
     * @param key
     * @param userGroup
     * @return
     * @throws SQLException 
     */
    private String getConfigAttr(final String key, final UserGroup userGroup) throws SQLException {         
        if (userGroup != null) {
            final String userGroupName = userGroup.getName();                        
            final String userGroupDomain = userGroup.getDomain();

            if (properties.getServerName().equals(userGroup.getDomain())) { // CHECKING NOW FOR BOTH ASSIGNEMENTS: LOCAL AND <SERVER_NAME>
                final String configAttr = getConfigAttr(key, userGroupName, "LOCAL");
                if (configAttr != null) return configAttr;
            }

            final String configAttr = getConfigAttr(key, userGroupName, userGroupDomain);
            if (configAttr != null) return configAttr;
        }

        return null;
    }    
    
    private String getConfigAttr(final String key, final String userGroupName, final String domain) throws SQLException {         
        try (final ResultSet ugValueSet = conPool.submitInternalQuery(
                DBConnection.DESC_FETCH_CONFIG_ATTR_UG_VALUE,
                userGroupName,
                domain,
                key)) {
            if (ugValueSet.next()) {
                return ugValueSet.getString(1);
            }
        }
        return null;
        
    }
    
    /**
     * ASSIGNED TO THE DOMAIN
     * 
     * @param key
     * @param domain
     * @return
     * @throws SQLException 
     */
    private String getConfigAttr(final String key, final String domain) throws SQLException {                  
        if (properties.getServerName().equals(domain)) { // CHECKING NOW FOR BOTH ASSIGNEMENTS: LOCAL AND <SERVER_NAME>
            final String configAttr = _getConfigAttr(key, "LOCAL");
            if (configAttr != null) return configAttr;
        }
        
        final String configAttr = _getConfigAttr(key, domain);
        if (configAttr != null) return configAttr;

        return null;
    }

    private String _getConfigAttr(final String key, final String domain) throws SQLException {                  
        try (final ResultSet domainValueSet = conPool.submitInternalQuery(
                DBConnection.DESC_FETCH_CONFIG_ATTR_DOMAIN_VALUE,
                domain,
                key)) {
            if (domainValueSet.next()) {
                return domainValueSet.getString(1);
            }
        }
        return null;        
    }
    
}
