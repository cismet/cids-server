/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RMServer.java
 *
 * Created on 23. November 2006, 15:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.registry.rmplugin;

import Sirius.server.ServerExit;
import Sirius.server.ServerExitError;
import Sirius.server.registry.rmplugin.exception.UnableToDeregisterException;
import Sirius.server.registry.rmplugin.exception.UnableToSendMessageException;
import Sirius.server.registry.rmplugin.exception.UserAlreadyRegisteredException;
import Sirius.server.registry.rmplugin.interfaces.RMRegistryServer;
import Sirius.server.registry.rmplugin.util.RMInfo;
import Sirius.server.registry.rmplugin.util.RMUser;

import de.cismet.rmplugin.interfaces.RMessenger;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This Class ist the implementation of the RMRegestry Server. This server provides functionality such as sending
 * messages to users, test rm object if there alive, determine online users etc.
 *
 * @author   Sebastian
 * @version  0.6
 */
public class RMRegistryServerImpl implements RMRegistryServer {

    //~ Static fields/initializers ---------------------------------------------

    /** the static logger variable. */
    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            RMRegistryServerImpl.class);

    //~ Instance fields --------------------------------------------------------

    /** the amount of active clients. */
    private Hashtable<String, Vector<RMInfo>> activeClients = new Hashtable<String, Vector<RMInfo>>();
    /** the update thread. */
    private Thread updateThread;
    /** the total count of last send messages. */
    private int total = 0;
    /** the RMI Registry. */
    private Registry reg;

    //~ Methods ----------------------------------------------------------------

    /**
     * This Constructor initializes RMI Registry with the given port. In this registry the RMRegistry is bind, which
     * enables clients to register and deregister RMPlugin instances. On the otherside it is possible to send messages
     * to active users.
     *
     * @param   port  The port which registry should be used or if no one exist where to create
     *
     * @throws  Exception  java.lang.Exception This method forwards any uncatched exception
     */
    public void startRMRegistryServer(int port) throws Exception {
        System.out.println("<RMREG> Initializing Remote Messenger Registry");
        log.info("<RMREG> Initializing Remote Messenger Registry");
        RMRegistryServer ser = (RMRegistryServer)UnicastRemoteObject.exportObject(this, port);
        reg = LocateRegistry.getRegistry(port);
        System.out.println("<RMREG> Bind RMRegistryServer on RMIRegistry as RMRegistryServer");
        log.info("<RMREG> Bind RMRegistryServer on RMIRegistry as RMRegistryServer");
        reg.rebind("RMRegistryServer", ser);
        System.out.println("<RMREG> ----------RMRegistryServer STARTED!!!----------\n");
    }

    /**
     * This method is responsible for unbinding the RMRegistry Object.
     *
     * @throws  Exception  java.lang.Exception Any Exception thrown in this method will be forwarded to the next higher
     *                     instance
     */
    public void stopRMRegistryServer() throws Exception {
        System.out.println("<RMREG> Shutting down the Remote Messenger Registry");
        log.info("<RMREG> Shutting down the Remote Messenger Registry");
        System.out.println("<RMREG> Unbind RMRegistryServer on RMIRegistry");
        log.info("<RMREG> Unbind RMRegistryServer on RMIRegistry");
        reg.unbind("RMRegistryServer");
        System.out.println("<RMREG> ----------RMRegistryServer STOPPED!!!----------\n");
    }

    /**
     * Checks if a user is availabe, available means in this context if you could send a message to this user and use
     * the other offered services.
     *
     * @param   target  The target is a string identifier build up out of three components seperated by @'s. The
     *                  identifier contains username, group and domain. for example heinz@ketschup@lecker where heinz
     *                  indicates the user, ketschup the group and lecker the domain.
     *
     * @return  return either true if the user is availabe or false if its not
     */
    public boolean available(String target) {
        if (log.isDebugEnabled()) {
            log.debug("is Adressat " + target + " verf\u00FCgbar ?");
        }
        Enumeration<String> keys = activeClients.keys();
        String targetUser = null;

        while (keys.hasMoreElements()) {
            String current = keys.nextElement();

            if (current.contains(target) && (activeClients.get(current) != null)) {
                targetUser = current;
                break;
            }
        }

        if (targetUser != null) {
            if (log.isDebugEnabled()) {
                log.debug("Adressat " + target + " verf\u00FCgbar");
            }
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Adressat " + target + " nicht verf\u00FCgbar");
            }
            return false;
        }
    }

    /**
     * Actually the same method as available(String target) but with the difference that not only if one target user is
     * online this account is available. A target is only available if the ipAddresses are equal.
     *
     * @param   target     The target is a string identifier build up out of three components seperated by @'s. The
     *                     identifier contains username, group and domain. for example heinz@ketschup@lecker where heinz
     *                     indicates the user, ketschup the group and lecker the domain.
     * @param   ipAddress  the ipAddress of the user
     *
     * @return  return either true if the user is availabe or false if its not
     */
    public boolean available(String target, String ipAddress) {
        if (log.isDebugEnabled()) {
            log.debug("is Adressat " + target + " verf\u00FCgbar ?");
        }
        Enumeration<String> keys = activeClients.keys();
        String targetUser = null;

        while (keys.hasMoreElements()) {
            String current = keys.nextElement();

            if (current.contains(target) && (activeClients.get(current) != null)) {
                Vector<RMInfo> tmp = activeClients.get(current);
                Iterator<RMInfo> it = tmp.iterator();
                while (it.hasNext()) {
                    RMInfo info = it.next();
                    if (info.getIP().equals(ipAddress)) {
                        targetUser = current;
                        break;
                    }
                }

                break;
            }
        }
        if (targetUser != null) {
            if (log.isDebugEnabled()) {
                log.debug("Adressat " + target + " verf\u00FCgbar");
            }
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Adressat " + target + " nicht verf\u00FCgbar");
            }
            return false;
        }
    }

    /**
     * This method is the heart of RMRegistry implementation and provides the main functionallity. This method sends a
     * message with a title to the given target. Target means in this context all user which contains the target string
     * complete.
     *
     * @param   target   The target is a string identifier build up out of three components seperated by @'s. The
     *                   identifier contains username, group and domain. for example heinz@ketschup@lecker where heinz
     *                   indicates the user, ketschup the group and lecker the domain.
     * @param   message  the message which should be send to the user
     * @param   title    the a short title of the message
     *
     * @return  returns the number messages actually send to users
     *
     * @throws  UnableToSendMessageException  Sirius.server.registry.rmplugin.exception.UnableToSendMessageException
     *                                        This Exception is thrown if any attemp to send a message to an user
     *                                        identified by the target string fails due to extern circumstances for
     *                                        example a remote Exception.
     */
    // Wenn ein senden fehl schl\u00E4gt dann  --> werden die restlichen nicht gesendet
    public int sendMessage(String target, String message, String title) throws UnableToSendMessageException {
        boolean flagSendProblem = false;
        Exception ex = null;
        total = 0;
        if (log.isDebugEnabled()) {
            log.debug("Nachricht senden an " + target);
        }
        Enumeration<String> keys = activeClients.keys();
        String targetUser = null;
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            if (current.contains(target)) {
                Vector<RMInfo> tmp = activeClients.get(current);
                if (tmp != null) {
                    Iterator<RMInfo> it = tmp.iterator();
                    while (it.hasNext()) {
                        RMInfo info = it.next();
                        if (info != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("RMInfo Objekt ist ungleich null");
                            }
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("RMIAddress:" + info.getRmiAddress().toString());
                                }
                                RMessenger messenger = (RMessenger)Naming.lookup(info.getRmiAddress().toString());
                                messenger.sendMessage(message, title);
                                if (log.isDebugEnabled()) {
                                    log.debug("Senden der Message erfolgreich");
                                }
                                total++;
                            } catch (Exception e) {
                                // if(total>0)total--;
                                log.error("Fehler beim \u00FCbermitteln der Nachricht an " + target, e);
                                // throw new UnableToSendMessageException("Sending Message to target "+target+" fails,
                                // one or more messages are maybe not delivered"+e.toString());
                                ex = e;
                                flagSendProblem = true;
                            }
                        }
                    }
                }
            }
        }
        if (flagSendProblem) {
            throw new UnableToSendMessageException(
                "Sending Message to target " + target
                + " fails, one or more messages are maybe not delivered\n Exception:\n" + ex.toString()
                + "\n Total sended: " + total,
                total);
        }
        return total;
    }

    /**
     * Actually the same as the Method sendMessage(String,String) but with this method it is possible to further select
     * the user via the ipAddress. The message is only send to a user exact on that maschine.
     *
     * @param   target     The target is a string identifier build up out of three components seperated by @'s. The
     *                     identifier contains username, group and domain. for example heinz@ketschup@lecker where heinz
     *                     indicates the user, ketschup the group and lecker the domain.
     * @param   ipAddress  the ipAddress to which the message should be send
     * @param   message    the message which should be send to the user
     * @param   title      the a short title of the message
     *
     * @return  returns the number messages actually send to users
     *
     * @throws  UnableToSendMessageException  Sirius.server.registry.rmplugin.exception.UnableToSendMessageException
     *                                        This Exception is thrown if any attemp to send a message to an user
     *                                        identified by the target string fails due to extern circumstances for
     *                                        example a remote Exception.
     */
    public int sendMessage(String target, String ipAddress, String message, String title)
        throws UnableToSendMessageException {
        boolean flagSendProblem = false;
        Exception ex = null;
        total = 0;
        if (log.isDebugEnabled()) {
            log.debug("Nachricht senden an " + target);
        }
        Enumeration<String> keys = activeClients.keys();
        String targetUser = null;
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            if (current.contains(target)) {
                Vector<RMInfo> tmp = activeClients.get(current);
                if (tmp != null) {
                    Iterator<RMInfo> it = tmp.iterator();
                    while (it.hasNext()) {
                        RMInfo info = it.next();
                        if ((info != null) && info.getIP().equals(ipAddress)) {
                            if (log.isDebugEnabled()) {
                                log.debug("RMInfo Objekt ist ungleich null");
                            }
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("RMIAddress:" + info.getRmiAddress().toString());
                                }
                                RMessenger messenger = (RMessenger)Naming.lookup(info.getRmiAddress().toString());
                                messenger.sendMessage(message, title);
                                if (log.isDebugEnabled()) {
                                    log.debug("Senden der Message erfolgreich");
                                }
                                total++;
                            } catch (Exception e) {
                                // if(total>0)total--;
                                log.error("Fehler beim \u00FCbermitteln der Nachricht an " + target, e);
                                ex = e;
                                flagSendProblem = true;
                            }
                        }
                    }
                }
            }
        }
        if (flagSendProblem) {
            throw new UnableToSendMessageException(
                "Sending Message to target " + target
                + " fails, one or more messages are maybe not delivered\n Exception:\n" + ex.toString()
                + "\n Total sended: " + total,
                total);
        }
        return total;
    }

    /**
     * This Method registers an RMPlugin in the RMRegistry. In this process a mapping is created between the full
     * qualified identifier (username@group@domain) and the delivered RMInfo Object which contains all necessary
     * information to connect to the RMPlugin. This information more specific the mapping will be saved in a hashmap.
     *
     * @param  info  This object contains all the information of the object who is asking for registration.
     */
    public void register(RMInfo info) {
        String key = info.getKey();
        if (log.isDebugEnabled()) {
            log.debug(key + " hat sich beim RMRegistryServer registriert");
        }
        // activeUsers.put(key,info);
        Vector<RMInfo> tmp = activeClients.get(info.getKey());
        if (tmp != null) {
            synchronized (tmp) {
                int index = tmp.indexOf(info);

                if (index != -1) {
                    tmp.remove(index);
                    tmp.add(info);
                } else {
                    tmp.add(info);
                }
            }
        } else {
            synchronized (activeClients) {
                tmp = activeClients.get(info.getKey());
                if (tmp != null) {
                    int index = tmp.indexOf(info);
                    if (index != -1) {
                        tmp.remove(index);
                        tmp.add(info);
                    } else {
                        tmp.add(info);
                    }
                } else {
                    tmp = new Vector<RMInfo>();
                    tmp.add(info);
                    activeClients.put(key, tmp);
                }
            }
        }
    }

    /**
     * This method is the pendant to the register method and deregisters a rmplugin by deleting the mapping between
     * qualified name and RMInfo object.
     *
     * @param   info  The object which should be removed from the RMRegistry
     *
     * @throws  UnableToDeregisterException  Sirius.server.registry.rmplugin.exception.UnableToDeregisterException This
     *                                       Exception is thrown if the object which should be deregister does not exist
     *                                       in the RMRegistry
     */
    public void deregister(RMInfo info) throws UnableToDeregisterException {
        String key = info.getKey();
        if (log.isDebugEnabled()) {
            log.debug(key + " versucht sich beim RMRegistryServer abzumelden");
            // activeUsers.remove(key);
        }
        Vector<RMInfo> tmp = activeClients.get(info.getKey());
        if (tmp != null) {
            synchronized (tmp) {
                int index = tmp.indexOf(info);
                if (log.isDebugEnabled()) {
                    log.debug("Index des Objektes ist:" + index);
                }

                if (index != -1) {
                    tmp.remove(index);
                    if (log.isDebugEnabled()) {
                        log.debug(key + " ist RMRegistryServer abgemeldet");
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(key + " kein solcher user im RMRegistryServer");
                    }
                    throw new UnableToDeregisterException("User is not in registry");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(key + " kein solcher user im RMRegistryServer");
            }
            throw new UnableToDeregisterException("User is not in registry");
        }
    }

//ungetestede weitergabe \u00FCberpr\u00FCft nicht ob das rmi objekt noch ansprechbar ist
    /**
     * This Method delivers all domain in which are currently user registrated.
     *
     * @return  a ArrayList which contains all domains
     */
    public ArrayList<String> getAllActiveDomains() {
        ArrayList activeDomains = new ArrayList();
        Enumeration<String> keys = activeClients.keys();
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            String currentDomain = extractDomainName(current);
            if ((currentDomain != null) && !activeDomains.contains(currentDomain)) {
                activeDomains.add(currentDomain);
            }
        }
        return activeDomains;
    }

    /**
     * This Method returns all groups within a specific domain. A group is returned if at least one member is currently
     * online
     *
     * @param   domain  Only the groups specified by this domain will be checked
     *
     * @return  an Arraylist containing the groups which at least have one active user
     */
    public ArrayList<String> getAllActiveGroups(String domain) {
        ArrayList activeGroups = new ArrayList();
        Enumeration<String> keys = activeClients.keys();
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            String currentDomain = extractDomainName(current);
            if ((currentDomain != null) && currentDomain.equals(domain)) {
                String currentGroup = extractGroupName(current);
                if ((currentGroup != null) && !activeGroups.contains(currentGroup)) {
                    activeGroups.add(currentGroup);
                }
            }
        }
        return activeGroups;
    }

    /**
     * This Method returns all active user specified by a group and a domain.
     *
     * @param   group   the group which should be checked
     * @param   domain  the domain which should be checked
     *
     * @return  an ArrayList containing all active users
     */
    public ArrayList<RMUser> getAllActiveUsers(String group, String domain) {
        ArrayList<RMUser> activeUsers = new ArrayList();
        Enumeration<String> keys = activeClients.keys();
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            String currentDomain = extractDomainName(current);
            String currentGroup = extractGroupName(current);
            if (
                ((currentDomain != null) && (currentGroup != null))
                        && (currentDomain.equals(domain) && currentGroup.equals(group))) {
                String currentUser = extractUserName(current);
                if ((currentUser != null) && !activeUsers.contains(currentUser)) {
                    Vector<RMInfo> tmp = activeClients.get(current);
                    if (tmp != null) {
                        Iterator<RMInfo> it = tmp.iterator();
                        while (it.hasNext()) {
                            RMInfo user = it.next();
                            activeUsers.add(
                                new RMUser(
                                    user.getUserName(),
                                    user.getUserGroup(),
                                    user.getUserDomain(),
                                    user.getOnlineTimeInMillis(),
                                    user.getIP()));
                        }
                    }
                }
            }
        }
        return activeUsers;
    }

    /**
     * This method returns every user registered in the RMRegistry in an ArrayList. The objects in the list are
     * containing the username and the onlineTime.
     *
     * @return  all current active User
     */
    public ArrayList<RMUser> getAllUsers() {
        ArrayList activeUsers = new ArrayList();
        Enumeration<String> keys = activeClients.keys();
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            Vector<RMInfo> tmp = activeClients.get(current);
            if (tmp != null) {
                Iterator<RMInfo> it = tmp.iterator();
                while (it.hasNext()) {
                    RMInfo user = it.next();
                    activeUsers.add(
                        new RMUser(
                            user.getUserName(),
                            user.getUserGroup(),
                            user.getUserDomain(),
                            user.getOnlineTimeInMillis(),
                            user.getIP()));
                }
            }
        }
        return activeUsers;
    }

    /**
     * this method is only for internal use and extracts the domainname out of a qualified name.
     *
     * @param   string  the qualified name wich contains a domain
     *
     * @return  the extracted domain
     */
    private String extractDomainName(String string) {
        String substring;
        try {
            int start = string.lastIndexOf("@");
            substring = string.substring(start + 1, string.length());
        } catch (Exception e) {
            log.error("Fehler beim extrahieren der Domain aus:" + string);
            return null;
        }
        return substring;
    }

    /**
     * This method is only for internal use and extracts the groupname out of a qualified name.
     *
     * @param   string  the qualified name wich contains a group
     *
     * @return  the extracted groupname
     */
    private String extractGroupName(String string) {
        String substring;
        try {
            int start = string.indexOf("@");
            int end = string.lastIndexOf("@");
            substring = string.substring(start + 1, end);
        } catch (Exception e) {
            log.error("Fehler beim extrahieren der Gruppe aus:" + string);
            return null;
        }
        return substring;
    }

    /**
     * This method is only for internal use and extracts the username out of a qualified name.
     *
     * @param   string  the qualified name wich contains a username
     *
     * @return  the extracted username
     */
    private String extractUserName(String string) {
        String substring;
        try {
            int end = string.indexOf("@");
            substring = string.substring(0, end);
        } catch (Exception e) {
            log.error("Fehler beim extrahieren der Gruppe aus:" + string);
            return null;
        }
        return substring;
    }

    /**
     * This method updates the registry that means every dead rmObject (if there is any problem by using the remote
     * methods) will be removed from the RMRegistry.
     */
    public synchronized void updateRegistry() {
        if (updateThread != null) {
            if (!updateThread.isAlive()) {
                if (log.isDebugEnabled()) {
                    log.debug("Starting update thread");
                }
                updateThread.run();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Update thread already running");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Creating new update thread");
            }
            updateThread = new Thread() {

                    public void run() {
                        update();
                    }
                };
            updateThread.run();
        }
    }

    /**
     * This method is practically called from a thread out of the updateRegistry() method.
     */
    private void update() {
        if (log.isDebugEnabled()) {
            log.debug("Updating Registry...");
        }
        Enumeration<String> keys = activeClients.keys();
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();

            Vector<RMInfo> userList = activeClients.get(current);
            if (userList != null) {
                Iterator<RMInfo> it = userList.iterator();
                while (it.hasNext()) {
                    RMInfo user = it.next();
                    if (user != null) {
                        try {
                            RMessenger messenger = (RMessenger)Naming.lookup(user.getRmiAddress().toString());
                            messenger.test();
                        } catch (Exception e) {
                            if (log.isDebugEnabled()) {
                                log.debug(
                                    "Exception during Registry Update: Entry --> " + user.getRmiAddress().toString()
                                    + " removing entry",
                                    e);
                            }
                            activeClients.remove(user.getKey());
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Updating Registry beendet");
        }
    }

    /**
     * This method logs the current content of the registry to the log4j logger in the way qualified name --> rmiAdress.
     */
    public void logCurrentRegistry() {
        if (log.isDebugEnabled()) {
            log.debug("logging Registry...");
        }
        Enumeration<String> keys = activeClients.keys();
        if (log.isDebugEnabled()) {
            log.debug("Registry:");
        }
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();

            Vector<RMInfo> userList = activeClients.get(current);
            if (userList != null) {
                Iterator<RMInfo> it = userList.iterator();
                while (it.hasNext()) {
                    RMInfo user = it.next();
                    if (user != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                current + " : " + user.getRmiAddress() + " --> online since "
                                + user.getOnlineTimeAsText());
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Logging Registry beendet");
        }
    }

    /**
     * Getter for the actual count of users online.
     *
     * @return  the count of online users
     */
    protected int getUserCount() {
        int count = 0;
        Enumeration<String> keys = activeClients.keys();
        while (keys.hasMoreElements()) {
            String current = keys.nextElement();
            Vector<RMInfo> userList = activeClients.get(current);
            if (userList != null) {
                Iterator<RMInfo> it = userList.iterator();
                while (it.hasNext()) {
                    RMInfo user = it.next();
                    if (user != null) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns how many messages are send at last invocation of sendMessage().
     *
     * @return  returns the total amount of last send messages
     */
    protected int getTotalCountOfLastSendMessages() {
        return total;
    }
}
