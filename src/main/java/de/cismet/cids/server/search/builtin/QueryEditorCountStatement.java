/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.connectioncontext.ConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContextProvider;
import de.cismet.cids.server.search.AbstractCidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class QueryEditorCountStatement extends AbstractCidsServerSearch implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(QueryEditorCountStatement.class);

    private static final String rawQuery = "SELECT count(*) FROM (SELECT * FROM {0} tbl WHERE {1} ) AS sub";

    //~ Instance fields --------------------------------------------------------

    private final String metaClass;
    private final String whereClause;
    private final String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryEditorCountStatement object.
     *
     * @param  domain       DOCUMENT ME!
     * @param  metaClass    DOCUMENT ME!
     * @param  whereClause  DOCUMENT ME!
     */
    public QueryEditorCountStatement(final String domain, final String metaClass, final String whereClause) {
        this.whereClause = whereClause;
        this.metaClass = metaClass;
        this.domain = domain;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<Long> performServerSearch() {
        try {
            final String sql = MessageFormat.format(rawQuery, metaClass, whereClause);

            final MetaService ms = (MetaService)getActiveLocalServers().get(domain);

            final ArrayList<ArrayList> result = ms.performCustomSearch(sql, getConnectionContext());

            final ArrayList<Long> aln = new ArrayList<Long>();
            for (final ArrayList al : result) {
                final Long count = (Long)al.get(0);
                aln.add(count);
            }

            return aln;
        } catch (final Exception e) {
            LOG.error("problem during count search", e); // NOI18N

            return null;
        }
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return ConnectionContext.create(QueryEditorCountStatement.class.getSimpleName());
    }
}
