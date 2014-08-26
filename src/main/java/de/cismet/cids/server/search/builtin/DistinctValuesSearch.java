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

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsServerSearch.class)
public class DistinctValuesSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final String query = "SELECT DISTINCT {1} FROM {0} order by {1} LIMIT 100;";
    private static final transient Logger LOG = Logger.getLogger(DistinctValuesSearch.class);

    //~ Instance fields --------------------------------------------------------

    private String metaClass;
    private String attribute;
    private String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DistinctValuesSearch object.
     */
    public DistinctValuesSearch() {
    }

    /**
     * Creates a new DistinctValuesSearch object.
     *
     * @param  domain     DOCUMENT ME!
     * @param  metaClass  DOCUMENT ME!
     * @param  attribute  DOCUMENT ME!
     */
    public DistinctValuesSearch(final String domain, final String metaClass, final String attribute) {
        setMetaClass(metaClass);
        setAttribute(attribute);
        setDomain(domain);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMetaClass() {
        return metaClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaClass  DOCUMENT ME!
     */
    public final void setMetaClass(final String metaClass) {
        this.metaClass = metaClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  attribute  DOCUMENT ME!
     */
    public final void setAttribute(final String attribute) {
        this.attribute = attribute;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    public final void setDomain(final String domain) {
        this.domain = domain;
    }

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(domain);
        if (ms != null) {
            try {
                final String query = MessageFormat.format(this.query, metaClass, attribute);
                final ArrayList<ArrayList> results = ms.performCustomSearch(query);
                return results;
            } catch (RemoteException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        } else {
            LOG.error("active local server not found"); // NOI18N
        }

        return null;
    }
}
