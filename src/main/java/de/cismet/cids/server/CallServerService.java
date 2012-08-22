/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server;

import Sirius.server.middleware.interfaces.proxy.*;

/**
 * Cumulated callserver interface.
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface CallServerService extends CatalogueService,
    MetaService,
    QueryStore,
    SearchService,
    SystemService,
    UserService,
    ActionService {
}
