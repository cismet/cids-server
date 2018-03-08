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
package de.cismet.cids.utils;

import Sirius.server.middleware.types.MetaClass;
import de.cismet.connectioncontext.ConnectionContext;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface MetaClassCacheService {

    //~ Methods ----------------------------------------------------------------

    @Deprecated
    MetaClass getMetaClass(String domain, String tableName);
    
    /**
     * DOCUMENT ME!
     *
     * @param   domain     DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass(String domain, String tableName, ConnectionContext connectionContext);

    @Deprecated    
    MetaClass getMetaClass(String domain, int classId);

    /**
     * DOCUMENT ME!
     *
     * @param   domain   DOCUMENT ME!
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass(String domain, int classId, ConnectionContext connectionContext);

    @Deprecated
    HashMap getAllClasses(String domain);

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    HashMap getAllClasses(String domain, ConnectionContext connectionContext);
}
