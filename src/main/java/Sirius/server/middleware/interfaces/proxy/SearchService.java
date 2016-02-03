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
import java.util.HashMap;

import de.cismet.cids.server.search.CidsServerSearch;

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
    Collection customServerSearch(final User user, CidsServerSearch serverSearch) throws RemoteException;
}
