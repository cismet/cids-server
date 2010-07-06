/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server;

import Sirius.server.middleware.interfaces.proxy.CatalogueService;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.middleware.interfaces.proxy.QueryStore;
import Sirius.server.middleware.interfaces.proxy.SearchService;
import Sirius.server.middleware.interfaces.proxy.SystemService;
import Sirius.server.middleware.interfaces.proxy.UserService;

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
    UserService {
}
