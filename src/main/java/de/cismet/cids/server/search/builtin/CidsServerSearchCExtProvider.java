/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.builtin.SearchCExtContext.Search;

import de.cismet.ext.CExtContext;
import de.cismet.ext.CExtProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class CidsServerSearchCExtProvider implements CExtProvider<CidsServerSearch> {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(CidsServerSearchCExtProvider.class);

    //~ Instance fields --------------------------------------------------------

    private final String ifaceClass;
    private final Set<String> concreteClasses;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsServerSearchCExtProvider object.
     */
    public CidsServerSearchCExtProvider() {
        ifaceClass = "de.cismet.cids.server.search.CidsServerSearch";               // NOI18N
        concreteClasses = new HashSet<String>(3);
        concreteClasses.add("de.cismet.cids.server.search.builtin.FullTextSearch"); // NOI18N
        concreteClasses.add("de.cismet.cids.server.search.builtin.GeoSearch");      // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<? extends CidsServerSearch> provideExtensions(final CExtContext context) {
        final ArrayList<CidsServerSearch> searches = new ArrayList<CidsServerSearch>(1);

        if (context instanceof SearchCExtContext) {
            final SearchCExtContext sContext = (SearchCExtContext)context;

            if (LOG.isTraceEnabled()) {
                LOG.trace("search context: " + sContext); // NOI18N
            }

            // NOTE: the searches are responsible for parameter validation
            try {
                final Geometry geometry = (Geometry)sContext.getProperty(SearchCExtContext.CTX_PROP_GEOMETRY);
                if (Search.FULLTEXT == sContext.getSearch()) {
                    final String searchText = (String)sContext.getProperty(SearchCExtContext.CTX_PROP_SEARCH_TEXT);
                    final Boolean caseSensitive = (Boolean)sContext.getProperty(
                            SearchCExtContext.CTX_PROP_CASE_SENSITIVE);

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("full text search params: [searchText=" + searchText + "|caseSensitive=" // NOI18N
                                    + caseSensitive + "|geometry=" + geometry + "]"); // NOI18N
                    }

                    searches.add(new FullTextSearch(searchText, caseSensitive, geometry));
                } else if (Search.SPATIAL == sContext.getSearch()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("spatial search params: [geometry=" + geometry + "]"); // NOI18N
                    }

                    searches.add(new GeoSearch(geometry));
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("search context not initialised properly, no search set"); // NOI18N
                    }
                }
            } catch (final Exception e) {
                LOG.warn("cannot instantiate search", e);                                    // NOI18N
            }
        }

        return searches;
    }

    @Override
    public Class<? extends CidsServerSearch> getType() {
        throw new UnsupportedOperationException("not suitable for multi providers");
    }

    @Override
    public boolean canProvide(final Class<?> c) {
        final String cName = c.getCanonicalName();

        return ifaceClass.equals(cName) || concreteClasses.contains(cName);
    }
}
