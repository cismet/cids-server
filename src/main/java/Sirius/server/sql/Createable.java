/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Creatable.java
 *
 * Created on 21. November 2003, 14:44
 */
package Sirius.server.sql;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface Createable {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object newInstance(Object[] params);
}
