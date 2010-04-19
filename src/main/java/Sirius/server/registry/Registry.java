/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry;

import Sirius.server.registry.rmplugin.RMRegistryServerImpl;

import java.security.Permission;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import java.util.Vector;

import Sirius.server.naming.*;

import java.io.*;

import Sirius.server.newuser.*;
import Sirius.server.observ.*;
import Sirius.server.property.ServerProperties;
import Sirius.server.*;

import java.util.HashMap;

/**
 * Name- und UserServer des gesammten Siriussystems. Jede Art von Server (LocalServer,CallServer,ProtocolServer)
 * registriert sich hier mit Namen und IPAdresse (Port optional) um f\u00FCr andere Server erreichbar zu sein. Ausserdem
 * \u00FCbernimmt die Registry eine UserServer-Funktionalit\u00E4t
 *
 * @version  1.0
 * @autor    Bernd Kiefer,Sascha Schlobinski
 */

public class Registry extends UnicastRemoteObject implements NameServer,
    Sirius.server.newuser.UserServer,
    RemoteObservable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Registry.class);

    protected static Registry THIS;

    //~ Instance fields --------------------------------------------------------

    protected ServerProperties props;

    protected Observable obs;

    protected ServerManager sm;

    protected Sirius.server.newuser.UserManager um;

    protected int port;

    protected java.rmi.registry.Registry rmiRegistry;

    protected ServerStatus status;

    protected RMRegistryServerImpl rmRegistryServer;

    //~ Constructors -----------------------------------------------------------

    /**
     * ========================Konstruktoren============================================================= pd.
     *
     * @param   port  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public Registry(int port) throws Throwable {
        super();
        this.port = port;

        obs = new Observable(this);
        sm = new ServerManager(); // xxx obs
        um = new Sirius.server.newuser.UserManager();

        // do the rmi-stuff
        startRMIServer(port);
        rmRegistryServer = new RMRegistryServerImpl();
        rmRegistryServer.startRMRegistryServer(port);

        status = new ServerStatus();

        THIS = this;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * -----------------------------------------------------------------------------------------------------
     *
     * @param   port  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private void startRMIServer(int port) throws Throwable {
        try {
            // if(System.getSecurityManager()==null)
            System.setSecurityManager(
                new RMISecurityManager() {

                    public void checkPermission(Permission perm, Object context) {
                    }

                    @Override
                    public void checkPermission(Permission perm) {
                    }
                });

            try {
                rmiRegistry = LocateRegistry.createRegistry(port);
            } catch (Exception e) {
                rmiRegistry = LocateRegistry.getRegistry(port);
            }

            // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest
            String[] list = rmiRegistry.list();

            if (list.length > 0) {
                System.out.println("<Reg> STATUS registerd with RMIRegistry:");  // NOI18N
            }

            String l = "";  // NOI18N
            for (int i = 0; i < list.length; i++) {
                l += ("\t" + list[i]);  // NOI18N
            }

            logger.info(l);

            System.out.println("<REG> Bind SiriusRegistry on RMIRegistry as nameServer and userServer");  // NOI18N
            logger.info("<REG> Bind SiriusRegistry on RMIRegistry as nameServer and userServer");  // NOI18N

            // pd:
            Naming.bind("rmi://localhost:" + port + "/userServer", this);  // NOI18N
            Naming.bind("rmi://localhost:" + port + "/nameServer", this);  // NOI18N

            System.out.println("<REG> ----------Sirius.Registry.Registry STARTED!!!----------\n");  // NOI18N
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerExitError(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Registry getServerInstance() {
        return THIS;
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * --------------------------Methods of the Interface Nameserver-----------------------------------------------
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param   typ   DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean registerServer(int typ, String name, String ip) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerServer called for ::" + name + " ip:" + ip);  // NOI18N
        }
        try {
            if (sm.registerServer(typ, name, ip)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("New Server", name + " altogether ::" + sm.getServerCount(typ) + " Typ " + typ);  // NOI18N
                return true;
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }

        return false;
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   typ   DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     * @param   port  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean registerServer(int typ, String name, String ip, String port) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerServer called for ::" + name + " ip:" + ip);  // NOI18N
        }
        try {
            if (sm.registerServer(typ, name, ip, port)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("New Server", name + " altogether ::" + sm.getServerCount(typ) + " Typ " + typ);  // NOI18N
                return true;
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }

        return false;
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   typ   DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean unregisterServer(int typ, String name, String ip) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("unregisterServer called for ::" + name + " ip:" + ip);  // NOI18N
        }
        try {
            if (sm.unregisterServer(typ, name, ip)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("Server signed off", name + " altogether ::" + sm.getServerCount(typ) + " Typ " + typ);  // NOI18N
                return true;
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }

        return false;
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   typ   DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     * @param   ip    DOCUMENT ME!
     * @param   port  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean unregisterServer(int typ, String name, String ip, String port) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("unregisterServer called for ::" + name + " ip:" + ip);  // NOI18N
        }
        try {
            if (sm.unregisterServer(typ, name, ip, port)) {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("Server signed off", name + " altogether ::" + sm.getServerCount(typ) + " Typ " + typ);  // NOI18N
                return true;
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }

        return false;
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   typ  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public HashMap<String, String> getServerIPs(int typ) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getServerIps called for servertyp::" + typ);  // NOI18N
        }
        try {
            return sm.getServerIPs(typ);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   typ   DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public String getServerIP(int typ, String name) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getServerIp called for servertyp::" + typ + " servername:" + name);  // NOI18N
        }
        try {
            return sm.getServerIP(typ, name);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   typ  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Server[] getServers(int typ) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getServers called for servertyp::" + typ);  // NOI18N
        }
        try {
            return sm.getServers(typ);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   typ   DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Server getServer(int typ, String name) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getServer called for servertyp::" + typ + " servername:" + name);  // NOI18N
        }
        try {
            return sm.getServer(typ, name);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * --------------------------remote Methods of the Interface
     * Userserver-----------------------------------------------
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUsers() throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUsers called");  // NOI18N
        }
        try {
            return um.getUsers();
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
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
    public User getUser(
            String userGroupLocalServerName,
            String userGroupName,
            String userLocalServerName,
            String userName,
            String password) throws RemoteException, UserException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUser called for user" + userName);  // NOI18N
        }
        User user = null;
        Remote remoteServer = null;
        try {
            user = um.getUser(userGroupLocalServerName, userGroupName, userLocalServerName, userName, password);

            // ServerObjekt des LocalServer
            Server localServer = getServer(ServerType.LOCALSERVER, userLocalServerName);

            String lookupString = localServer.getRMIAddress();

            // Referenz auf LocalServer
            remoteServer = (Remote)Naming.lookup(lookupString);
        } catch (UserException e) {
            logger.error(e);
            throw e;
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }

        // xxx
        // if (!((UserService)remoteServer).validateUser(user,password))
        // {
        // logger.error("<REG> result von remoteServer.validateUser(user,password) :: false");
        // throw new UserException("<REG> wrong password for "+user,false,true,false,false);
        // }
        //

        return user;
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   user  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void registerUser(User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerUser called for user::" + user);  // NOI18N
        }
        try {
            um.registerUser(user);
            // status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ user.toString() +"\nBenutzer  System
            // insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("User added", "Groups :: " + user.toString() + "\nUser ");  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   users  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void registerUsers(Vector users) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerUsers called");  // NOI18N
        }
        try {
            um.registerUsers(users);
            // status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ users.toString() +"\nBenutzer  System
            // insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage(
                "User added",  // NOI18N
                "Groups :: " + users.toString() + "\nUsers  System altogether ::");  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   users  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void unregisterUsers(Vector users) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("unregisterUsers called");  // NOI18N
        }
        try {
            um.unregisterUsers(users);
            // status.addMessage("Benutzer entfernt","Benutzer :: "+ users.toString() +"\nBenutzer  System insgesamt
            // ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("User added", "Groups :: " + users.toString() + "\nUsers");  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void unregisterUser(User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("unregisterUser called for user::" + user);  // NOI18N
        }
        try {
            um.unregisterUser(user);
            // status.addMessage("Benutzer entfernt","Benutzer :: "+ user.toString() +"\nBenutzer implements System
            // insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("User removed", "User :: " + user.toString() + "\nUsers ");  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void registerUserGroup(UserGroup userGroup) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerUserGroup called for userGroup::" + userGroup);  // NOI18N
        }
        try {
            um.registerUserGroup(userGroup);
            // status.addMessage("Benutzergruppe hinzugef\u00FCgt","Gruppen :: "+ userGroup.toString() +"\nBenutzer
            // implements System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("User removed", "User :: " + userGroup.toString() + "\nUsers");  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   userGroups  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void registerUserGroups(Vector userGroups) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerUserGroups called");  // NOI18N
        }
        try {
            um.registerUserGroups(userGroups);

            status.addMessage("Usergroup added", "Groups :: " + userGroups.toString());  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   userGroups  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void unregisterUserGroups(Vector userGroups) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("unregisterUserGroups called");  // NOI18N
        }
        try {
            um.unregisterUserGroups(userGroups);
            // status.addMessage("Benutzergruppen entfernt","Benutzergruppen :: "+ userGroups.toString() +"\nBenutzer
            // System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Usergroups removed", "Usergroups :: " + userGroups.toString());  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void unregisterUserGroup(UserGroup userGroup) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("unregisterUserGroup called for userGroup" + userGroup);  // NOI18N
        }
        try {
            um.unregisterUserGroup(userGroup);
            status.addMessage("Usergroup removed", "Usergroups :: " + userGroup.toString());  // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   membership  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean registerUserMembership(Membership membership) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerUserMembership called for membership" + membership);  // NOI18N
        }
        try {
            return um.registerUserMembership(membership);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @param   memberships  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void registerUserMemberships(Vector memberships) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("registerUserMemberships called");  // NOI18N
        }
        try {
            um.registerUserMemberships(memberships);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * liefert alle Benutergruppen.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroups() throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUserGroups called");  // NOI18N
        }
        try {
            return um.getUserGroups();
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * liefert einen Vector mit String-Arrays[2] String[0] - userName String[1] - userLocalServerName.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroupNames(User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUserGroupNames called for user::" + user);  // NOI18N
        }
        try {
            return um.getUserGroupNames(user);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

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
    public Vector getUserGroupNames(String userName, String lsName) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUserGroupNames called for userName::" + userName);  // NOI18N
        }
        try {
            return um.getUserGroupNames(userName, lsName);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * --------------------------remote Methods of the Interface
     * RemoteObservable-----------------------------------------------
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param   ob  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void addObserver(RemoteObserver ob) throws RemoteException {
        try {
            obs.addObserver(ob);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   ob  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void deleteObserver(RemoteObserver ob) throws RemoteException {
        try {
            obs.deleteObserver(ob);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int countObservers() throws RemoteException {
        try {
            return obs.countObservers();
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void notifyObservers() throws RemoteException {
        try {
            obs.notifyObservers();
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void notifyObservers(Remote arg) throws RemoteException {
        try {
            obs.notifyObservers(arg);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void notifyObservers(Serializable arg) throws RemoteException {
        try {
            obs.notifyObservers(arg);
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean hasChanged() throws RemoteException {
        try {
            return obs.hasChanged();
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage(), e);
        }
    }
    /**
     * -----------------------------------------------------------------------------------------------------
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public void shutdown() throws Throwable {
        try {
            Naming.unbind("rmi://localhost:" + port + "/userServer");  // NOI18N
            Naming.unbind("rmi://localhost:" + port + "/nameServer");  // NOI18N
            rmRegistryServer.stopRMRegistryServer();
            rmiRegistry = null;
            System.gc();

            throw new ServerExit("Server exited regularly");  // NOI18N
        } catch (Exception e) {
            throw new ServerExitError(e);
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
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static void main(String[] args) throws Throwable {
        int port = 1099;

        try {
            if (args == null) {
                throw new ServerExitError(
                    "args == null no commandline parameter given (Configfile / port)");  // NOI18N
            } else if (args.length > 0) {
                port = Integer.valueOf(args[0]).intValue();
            }
        } catch (NumberFormatException nfexp) {
            System.err.println(nfexp.getMessage());
        }

        try {
            // Sirius.Registry.Registry siriusRegistry = new Registry();
            new Registry(port);
        } catch (Exception e) {
            logger.error(e);
            throw new ServerExitError(e);
        }
    }
} // end class registry
