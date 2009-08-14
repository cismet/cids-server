package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;
import Sirius.server.middleware.types.*;
import Sirius.server.localserver.method.*;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.newuser.*;
import Sirius.server.search.*;


/** Interface for retrieving or modifying meta information sets*/

public interface MetaService extends Remote {
    
    // retrieves a Meta data object( as Node)  referenced by a symbolic pointer to the MIS
    public Node getMetaObjectNode(User usr, int nodeID)throws RemoteException;
    
    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    public Node[] getMetaObjectNode(User usr,String query) 			throws RemoteException;
    
    
    // retrieves Meta data objects with meta data matching query (Search)
    
    public Node[] getMetaObjectNode(User usr,Query query) 			throws RemoteException;
    
       
    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    public MetaObject[] getMetaObject(User usr,String query) 			throws RemoteException;
    
    
    // retrieves Meta data objects with meta data matching query (Search)
    
    public MetaObject[] getMetaObject(User usr,Query query) 			throws RemoteException;
    
    
    /*-*-*-*-*-*-*-*-*-*-*-*-*-* MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*-*/

    /* ???
     * public MetaObject getMetaObject(User usr, String query) throws RemoteException;
     */
    
    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS
    // XXX New Method XXX
    // MetaObject ersetzt Object
    public MetaObject getMetaObject(User usr,int objectID,int classID) throws RemoteException;
    
    // inserts metaObject in the MIS
    //XXX NEW Method XXX
    public MetaObject insertMetaObject(User user, MetaObject metaObject)	throws RemoteException;
    
    // inserts metaObject in the MIS
    //XXX NEW Method XXX
    public int insertMetaObject(User user, Query query)	throws RemoteException;
    
    // updates metaObject in the MIS
    // XXX New Method XXX
    public int updateMetaObject(User user,MetaObject metaObject)	throws RemoteException;
    
    // insertion, deletion or update of meta data according to the query returns how many object's are effected
    // XXX New Method XXX
    public int update(User user,String query)	throws RemoteException;
    
    public int deleteMetaObject(User user,MetaObject metaObject) throws RemoteException;
    
    /*-*-*-*-*-*-*-*-*-*-*-*-*-* /MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*-*/
    
   // creates an Instance of a MetaObject with all attribute values set to default
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException;
    
    //---------------------------- Class retrieval----------------------------------------
    
    // MetaClass ersetzt Class
    
    // retrieves a certain class ( classification and definition ) of meta objects
    public MetaClass getClass(User user, int classID) throws RemoteException;


    public MetaClass getClassByTableName(User user, String tablename) throws RemoteException;
    
    // retrieves a certain class ( classification and definition ) of meta objects
    
    public MetaClass[] getClasses(User user) throws RemoteException;
    
    // navigation??
    public NodeReferenceList getClassTreeNodes(User user) throws RemoteException;
    
    
    
    
    // ----------------------------Method Retrieval---------------------------------------------
    
    // retrieves user services accessible from the Navigator
    // XXX muss \u00FCberarbeitet werden XXX
    public MethodMap getMethods(User user) throws RemoteException;

    // ----------------------------LightweightMetaObjects---------------------------------------------
    //---!!!
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields, String representationPattern) throws RemoteException;
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields) throws RemoteException;
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields, String representationPattern) throws RemoteException;
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields) throws RemoteException;

}