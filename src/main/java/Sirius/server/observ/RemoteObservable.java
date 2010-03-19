/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.observ;

import java.rmi.*;

import java.io.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public interface RemoteObservable extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   ob  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void addObserver(RemoteObserver ob) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   ob  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void deleteObserver(RemoteObserver ob) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int countObservers() throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void notifyObservers() throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void notifyObservers(Remote arg) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void notifyObservers(Serializable arg) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean hasChanged() throws RemoteException;
}
