/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.newuser.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Collection;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * encapsulates query, search mechanisms.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface SearchService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user          DOCUMENT ME!
     * @param   serverSearch  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    Collection customServerSearch(final User user, CidsServerSearch serverSearch) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user          DOCUMENT ME!
     * @param   serverSearch  DOCUMENT ME!
     * @param   context       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Collection customServerSearch(final User user, CidsServerSearch serverSearch, final ConnectionContext context)
            throws RemoteException;
}
