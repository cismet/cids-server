/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;

import Sirius.util.image.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */

public interface SystemService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Image[] getDefaultIcons() throws RemoteException;
}
