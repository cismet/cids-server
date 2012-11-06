/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import de.cismet.ext.CExtContext;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SearchCExtContext extends CExtContext {

    //~ Static fields/initializers ---------------------------------------------

    public static final String CTX_PROP_GEOMETRY = "__ctx_prop_geometry__";             // NOI18N
    public static final String CTX_PROP_SEARCH_TEXT = "__ctx_prop_search_text__";       // NOI18N
    public static final String CTX_PROP_CASE_SENSITIVE = "__ctx_prop_case_sensitive__"; // NOI18N

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Search {

        //~ Enum constants -----------------------------------------------------

        FULLTEXT, SPATIAL
    }

    //~ Instance fields --------------------------------------------------------

    private Search search;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchCExtContext object.
     */
    public SearchCExtContext() {
    }

    /**
     * Creates a new SearchCExtContext object.
     *
     * @param  search  DOCUMENT ME!
     */
    public SearchCExtContext(final Search search) {
        this.search = search;
    }

    /**
     * Creates a new SearchCExtContext object.
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public SearchCExtContext(final String key, final Object value) {
        super(key, value);
    }

    /**
     * Creates a new SearchCExtContext object.
     *
     * @param  search  DOCUMENT ME!
     * @param  key     DOCUMENT ME!
     * @param  value   DOCUMENT ME!
     */
    public SearchCExtContext(final Search search, final String key, final Object value) {
        super(key, value);
        this.search = search;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Search getSearch() {
        return search;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  search  DOCUMENT ME!
     */
    public void setSearch(final Search search) {
        this.search = search;
    }
}
