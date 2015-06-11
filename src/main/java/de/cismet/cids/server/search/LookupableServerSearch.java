/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search;

import de.cismet.cids.server.api.types.SearchInfo;
import java.util.Collection;

/**
 * A Lookupable Server Search that provides SearchInfo and typed collection search results.
 * 
 * @author Pascal Dihé
 */
public interface LookupableServerSearch extends CidsServerSearch {
    
    /**
     * Returns SearchInfo describing the parameters and return type of the
     * ServerSearch instance.
     * 
     * @return SearchInfo of the Search instance
     */
    public SearchInfo getSearchInfo();
    
    
    /**
     * Performs a search on the server.
     * 
     * @param <T> type of the result collection
     * @return results of the search
     * @throws SearchException if any error occours
     * @see CidsServerSearch#performServerSearch() 
     */
    <T> Collection<T> performServerSearch() throws SearchException;
}
