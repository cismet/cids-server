/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.search;

import java.util.Collection;

import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.SearchException;

import de.cismet.cidsx.server.api.types.SearchInfo;

/**
 * A Lookupable Server Search that provides SearchInfo and typed collection search results.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public interface RestApiCidsServerSearch extends CidsServerSearch {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns SearchInfo describing the parameters and return type of the ServerSearch instance.
     *
     * @return  SearchInfo of the Search instance
     */
    SearchInfo getSearchInfo();

    /**
     * Performs a search on the server.
     *
     * @return  results of the search
     *
     * @throws  SearchException  if any error occours
     *
     * @see     CidsServerSearch#performServerSearch()
     */
    @Override
    Collection performServerSearch() throws SearchException;
}
