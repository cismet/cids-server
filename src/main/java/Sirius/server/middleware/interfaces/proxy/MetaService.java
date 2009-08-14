package Sirius.server.middleware.interfaces.proxy;

import java.rmi.*;
import Sirius.server.middleware.types.*;
import Sirius.server.localserver.method.*;
import Sirius.server.newuser.*;
import Sirius.server.search.*;


/** Interface for retrieving or modifying meta information sets*/

public interface MetaService extends Remote
{
    
    /**
     * retrieves all availlable meta data systems (Local Servers)
     * @throws java.rmi.RemoteException server error
     * @return list of all server names
     */
    public String[] getDomains()  	throws RemoteException;
    
    
    
    
    /**
     * retrieves a Meta data object( as Node)  referenced by a symbolic pointer to the MIS
     * @param usr user token
     * @param nodeID this nodes logical pointer
     * @param domain domain where the node referenced by nodeId is hosted
     * @throws java.rmi.RemoteException server error
     * @return Node representation of a meta object
     */
    public Node getMetaObjectNode(User usr, int nodeID, String domain)throws RemoteException;
    
    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    /**
     * retrieves Meta data objects with meta data matching query (Search)
     * @param usr user token
     * @param query sql query to retrieve a meta object's node representation
     * @throws java.rmi.RemoteException server error
     * @return Node representation of a meta object
     */
    public Node[] getMetaObjectNode(User usr,String query) 			throws RemoteException;
    
    
    // retrieves Meta data objects with meta data matching query (Search)
    
    /**
     * retrieves Meta data objects with meta data matching query (Search)
     * @param usr user token
     * @param query query object (search) to retrieve a meta object's node representation
     * @throws java.rmi.RemoteException server error
     * @return Node representation of a meta object
     */
    public Node[] getMetaObjectNode(User usr,Query query) 			throws RemoteException;
    
    
    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    /**
     * retrieves Meta data objects with meta data matching sql query (Search)
     * @param usr user token
     * @param query sql query
     * @throws java.rmi.RemoteException server error
     * @return array of meta objects matching the query
     */
    public MetaObject[] getMetaObject(User usr,String query) 			throws RemoteException;
    
    
    // retrieves Meta data objects with meta data matching query (Search)
    
    /**
     * retrieves Meta data objects with meta data matching query (Search)
     * @param usr user token
     * @param query Query object (search)
     * @throws java.rmi.RemoteException server error (eg bad query)
     * @return Metaobjects matching to the query
     */
    public MetaObject[]  getMetaObject(User usr,Query query) 			throws RemoteException;
    
    /*-*-*-*-*-*-*-*-*-*-*-*-*-* MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*-*/
    
    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS
    
    // MetaObject ersetzt Object
    /**
     * retrieves a Meta data object  referenced by a symbolic pointer to the MIS
     * objctId@classID@domain form this pointer
     * @param usr user token
     * @param objectID symbolic pointer to the meta object
     * @param classID class of the meta object
     * @param domain domain where the meta object is hosted
     * @throws java.rmi.RemoteException server error
     * @return the referenced meta object
     */
    public MetaObject getMetaObject(User usr,int objectID,int classID,String domain) throws RemoteException;
    
    // inserts metaObject in the MIS
    
    /**
     * inserts metaObject in the MIS
     * @param user user token
     * @param metaObject the meta object to be inserted in the MIS
     * @param domain domain that is to host the meta object
     * @throws java.rmi.RemoteException server error
     * @return inserted successfully
     */
    public MetaObject insertMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException;
    
    // inserts metaObject in the MIS
    
    /**
     * inserts metaObject in the MIS, can be used to execute SQL queries on a domainserver
     * @param user user token
     * @param query query efecting an insert (update or delete is possible but not recommended)
     * @param domain domain where the meta object is to be hosted
     * @throws java.rmi.RemoteException server error
     * @return whether the meta object was inserted successfully
     */
    public int insertMetaObject(User user, Query query, String domain) throws RemoteException;
    
    // updates metaObject in the MIS
    
    /**
     * updates metaObject in the MIS, the meta objects current attribute values will replace
     * the values in the MIS
     * @param user user token
     * @param metaObject the current state of the meta object
     * @param domain domain where meta object is hosted
     * @throws java.rmi.RemoteException server error
     * @return whether the update was successfull > 0
     */
    public int updateMetaObject(User user,MetaObject metaObject, String domain)	throws RemoteException;
    
