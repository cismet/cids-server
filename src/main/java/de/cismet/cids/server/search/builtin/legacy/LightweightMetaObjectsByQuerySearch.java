/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin.legacy;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.LightweightMetaObject;

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
import de.cismet.cids.server.search.RestApiCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * Builtin Legacy Search to delegate the operation getLightweightMetaObjectsByQuery to the cids Pure REST Search API.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = RestApiCidsServerSearch.class)
public class LightweightMetaObjectsByQuerySearch extends AbstractCidsServerSearch implements RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(LightweightMetaObjectsByQuerySearch.class);

    //~ Instance fields --------------------------------------------------------

    @Getter
    private final SearchInfo searchInfo;
    @Getter
    @Setter
    private String domain;
    @Getter
    @Setter
    private int classId;
    @Getter
    @Setter
    private String query;
    @Getter
    @Setter
    private String[] representationFields;
    @Getter
    @Setter
    private String representationPattern;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LightweightMetaObjectsByQuerySearch object.
     */
    public LightweightMetaObjectsByQuerySearch() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription(
            "Builtin Legacy Search to delegate the operation getLightweightMetaObjectsByQuery to the cids Pure REST Search API.");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<SearchParameterInfo>();
        SearchParameterInfo searchParameterInfo;

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("domain");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("classId");
        searchParameterInfo.setType(Type.INTEGER);
        parameterDescription.add(searchParameterInfo);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("query");
        searchParameterInfo.setDescription(
            "the query must generate a result set with columns legacy objectId and all specified representationFields");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("representationFields");
        searchParameterInfo.setType(Type.STRING);
        searchParameterInfo.setArray(true);
        parameterDescription.add(searchParameterInfo);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("representationPattern");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.ENTITY_REFERENCE);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService metaService = (MetaService)this.getActiveLocalServers().get(this.getDomain());
        if (metaService == null) {
            final String message = "Lightweight Meta Objects By Query Search "
                        + "could not connect ot MetaService @domain '" + this.domain + "'";
            LOG.error(message);
            throw new SearchException(message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("performing search for Lightweight Meta Objects for class '"
                        + this.classId + "' with query '" + this.getQuery() + "'");
        }
        try {
            final LightweightMetaObject[] lightWightMetaObjects;

            if ((this.representationPattern != null) && !this.representationPattern.isEmpty()) {
                lightWightMetaObjects = metaService.getLightweightMetaObjectsByQuery(
                        classId,
                        this.getUser(),
                        query,
                        representationFields,
                        representationPattern);
            } else {
                lightWightMetaObjects = metaService.getLightweightMetaObjectsByQuery(
                        classId,
                        this.getUser(),
                        query,
                        representationFields);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(lightWightMetaObjects.length + " Lightweight Meta Objects found");
            }
            return Arrays.asList(lightWightMetaObjects);
        } catch (RemoteException ex) {
            final String message = "could not perform Lightweight Meta Objects By Query Search for class '"
                        + this.classId + "' with query '" + this.getQuery() + "': " + ex.getMessage();
            LOG.error(message, ex);
            throw new SearchException(message, ex);
        }
    }
}
