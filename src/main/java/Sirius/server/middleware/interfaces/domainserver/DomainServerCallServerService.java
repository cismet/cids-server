/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.interfaces.domainserver;

import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.user.PasswordCheckException;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.property.ServerProperties;

import Sirius.util.image.Image;

import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.security.Key;

import java.util.Collection;
import java.util.Vector;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.CallServerServiceProvider;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class DomainServerCallServerService implements CallServerService {

    //~ Instance fields --------------------------------------------------------

    private final DomainServerImpl domainServer = DomainServerImpl.getServerInstance();
    private final ServerProperties serverProperties = DomainServerImpl.getServerProperties();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    private void checkDomain(final String domain) {
        final String serverDomain = serverProperties.getServerName();
        if ((domain == null) || !domain.equals(serverDomain)) {
            throw new RuntimeException(String.format(
                    "given domain %s is not allowed for domainserver %s",
                    domain,
                    serverDomain));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DomainServerImpl getDomainServer() {
        return domainServer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DomainServerCallServerService getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CallServerService getCallServerServiceInstance() {
        final CallServerServiceProvider csProvider = Lookup.getDefault().lookup(CallServerServiceProvider.class);
        if (csProvider != null) {
            return csProvider.getCallServerService();
        } else {
            return getInstance();
        }
    }

    @Override
    public Node[] getRoots(final User user, final String domain, final ConnectionContext context)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getRoots(user, context).getLocalNodes();
    }

    @Override
    public Node[] getRoots(final User user, final ConnectionContext context) throws RemoteException {
        return getDomainServer().getRoots(user, context).getLocalNodes();
    }

    @Override
    public Node[] getChildren(final Node node, final User usr, final ConnectionContext context) throws RemoteException {
        return getDomainServer().getChildren(node, usr, context).getLocalNodes();
    }

    @Override
    public Node addNode(final Node node, final Link parent, final User user, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().addNode(node, parent, user, context);
    }

    @Override
    public boolean deleteNode(final Node node, final User user, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().deleteNode(node, user, context);
    }

    @Override
    public boolean addLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().addLink(from, to, user, context);
    }

    @Override
    public boolean deleteLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().deleteLink(from, to, user, context);
    }

    @Override
    public Node getMetaObjectNode(final User usr,
            final int nodeID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getMetaObjectNode(usr, nodeID, context);
    }

    @Override
    public Node[] getMetaObjectNode(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().getMetaObjectNode(usr, query, context);
    }

    @Override
    public MetaObject[] getMetaObject(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().getMetaObject(usr, query, context);
    }

    @Override
    public MetaObject[] getMetaObject(final User usr,
            final String query,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getMetaObject(usr, query, context);
    }

    @Override
    public MetaObject getMetaObject(final User usr,
            final int objectID,
            final int classID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getMetaObject(usr, objectID, classID, context);
    }

    @Override
    public MetaObject insertMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().insertMetaObject(user, metaObject, context);
    }

    @Override
    public int updateMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().updateMetaObject(user, metaObject, context);
    }

    @Override
    public int deleteMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().deleteMetaObject(user, metaObject, context);
    }

    @Override
    public MetaObject getInstance(final User user, final MetaClass c, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().getInstance(user, c, context);
    }

    @Override
    public MetaClass getClass(final User user, final int classID, final String domain, final ConnectionContext context)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClass(user, classID, context);
    }

    @Override
    public MetaClass getClassByTableName(final User user,
            final String tableName,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClassByTableName(user, tableName, context);
    }

    @Override
    public MetaClass[] getClasses(final User user, final String domain, final ConnectionContext context)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClasses(user, context);
    }

    @Override
    public Node[] getClassTreeNodes(final User user, final ConnectionContext context) throws RemoteException {
        return getDomainServer().getClassTreeNodes(user, context).getLocalNodes();
    }

    @Override
    public Node[] getClassTreeNodes(final User user, final String domain, final ConnectionContext context)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClassTreeNodes(user, context).getLocalNodes();
    }

    @Override
    public MethodMap getMethods(final User user, final ConnectionContext context) throws RemoteException {
        return getDomainServer().getMethods(user, context);
    }

    @Override
    public MethodMap getMethods(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().getMethods(user, context);
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        return getDomainServer().getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern,
                context);
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final ConnectionContext context) throws RemoteException {
        return getDomainServer().getAllLightweightMetaObjectsForClass(classId, user, representationFields, context);
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        return getDomainServer().getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern,
                context);
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final ConnectionContext context) throws RemoteException {
        return getDomainServer().getLightweightMetaObjectsByQuery(classId, user, query, representationFields, context);
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements,
            final ConnectionContext context) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getHistory(classId, objectId, user, elements, context);
    }

    @Override
    public Image[] getDefaultIcons(final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getDefaultIcons();
    }

    @Override
    public Image[] getDefaultIcons(final String domain, final ConnectionContext connectionContext)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getDefaultIcons();
    }

    @Override
    public Image[] getDefaultIcons() throws RemoteException {
        return getDomainServer().getDefaultIcons();
    }

    @Override
    public Image[] getDefaultIcons(final ConnectionContext connectionContext) throws RemoteException {
        return getDomainServer().getDefaultIcons();
    }

    @Override
    public String getConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().getConfigAttr(user, key, context);
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        return getDomainServer().hasConfigAttr(user, key, context);
    }

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ConnectionContext context,
            final ServerActionParameter... params) throws RemoteException {
        return getDomainServer().executeTask(user, taskname, body, context, params);
    }

    @Override
    public boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext context) throws RemoteException, UserException, PasswordCheckException {
        return getDomainServer().changePassword(user, oldPassword, newPassword, context);
    }

    @Override
    public String[] getDomains() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public String[] getDomains(final ConnectionContext context) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password,
            final ConnectionContext context) throws RemoteException, UserException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public Vector getUserGroupNames() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public Vector getUserGroupNames(final ConnectionContext context) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome, final ConnectionContext context)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public Collection customServerSearch(final User user,
            final CidsServerSearch serverSearch,
            final ConnectionContext context) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    @Deprecated
    public Node[] getRoots(final User user, final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getRoots(user).getLocalNodes();
    }

    @Override
    @Deprecated
    public Node[] getRoots(final User user) throws RemoteException {
        return getDomainServer().getRoots(user).getLocalNodes();
    }

    @Override
    @Deprecated
    public Node[] getChildren(final Node node, final User usr) throws RemoteException {
        return getDomainServer().getChildren(node, usr).getLocalNodes();
    }

    @Override
    @Deprecated
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        return getDomainServer().addNode(node, parent, user);
    }

    @Override
    @Deprecated
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        return getDomainServer().deleteNode(node, user);
    }

    @Override
    @Deprecated
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        return getDomainServer().addLink(from, to, user);
    }

    @Override
    @Deprecated
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        return getDomainServer().deleteLink(from, to, user);
    }

    @Override
    @Deprecated
    public Node getMetaObjectNode(final User usr, final int nodeID, final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getMetaObjectNode(usr, nodeID);
    }

    @Override
    @Deprecated
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getDomainServer().getMetaObjectNode(usr, query);
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        return getDomainServer().getMetaObject(usr, query);
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query, final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getMetaObject(usr, query);
    }

    @Override
    @Deprecated
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getMetaObject(usr, objectID, classID);
    }

    @Override
    @Deprecated
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().insertMetaObject(user, metaObject);
    }

    @Override
    @Deprecated
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().updateMetaObject(user, metaObject);
    }

    @Override
    @Deprecated
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().deleteMetaObject(user, metaObject);
    }

    @Override
    @Deprecated
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return getDomainServer().getInstance(user, c);
    }

    @Override
    @Deprecated
    public MetaClass getClass(final User user, final int classID, final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClass(user, classID);
    }

    @Override
    @Deprecated
    public MetaClass getClassByTableName(final User user, final String tableName, final String domain)
            throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClassByTableName(user, tableName);
    }

    @Override
    @Deprecated
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClasses(user);
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        return getDomainServer().getClassTreeNodes(user).getLocalNodes();
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user, final String domain) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getClassTreeNodes(user).getLocalNodes();
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user) throws RemoteException {
        return getDomainServer().getMethods(user);
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user, final String localServerName) throws RemoteException {
        return getDomainServer().getMethods(user);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getDomainServer().getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return getDomainServer().getAllLightweightMetaObjectsForClass(classId, user, representationFields);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getDomainServer().getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        return getDomainServer().getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
    }

    @Override
    @Deprecated
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        checkDomain(domain);
        return getDomainServer().getHistory(classId, objectId, user, elements);
    }

    @Override
    @Deprecated
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        return getDomainServer().getConfigAttr(user, key);
    }

    @Override
    @Deprecated
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        return getDomainServer().hasConfigAttr(user, key);
    }

    @Override
    @Deprecated
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return getDomainServer().executeTask(user, taskname, body, params);
    }

    @Override
    @Deprecated
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException, PasswordCheckException {
        return getDomainServer().changePassword(user, oldPassword, newPassword);
    }

    @Override
    public Key getPublicJwtKey(final String domain) throws RemoteException {
        return getDomainServer().getPublicJwtKey();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final DomainServerCallServerService INSTANCE = new DomainServerCallServerService();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
