/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.search.builtin;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.connectioncontext.ServerConnectionContext;
import de.cismet.cids.server.connectioncontext.ServerConnectionContextProvider;
import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class DistinctValuesSearch extends AbstractCidsServerSearch implements ServerConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DistinctValuesSearch.class);

    //~ Instance fields --------------------------------------------------------

    private String metaClass;
    private String attribute;
    private String DOMAIN;

    private ServerConnectionContext serverConnectionContext = ServerConnectionContext.create(getClass()
                    .getSimpleName());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DistinctValuesSearch object.
     *
     * @param  domain     DOCUMENT ME!
     * @param  metaClass  DOCUMENT ME!
     * @param  attribute  DOCUMENT ME!
     */
    public DistinctValuesSearch(final String domain, final String metaClass, final String attribute) {
        this.metaClass = metaClass;
        this.attribute = attribute;
        this.DOMAIN = domain;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(DOMAIN);
        if (ms != null) {
            try {
                final String query = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                    .getDialect())
                            .getDistinctValuesSearchStmt(metaClass, attribute);
                final ArrayList<ArrayList> results = ms.performCustomSearch(query, getServerConnectionContext());
                return results;
            } catch (RemoteException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        } else {
            LOG.error("active local server not found"); // NOI18N
        }

        return null;
    }

    @Override
    public ServerConnectionContext getServerConnectionContext() {
        return serverConnectionContext;
    }

    @Override
    public void setServerConnectionContext(final ServerConnectionContext serverConnectionContext) {
        this.serverConnectionContext = serverConnectionContext;
    }
}
