/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.util.image.*;
import de.cismet.connectioncontext.ConnectionContext;

import java.rmi.*;

/**
 * gives access to system parameters, resources and configurations of the MIS and its domain servers.
 *
 * @version  $Revision$, $Date$
 */

public interface SystemService extends Remote {

    //~ Methods ----------------------------------------------------------------

    // public Server getTranslationServer() throws RemoteException;

    /**
     * delivers default icons of a certain domain (resources of this domain) used to initialize a client (navigator).
     *
     * @param   lsName  domain
     *
     * @return  default icons as images of a certain domain
     *
     * @throws  RemoteException  server error
     */
    @Deprecated
    Image[] getDefaultIcons(String lsName) throws RemoteException;

    Image[] getDefaultIcons(String lsName, ConnectionContext connectionContext) throws RemoteException;
    
    /**
     * delivers default icons of a random domain used to initialize a client (navigator).
     *
     * @return  list of default icons (images)
     *
     * @throws  RemoteException  server error (eg no domain server online)
     */
    @Deprecated
    Image[] getDefaultIcons() throws RemoteException;

    Image[] getDefaultIcons(ConnectionContext connectionContext) throws RemoteException;
    
    // public Sirius.Server.Server getTranslationServer() throws RemoteException;

}
