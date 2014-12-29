/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;

import org.openide.util.lookup.ServiceProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = DialectProvider.class)
public final class DefaultDialectProvider implements DialectProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getDialect() {
        if (DomainServerImpl.getServerProperties() == null) {
            return null;
        } else {
            return DomainServerImpl.getServerProperties().getInteralDialect();
        }
    }
}
