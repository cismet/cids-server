/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search;

import Sirius.server.MetaClassCache;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for the handling of {@link CidsServerSearch}es. For compatibility reasons this class extends the
 * {@link Sirius.server.search.CidsServerSearch} class
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCidsServerSearch implements CidsServerSearch {

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Collection<MetaClass>> classesPerDomain;

    private User user;
    private Map activeLocalServers;
    private Map<String, Set<String>> classesInSnippetsPerDomain;
    private Collection<MetaClass> validClasses;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractCidsServerSearch object.
     */
    public AbstractCidsServerSearch() {
        classesPerDomain = new HashMap<>();
        classesInSnippetsPerDomain = new HashMap<>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  user  DOCUMENT ME!
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection<MetaClass> getValidClasses() {
        return validClasses;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  validClasses  DOCUMENT ME!
     */
    @Override
    public void setValidClasses(final Collection<MetaClass> validClasses) {
        this.validClasses = validClasses;
        classesPerDomain.clear();
        for (final MetaClass mc : validClasses) {
            if (classesPerDomain.containsKey(mc.getDomain())) {
                classesPerDomain.get(mc.getDomain()).add(mc);
            } else {
                final ArrayList<MetaClass> cA = new ArrayList<>();
                cA.add(mc);
                classesPerDomain.put(mc.getDomain(), cA);
            }
        }
        classesInSnippetsPerDomain.clear();
        for (final String domain : classesPerDomain.keySet()) {
            classesInSnippetsPerDomain.put(
                domain,
                StaticSearchTools.getMetaClassIdsForInStatement(classesPerDomain.get(domain)));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @Override
    public void setValidClassesFromStrings(final Collection<String> classes) throws IllegalArgumentException {
        classesInSnippetsPerDomain.clear();
        for (final String classString : classes) {
            final String[] sa = classString.split("@");
            if ((sa == null) || (sa.length != 2)) {
                throw new IllegalArgumentException("Strings must be of the form of classid@DOMAINNAME");
            }
            final String classKey = sa[0];
            final String domain = sa[1];
            final Set<String> inStr = classesInSnippetsPerDomain.get(domain);
            if (inStr != null) {
                inStr.add(classKey);
            } else {
                classesInSnippetsPerDomain.put(domain, new HashSet<>(Arrays.asList(classKey)));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Map<String, Set<String>> getClassesInSnippetsPerDomain() {
        return classesInSnippetsPerDomain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Map<String, Collection<MetaClass>> getClassesPerDomain() {
        return classesPerDomain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classesInSnippetsPerDomain  DOCUMENT ME!
     */
    @Override
    public void setClassesInSnippetsPerDomain(final Map<String, Set<String>> classesInSnippetsPerDomain) {
        this.classesInSnippetsPerDomain = classesInSnippetsPerDomain;
    }

    @Override
    public Map getActiveLocalServers() {
        return activeLocalServers;
    }

    @Override
    public void setActiveLocalServers(final Map activeLocalServers) {
        this.activeLocalServers = activeLocalServers;
    }
}
