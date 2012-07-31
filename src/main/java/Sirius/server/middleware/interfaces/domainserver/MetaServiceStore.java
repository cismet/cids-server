/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface MetaServiceStore {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  sevice  DOCUMENT ME!
     */
    void setMetaService(MetaService sevice);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaService getMetaService();
}
