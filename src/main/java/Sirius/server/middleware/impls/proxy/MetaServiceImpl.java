/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MetaServiceImpl.java
 *
 * Created on 25. September 2003, 10:42
 */
package Sirius.server.middleware.impls.proxy;

import java.rmi.*;

import Sirius.server.newuser.*;
import Sirius.server.*;
import Sirius.server.naming.NameServer;
import Sirius.server.middleware.types.Node;
import Sirius.server.localserver.method.*;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.MetaObject;
//import Sirius.middleware.interfaces.domainserver.*;
import Sirius.server.newuser.permission.Policy;
import Sirius.server.search.*;

import Sirius.util.NodeComparator;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class MetaServiceImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private java.util.Hashtable activeLocalServers;
    private NameServer nameServer;
    private Server[] localServers;
    // resolves Query tree
    private QueryExecuter qex;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MetaServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaServiceImpl(
            java.util.Hashtable activeLocalServers,
            NameServer nameServer /*,
                                   *Sirius.Server.Server[] localServers*/) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
        // this.localServers = localServers;
        this.localServers = nameServer.getServers(ServerType.LOCALSERVER);
        qex = new QueryExecuter(activeLocalServers);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaClass getClass(User user, int classID, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("getClass ::" + classID + " user::" + user + " domain::" + domain);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain)).getClass(
                user,
                classID);
    }

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
    public MetaClass getClassByTableName(User user, String tableName, String domain) throws RemoteException {
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .getClassByTableName(user, tableName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaClass[] getClasses(User user, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("getClasses for User : " + user);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .getClasses(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node[] getClassTreeNodes(User user) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getClassTreeNodes for user" + user);   // NOI18N
            }
        }

        java.util.Vector ctns = new java.util.Vector(10, 10);
        java.util.Iterator iter = activeLocalServers.values().iterator();
        Node[] classNodes = new Node[0];

        int size = 0;
        try {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<CS> iter: " + iter);   // NOI18N
                }
            }

            while (iter.hasNext()) {
                try {
                    NodeReferenceList children = (NodeReferenceList)
                        ((Sirius.server.middleware.interfaces.domainserver.MetaService)iter.next()).getClassTreeNodes(
                            user);

                    if ((children != null) && (children.getLocalNodes() != null)) {
                        Node[] tmp = children.getLocalNodes();
                        if (logger.isDebugEnabled()) {
                            logger.debug("<CS> found valid localserver delivers topnodes ::" + tmp.length);   // NOI18N
                        }
                        size += tmp.length;
                        ctns.addElement(tmp);
                    }
                } catch (Exception e) {
                    logger.error("<CS> getTopNodes(user) of a domainserver:", e);   // NOI18N
                }
            }
            classNodes = new Node[size];

            for (int i = 0; i < ctns.size(); i++) {
                Node[] tmp = (Node[])ctns.get(i);

                for (int j = 0; j < tmp.length; j++) {
                    --size;
                    classNodes[size] = tmp[j]; // wird von hinten nach vorne belegt
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("<CS> getTopNodes(user):", e);   // NOI18N
            }
            throw new RemoteException("<CS> getTopNodes(user)", e);   // NOI18N
        }

        java.util.Arrays.sort(classNodes, new NodeComparator());
        return classNodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node[] getClassTreeNodes(User user, String localServerName) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getClassTreeNode for user" + user);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(localServerName))
                    .getClassTreeNodes(user).getLocalNodes();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public String[] getDomains() throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getDomains called ");   // NOI18N
            }
        }
        Server[] ls = nameServer.getServers(ServerType.LOCALSERVER);

        validateLocalServers(ls);

        String[] lsnames = new String[ls.length];

        for (int i = 0; i < ls.length; i++) {
            lsnames[i] = ls[i].getName();
        }

        return lsnames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr     DOCUMENT ME!
     * @param   nodeID  DOCUMENT ME!
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node getMetaObjectNode(User usr, int nodeID, String lsName) throws RemoteException {
        // usr wird nicht beachtet fuer spaetere anpassungen

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObjectNode for user" + usr + "node ::" + nodeID + " domain" + lsName);   // NOI18N
            }
        }
        java.lang.Object name = activeLocalServers.get(lsName);
        Node n = null;
        int[] ids = new int[1];
        ids[0] = nodeID;

        if (name != null) {
            n = ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)name).getNodes(usr, ids)[0];
        } else {
            Node error = new MetaNode(
                    ids[0],
                    lsName,
                    lsName + " not available!",   // NOI18N
                    lsName + " not available!",   // NOI18N
                    true,
                    Policy.createWIKIPolicy(),
                    -1,
                    null,
                    false,
                    -1);
            error.validate(false);
            n = error;
        }

        return n;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node[] getMetaObjectNode(User usr, String query) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObjectNode for user" + usr + "queryString ::" + query);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(usr.getDomain()))
                    .getMetaObjectNode(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node[] getMetaObjectNode(User usr, Query query) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObjectNode for user" + usr + "query ::" + query);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(usr.getDomain()))
                    .getMetaObjectNode(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject[] getMetaObject(User usr, String query) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObject for user" + usr + "queryString ::" + query);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(usr.getDomain()))
                    .getMetaObject(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject[] getMetaObject(User usr, Query query) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObject for user" + usr + "query ::" + query);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(usr.getDomain()))
                    .getMetaObject(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr       DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     * @param   classID   DOCUMENT ME!
     * @param   domain    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject getMetaObject(User usr, int objectID, int classID, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<CS> getMetaObject for user" + usr + "objectID ::" + objectID + " classID" + classID + " domain::"   // NOI18N
                    + domain);
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .getMetaObject(usr, objectID, classID);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject insertMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        if (logger != null) {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "<CS>insertMetaObject  for user" + user + "metaObject ::" + metaObject + " domain::" + domain);   // NOI18N
                }
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .insertMetaObject(user, metaObject);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   query   DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int insertMetaObject(User user, Query query, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>insertMetaObject  for user" + user + "query ::" + query + " domain::" + domain);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .insertMetaObject(user, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int deleteMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<CS>delete MetaObject  for user" + user + "metaObject ::" + metaObject + " domain::" + domain);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .deleteMetaObject(user, metaObject);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int updateMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<CS>updateMetaObject  for user" + user + "metaObject ::" + metaObject + " domain::" + domain);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .updateMetaObject(user, metaObject);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   metaSQL  DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int update(User user, String metaSQL, String domain) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>update  for user" + user + "metaSQL ::" + metaSQL + " domain::" + domain);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain)).update(
                user,
                metaSQL);
    }
    /**
     * creates an Instance of a MetaObject with all attribute values set to default.
     *
     * @param   user  DOCUMENT ME!
     * @param   c     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>getInstance  for user" + user + "metaClass ::" + c);   // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(c.getDomain()))
                    .getInstance(user, c);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MethodMap getMethods(User user) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>getMethods for user" + user);   // NOI18N
            }
        }

        MethodMap result = new MethodMap();
        for (int i = 0; i < localServers.length; i++) {
            Sirius.server.middleware.interfaces.domainserver.MetaService s =
                (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(
                    localServers[i].getName().trim());
            result.putAll(s.getMethods(user));
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MethodMap getMethods(User user, String lsName) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>getMethods for user" + user + " domain::" + lsName);   // NOI18N
            }
        }
        Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(lsName.trim());
        return s.getMethods(user);
    }
    /**
     * ---!!!
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
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getAllLightweightMetaObjectsForClass(classId, user, representationFields, representationPattern);
    }

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
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getAllLightweightMetaObjectsForClass(classId, user, representationFields);
    }

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
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getLightweightMetaObjectsByQuery(classId, user, query, representationFields, representationPattern);
    }

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
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
    }
    /**
     * private Fkt-en.
     *
     * @param  ls  DOCUMENT ME!
     */
    private void validateLocalServers(Server[] ls) {
        // aktualliseren der lokalserver wann immer ein Client anfragt

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>private function validateLocalServer called");   // NOI18N
            }
        }
        localServers = ls;

        try {
            for (int i = 0; i < ls.length; i++) {
                if (!activeLocalServers.contains(ls[i].getName())) {
                    activeLocalServers.put(ls[i].getName(), Naming.lookup(ls[i].getRMIAddress()));
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
