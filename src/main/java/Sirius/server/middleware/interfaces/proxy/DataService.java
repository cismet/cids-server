/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import java.rmi.*;

import Sirius.server.dataretrieval.*;
import Sirius.server.middleware.types.*;
import Sirius.server.search.*;
import Sirius.server.newuser.*;
/**
 * Interface for data retrieval.
 *
 * @version  $Revision$, $Date$
 */

public interface DataService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * retrieves data referenced by a symbolic pointer to the data source.
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    DataObject getDataObject(User user, MetaObject metaObject) throws RemoteException, DataRetrievalException;
    /**
     * retrieves dataItems with meta data matching query (Search).
     *
     * @param   user   DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    DataObject[] getDataObject(User user, Query query) throws RemoteException, DataRetrievalException;

    // retrieves data referenced by a symbolic pointer to the MIS
// DataObject getDataObject(MetaIdentifier id,...);        throws RemoteException;
}
