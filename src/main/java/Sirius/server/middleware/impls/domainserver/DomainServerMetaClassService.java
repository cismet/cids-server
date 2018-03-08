/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.impls.domainserver;

import Sirius.server.MetaClassCache;
import Sirius.server.middleware.types.MetaClass;

import java.util.HashMap;

import de.cismet.cids.utils.MetaClassCacheService;
import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = MetaClassCacheService.class)
public class DomainServerMetaClassService implements MetaClassCacheService {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NavigatorMetaClassService object.
     */
    public DomainServerMetaClassService() {
        if (log.isDebugEnabled()) {
            log.debug("DomainServerMetaClassService inited"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    @Deprecated
    public HashMap getAllClasses(final String domain) {
        return getAllClasses(domain, ConnectionContext.createDeprecated());
    }
    
    @Override
    public HashMap getAllClasses(final String domain, final ConnectionContext connectionContext) {
        return MetaClassCache.getInstance().getAllClasses(domain);
    }

    @Override
    @Deprecated
    public MetaClass getMetaClass(final String domain, final String tableName) {
        return getMetaClass(domain, tableName, ConnectionContext.createDeprecated());
    }
    
    @Override
    public MetaClass getMetaClass(final String domain, final String tableName, final ConnectionContext connectionContext) {
        return MetaClassCache.getInstance().getMetaClass(domain, tableName);
    }

    @Override
    @Deprecated
    public MetaClass getMetaClass(final String domain, final int classId) {
        return getMetaClass(domain, classId, ConnectionContext.createDeprecated());
    }
    
    @Override
    public MetaClass getMetaClass(final String domain, final int classId, final ConnectionContext connectionContext) {
        return MetaClassCache.getInstance().getMetaClass(domain, classId);
    }
}
