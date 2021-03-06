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

import java.util.HashMap;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface MetaClassCacheService {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   domain     DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    MetaClass getMetaClass(String domain, String tableName);

    /**
     * DOCUMENT ME!
     *
     * @param   domain             DOCUMENT ME!
     * @param   tableName          DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass(String domain, String tableName, ConnectionContext connectionContext);

    /**
     * DOCUMENT ME!
     *
     * @param   domain   DOCUMENT ME!
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    MetaClass getMetaClass(String domain, int classId);

    /**
     * DOCUMENT ME!
     *
     * @param   domain             DOCUMENT ME!
     * @param   classId            DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass(String domain, int classId, ConnectionContext connectionContext);

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    HashMap getAllClasses(String domain);

    /**
     * DOCUMENT ME!
     *
     * @param   domain             DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    HashMap getAllClasses(String domain, ConnectionContext connectionContext);
}
