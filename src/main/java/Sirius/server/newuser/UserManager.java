/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser;

import Sirius.util.collections.MultiMap;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class UserManager implements UserServer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserManager.class);

    //~ Instance fields --------------------------------------------------------

    protected Hashtable users;

//    protected Hashtable ugs;

    protected HashMap<String, HashMap<Object, UserGroup>> usergroupsOfUserDomains = new HashMap(10);

    protected MultiMap memberships;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserManager object.
     */
    public UserManager() {
        this.users = new Hashtable();
//        this.ugs = new Hashtable();
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("user found :: " + u + " for " + user + "@" + userDomain + " :: users available " + users); // NOI18N
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
            final List<Membership> l = (List)memberships.get(u.getRegistryKey());
            if (userGroup != null) {
                for (int i = 0; i < l.size(); i++) {
                    final Membership m = (Membership)l.get(i);

                    if (m.getUgDomain().equals(userGroupDomain) && m.getUg().equals(userGroup)) {
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
                // userDomain;
                final UserGroup ug = usergroupsOfUserDomains.get(userDomain)
                            .get(constructKey(userGroup, userGroupDomain));
                // final UserGroup ug = (UserGroup)ugs.get(constructKey(userGroup, userGroupDomain));
                u.setUserGroup(ug);
            } else {
                final Vector<String[]> ugInfos = getUserGroupNames(u);
                final List<UserGroup> ugList = new ArrayList<UserGroup>(ugInfos.size());
                for (final String[] ugInfo : ugInfos) {
                    final UserGroup ug = usergroupsOfUserDomains.get(userDomain)
                                .get(constructKey(ugInfo[0], ugInfo[1]));
                    // final UserGroup ug = (UserGroup)ugs.get(constructKey(ugInfo[0], ugInfo[1]));
                    if (ug != null) {
                        ugList.add(ug);
                    }
                }
                Collections.sort(ugList, new Comparator<UserGroup>() {

                        @Override
                        public int compare(final UserGroup ug1, final UserGroup ug2) {
                            final int prio1 = (ug1 == null) ? Integer.MAX_VALUE : ug1.getPrio();
                            final int prio2 = (ug2 == null) ? Integer.MAX_VALUE : ug2.getPrio();
                            return new Integer(prio1).compareTo(new Integer(prio2));
                        }
                    });
                u.setPotentialUserGroups(ugList);
                u.setUserGroup(null);
            }
        }

        return u;
    }

    @Override
    public Vector getUserGroupNames(final User user) throws RemoteException {
        if (user == null) {
            if (LOG != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no  user " + user); // NOI18N
                }
            }

            return new Vector(0);
        }

        return getUserGroupNames(user.getName(), user.getDomain());
    }

    @Override
    public Vector getUserGroupNames(final String userName, final String domain) throws java.rmi.RemoteException {
        if ((userName == null) || (domain == null)) {
            if (LOG != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no  user " + userName + "or no domain " + domain); // NOI18N
                }
            }

            return new Vector(0);
        }

        final List l = (List)memberships.get(constructKey(userName, domain));

        if (l == null) {
            if (LOG != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no usergroup for user " + userName); // NOI18N
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
    public Vector getUserGroups() throws RemoteException {
        final ArrayList<UserGroup> homeGroups = new ArrayList<>();
        final HashMap<Object, UserGroup> allGroups = new HashMap<Object, UserGroup>();

        for (final String homeDomain : usergroupsOfUserDomains.keySet()) {
            for (final UserGroup ug : usergroupsOfUserDomains.get(homeDomain).values()) {
                allGroups.put(ug.constructKey(ug), ug);
                if (ug.domain == homeDomain) {
                    homeGroups.add(ug);
                }
            }
        }

        for (final UserGroup ug : homeGroups) {
            allGroups.put(ug.constructKey(ug), ug);
        }

        return new Vector(allGroups.values());
    }

    @Override
    public Vector getUsers() throws RemoteException {
        return new Vector(users.values());
    }

    @Override
    public void registerUser(final User user) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("register user " + user); // NOI18N
        }
        users.put(user.getRegistryKey(), user);
    }

    @Override
    public void registerUserGroup(final String localServerName, final UserGroup userGroup) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("register userGroup " + userGroup); // NOI18N
        }
        System.out.println("4" + localServerName + " " + userGroup.getKey() + "->" + userGroup.id);
        usergroupsOfUserDomains.get(localServerName).put(userGroup.getKey(), userGroup);
//        ugs.put(userGroup.getKey(), userGroup);
    }

    @Override
    public void registerUserGroups(final String localServerName, final Vector userGroups) throws RemoteException {
        usergroupsOfUserDomains.put(localServerName, new HashMap<Object, UserGroup>(userGroups.capacity()));
        for (int i = 0; i < userGroups.size(); i++) {
            registerUserGroup(localServerName, (UserGroup)userGroups.get(i));
        }
    }

    @Override
    public boolean registerUserMembership(final Membership membership) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("register Membership " + membership); // NOI18N
        }
        memberships.put(membership.getUserKey(), membership);
        return true;                                        // unsinn
    }

    @Override
    public void registerUserMemberships(final Vector memberships) throws RemoteException {
        for (int i = 0; i < memberships.size(); i++) {
            registerUserMembership((Membership)memberships.get(i));
        }
    }

    @Override
    public void registerUsers(final Vector users) throws RemoteException {
        for (int i = 0; i < users.size(); i++) {
            registerUser((User)users.get(i));
        }
    }

    @Override
    public void unregisterUser(final User user) throws RemoteException {
        this.users.remove(user.getRegistryKey());

        memberships.remove(user.getRegistryKey());
    }

    @Override
    public void unregisterUserGroup(final String localServerName, final UserGroup userGroup) throws RemoteException {
        usergroupsOfUserDomains.get(localServerName).remove(userGroup.getKey());

//        this.ugs.remove(userGroup.getKey());
        // memberships
    }

    @Override
    public void unregisterUserGroups(final String localServerName, final Vector userGroups) throws RemoteException {
        for (int i = 0; i < userGroups.size(); i++) {
            unregisterUserGroup(localServerName, (UserGroup)userGroups.get(i));
        }
    }

    @Override
    public void unregisterUsers(final java.util.Vector users) throws RemoteException {
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
            return first + "@" + second; // NOI18N
        }
    }
}
