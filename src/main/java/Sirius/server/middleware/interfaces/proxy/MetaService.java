/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.localserver.method.*;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
import Sirius.server.search.*;

import java.rmi.*;

/**
 * Interface for retrieving or modifying meta information sets.
 *
 * @version  $Revision$, $Date$
 */

public interface MetaService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * retrieves all availlable meta data systems (Local Servers).
     *
     * @return  list of all server names
     *
     * @throws  RemoteException  server error
     */
    String[] getDomains() throws RemoteException;

    /**
     * retrieves a Meta data object( as Node) referenced by a symbolic pointer to the MIS.
     *
     * @param   usr     user token
     * @param   nodeID  this nodes logical pointer
     * @param   domain  domain where the node referenced by nodeId is hosted
     *
     * @return  Node representation of a meta object
     *
     * @throws  RemoteException  server error
     */
    Node getMetaObjectNode(User usr, int nodeID, String domain) throws RemoteException;

    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    /**
     * retrieves Meta data objects with meta data matching query (Search).
     *
     * @param   usr    user token
     * @param   query  sql query to retrieve a meta object's node representation
     *
     * @return  Node representation of a meta object
     *
     * @throws  RemoteException  server error
     */
    Node[] getMetaObjectNode(User usr, String query) throws RemoteException;

    // retrieves Meta data objects with meta data matching query (Search)

    /**
     * retrieves Meta data objects with meta data matching query (Search).
     *
     * @param   usr    user token
     * @param   query  query object (search) to retrieve a meta object's node representation
     *
     * @return  Node representation of a meta object
     *
     * @throws  RemoteException  server error
     */
    Node[] getMetaObjectNode(User usr, Query query) throws RemoteException;

    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    /**
     * retrieves Meta data objects with meta data matching sql query (Search).
     *
     * @param   usr    user token
     * @param   query  sql query
     *
     * @return  array of meta objects matching the query
     *
     * @throws  RemoteException  server error
     */
    MetaObject[] getMetaObject(User usr, String query) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   usr     DOCUMENT ME!
     * @param   query   DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject[] getMetaObject(User usr, String query, String domain) throws RemoteException;

    // retrieves Meta data objects with meta data matching query (Search)

    /**
     * retrieves Meta data objects with meta data matching query (Search).
     *
     * @param   usr    user token
     * @param   query  Query object (search)
     *
     * @return  Metaobjects matching to the query
     *
     * @throws  RemoteException  server error (eg bad query)
     */
    MetaObject[] getMetaObject(User usr, Query query) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   usr     DOCUMENT ME!
     * @param   query   DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject[] getMetaObject(User usr, Query query, String domain) throws RemoteException;

    /*-*-*-*-*-*-*-*-*-*-*-*-*-* MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*-*/

    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS

    // MetaObject ersetzt Object
    /**
     * retrieves a Meta data object referenced by a symbolic pointer to the MIS objctId@classID@domain form this
     * pointer.
     *
     * @param   usr       user token
     * @param   objectID  symbolic pointer to the meta object
     * @param   classID   class of the meta object
     * @param   domain    domain where the meta object is hosted
     *
     * @return  the referenced meta object
     *
     * @throws  RemoteException  server error
     */
    MetaObject getMetaObject(User usr, int objectID, int classID, String domain) throws RemoteException;

    // inserts metaObject in the MIS

    /**
     * inserts metaObject in the MIS.
     *
     * @param   user        user token
     * @param   metaObject  the meta object to be inserted in the MIS
     * @param   domain      domain that is to host the meta object
     *
     * @return  inserted successfully
     *
     * @throws  RemoteException  server error
     */
    MetaObject insertMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException;

    // inserts metaObject in the MIS

    /**
     * inserts metaObject in the MIS, can be used to execute SQL queries on a domainserver.
     *
     * @param   user    user token
     * @param   query   query efecting an insert (update or delete is possible but not recommended)
     * @param   domain  domain where the meta object is to be hosted
     *
     * @return  whether the meta object was inserted successfully
     *
     * @throws  RemoteException  server error
     */
    int insertMetaObject(User user, Query query, String domain) throws RemoteException;

    // updates metaObject in the MIS

    /**
     * updates metaObject in the MIS, the meta objects current attribute values will replace the values in the MIS.
     *
     * @param   user        user token
     * @param   metaObject  the current state of the meta object
     * @param   domain      domain where meta object is hosted
     *
     * @return  whether the update was successfull (&gt; 0)
     *
     * @throws  RemoteException  server error
     */
    int updateMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException;

    /**
     * deletes metaObject in the MIS.
     *
     * @param   user        user token
     * @param   metaObject  meta object to be deleted
     * @param   domain      domain where the object to be deleted resides
     *
     * @return  succesfull if &gt; 0
     *
     * @throws  RemoteException  server error
     */
    int deleteMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException;

    // insertion, deletion or update of meta data according to the query returns how many object's are effected
    // XXX New Method XXX
    /**
     * insertion, deletion or update of meta data according to the query returns how many object's are effected.
     *
     * @param   user    user token
     * @param   query   sql query (update, insert, delete)
     * @param   domain  domain where the query is to be executed
     *
     * @return  how many data sets are affected
     *
     * @throws  RemoteException  server error (eg bad sql)
     */
    int update(User user, String query, String domain) throws RemoteException;

    // creates an Instance of a MetaObject with all attribute values set to default
    /**
     * retrieves and empty Instance (template) of a meta object of a certain meta class. please note that no meta
     * objects being attributes of this instance will be provided (multi level objects). For each meta object this
     * method has to be called
     *
     * @param   user  user token
     * @param   c     meta class of this instance
     *
     * @return  Instance (meta object) of this meta class
     *
     * @throws  RemoteException  server error
     */
    MetaObject getInstance(User user, MetaClass c) throws RemoteException;

    /*-*-*-*-*-*-*-*-*-*-*-*-*-* /MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*-*/

    // ---------------------------- Class retrieval----------------------------------------

    // MetaClass ersetzt Class

    // retrieves a certain class ( classification and definition ) of meta objects
    /**
     * retrieves a certain class ( classification and definition ) of meta objects referenced: classID@domain
     *
     * @param   user     user token
     * @param   classID  id of the class to be retrieved
     * @param   domain   doamin where this class is hosted
     *
     * @return  meta class coresponding to classID@domain
     *
     * @throws  RemoteException  server error (eg bad classID)
     */
    MetaClass getClass(User user, int classID, String domain) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user       DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaClass getClassByTableName(User user, String tableName, String domain) throws RemoteException;

    // retrieves a certain class ( classification and definition ) of meta objects

    /**
     * retrieves a all classes ( classification and definition ) of meta objects of a domain.
     *
     * @param   user    user token
     * @param   domain  domain of the classes to be retrieved
     *
     * @return  List of all classes availabe at this domain visible for this user
     *
     * @throws  RemoteException  server error (eg bad domain)
     */
    MetaClass[] getClasses(User user, String domain) throws RemoteException;

    // navigation??
    /**
     * retrieves all root classes in a predifined graph (used for search categorization).
     *
     * @param   user  user token
     *
     * @return  class root nodes
     *
     * @throws  RemoteException  server error
     */
    Node[] getClassTreeNodes(User user) throws RemoteException;

    /**
     * retrieves all root classes in a predifined graph (used for search categorization) of a certain doamin.
     *
     * @param   user    user token
     * @param   domain  domain where the class root nodes are hosted
     *
     * @return  root class nodes of this domain
     *
     * @throws  RemoteException  server error
     */
    Node[] getClassTreeNodes(User user, String domain) throws RemoteException;

    // ----------------------------Method Retrieval---------------------------------------------

    // retrieves user services accessible from the Navigator
    // XXX muss \u00FCberarbeitet werden XXX
    /**
     * retrieves acivated plugin method entries (context menu).
     *
     * @param   user  user token
     *
     * @return  all methods available for this user
     *
     * @throws  RemoteException  server error
     */
    MethodMap getMethods(User user) throws RemoteException;

    /**
     * retrieves acivated plugin method entries (context menu) of a certain domain.
     *
     * @param   user             user token
     * @param   localServerName  domain
     *
     * @return  all methods available for this user on this domain
     *
     * @throws  RemoteException  server error
     */
    MethodMap getMethods(User user, String localServerName) throws RemoteException;

    // .........................................................................
    // ---!!!
    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields,
            String representationPattern) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields)
            throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields,
            String representationPattern) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields) throws RemoteException;

    /**
     * Returns the history of the given object of the given class. The number of historic elements that will be
     * retrieved depends on the given element count and the amount of available historic elements. Resolution strategy:
     *
     * <ul>
     *   <li>elements &lt; 1: order by timestamp</li>
     *   <li>elements &gt; 0: order by timestamp limit <code>elements</code></li>
     * </ul>
     *
     * @param   classId   the id of the desired class
     * @param   objectId  the id of the object of the desired class
     * @param   domain    the name of the domain the desired class belongs to
     * @param   user      the user that requests the history
     * @param   elements  the number of historic elements to be retrieved or an int &lt; 1 to retrieve all available
     *                    elements
     *
     * @return  the historic objects
     *
     * @throws  RemoteException  if any error occurs
     */
    HistoryObject[] getHistory(int classId, int objectId, String domain, User user, int elements)
            throws RemoteException;
}
