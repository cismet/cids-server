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
public interface UserServiceStore {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    void setUserService(UserService service);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    UserService getUserService();
}
