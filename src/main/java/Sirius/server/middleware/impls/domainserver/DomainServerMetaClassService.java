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

import Sirius.server.middleware.types.MetaClass;

import java.util.HashMap;

import de.cismet.cids.utils.MetaClassCacheService;

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
    public HashMap getAllClasses(final String domain) {
        return DomainServerClassCache.getInstance().getAllClasses();
    }

    @Override
    public MetaClass getMetaClass(final String domain, final String tableName) {
        return DomainServerClassCache.getInstance().getMetaClass(tableName);
    }

    @Override
    public MetaClass getMetaClass(final String domain, final int classId) {
        return DomainServerClassCache.getInstance().getMetaClass(classId);
    }
    
    
    
}
