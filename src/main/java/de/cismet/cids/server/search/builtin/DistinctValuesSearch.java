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

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.middleware.interfaces.domainserver.MetaService;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class DistinctValuesSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final String query = "SELECT DISTINCT {1} FROM {0} order by {1} LIMIT 100;";
    private static final String WRRL_DOMAIN = "WRRL_DB_MV"; // NOI18N
    private static final transient Logger LOG = Logger.getLogger(DistinctValuesSearch.class);

    //~ Instance fields --------------------------------------------------------

    private String metaClass;
    private String attribute;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DistinctValuesSearch object.
     *
     * @param  metaClass  DOCUMENT ME!
     * @param  attribute  DOCUMENT ME!
     */
    public DistinctValuesSearch(final String metaClass, final String attribute) {
        this.metaClass = metaClass;
        this.attribute = attribute;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(WRRL_DOMAIN);
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
