/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.UserService;
import Sirius.server.middleware.interfaces.proxy.SearchService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashMap;

import de.cismet.cids.server.connectioncontext.ConnectionContextLogger;
import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class SearchServiceImpl implements SearchService {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    // resolves Query tree
    private final java.util.Hashtable activeLocalServers;
    private final NameServer nameServer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchServiceImpl object.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public SearchServiceImpl(final java.util.Hashtable activeLocalServers, final NameServer nameServer)
            throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user          DOCUMENT ME!
     * @param   serverSearch  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        return customServerSearch(user, serverSearch, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user               DOCUMENT ME!
     * @param   serverSearch       DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Collection customServerSearch(final User user,
            final CidsServerSearch serverSearch,
            final ConnectionContext connectionContext) throws RemoteException {
        ConnectionContextLogger.getInstance()
                .logConnectionContext((ConnectionContext)connectionContext,
                    user,
                    "customServerSearch",
                    "serverSearch:"
                    + serverSearch);
        serverSearch.setUser(user);
        serverSearch.setActiveLocalServers(new HashMap(activeLocalServers));
        if (serverSearch instanceof ConnectionContextStore) {
            ((ConnectionContextStore)serverSearch).initWithConnectionContext((ConnectionContext)connectionContext);
        }
        try {
            final Collection searchResults = serverSearch.performServerSearch();
            addDynamicChildren(user, searchResults, connectionContext);
            return searchResults;
        } catch (Exception e) {
            logger.error("Error in customSearch", e);
            throw new RemoteException("Error in customSearch", e);
        }
    }

    /**
     * Adds the dynamic children to the search results, if there are any assigned. Dynamic children will only be
     * assigned to MetaObjectNode objects
     *
     * @param  user           the user
     * @param  searchResults  the collection with the results of the search
     * @param  context        DOCUMENT ME!
     */
    private void addDynamicChildren(final User user, final Collection searchResults, final ConnectionContext context) {
        // caches the dynamic children due to performance reasons
        final HashMap<String, String> dynChildCache = new HashMap<String, String>();

        if (searchResults != null) {
            for (final Object tmp : searchResults) {
                if (tmp instanceof MetaObjectNode) {
                    final MetaObjectNode node = (MetaObjectNode)tmp;
                    final String key = node.getDomain() + ";" + node.getClassId();

                    if (dynChildCache.containsKey(key)) {
                        // the dynamic children is contained in the cache
                        String dynamicChildrenStatement = dynChildCache.get(key);

                        if (dynamicChildrenStatement != null) {
                            // replace the object id wildcard with the object id. This wildcard can be used within the
                            // dynamic children sql statements
                            dynamicChildrenStatement = dynamicChildrenStatement.replace((CharSequence)"<object_id>",
                                    (CharSequence)String.valueOf(node.getObjectId()));
                            node.setDynamicChildrenStatement(dynamicChildrenStatement);
                            node.setDynamic(true);
                            node.setLeaf(node.getDynamicChildrenStatement() == null);
                        }
                    } else {
                        try {
                            // the dynamic children is not contained in the cache
                            final MetaClass cl = ((MetaService)activeLocalServers.get(node.getDomain())).getClass(
                                    user,
                                    node.getClassId(),
                                    context);
                            ClassAttribute dynChild = cl.getClassAttribute("searchHit_dynamicChildren");
                            final ClassAttribute attribute = cl.getClassAttribute("searchHit_dynamicChildrenAttribute");
                            boolean hasAttribute = false;
                            String value = null;

                            if (attribute != null) {
                                value = ((UserService)activeLocalServers.get(node.getDomain())).getConfigAttr(
                                        user,
                                        (String)attribute.getValue(),
                                        context);

                                if (value != null) {
                                    hasAttribute = true;
                                }
                            }

                            if (hasAttribute) {
                                final ClassAttribute otherDynChild = cl.getClassAttribute(value);

                                if (otherDynChild != null) {
                                    dynChild = otherDynChild;
                                }
                            }

                            if ((dynChild != null) && ((attribute == null) || hasAttribute)) {
                                String sqlText = (String)dynChild.getValue();
                                dynChildCache.put(key, sqlText);

                                // replace the object id wildcard with the object id. This wildcard can be used within
                                // the dynamic children sql statements
                                sqlText = sqlText.replace((CharSequence)"<object_id>",
                                        (CharSequence)String.valueOf(node.getObjectId()));
                                node.setDynamicChildrenStatement(sqlText);
                                node.setDynamic(true);
                                node.setLeaf(node.getDynamicChildrenStatement() == null);
                            } else {
                                dynChildCache.put(key, node.getDynamicChildrenStatement());
                            }
                        } catch (RemoteException e) {
                            logger.error("Error while trying to add the dynamic children to the search results.", e);
                        }
                    }
                }
            }
        }
    }
}
