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
public interface CatalogueServiceStore {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    void setCatalogueService(CatalogueService service);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CatalogueService getCatalogueService();
}
