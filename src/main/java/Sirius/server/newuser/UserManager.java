/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UserManager.java
 *
 * Created on 14. M\u00E4rz 2005, 12:20
 */
package Sirius.server.newuser;
import java.util.*;

import de.cismet.tools.collections.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class UserManager implements Sirius.server.newuser.UserServer {

    //~ Instance fields --------------------------------------------------------

    protected Hashtable users;

    protected Hashtable ugs;

    protected MultiMap memberships;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UserManager.
     */
    public UserManager() {
        this.users = new Hashtable();
        this.ugs = new Hashtable();
        this.memberships = new MultiMap();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public User getUser(final String userGroupDomain,
            final String userGroup,
            final String userDomain,
            final String user,
            final String password) throws java.rmi.RemoteException, UserException {
        final User u = (User)users.get(constructKey(user, userDomain));
        if (logger.isDebugEnabled()) {
            logger.debug("user found :: " + u + " for " + user + "@" + userDomain + " :: users available " + users); // NOI18N
        }

        // check ob membership
        if (u == null) {
            throw new UserException(
                "UserException :: no User :: " // NOI18N
                        + user
                        + ", "                 // NOI18N
                        + userDomain
                        + ", "                 // NOI18N
                        + userGroup
                        + ", "                 // NOI18N
                        + userGroupDomain
                        + " registered",       // NOI18N
                true,
                false,
                false,
                false);
        } else if (!memberships.containsKey(u.getRegistryKey())) {
            throw new UserException(
                "UserException :: no User :: " // NOI18N
                        + user
                        + ", "                 // NOI18N
                        + userDomain
                        + ", "                 // NOI18N
                        + userGroup
                        + ", "                 // NOI18N
                        + userGroupDomain
                        + " registered",       // NOI18N
                true,
                false,
                false,
                false);
        } else                                 // es ist mindestens eine Membership eingetragen
        {
            final List l = (List)memberships.get(u.getRegistryKey());

            for (int i = 0; i < l.size(); i++) {
                final Membership m = (Membership)l.get(i);

                if (m.getUgDomain().equalsIgnoreCase(userGroupDomain) && m.getUg().equalsIgnoreCase(userGroup)) {
                    break;
                }

                if (i == (l.size() - 1))                    // last element and no break
                {
                    throw new UserException(
                        "UserException :: no UserGroup :: " // NOI18N
                                + userGroup
                                + ", "                      // NOI18N
                                + userGroupDomain,
                        false,
                        false,
                        true,
                        false);
                }
            }
        }

        final UserGroup ug = (UserGroup)ugs.get(constructKey(userGroup, userGroupDomain));

        u.setUserGroup(ug);

        return u;
    }

    @Override
    public java.util.Vector getUserGroupNames(final User user) throws java.rmi.RemoteException {
        if (user == null) { // throw new java.rmi.RemoteException("UserException::no Usergroup for ::"+userName + "
                            // -- "+domain);
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no  user " + user);   // NOI18N
                }
            }
            return new Vector(0);
        }
        return getUserGroupNames(user.getName(), user.getDomain());
    }

    @Override
    public java.util.Vector getUserGroupNames(final String userName, final String domain)
            throws java.rmi.RemoteException {
        if ((userName == null) || (domain == null)) { // throw new java.rmi.RemoteException("UserException::no
                                                      // Usergroup for ::"+userName + " -- "+domain);
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no  user " + userName + "or no domain " + domain);   // NOI18N
                }
            }
            return new Vector(0);
        }

        final List l = (List)memberships.get(constructKey(userName, domain));

        if (l == null) { // throw new java.rmi.RemoteException("UserException::no Usergroup for ::"+userName + " --
                         // "+domain);
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no usergroup for user " + userName);   // NOI18N
                }
            }
            return new Vector(0);
        }

        final Vector result = new Vector(l.size());

        for (int i = 0; i < l.size(); i++) {
            final Membership ug = (Membership)l.get(i);

            if ((ug != null) && ug.getUgDomain().equalsIgnoreCase(ug.getUserDomain())) // fremde Nutzerrechte werden
                                                                                       // nicht in der Auswahl
                                                                                       // angeboten
            {
                final String[] userInfo = new String[2];

                userInfo[0] = ug.getUg();
                userInfo[1] = ug.getUgDomain();

                result.add(userInfo);
            }
        }

        return result;
    }

    @Override
    public java.util.Vector getUserGroups() throws java.rmi.RemoteException {
        return new Vector(ugs.values());
    }

    @Override
    public java.util.Vector getUsers() throws java.rmi.RemoteException {
        return new Vector(users.values());
    }

    @Override
    public void registerUser(final User user) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("register user " + user);   // NOI18N
        }
        users.put(user.getRegistryKey(), user);
    }

    @Override
    public void registerUserGroup(final UserGroup userGroup) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("register userGroup " + userGroup);   // NOI18N
        }
        ugs.put(userGroup.getKey(), userGroup);
    }

    @Override
    public void registerUserGroups(final java.util.Vector userGroups) throws java.rmi.RemoteException {
        for (int i = 0; i < userGroups.size(); i++) {
            registerUserGroup((UserGroup)userGroups.get(i));
        }
    }

    @Override
    public boolean registerUserMembership(final Membership membership) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("register Membership " + membership);   // NOI18N
        }
        memberships.put(membership.getUserKey(), membership);
        return true; // unsinn
    }

    @Override
    public void registerUserMemberships(final java.util.Vector memberships) throws java.rmi.RemoteException {
        for (int i = 0; i < memberships.size(); i++) {
            registerUserMembership((Membership)memberships.get(i));
        }
    }

    @Override
    public void registerUsers(final java.util.Vector users) throws java.rmi.RemoteException {
        for (int i = 0; i < users.size(); i++) {
            registerUser((User)users.get(i));
        }
    }

    @Override
    public void unregisterUser(final User user) throws java.rmi.RemoteException {
        this.users.remove(user.getRegistryKey());

        memberships.remove(user.getRegistryKey());
    }

    @Override
    public void unregisterUserGroup(final UserGroup userGroup) throws java.rmi.RemoteException {
        this.ugs.remove(userGroup.getKey());
        // memberships
    }

    @Override
    public void unregisterUserGroups(final java.util.Vector userGroups) throws java.rmi.RemoteException {
        for (int i = 0; i < userGroups.size(); i++) {
            unregisterUserGroup((UserGroup)userGroups.get(i));
        }
    }

    @Override
    public void unregisterUsers(final java.util.Vector users) throws java.rmi.RemoteException {
        for (int i = 0; i < users.size(); i++) {
            unregisterUser((User)users.get(i));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   first   DOCUMENT ME!
     * @param   second  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String constructKey(final String first, final String second) {
        if ((first == null) || (second == null)) {
            return null;
        } else {
            return first + "@" + second;   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String constructKey(final String[] s) {
        String result = s[0];

        for (int i = 1; i < s.length; i++) {
            result += s[i];
        }

        return result;
    }
}
