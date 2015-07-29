/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.search.builtin.legacy;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.SearchInfo;
import de.cismet.cidsx.server.api.types.ParameterInfo;
import de.cismet.cidsx.server.search.RestApiCidsServerSearch;

/**
 * Builtin Legacy Search to delegate the operation getMetaObjectNodes(String query, ...) the cids Pure REST Search API.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = RestApiCidsServerSearch.class)
public class MetaObjectNodesByQuerySearch extends AbstractCidsServerSearch implements RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MetaObjectNodesByQuerySearch.class);

    //~ Instance fields --------------------------------------------------------

    @Getter private final SearchInfo searchInfo;
    @Getter @Setter private String domain;
    @Getter @Setter private String query;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaObjectNodesByQuerySearch object.
     */
    public MetaObjectNodesByQuerySearch() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription(
            "Builtin Legacy Search to delegate the operation getMetaObjectNodes(String query, ...) to the cids Pure REST Search API.");

        final List<ParameterInfo> parameterDescription = new LinkedList<ParameterInfo>();
        ParameterInfo searchParameterInfo;

        searchParameterInfo = new ParameterInfo();
        searchParameterInfo.setKey("domain");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchParameterInfo = new ParameterInfo();
        searchParameterInfo.setKey("query");
        searchParameterInfo.setDescription(
            "the query must generate a result set with columns legacy classId and legacy objectId");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        final ParameterInfo resultParameterInfo = new ParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.NODE);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService metaService = (MetaService)this.getActiveLocalServers().get(this.getDomain());
        if (metaService == null) {
            final String message = "Meta Object Nodes By Query Search "
                        + "could not connect ot MetaService @domain '" + this.domain + "'";
            LOG.error(message);
            throw new SearchException(message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("performing search for Meta Object Nodes By Query with query '"
                        + this.getQuery() + "'");
        }
        try {
            final Node[] metaObjectNodes = metaService.getMetaObjectNode(this.getUser(), this.getQuery());

            if (LOG.isDebugEnabled()) {
                LOG.debug(metaObjectNodes.length + " Meta Object Nodes found.");
            }
            return Arrays.asList(metaObjectNodes);
        } catch (RemoteException ex) {
            final String message = "could not perform Meta Object Nodes By Query Search with query '"
                        + this.getQuery() + "': " + ex.getMessage();
            LOG.error(message, ex);
            throw new SearchException(message, ex);
        }
    }
}
