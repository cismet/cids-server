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

import Sirius.server.Server;
import Sirius.server.ServerType;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.NodeComparator;

import java.rmi.Naming;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cids.server.connectioncontext.ClientConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContext;
import de.cismet.cids.server.connectioncontext.ServerConnectionContext;
import de.cismet.cids.server.connectioncontext.ServerConnectionContextLogger;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class MetaServiceImpl implements MetaService {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private final Map activeLocalServers;
    private final NameServer nameServer;
    private Server[] localServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MetaServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaServiceImpl(final Map activeLocalServers,
            final NameServer nameServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
        // this.localServers = localServers;
        this.localServers = nameServer.getServers(ServerType.LOCALSERVER);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    @Deprecated
    public MetaClass getClass(final User user, final int classID, final String domain) throws RemoteException {
        return getClass(user, classID, domain, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClass(final User user, final int classID, final String domain, final ConnectionContext context)
            throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("getClass ::" + classID + " user::" + user + " domain::" + domain); // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain)).getClass(
                user,
                classID,
                context);
    }

    @Override
    @Deprecated
    public MetaClass getClassByTableName(final User user, final String tableName, final String domain)
            throws RemoteException {
        return getClassByTableName(user, tableName, domain, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user       DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     * @param   context    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClassByTableName(final User user,
            final String tableName,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .getClassByTableName(user, tableName, context);
    }

    @Override
    @Deprecated
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {
        return getClasses(user, domain, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass[] getClasses(final User user, final String domain, final ConnectionContext context)
            throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("getClasses for User : " + user); // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .getClasses(user, context);
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        return getClassTreeNodes(user, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final ConnectionContext context) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getClassTreeNodes for user" + user); // NOI18N
            }
        }

        final List ctns = new ArrayList();
        final java.util.Iterator iter = activeLocalServers.values().iterator();
        Node[] classNodes = new Node[0];

        int size = 0;
        try {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<CS> iter: " + iter); // NOI18N
                }
            }

            while (iter.hasNext()) {
                try {
                    final NodeReferenceList children =
                        ((Sirius.server.middleware.interfaces.domainserver.MetaService)iter.next()).getClassTreeNodes(
                            user,
                            context);

                    if ((children != null) && (children.getLocalNodes() != null)) {
                        final Node[] tmp = children.getLocalNodes();
                        if (logger.isDebugEnabled()) {
                            logger.debug("<CS> found valid localserver delivers topnodes ::" + tmp.length); // NOI18N
                        }
                        size += tmp.length;
                        ctns.add(tmp);
                    }
                } catch (Exception e) {
                    logger.error("<CS> getTopNodes(user) of a domainserver:", e);                           // NOI18N
                }
            }
            classNodes = new Node[size];

            for (int i = 0; i < ctns.size(); i++) {
                final Node[] tmp = (Node[])ctns.get(i);

                for (int j = 0; j < tmp.length; j++) {
                    --size;
                    classNodes[size] = tmp[j]; // wird von hinten nach vorne belegt
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("<CS> getTopNodes(user):", e); // NOI18N
            }
            throw new RemoteException("<CS> getTopNodes(user)", e); // NOI18N
        }

        java.util.Arrays.sort(classNodes, new NodeComparator());
        return classNodes;
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user, final String localServerName) throws RemoteException {
        return getClassTreeNodes(user, localServerName, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getClassTreeNode for user" + user); // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(localServerName))
                    .getClassTreeNodes(user, context).getLocalNodes();
    }

    @Override
    @Deprecated
    public String[] getDomains() throws RemoteException {
        return getDomains(ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public String[] getDomains(final ConnectionContext context) throws RemoteException {
        ServerConnectionContextLogger.getInstance()
                .logConnectionContext((ServerConnectionContext)context, null, "getDomains");

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getDomains called "); // NOI18N
            }
        }
        final Server[] ls = nameServer.getServers(ServerType.LOCALSERVER);

        validateLocalServers(ls);

        final String[] lsnames = new String[ls.length];

        for (int i = 0; i < ls.length; i++) {
            lsnames[i] = ls[i].getName();
        }

        return lsnames;
    }

    @Override
    @Deprecated
    public Node getMetaObjectNode(final User usr, final int nodeID, final String lsName) throws RemoteException {
        return getMetaObjectNode(usr, nodeID, lsName, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   nodeID   DOCUMENT ME!
     * @param   lsName   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node getMetaObjectNode(final User user,
            final int nodeID,
            final String lsName,
            final ConnectionContext context) throws RemoteException {
        // usr wird nicht beachtet fuer spaetere anpassungen
        ServerConnectionContextLogger.getInstance()
                .logConnectionContext((ServerConnectionContext)context,
                    user,
                    "getMetaObjectNode",
                    "nodeID:"
                    + nodeID,
                    "lsName:"
                    + lsName);

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObjectNode for user" + user + "node ::" + nodeID + " domain" + lsName); // NOI18N
            }
        }
        final java.lang.Object name = activeLocalServers.get(lsName);
        Node n = null;
        final int[] ids = new int[1];
        ids[0] = nodeID;

        if (name != null) {
            n = ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)name).getNodes(user, ids, context)[0];
        } else {
            final Node error = new MetaNode(
                    ids[0],
                    lsName,
                    lsName
                            + " not available!", // NOI18N
                    lsName
                            + " not available!", // NOI18N
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

    @Override
    @Deprecated
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getMetaObjectNode(usr, query, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr      DOCUMENT ME!
     * @param   query    DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObjectNode for user" + usr + "queryString ::" + query); // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(usr.getDomain()))
                    .getMetaObjectNode(usr, query, context);
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
    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        return getMetaObject(usr, query, usr.getDomain(), ClientConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject[] getMetaObject(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        return getMetaObject(usr, query, usr.getDomain(), context);
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query, final String domain) throws RemoteException {
        return getMetaObject(usr, query, ClientConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject[] getMetaObject(final User usr,
            final String query,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> getMetaObject for [user=" + usr + "|domain=" + domain + "|query=" + query + "]"); // NOI18N
            }
        }

        final Sirius.server.middleware.interfaces.domainserver.MetaService metaService =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain);

        if (metaService == null) {
            throw new RemoteException("no server registered for domain: " + domain); // NOI18N
        }

        return metaService.getMetaObject(usr, query, context);
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
    @Override
    @Deprecated
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        return getMetaObject(usr, objectID, classID, domain, ClientConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject getMetaObject(final User usr,
            final int objectID,
            final int classID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<CS> getMetaObject for user" // NOI18N
                            + usr
                            + "objectID ::"       // NOI18N
                            + objectID
                            + " classID"          // NOI18N
                            + classID
                            + " domain::"         // NOI18N
                            + domain);
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .getMetaObject(usr, objectID, classID, context);
    }

    @Override
    @Deprecated
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return insertMetaObject(user, metaObject, domain, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject insertMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        if (logger != null) {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "<CS>insertMetaObject  for user" // NOI18N
                                + user
                                + "metaObject ::"        // NOI18N
                                + metaObject
                                + " domain::"            // NOI18N
                                + domain);
                }
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .insertMetaObject(user, metaObject, context);
    }

    @Override
    @Deprecated
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return deleteMetaObject(user, metaObject, domain, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int deleteMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<CS>delete MetaObject  for user" // NOI18N
                            + user
                            + "metaObject ::"         // NOI18N
                            + metaObject
                            + " domain::"             // NOI18N
                            + domain);
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .deleteMetaObject(user, metaObject, context);
    }

    @Override
    @Deprecated
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return updateMetaObject(user, metaObject, domain, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int updateMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        if ((logger != null) && !(metaObject instanceof LightweightMetaObject)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<CS>updateMetaObject  for user" // NOI18N
                            + user
                            + "metaObject ::"        // NOI18N
                            + metaObject
                            + " domain::"            // NOI18N
                            + domain);
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain))
                    .updateMetaObject(user, metaObject, context);
    }

    @Override
    @Deprecated
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return getInstance(user, c, ClientConnectionContext.createDeprecated());
    }

    /**
     * creates an Instance of a MetaObject with all attribute values set to default.
     *
     * @param   user     DOCUMENT ME!
     * @param   c        DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getInstance(final User user, final MetaClass c, final ConnectionContext context)
            throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>getInstance  for user" + user + "metaClass ::" + c); // NOI18N
            }
        }
        return ((Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(c.getDomain()))
                    .getInstance(user, c, context);
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user) throws RemoteException {
        return getMethods(user, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final ConnectionContext context) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>getMethods for user" + user); // NOI18N
            }
        }

        final MethodMap result = new MethodMap();
        for (int i = 0; i < localServers.length; i++) {
            final Sirius.server.middleware.interfaces.domainserver.MetaService s =
                (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(
                    localServers[i].getName().trim());
            result.putAll(s.getMethods(user, context));
        }

        return result;
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user, final String lsName) throws RemoteException {
        return getMethods(user, lsName, ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   lsName   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final String lsName, final ConnectionContext context)
            throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>getMethods for user" + user + " domain::" + lsName); // NOI18N
            }
        }
        final Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(lsName.trim());
        return s.getMethods(user, context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                ClientConnectionContext.createDeprecated());
    }

    /**
     * ---!!!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     * @param   context                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        final Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern,
                context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   context               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final ConnectionContext context) throws RemoteException {
        final Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getAllLightweightMetaObjectsForClass(classId, user, representationFields, context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern,
                ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     * @param   context                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        final Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern,
                context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        return getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                ClientConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   context               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final ConnectionContext context) throws RemoteException {
        final Sirius.server.middleware.interfaces.domainserver.MetaService s =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(user.getDomain());
        return s.getLightweightMetaObjectsByQuery(classId, user, query, representationFields, context);
    }

    /**
     * private Fkt-en.
     *
     * @param  ls  DOCUMENT ME!
     */
    private void validateLocalServers(final Server[] ls) {
        // aktualliseren der lokalserver wann immer ein Client anfragt

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS>private function validateLocalServer called"); // NOI18N
            }
        }
        localServers = ls;

        try {
            for (int i = 0; i < ls.length; i++) {
                if (!activeLocalServers.containsKey(ls[i].getName())) {
                    activeLocalServers.put(ls[i].getName(), Naming.lookup(ls[i].getRMIAddress()));
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    @Deprecated
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        return getHistory(classId, objectId, domain, user, elements, ClientConnectionContext.createDeprecated());
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements,
            final ConnectionContext context) throws RemoteException {
        final Sirius.server.middleware.interfaces.domainserver.MetaService service =
            (Sirius.server.middleware.interfaces.domainserver.MetaService)activeLocalServers.get(domain);

        return service.getHistory(classId, objectId, user, elements, context);
    }
}
