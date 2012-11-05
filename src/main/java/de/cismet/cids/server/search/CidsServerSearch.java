/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import java.io.Serializable;

import java.util.Collection;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface CidsServerSearch extends Serializable {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Map getActiveLocalServers();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Map<String, String> getClassesInSnippetsPerDomain();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Map<String, Collection<MetaClass>> getClassesPerDomain();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    User getUser();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<MetaClass> getValidClasses();

    /**
     * Performs the specified search.
     *
     * <p>Be aware that this method runs in server context and terefore has no access to the Navigator or cismap.</p>
     *
     * @return  The objects matching the specified search.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    Collection performServerSearch() throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param  activeLocalServers  DOCUMENT ME!
     */
    void setActiveLocalServers(final Map activeLocalServers);

    /**
     * DOCUMENT ME!
     *
     * @param  classesInSnippetsPerDomain  DOCUMENT ME!
     */
    void setClassesInSnippetsPerDomain(final Map<String, String> classesInSnippetsPerDomain);

    /**
     * DOCUMENT ME!
     *
     * @param  user  DOCUMENT ME!
     */
    void setUser(final User user);

    /**
     * DOCUMENT ME!
     *
     * @param  validClasses  DOCUMENT ME!
     */
    void setValidClasses(final Collection<MetaClass> validClasses);

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    void setValidClassesFromStrings(final Collection<String> classes) throws IllegalArgumentException;
}
