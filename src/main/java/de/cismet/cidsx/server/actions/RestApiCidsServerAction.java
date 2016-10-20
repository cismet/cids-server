/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.actions;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.cidsx.server.api.types.ActionInfo;
import de.cismet.cidsx.server.api.types.GenericResourceWithContentType;

/**
 * A Lookupable Server Action that provides ActionInfo.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public interface RestApiCidsServerAction extends ServerAction {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns ActionInfo describing the (body) parameters and return type of the ActionInfo instance.
     *
     * @return  SearchInfo of the Search instance
     */
    ActionInfo getActionInfo();
    @Override
    GenericResourceWithContentType execute(Object body, ServerActionParameter... params);
}
