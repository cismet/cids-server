/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry;

import Sirius.server.Server;
import Sirius.server.ServerExit;
import Sirius.server.ServerExitError;
import Sirius.server.ServerStatus;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.Membership;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.UserManager;
import Sirius.server.newuser.UserServer;
import Sirius.server.observ.RemoteObservable;
import Sirius.server.observ.RemoteObserver;
import Sirius.server.registry.rmplugin.RMRegistryServerImpl;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.io.Serializable;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Vector;

import de.cismet.cids.server.ServerSecurityManager;

/**
 * Name- und UserServer des gesammten Siriussystems. Jede Art von Server (LocalServer,CallServer,ProtocolServer)
 * registriert sich hier mit Namen und IPAdresse (Port optional) um f\u00FCr andere Server erreichbar zu sein. Ausserdem
 * \u00FCbernimmt die Registry eine UserServer-Funktionalit\u00E4t
 *
 * @author   Bernd Kiefer,Sascha Schlobinski, Martin Scholl
 * @version  1.0
 */
// TODO: remove sout
// TODO: are those status messages of any relevance since they're sometimes incomplete etc..
public final class Registry extends UnicastRemoteObject implements NameServer, UserServer, RemoteObservable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(Registry.class);

    private static Registry instance;

    //~ Instance fields --------------------------------------------------------

    private final transient Observable obs;
    private final transient ServerManager sm;
    private final transient UserManager um;
    private final transient ServerStatus status;
    private final transient RMRegistryServerImpl rmRegistryServer;
    private final transient int rmiRegPort;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Registry object.
     *
     * @param   port  DOCUMENT ME!
     *
     * @throws  RemoteException  Throwable DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public Registry(final int port) throws RemoteException, ServerExitError {
        obs = new Observable(this);
        sm = new ServerManager(); // xxx obs
        um = new UserManager();

        // do the rmi-stuff
        startRMIServer(port);

        // create and bind the remote messaging registry
        rmRegistryServer = new RMRegistryServerImpl();
        rmRegistryServer.startRMRegistryServer(port);

        status = new ServerStatus();
        rmiRegPort = port;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   port  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private void startRMIServer(final int port) throws ServerExitError {
        try {
            System.setSecurityManager(new ServerSecurityManager());

            java.rmi.registry.Registry reg;
            try {
                reg = LocateRegistry.createRegistry(port);
            } catch (final Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("create registry failed, trying to get existing one, port: " + port, e); // NOI18N
                }
                reg = LocateRegistry.getRegistry(port);
            }

            if (reg == null) {
                final String message = "RMI registry not present"; // NOI18N
                LOG.error(message);
                throw new IllegalStateException(message);
            }

            final String[] list = reg.list();

            if (list.length > 0) {
                System.out.println("<Reg> STATUS registerd with RMIRegistry:"); // NOI18N
            }

            final String bindInfo = "<REG> INFO: Bind SiriusRegistry on RMIRegistry as nameServer and userServer"; // NOI18N
            if (LOG.isInfoEnabled()) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < list.length; i++) {
                    sb.append('\t').append(list[i]);
                }

                LOG.info("registry listing: " + sb.toString()); // NOI18N
                LOG.info(bindInfo);
            }
            System.out.println(bindInfo);

            // binding this registry as userServer and nameServer
            Naming.bind("rmi://localhost:" + port + "/userServer", this); // NOI18N
            Naming.bind("rmi://localhost:" + port + "/nameServer", this); // NOI18N

            final String started = "<REG> ----------Sirius.Registry.Registry STARTED!!!----------\n"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(started);
            }
            System.out.println(started);
        } catch (final Exception e) {
            final String message = "could not start RMI server, port: " + port;                       // NOI18N
            LOG.fatal(message, e);
            throw new ServerExitError(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   port  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static synchronized Registry getServerInstance(final int port) throws ServerExitError {
        if (instance == null) {
            try {
                instance = new Registry(port);
            } catch (final RemoteException e) {
                final String message = "cannot create registry at port: " + port; // NOI18N
                LOG.fatal(message, e);
                throw new ServerExitError(message, e);
            }
        }

        return instance;
    }

    /**
     * Returns the actual server instance.
     *
     * @return      DOCUMENT ME!
     *
     * @throws      ServerExitError  DOCUMENT ME!
     *
     * @deprecated  THIS IS HIGLY ERROR PRONE SINCE THE IMPLEMENTATION IS NOT A SINGLETON
     */
    public static synchronized Registry getServerInstance() throws ServerExitError {
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean registerServer(final int type, final String name, final String ip) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerServer called :: type = " + type + " :: name = " + name + " :: ip = " + ip); // NOI18N
        }

        try {
            if (sm.registerServer(type, name, ip)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage(
                    NbBundle.getMessage(Registry.class, "Registry.registerServer(int,String,String).title") // NOI18N
                    ,
                    NbBundle.getMessage(
                        Registry.class,
                        "Registry.registerServer(int,String,String).message"                                // NOI18N
                        ,
                        new Object[] { name, sm.getServerCount(type), type }));
                return true;
            } else {
                return false;
            }
        } catch (final Exception e) {
            final String message = "could not register server :: type = "                                   // NOI18N
                        + type + " :: name = " + name + " :: ip = " + ip;                                   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     * @param   port  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean registerServer(final int type, final String name, final String ip, final String port)
            throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "registerServer called :: type = " // NOI18N
                        + type
                        + " :: name = "            // NOI18N
                        + name
                        + " :: ip = "              // NOI18N
                        + ip
                        + " :: port = "            // NOI18N
                        + port);
        }

        try {
            if (sm.registerServer(type, name, ip, port)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage(
                    NbBundle.getMessage(Registry.class, "Registry.registerServer(int,String,String,String).title") // NOI18N
                    ,
                    NbBundle.getMessage(
                        Registry.class,
                        "Registry.registerServer(int,String,String,String).message"                                // NOI18N
                        ,
                        new Object[] { name, sm.getServerCount(type), type }));
                return true;
            } else {
                return false;
            }
        } catch (final Exception e) {
            final String message = "could not register server :: type = "                                          // NOI18N
                        + type + " :: name = " + name + " :: ip = " + ip + " :: port = " + port;                   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean unregisterServer(final int type, final String name, final String ip) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unregisterServer called :: type = " + type + " :: name = " + name + " :: ip = " + ip); // NOI18N
        }

        try {
            if (sm.unregisterServer(type, name, ip)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage(
                    NbBundle.getMessage(Registry.class, "Registry.unregisterServer(int,String,String).title") // NOI18N
                    ,
                    NbBundle.getMessage(
                        Registry.class,
                        "Registry.unregisterServer(int,String,String).message"                                // NOI18N
                        ,
                        new Object[] { name, sm.getServerCount(type), type }));
                return true;
            } else {
                return false;
            }
        } catch (final Exception e) {
            final String message = "could not unregister server :: type = "                                   // NOI18N
                        + type + " :: name = " + name + " :: ip = " + ip;                                     // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     * @param   port  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean unregisterServer(final int type, final String name, final String ip, final String port)
            throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "unregisterServer called :: type = " // NOI18N
                        + type
                        + " :: name = "              // NOI18N
                        + name
                        + " :: ip = "                // NOI18N
                        + ip
                        + " :: port = "              // NOI18N
                        + port);
        }
        try {
            if (sm.unregisterServer(type, name, ip, port)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage(
                    NbBundle.getMessage(Registry.class, "Registry.unregisterServer(int,String,String,String).title") // NOI18N
                    ,
                    NbBundle.getMessage(
                        Registry.class,
                        "Registry.unregisterServer(int,String,String,String).message" // NOI18N
                        ,
                        new Object[] { name, sm.getServerCount(type), type }));
                return true;
            } else {
                return false;
            }
        } catch (final Exception e) {
            final String message = "could not unregister server :: type = " // NOI18N
                        + type + " :: name = " + name + " :: ip = " + ip + " :: port = " + port; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public HashMap<String, String> getServerIPs(final int type) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServerIps called :: servertype = " + type); // NOI18N
        }

        try {
            return sm.getServerIPs(type);
        } catch (final Exception e) {
            final String message = "could not get server ips for type: " + type; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public String getServerIP(final int type, final String name) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServerIp called :: servertype = " + type + " :: servername = " + name); // NOI18N
        }

        try {
            return sm.getServerIP(type, name);
        } catch (final Exception e) {
            final String message = "could not get server ip :: servertype = " + type + " :: servername = " + name; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Server[] getServers(final int type) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServers called :: servertype = " + type); // NOI18N
        }

        try {
            return sm.getServers(type);
        } catch (final Exception e) {
            final String message = "could not get servers :: servertype = " + type; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Server getServer(final int type, final String name) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServer called :: servertype = " + type + " :: servername = " + name); // NOI18N
        }

        try {
            return sm.getServer(type, name);
        } catch (final Exception e) {
            final String message = "could not get servers :: servertype = " + type + " :: servername = " + name; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUsers() throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsers called"); // NOI18N
        }

        try {
            return um.getUsers();
        } catch (final Exception e) {
            final String message = "could not get users"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLocalServerName  DOCUMENT ME!
     * @param   userGroupName             DOCUMENT ME!
     * @param   userLocalServerName       DOCUMENT ME!
     * @param   userName                  DOCUMENT ME!
     * @param   password                  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public User getUser(
            final String userGroupLocalServerName,
            final String userGroupName,
            final String userLocalServerName,
            final String userName,
            final String password) throws RemoteException, UserException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "getUser called :: userGroupLocalServerName = " // NOI18N
                        + userGroupLocalServerName
                        + " :: userGroupName = "                // NOI18N
                        + userGroupName
                        + " :: userLocalServerName = "          // NOI18N
                        + userLocalServerName
                        + " :: username = "                     // NOI18N
                        + userName
                        + " :: password = "                     // NOI18N
                        + password);
        }

        try {
            return um.getUser(userGroupLocalServerName, userGroupName, userLocalServerName, userName, password);
        } catch (final UserException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "userexception at getuser :: userGroupLocalServerName = " // NOI18N
                            + userGroupLocalServerName
                            + " :: userGroupName = "                          // NOI18N
                            + userGroupName
                            + " :: userLocalServerName = "                    // NOI18N
                            + userLocalServerName
                            + " :: username = "                               // NOI18N
                            + userName
                            + " :: password = "                               // NOI18N
                            + password,
                    e);
            }
            throw e;
        } catch (final Exception e) {
            final String message = "could not get user :: userGroupLocalServerName = " // NOI18N
                        + userGroupLocalServerName                            // NOI18N
                        + " :: userGroupName = " + userGroupName              // NOI18N
                        + " :: userLocalServerName = " + userLocalServerName  // NOI18N
                        + " :: username = " + userName                        // NOI18N
                        + " :: password = " + password;                       // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void registerUser(final User user) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUser called :: user = " + user);                           // NOI18N
        }
        try {
            um.registerUser(user);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.registerUser(User).title"), // NOI18N
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.registerUser(User).message",                                // NOI18N
                    new Object[] { user.toString() }));
        } catch (final Exception e) {
            LOG.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void unregisterUser(final User user) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unregisterUser called :: user = " + user); // NOI18N
        }

        try {
            um.unregisterUser(user);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.unregisterUser(User).title"), // NOI18N
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.unregisterUser(User).message", // NOI18N
                    new Object[] { user.toString() }));
        } catch (final Exception e) {
            final String message = "could not unregister user :: user = " + user; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   users  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void registerUsers(final Vector users) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUsers called :: users = " + users); // NOI18N
        }

        try {
            um.registerUsers(users);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.registerUsers(Vector).title"), // NOI18N
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.registerUsers(Vector).message", // NOI18N
                    new Object[] { users.toString() }));
        } catch (final Exception e) {
            final String message = "could not register users :: users = " + users; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   users  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void unregisterUsers(final Vector users) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unregisterUsers called :: users = " + users); // NOI18N
        }

        try {
            um.unregisterUsers(users);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.unregisterUsers(Vector).title") // NOI18N
                ,
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.unregisterUsers(Vector).message"                                // NOI18N
                    ,
                    new Object[] { users.toString() }));
        } catch (final Exception e) {
            final String message = "could not unregister users :: users = " + users;          // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void registerUserGroup(final UserGroup userGroup) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUserGroup called :: userGroup = " + userGroup); // NOI18N
        }

        try {
            um.registerUserGroup(userGroup);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.registerUserGroup(UserGroup).title") // NOI18N
                ,
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.registerUserGroup(UserGroup).message"                                // NOI18N
                    ,
                    new Object[] { userGroup.toString() }));
        } catch (final Exception e) {
            final String message = "could not register usergroup :: usergroup = " + userGroup;     // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void unregisterUserGroup(final UserGroup userGroup) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unregisterUserGroup called :: userGroup = " + userGroup); // NOI18N
        }

        try {
            um.unregisterUserGroup(userGroup);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.unregisterUserGroup(UserGroup).title") // NOI18N
                ,
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.unregisterUserGroup(UserGroup).message"                                // NOI18N
                    ,
                    new Object[] { userGroup.toString() }));
        } catch (Exception e) {
            final String message = "could not unregister usergroup :: usergroup = " + userGroup;     // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroups  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void registerUserGroups(final Vector userGroups) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUserGroups called :: usergroups = " + userGroups); // NOI18N
        }

        try {
            um.registerUserGroups(userGroups);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.registerUserGroups(Vector).title") // NOI18N
                ,
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.registerUserGroups(Vector).message"                                // NOI18N
                    ,
                    new Object[] { userGroups.toString() }));
        } catch (final Exception e) {
            final String message = "could not register usergroups :: usergroups = " + userGroups; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroups  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void unregisterUserGroups(final Vector userGroups) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unregisterUserGroups called :: usergroups = " + userGroups); // NOI18N
        }

        try {
            um.unregisterUserGroups(userGroups);
            status.addMessage(
                NbBundle.getMessage(Registry.class, "Registry.unregisterUserGroups(Vector).title") // NOI18N
                ,
                NbBundle.getMessage(
                    Registry.class,
                    "Registry.unregisterUserGroups(Vector).message"                                // NOI18N
                    ,
                    new Object[] { userGroups.toString() }));
        } catch (final Exception e) {
            final String message = "could not unregister usergroups :: usergroups = " + userGroups; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   membership  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean registerUserMembership(final Membership membership) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUserMembership called :: membership = " + membership); // NOI18N
        }

        try {
            return um.registerUserMembership(membership);
        } catch (final Exception e) {
            final String message = "could not register user membership ::  membership = " + membership; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   memberships  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void registerUserMemberships(final Vector memberships) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUserMemberships called :: memberships = " + memberships); // NOI18N
        }

        try {
            um.registerUserMemberships(memberships);
        } catch (final Exception e) {
            final String message = "could not register user memberships ::  memberships = " + memberships; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * Gets all registered usergroups.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroups() throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroups called"); // NOI18N
        }

        try {
            return um.getUserGroups();
        } catch (final Exception e) {
            final String message = "could not get usergroups"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * Returns a Vector with String-Arrays[2] String[0] - userName String[1] - userLocalServerName.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final User user) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroupNames called :: user = " + user); // NOI18N
        }

        try {
            return um.getUserGroupNames(user);
        } catch (final Exception e) {
            final String message = "could not get usergroup names :: user = " + user; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsName    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final String userName, final String lsName) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroupNames called :: userName = " + userName + " :: lsname = " + lsName); // NOI18N
        }

        try {
            return um.getUserGroupNames(userName, lsName);
        } catch (final Exception e) {
            final String message = "could not get usergroup names :: userName = " + userName + " :: lsname = " + lsName; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ob  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void addObserver(final RemoteObserver ob) throws RemoteException {
        try {
            obs.addObserver(ob);
        } catch (final Exception e) {
            final String message = "could not add remote observer :: observer = " + ob; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ob  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void deleteObserver(final RemoteObserver ob) throws RemoteException {
        try {
            obs.deleteObserver(ob);
        } catch (final Exception e) {
            final String message = "could not delete remote observer :: observer = " + ob; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int countObservers() throws RemoteException {
        try {
            return obs.countObservers();
        } catch (final Exception e) {
            final String message = "could not count observers"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void notifyObservers() throws RemoteException {
        try {
            obs.notifyObservers();
        } catch (final Exception e) {
            final String message = "could not notify observers"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   remote  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void notifyObservers(final Remote remote) throws RemoteException {
        try {
            obs.notifyObservers(remote);
        } catch (final Exception e) {
            final String message = "could not notify observers :: remote = " + remote; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serializable  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public void notifyObservers(final Serializable serializable) throws RemoteException {
        try {
            obs.notifyObservers(serializable);
        } catch (final Exception e) {
            final String message = "could not notify observers :: serializable = " + serializable; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean hasChanged() throws RemoteException {
        try {
            return obs.hasChanged();
        } catch (final Exception e) {
            final String message = "could not indicate hasChanged"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  ServerExit       Throwable DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public void shutdown() throws ServerExit, ServerExitError {
        try {
            // unbinding userserver
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("unbinding userserver");                                       // NOI18N
                }
                Naming.unbind("//localhost:" + rmiRegPort + "/userServer");                  // NOI18N
            } catch (final NotBoundException e) {
                LOG.warn("userserver not available (anymore), probably already unbound", e); // NOI18N
            }

            // unbinding nameserver
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("unbinding nameserver");                                       // NOI18N
                }
                Naming.unbind("//localhost:" + rmiRegPort + "/nameServer");                  // NOI18N
            } catch (final NotBoundException e) {
                LOG.warn("nameserver not available (anymore), probably already unbound", e); // NOI18N
            }

            rmRegistryServer.stopRMRegistryServer();

            // TODO: refactor server exit
            throw new ServerExit("Server ist regulär beendet worden"); // NOI18N
        } catch (final Exception e) {
            final String message = "could not shutdown registry";      // NOI18N
            LOG.fatal(message, e);
            throw new ServerExitError(message, e);
        } finally {
            instance = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerStatus getStatus() {
        return status;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static void main(final String[] args) throws ServerExitError {
        int port;

        try {
            if (args == null) {
                LOG.warn("no command line parameters, using default port: 1099");                        // NOI18N
                port = 1099;
            } else if (args.length > 0) {
                port = Integer.valueOf(args[0]);
            } else {
                port = 1099;
            }
        } catch (final NumberFormatException nfexp) {
            final String message = "invalid port argument: '" + args[0] + "', using default port: 1099"; // NOI18N
            LOG.warn(message, nfexp);
            port = 1099;
            // throw new ServerExitError(message, nfexp); // NOI18N
        }

        try {
            instance = new Registry(port);
        } catch (final Exception e) {
            final String message = "could not create registry at port: " + port; // NOI18N
            LOG.error(message, e);
            throw new ServerExitError(message, e);
        }
    }
}