    /**
     * deletes metaObject in the MIS
     * @param user user token
     * @param metaObject meta object to be deleted
     * @param domain domain where the object to be deleted resides
     * @throws java.rmi.RemoteException server error
     * @return succesfull if > 0
     */
    public int deleteMetaObject(User user,MetaObject metaObject, String domain) throws RemoteException;
    
    // insertion, deletion or update of meta data according to the query returns how many object's are effected
    // XXX New Method XXX
    /**
     * insertion, deletion or update of meta data according to the query returns how many object's are effected
     * @param user user token
     * @param query sql query (update, insert, delete)
     * @param domain domain where the query is to be executed
     * @throws java.rmi.RemoteException server error (eg bad sql)
     * @return how many data sets are affected
     */
    public int update(User user, String query, String domain)	throws RemoteException;
    
    // creates an Instance of a MetaObject with all attribute values set to default
    /**
     * retrieves and empty Instance (template) of a meta object of a certain meta class.
     * please note that no meta objects being attributes of this instance will be provided
     * (multi level objects). For each meta object this method has to be called
     * @param user user token
     * @param c meta class of this instance
     * @throws java.rmi.RemoteException server error
     * @return Instance (meta object) of this meta class
     */
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException;
    
    /*-*-*-*-*-*-*-*-*-*-*-*-*-* /MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*-*/
    
    
    
    //---------------------------- Class retrieval----------------------------------------
    
    // MetaClass ersetzt Class
    
    // retrieves a certain class ( classification and definition ) of meta objects
    /**
     * retrieves a certain class ( classification and definition ) of meta objects
     * referenced: classID@domain
     * @param user user token
     * @param classID id of the class to be retrieved
     * @param domain doamin where this class is hosted
     * @throws java.rmi.RemoteException server error (eg bad classID)
     * @return meta class coresponding to classID@domain
     */
    public MetaClass getClass(User user, int classID, String domain) throws RemoteException;
    



    public MetaClass getClassByTableName(User user,String tableName, String domain) throws RemoteException;


    // retrieves a certain class ( classification and definition ) of meta objects
    
    /**
     * retrieves a all classes ( classification and definition ) of meta objects of a domain
     * @param user user token
     * @param domain domain of the classes to be retrieved
     * @throws java.rmi.RemoteException server error (eg bad domain)
     * @return List of all classes availabe at this domain visible for this user
     */
    public MetaClass[] getClasses(User user, String domain) throws RemoteException;
    
    // navigation??
    /**
     * retrieves all root classes in a predifined graph (used for search categorization)
     * @param user user token
     * @throws java.rmi.RemoteException server error
     * @return class root nodes
     */
    public Node[] getClassTreeNodes(User user) throws RemoteException;
    
    /**
     * retrieves all root classes in a predifined graph (used for search categorization) of a certain doamin
     * @param user user token
     * @param domain domain where the class root nodes are hosted
     * @throws java.rmi.RemoteException server error
     * @return root class nodes of this domain
     */
    public Node[] getClassTreeNodes(User user, String domain) throws RemoteException;
    
    
    
    // ----------------------------Method Retrieval---------------------------------------------
    
    // retrieves user services accessible from the Navigator
    // XXX muss \u00FCberarbeitet werden XXX
    /**
     * retrieves acivated plugin method entries (context menu)
     * @param user user token
     * @throws java.rmi.RemoteException server error
     * @return all methods available for this user
     */
    public MethodMap getMethods(User user) throws RemoteException;
    
    /**
     * retrieves acivated plugin method entries (context menu) of a certain domain
     * @param user user token
     * @param localServerName domain
     * @throws java.rmi.RemoteException server error
     * @return all methods available for this user on this domain
     */
    public MethodMap getMethods(User user, String localServerName) throws RemoteException;

    // .........................................................................
    //---!!!
    /**
     *
     * @param classId
     * @param user
     * @param representationFields
     * @param representationPattern
     * @return
     * @throws RemoteException
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields, String representationPattern) throws RemoteException;

    /**
     * 
     * @param classId
     * @param user
     * @param representationFields
     * @param formater
     * @return
     * @throws RemoteException
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields) throws RemoteException;

    /**
     * 
     * @param classId
     * @param user
     * @param query
     * @param representationFields
     * @param representationPattern
     * @return
     * @throws RemoteException
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields, String representationPattern) throws RemoteException;

    /**
     * 
     * @param classId
     * @param user
     * @param query
     * @param representationFields
     * @param formater
     * @return
     * @throws RemoteException
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields) throws RemoteException;
    
}