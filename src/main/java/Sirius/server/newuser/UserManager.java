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

    public User getUser(String userGroupDomain, String userGroup, String userDomain, String user, String password)
        throws java.rmi.RemoteException, UserException {
        User u = (User)users.get(constructKey(user, userDomain));
        if (logger.isDebugEnabled()) {
            logger.debug("user found :: " + u + " for " + user + " " + userDomain + "user available " + users);
        }

        // check ob membership
        if (u == null) {
            throw new UserException(
                "UserException::no User::" + user + "," + userDomain + "," + userGroup + "," + userGroupDomain
                + " registered",
                true,
                false,
                false,
                false);
        } else if (!memberships.containsKey(u.getRegistryKey())) {
            throw new UserException(
                "UserException::no User::" + user + "," + userDomain + "," + userGroup + "," + userGroupDomain
                + " registered",
                true,
                false,
                false,
                false);
        } else // es ist mindestens eine Membership eingetragen
        {
            List l = (List)memberships.get(u.getRegistryKey());

            for (int i = 0; i < l.size(); i++) {
                Membership m = (Membership)l.get(i);

                if (m.getUgDomain().equalsIgnoreCase(userGroupDomain) && m.getUg().equalsIgnoreCase(userGroup)) {
                    break;
                }

                if (i == (l.size() - 1)) // last element and no break
                {
                    throw new UserException(
                        "UserException::no UserGroup::" + userGroup + "," + userGroupDomain,
                        false,
                        false,
                        true,
                        false);
                }
            }
        }

        UserGroup ug = (UserGroup)ugs.get(constructKey(userGroup, userGroupDomain));

        u.setUserGroup(ug);

        return u;
    }

    public java.util.Vector getUserGroupNames(User user) throws java.rmi.RemoteException {
        if (user == null) { // throw new java.rmi.RemoteException("UserException::no Usergroup for ::"+userName + "
                            // -- "+domain);
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no  user " + user);
                }
            }
            return new Vector(0);
        }
        return getUserGroupNames(user.getName(), user.getDomain());
    }

    public java.util.Vector getUserGroupNames(String userName, String domain) throws java.rmi.RemoteException {
        if ((userName == null) || (domain == null)) { // throw new java.rmi.RemoteException("UserException::no
                                                      // Usergroup for ::"+userName + " -- "+domain);
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no  user " + userName + "or no domain " + domain);
                }
            }
            return new Vector(0);
        }

        List l = (List)memberships.get(constructKey(userName, domain));

        if (l == null) { // throw new java.rmi.RemoteException("UserException::no Usergroup for ::"+userName + " --
                         // "+domain);
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no usergroup for user " + userName);
                }
            }
            return new Vector(0);
        }

        Vector result = new Vector(l.size());

        for (int i = 0; i < l.size(); i++) {
            Membership ug = (Membership)l.get(i);

            if ((ug != null) && ug.getUgDomain().equalsIgnoreCase(ug.getUserDomain())) // fremde Nutzerrechte werden
                                                                                       // nicht in der Auswahl
                                                                                       // angeboten
            {
                String[] userInfo = new String[2];

                userInfo[0] = ug.getUg();
                userInfo[1] = ug.getUgDomain();

                result.add(userInfo);
            }
        }

        return result;
    }

    public java.util.Vector getUserGroups() throws java.rmi.RemoteException {
        return new Vector(ugs.values());
    }

    public java.util.Vector getUsers() throws java.rmi.RemoteException {
        return new Vector(users.values());
    }

    public void registerUser(User user) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("register user " + user);
        }
        users.put(user.getRegistryKey(), user);
    }

    public void registerUserGroup(UserGroup userGroup) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("register userGroup " + userGroup);
        }
        ugs.put(userGroup.getKey(), userGroup);
    }

    public void registerUserGroups(java.util.Vector userGroups) throws java.rmi.RemoteException {
        for (int i = 0; i < userGroups.size(); i++) {
            registerUserGroup((UserGroup)userGroups.get(i));
        }
    }

    public boolean registerUserMembership(Membership membership) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("register Membership " + membership);
        }
        memberships.put(membership.getUserKey(), membership);
        return true; // unsinn
    }

    public void registerUserMemberships(java.util.Vector memberships) throws java.rmi.RemoteException {
        for (int i = 0; i < memberships.size(); i++) {
            registerUserMembership((Membership)memberships.get(i));
        }
    }

    public void registerUsers(java.util.Vector users) throws java.rmi.RemoteException {
        for (int i = 0; i < users.size(); i++) {
            registerUser((User)users.get(i));
        }
    }

    public void unregisterUser(User user) throws java.rmi.RemoteException {
        this.users.remove(user.getRegistryKey());

        memberships.remove(user.getRegistryKey());
    }

    public void unregisterUserGroup(UserGroup userGroup) throws java.rmi.RemoteException {
        this.ugs.remove(userGroup.getKey());
        // memberships
    }

    public void unregisterUserGroups(java.util.Vector userGroups) throws java.rmi.RemoteException {
        for (int i = 0; i < userGroups.size(); i++) {
            unregisterUserGroup((UserGroup)userGroups.get(i));
        }
    }

    public void unregisterUsers(java.util.Vector users) throws java.rmi.RemoteException {
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
    private String constructKey(String first, String second) {
        if ((first == null) || (second == null)) {
            return null;
        } else {
            return first + "@" + second;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String constructKey(String[] s) {
        String result = s[0];

        for (int i = 1; i < s.length; i++) {
            result += s[i];
        }

        return result;
    }
}
