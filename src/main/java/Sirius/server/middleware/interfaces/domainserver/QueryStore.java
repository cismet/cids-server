/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;
import java.rmi.server.*;

import Sirius.server.newuser.*;
import Sirius.server.search.store.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public interface QueryStore extends java.rmi.Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean storeQuery(User user, QueryData data) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Info[] getQueryInfos(User user) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Info[] getQueryInfos(UserGroup userGroup) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    QueryData getQuery(int id) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean delete(int id) throws RemoteException;
}
