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

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * As this search allows the user to specify a where clause he has to know what backend the server it is executed on
 * uses. Thus the search may fail due to wrong dialect.
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class QueryEditorSearch extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(QueryEditorSearch.class);

    //~ Instance fields --------------------------------------------------------

    private String metaClass;
    private String whereClause;
    private int classId;
    private String DOMAIN;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryEditorSearch object.
     *
     * @param  domain       DOCUMENT ME!
     * @param  metaClass    DOCUMENT ME!
     * @param  whereClause  DOCUMENT ME!
     * @param  classId      DOCUMENT ME!
     */
    public QueryEditorSearch(final String domain, final String metaClass, final String whereClause, final int classId) {
        this.whereClause = whereClause;
        this.metaClass = metaClass;
        this.classId = classId;
        this.DOMAIN = domain;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(DOMAIN);
        final ArrayList<MetaObjectNode> metaObjects = new ArrayList<MetaObjectNode>();
        if (ms != null) {
            try {
                final String query = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                    .getDialect())
                            .getQueryEditorSearchStmt(metaClass, classId, whereClause);
                LOG.info(query);
                final ArrayList<ArrayList> results = ms.performCustomSearch(query);

                for (final ArrayList al : results) {
                    final int cid = (Integer)al.get(0);
                    final int oid = (Integer)al.get(1);
                    String name = null;
                    try {
                        name = (String)al.get(2);
                    } catch (final Exception e) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("no name present for metaobjectnode", e); // NOI18N
                        }
                    }
                    final MetaObjectNode mon = new MetaObjectNode(DOMAIN, oid, cid, name);
                    metaObjects.add(mon);
                }

                return metaObjects;
            } catch (RemoteException ex) {
                LOG.error(ex.getMessage(), ex);
                throw new SearchException("An error is occured, possibly an sql syntax error", ex);
            }
        } else {
            LOG.error("active local server not found"); // NOI18N
        }

        return metaObjects;
    }
}
