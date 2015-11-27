/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search;

import Sirius.server.middleware.types.MetaObjectNode;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author      martin.scholl@cismet.de
 * @version     $Revision$, $Date$
 * @deprecated  use de.cismet.cidsx.server.search.RestApiCidsServerSearch instead
 */
public interface MetaObjectNodeServerSearch extends CidsServerSearch {

    //~ Methods ----------------------------------------------------------------

    @Override
    Collection<MetaObjectNode> performServerSearch() throws SearchException;
}
