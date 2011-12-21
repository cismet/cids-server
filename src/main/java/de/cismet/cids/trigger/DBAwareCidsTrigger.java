/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger;

import Sirius.server.localserver.DBServer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface DBAwareCidsTrigger extends CidsTrigger {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    DBServer getDbServer();

    /**
     * DOCUMENT ME!
     *
     * @param  dbServer  DOCUMENT ME!
     */
    void setDbServer(DBServer dbServer);
}
