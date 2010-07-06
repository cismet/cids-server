/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.user;

import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.Shutdownable;
import Sirius.server.newuser.*;
import Sirius.server.property.*;
import Sirius.server.sql.*;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.sql.*;

import java.util.*;

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

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 3902369677120412592L;

    private static final transient Logger LOG = Logger.getLogger(UserStore.class);

    //~ Instance fields --------------------------------------------------------

    protected DBConnectionPool conPool;

    protected Vector users;
    protected Vector userGroups;
    // protected Hashtable userGroupHash;
    protected Vector memberships;
    // protected Hashtable membershipHash;// by userIDplusLsName
    protected ServerProperties properties;
    protected PreparedStatement validateUser;

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

        final DBConnection con = conPool.getConnection();

        try {
            final ResultSet userTable = con.submitQuery("get_all_users", new Object[0]);//NOI18N

            // --------------------load users--------------------------------------------------

            while (userTable.next()) {
                try {
                    // User tmp = new
                    // User(userTable.getString("login_name").trim(),properties.getLocalServerName(),userTable.getInt("id"),userTable.getBoolean("administrator")
                    // ); User tmp = new
                    // User(userTable.getString("login_name").trim(),properties.getServerName(),userTable.getInt("id"),DBConnection.stringToBool(userTable.getString("administrator"))
                    // );
                    final User tmp = new User(
                            userTable.getInt("id"),   // NOI18N
                            userTable.getString("login_name").trim(),   // NOI18N
                            properties.getServerName(),
                            userTable.getBoolean("administrator"));   // NOI18N

                    users.addElement(tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            } // end while

            userTable.close();

            // --------------------load userGroups--------------------------------------------------

            final ResultSet userGroupTable = con.submitQuery("get_all_usergroups", new Object[0]);//NOI18N

            while (userGroupTable.next()) {
                try {
                    // UserGroup tmp = new
                    // UserGroup(userGroupTable.getString("name").trim(),properties.getServerName(),userGroupTable.getInt("id")
                    // );

                    final UserGroup tmp = new UserGroup(
                            userGroupTable.getInt("id"),   // NOI18N
                            userGroupTable.getString("name").trim(),   // NOI18N
                            properties.getServerName(),
                            userGroupTable.getString("descr"));   // NOI18N
                    userGroups.addElement(tmp);
                    // userGroupHash.put(new Integer(tmp.getID()),tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            } // end while

            userGroupTable.close();

            // --------------------load memberships--------------------------------------------------

            final ResultSet memberTable = con.submitQuery("get_all_memberships", new Object[0]);//NOI18N

            while (memberTable.next()) {
                try {
                    final String lsName = properties.getServerName();

                    final String login = memberTable.getString("login_name");
                    final String ug = memberTable.getString("ug");

                    String ugDomain = memberTable.getString("ugDomain");   // NOI18N

                    if ((ugDomain == null) || ugDomain.equalsIgnoreCase("local")) {   // NOI18N
                        ugDomain = lsName;
                    }

                    final String usrDomain = lsName;

                    final Membership tmp = new Membership(login, usrDomain, ug, ugDomain);
                    memberships.addElement(tmp);
                    // durch getkey ersetzen  xxxx
                    // membershipHash.put(login+usrDomain,tmp);
                } catch (Exception e) {
                    LOG.error(e);

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }
            } // end while

            memberTable.close();

            // addSearchMasks(con);

            // prepare statement for validate user (called very often) :-)
            final String valUser =
                "select count(*) from cs_usr as u ,cs_ug as ug ,cs_ug_membership as m where u.id=m.usr_id and  ug.id = m.ug_id and trim(login_name) = ? and trim(ug.name) = ?";   // NOI18N
            validateUser = con.getConnection().prepareStatement(valUser);

            addShutdown(new Shutdownable() {

                    @Override
                    public void shutdown() throws ServerExitError {
                        users.clear();
                        userGroups.clear();
                        memberships.clear();
                        try {
                            validateUser.close();
                        } catch (final SQLException ex) {
                            LOG.warn("could not close validate user statement", ex); // NOI18N
                        }
                    }
                });
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR ::  in membership statement" + e.getMessage(), e);   // NOI18N
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
        final DBConnection con = conPool.getConnection();

        final java.lang.Object[] params = new java.lang.Object[3];

        params[0] = newPassword;
        params[1] = user.getName().toLowerCase();
        params[2] = oldPassword;

        if (con.submitUpdate("change_user_password", params) > 0) {   // NOI18N
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
    public boolean validateUser(final User user) {
//        if(user == null)
//        {   logger.error("user for validation was null");
//            return false;
//        }
//        String name = user.getName().trim();
//        String ug_name = user.getUserGroup().getName().trim();
//
//
//
//        if(name == null || ug_name==null)
//        {   logger.error("user name for validation was null");
//            return false;
//        }
//
//        logger.debug("stmnt at validate user "+ name + " user group " +ug_name);
//
//        try
//        {
//            validateUser.setString(1, name);
//            validateUser.setString(2, ug_name);
//
//            ResultSet result = validateUser.executeQuery();
//
//            if( result.next())
//                if(result.getInt(1)>0)
//                    return true;
//
//
//        }
//        catch(Exception e)
//        {logger.error("wahrscheinlich user nicht gefunden",e);}
//
//        return false;

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
        final DBConnection con = conPool.getConnection();
        ResultSet result = null;
        try {
            // TODO: should username and password be trimmed?
            result = con.submitInternalQuery(
                    "verify_user_password",   // NOI18N
                    user.getName().trim().toLowerCase(),
                    password.trim().toLowerCase());
            return result.next() && (result.getInt(1) == 1);
        } finally {
            DBConnection.closeResultSets(result);
        }
    }
}
