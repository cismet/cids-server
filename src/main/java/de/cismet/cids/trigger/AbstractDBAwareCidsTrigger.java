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
public abstract class AbstractDBAwareCidsTrigger extends AbstractCidsTrigger implements DBAwareCidsTrigger {

    //~ Instance fields --------------------------------------------------------

    protected DBServer dbServer;

    //~ Methods ----------------------------------------------------------------

    @Override
    public DBServer getDbServer() {
        return dbServer;
    }

    @Override
    public void setDbServer(final DBServer dbServer) {
        this.dbServer = dbServer;
    }
}
