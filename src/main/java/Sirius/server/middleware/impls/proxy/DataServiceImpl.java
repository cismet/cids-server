/*
 * DataServiceImpl.java
 *
 * Created on 25. September 2003, 15:25
 */

package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;
//import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.server.dataretrieval.*;
import Sirius.server.middleware.types.*;
import Sirius.server.localserver.attribute.*;
import Sirius.server.search.*;
import Sirius.server.middleware.interfaces.domainserver.*;
import Sirius.server.newuser.*;
import com.enterprisedt.net.ftp.*;
import org.apache.log4j.*;

/**
 *
 * @author  awindholz
 */
public class DataServiceImpl  {
    
    private static Logger logger = Logger.getLogger(DataServiceImpl.class);
    // Name des Attributes der den Protokolnamen enth\u00E4lt.

    private java.util.Hashtable activeLocalServers;
    
    static {
        MetaObjectProtoMgr.register(new FTPProto());
        MetaObjectProtoMgr.register(new JDBCProto());
        JDBC_XMLProto jdbcxml = new JDBC_XMLProto();
        jdbcxml.setDeleteOnExit(true);
        MetaObjectProtoMgr.register(jdbcxml);
        JDBC_CSVProto jdbccsv = new JDBC_CSVProto();
        jdbccsv.setDeleteOnExit(true);
        MetaObjectProtoMgr.register(jdbccsv);
    }
    
    /** Creates a new instance of DataServiceImpl */
    public DataServiceImpl(java.util.Hashtable activeLocalServers)
    throws RemoteException {
        this.activeLocalServers = activeLocalServers;
    }
    
    public DataObject getDataObject(User user, MetaObject metaObject)
    throws RemoteException, DataRetrievalException {
        
        MetaObjectProto mop = MetaObjectProtoMgr.getProtocol(metaObject);
        
        return mop.getDataObject(metaObject);
    }
    
    /**
     * Noch nicht implementiert. Wenn MetaService.getMetaObject(User, Query)
     * eindeutig deffinirt ist (z.Zt liefert sie Node[], hier ohne weiteres nicht vertwendbar),
     * kann diese Fkt auch zu Ende implementiert werden.
     */
    public DataObject[] getDataObject(User user, Query query)
    throws RemoteException, DataRetrievalException {
        MetaObject metaObject;
        DataObject[] dataObject;
        
        MetaService metaService = (MetaService)activeLocalServers.get(user.getDomain());
        
        MetaObject[] mos = metaService.getMetaObject(user, query);
        dataObject = new DataObject[mos.length];
 
        for(int i = 0; i < mos.length; i++) {
            dataObject[i] = getDataObject(user, mos[i]);
        }
         return dataObject;
    }
}