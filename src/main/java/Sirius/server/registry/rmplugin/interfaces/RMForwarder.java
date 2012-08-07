/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RMForwarder.java
 *
 * Created on 23. November 2006, 16:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.registry.rmplugin.interfaces;

import Sirius.server.registry.rmplugin.exception.UnableToSendMessageException;
import Sirius.server.registry.rmplugin.util.RMUser;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Iterator;
/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public interface RMForwarder extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is the heart of RMRegistry implementation and represents the main functionallity. This method sends a
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
     * @throws  RemoteException               Sirius.server.registry.rmplugin.exception.UnableToSendMessageException
     *                                        This Exception is thrown if any attemp to send a message to an user
     *                                        identified by the target string fails due to extern circumstances for
     *                                        example a remote Exception.
     * @throws  UnableToSendMessageException  DOCUMENT ME!
     */
    int sendMessage(String target, String message, String title) throws RemoteException, UnableToSendMessageException;

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
     * @throws  RemoteException               Sirius.server.registry.rmplugin.exception.UnableToSendMessageException
     *                                        This Exception is thrown if any attemp to send a message to an user
     *                                        identified by the target string fails due to extern circumstances for
     *                                        example a remote Exception.
     * @throws  UnableToSendMessageException  DOCUMENT ME!
     */
    int sendMessage(String target, String ipAddress, String message, String title) throws RemoteException,
        UnableToSendMessageException;

    /**
     * Checks if a user is availabe, available means in this context if you could send a message to this user and use
     * the other offered services.
     *
     * @param   target  The target is a string identifier build up out of three components seperated by @'s. The
     *                  identifier contains username, group and domain. for example heinz@ketschup@lecker where heinz
     *                  indicates the user, ketschup the group and lecker the domain.
     *
     * @return  return either true if the user is availabe or false if its not
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean available(String target) throws RemoteException;

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
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean available(String target, String ipAddress) throws RemoteException;

    /**
     * This method updates the registry that means every dead rmObject (if there is any problem by using the remote
     * methods) will be removed from the RMRegistry.
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void updateRegistry() throws RemoteException;

    /**
     * This method logs the current content of the registry to the log4j logger in the way qualified name --> rmiAdress.
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void logCurrentRegistry() throws RemoteException;

    /**
     * This Method delivers all domain in which are currently user registrated.
     *
     * @return  a ArrayList which contains all domains
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<String> getAllActiveDomains() throws RemoteException;

    /**
     * This Method returns all groups within a specific domain. A group is returned if at least one member is currently
     * online
     *
     * @param   domain  Only the groups specified by this domain will be checked
     *
     * @return  an Arraylist containing the groups which at least have one active user
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<String> getAllActiveGroups(String domain) throws RemoteException;

    /**
     * This Method returns all active user specified by a group and a domain.
     *
     * @param   group   the group which should be checked
     * @param   domain  the domain which should be checked
     *
     * @return  an ArrayList containing all active users
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<RMUser> getAllActiveUsers(String group, String domain) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<RMUser> getAllUsers() throws RemoteException;
}
