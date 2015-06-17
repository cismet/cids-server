/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin.legacy;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObject;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.base.types.Type;

import de.cismet.cids.server.api.types.SearchInfo;
import de.cismet.cids.server.api.types.SearchParameterInfo;
import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.LookupableServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * Builtin Legacy Search to delegate the operation getMetaObjects(String query, ...) the cids Pure REST Search API.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = LookupableServerSearch.class)
public class MetaObjectsByQuerySearch extends AbstractCidsServerSearch implements LookupableServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MetaObjectsByQuerySearch.class);

    //~ Instance fields --------------------------------------------------------

    @Getter
    private final SearchInfo searchInfo;
    @Getter
    @Setter
    private String domain;
    @Getter
    @Setter
    private String query;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LightweightMetaObjectsByQuerySearch object.
     */
    public MetaObjectsByQuerySearch() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription(
            "Builtin Legacy Search to delegate the operation getMetaObjects(String query, ...) to the cids Pure REST Search API.");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<SearchParameterInfo>();
        SearchParameterInfo searchParameterInfo;

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("domain");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("query");
        searchParameterInfo.setDescription(
            "the query must generate a result set with columns legacy classId and legacy objectId");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.ENTITY);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService metaService = (MetaService)this.getActiveLocalServers().get(this.getDomain());
        if (metaService == null) {
            final String message = "Meta Objects By Query Search "
                        + "could not connect ot MetaService @domain '" + this.domain + "'";
            LOG.error(message);
            throw new SearchException(message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("performing search for Meta Objects By Query with query '"
                        + this.getQuery() + "'");
        }
        try {
            final MetaObject[] metaObjects = metaService.getMetaObject(this.getUser(), this.getQuery());

            if (LOG.isDebugEnabled()) {
                LOG.debug(metaObjects.length + " Meta Objects found.");
            }
            return Arrays.asList(metaObjects);
        } catch (RemoteException ex) {
            final String message = "could not perform Meta Objects By Query Search with query '"
                        + this.getQuery() + "': " + ex.getMessage();
            LOG.error(message, ex);
            throw new SearchException(message, ex);
        }
    }
}
