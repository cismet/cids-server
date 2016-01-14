/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.search.Query;
import Sirius.server.sql.PreparableStatement;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.ArrayList;

import de.cismet.cids.server.search.QueryPostProcessor;

/**
 * Interface for retrieving or modifying meta information sets.
 *
 * @version  $Revision$, $Date$
 */

public interface MetaService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * retrieves a Meta data object( as Node) referenced by a symbolic pointer to the MIS.
     *
     * @param   usr     DOCUMENT ME!
     * @param   nodeID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Node getMetaObjectNode(User usr, int nodeID) throws RemoteException;
    /**
     * retrieves Meta data objects with meta data matching query (Search) Query not yet defined but will be MetaSQL.
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Node[] getMetaObjectNode(User usr, String query) throws RemoteException;

    /**
     * retrieves Meta data objects with meta data matching query (Search).
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Node[] getMetaObjectNode(User usr, Query query) throws RemoteException;
    /**
     * retrieves Meta data objects with meta data matching query (Search) Query not yet defined but will be MetaSQL.
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject[] getMetaObject(User usr, String query) throws RemoteException;

    /**
     * retrieves Meta data objects with meta data matching query (Search).
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject[] getMetaObject(User usr, Query query) throws RemoteException;
    /**
     * -*-*-*-*-*-*-*-*-*-*-*-*-* MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*- ??? public MetaObject getMetaObject(User usr,
     * String query) throws RemoteException; retrieves a Meta data object referenced by a symbolic pointer to the MIS
     * XXX New Method XXX MetaObject ersetzt Object
     *
     * @param   usr       DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     * @param   classID   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject getMetaObject(User usr, int objectID, int classID) throws RemoteException;
    /**
     * inserts metaObject in the MIS XXX NEW Method XXX.
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject insertMetaObject(User user, MetaObject metaObject) throws RemoteException;
    /**
     * inserts metaObject in the MIS XXX NEW Method XXX.
     *
     * @param   user   DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int insertMetaObject(User user, Query query) throws RemoteException;
    /**
     * updates metaObject in the MIS XXX New Method XXX.
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int updateMetaObject(User user, MetaObject metaObject) throws RemoteException;
    /**
     * insertion, deletion or update of meta data according to the query returns how many object's are effected XXX New
     * Method XXX.
     *
     * @param   user   DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int update(User user, String query) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int deleteMetaObject(User user, MetaObject metaObject) throws RemoteException;
    /**
     * -*-*-*-*-*-*-*-*-*-*-*-*-* /MetaJDBC Zeugs *-*-*-*-*-*-*-*-*-*-*-*- creates an Instance of a MetaObject with all
     * attribute values set to default.
     *
     * @param   user  DOCUMENT ME!
     * @param   c     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaObject getInstance(User user, MetaClass c) throws RemoteException;
    /**
     * ---------------------------- Class retrieval---------------------------------------- MetaClass ersetzt Class
     * retrieves a certain class ( classification and definition ) of meta objects.
     *
     * @param   user     DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaClass getClass(User user, int classID) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user       DOCUMENT ME!
     * @param   tablename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaClass getClassByTableName(User user, String tablename) throws RemoteException;

    /**
     * retrieves a certain class ( classification and definition ) of meta objects.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MetaClass[] getClasses(User user) throws RemoteException;
    /**
     * navigation??
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    NodeReferenceList getClassTreeNodes(User user) throws RemoteException;
    /**
     * ----------------------------Method Retrieval--------------------------------------------- retrieves user services
     * accessible from the Navigator XXX muss \u00FCberarbeitet werden XXX.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    MethodMap getMethods(User user) throws RemoteException;
    /**
     * ----------------------------LightweightMetaObjects--------------------------------------------- ---!!!
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
     * DOCUMENT ME!
     *
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<ArrayList> performCustomSearch(String query) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   query  DOCUMENT ME!
     * @param   qpp    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<ArrayList> performCustomSearch(String query, QueryPostProcessor qpp) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   ps  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<ArrayList> performCustomSearch(PreparableStatement ps) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   ps   DOCUMENT ME!
     * @param   qpp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    ArrayList<ArrayList> performCustomSearch(PreparableStatement ps, QueryPostProcessor qpp) throws RemoteException;

    /**
     * @see  Sirius.server.middleware.interfaces.proxy.MetaService#getHistory(int, int, java.lang.String,
     *       Sirius.server.newuser.User, int)
     */
    HistoryObject[] getHistory(int classId, int objectId, User user, int elements) throws RemoteException;
}
