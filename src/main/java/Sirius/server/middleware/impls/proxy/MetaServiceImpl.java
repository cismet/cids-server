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
 *
 * @author  awindholz
 */
public class MetaServiceImpl {

    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private java.util.Hashtable activeLocalServers;
    private NameServer nameServer;
    private Server[] localServers;
    // resolves Query tree
    private QueryExecuter qex;

    /** Creates a new instance of MetaServiceImpl */
    public MetaServiceImpl(
            java.util.Hashtable activeLocalServers,
            NameServer nameServer /*,
            Sirius.Server.Server[] localServers*/) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
        //        this.localServers = localServers;
        this.localServers = nameServer.getServers(ServerType.LOCALSERVER);
        qex = new QueryExecuter(activeLocalServers);
    }

    public MetaClass getClass(User user, int classID, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("getClass ::" + classID + " user::" + user + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).getClass(user, classID);
    }

    public MetaClass getClassByTableName(User user, String tableName, String domain) throws RemoteException {
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).getClassByTableName(user, tableName);
    }

    public MetaClass[] getClasses(User user, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("getClasses for User : " + user);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).getClasses(user);

    }

    public Node[] getClassTreeNodes(User user) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getClassTreeNodes for user" + user);
        }

        java.util.Vector ctns = new java.util.Vector(10, 10);
        java.util.Iterator iter = activeLocalServers.values().iterator();
        Node[] classNodes = new Node[0];

        int size = 0;
        try {
            if (logger != null) {
                logger.debug("<CS> iter: " + iter);
            }

            while (iter.hasNext()) {
                try {
                    NodeReferenceList children = (NodeReferenceList) ((Sirius.server.middleware.interfaces.domainserver.MetaService) iter.next()).getClassTreeNodes(user);

                    if (children != null && children.getLocalNodes() != null) {
                        Node[] tmp = children.getLocalNodes();
                        logger.debug("<CS> found valid localserver delivers topnodes ::" + tmp.length);
                        size += tmp.length;
                        ctns.addElement(tmp);
                    }
                } catch (Exception e) {
                    logger.error("<CS> getTopNodes(user) of a domainserver:", e);
                }
            }
            classNodes = new Node[size];

            for (int i = 0; i < ctns.size(); i++) {
                Node[] tmp = (Node[]) ctns.get(i);

                for (int j = 0; j < tmp.length; j++) {
                    --size;
                    classNodes[size] = tmp[j]; // wird von hinten nach vorne belegt
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("<CS> getTopNodes(user):", e);

            }
            throw new RemoteException("<CS> getTopNodes(user)", e);
        }

        java.util.Arrays.sort(classNodes, new NodeComparator());
        return classNodes;
    }

    public Node[] getClassTreeNodes(User user, String localServerName) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getClassTreeNode for user" + user);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(localServerName)).getClassTreeNodes(user).getLocalNodes();
    }

    public String[] getDomains() throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getDomains gerufen ");
        }
        Server[] ls = nameServer.getServers(ServerType.LOCALSERVER);

        validateLocalServers(ls);

        String[] lsnames = new String[ls.length];

        for (int i = 0; i < ls.length; i++) {
            lsnames[i] = ls[i].getName();
        }

        return lsnames;
    }

    public Node getMetaObjectNode(User usr, int nodeID, String lsName) throws RemoteException {
        // usr wird nicht beachtet fuer spaetere anpassungen

        if (logger != null) {
            logger.debug("<CS> getMetaObjectNode for user" + usr + "node ::" + nodeID + " domain" + lsName);
        }
        java.lang.Object name = activeLocalServers.get(lsName);
        Node n = null;
        int ids[] = new int[1];
        ids[0] = nodeID;

        if (name != null) {
            n = ((Sirius.server.middleware.interfaces.domainserver.CatalogueService) name).getNodes(usr, ids)[0];

        } else {
            Node error = new MetaNode(ids[0], lsName, lsName + " not available!", lsName + " not available!", true, Policy.createWIKIPolicy(), -1, null, false, -1);
            error.validate(false);
            n = error;
        }

        return n;
    }

    public Node[] getMetaObjectNode(User usr, String query) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getMetaObjectNode for user" + usr + "queryString ::" + query);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(usr.getDomain())).getMetaObjectNode(usr, query);
    }

    public Node[] getMetaObjectNode(User usr, Query query) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getMetaObjectNode for user" + usr + "query ::" + query);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(usr.getDomain())).getMetaObjectNode(usr, query);

    }

    public MetaObject[] getMetaObject(User usr, String query) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getMetaObject for user" + usr + "queryString ::" + query);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(usr.getDomain())).getMetaObject(usr, query);
    }

    public MetaObject[] getMetaObject(User usr, Query query) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getMetaObject for user" + usr + "query ::" + query);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(usr.getDomain())).getMetaObject(usr, query);

    }

    public MetaObject getMetaObject(User usr, int objectID, int classID, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS> getMetaObject for user" + usr + "objectID ::" + objectID + " classID" + classID + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).getMetaObject(usr, objectID, classID);
    }

    public MetaObject insertMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        if (logger != null) {
            if (logger != null) {
                logger.debug("<CS>insertMetaObject  for user" + user + "metaObject ::" + metaObject + " domain::" + domain);
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).insertMetaObject(user, metaObject);
    }

    public int insertMetaObject(User user, Query query, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>insertMetaObject  for user" + user + "query ::" + query + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).insertMetaObject(user, query);
    }

    public int deleteMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>delete MetaObject  for user" + user + "metaObject ::" + metaObject + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).deleteMetaObject(user, metaObject);
    }

    public int updateMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>updateMetaObject  for user" + user + "metaObject ::" + metaObject + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).updateMetaObject(user, metaObject);
    }

    public int update(User user, String metaSQL, String domain) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>update  for user" + user + "metaSQL ::" + metaSQL + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(domain)).update(user, metaSQL);
    }

    // creates an Instance of a MetaObject with all attribute values set to default
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>getInstance  for user" + user + "metaClass ::" + c);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(c.getDomain())).getInstance(user, c);

    }

    public MethodMap getMethods(User user) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>getMethods for user" + user);
        }

        MethodMap result = new MethodMap();
        for (int i = 0; i < localServers.length; i++) {
            Sirius.server.middleware.interfaces.domainserver.MetaService s = (Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(localServers[i].getName().trim());
            result.putAll(s.getMethods(user));
        }

        return result;
    }

    public MethodMap getMethods(User user, String lsName) throws RemoteException {
        if (logger != null) {
            logger.debug("<CS>getMethods for user" + user + " domain::" + lsName);
        }
        Sirius.server.middleware.interfaces.domainserver.MetaService s = (Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(lsName.trim());
        return s.getMethods(user);
    }

    //---!!!
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields, String representationPattern) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s = (Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(user.getDomain());
        return s.getAllLightweightMetaObjectsForClass(classId, user, representationFields, representationPattern);
    }

    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s = (Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(user.getDomain());
        return s.getAllLightweightMetaObjectsForClass(classId, user, representationFields);
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields, String representationPattern) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s = (Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(user.getDomain());
        return s.getLightweightMetaObjectsByQuery(classId, user, query, representationFields, representationPattern);
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields) throws RemoteException {
        Sirius.server.middleware.interfaces.domainserver.MetaService s = (Sirius.server.middleware.interfaces.domainserver.MetaService) activeLocalServers.get(user.getDomain());
        return s.getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
    }

    // private Fkt-en
    private void validateLocalServers(Server[] ls) {
        // aktualliseren der lokalserver wann immer ein Client anfragt

        if (logger != null) {
            logger.debug("<CS>private function validateLocalServer gerufen");
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
