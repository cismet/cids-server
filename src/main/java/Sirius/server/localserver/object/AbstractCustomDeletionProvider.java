/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.object;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCustomDeletionProvider implements CustomDeletionProvider,
    ConnectionContextStore,
    MetaServiceStore {

    //~ Instance fields --------------------------------------------------------

    private ConnectionContext connectionContext;
    private MetaService metaService;

    //~ Methods ----------------------------------------------------------------

    @Override
    public MetaService getMetaService() {
        return metaService;
    }

    @Override
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getTableName();

    @Override
    public boolean isMatching(final User user, final MetaObject metaObject) {
        return (metaObject != null)
                    && (metaObject.getMetaClass() != null)
                    && (metaObject.getMetaClass().getTableName() != null)
                    && metaObject.getMetaClass().getTableName().equalsIgnoreCase(getTableName());
    }
}
