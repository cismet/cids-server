package Sirius.server.middleware.interfaces.proxy;

import java.rmi.*;

import Sirius.server.dataretrieval.*;
import Sirius.server.middleware.types.*;
import Sirius.server.search.*;
import Sirius.server.newuser.*;
/** Interface for data retrieval*/

public interface DataService extends Remote {

	// retrieves data referenced by a symbolic pointer to the data source
    DataObject getDataObject(User user, MetaObject metaObject) throws RemoteException, DataRetrievalException;

    // retrieves dataItems with meta data matching query (Search)
    DataObject[] getDataObject(User user, Query query) throws RemoteException, DataRetrievalException;

	// retrieves data referenced by a symbolic pointer to the MIS
//	DataObject getDataObject(MetaIdentifier id,...); 	throws RemoteException;
}