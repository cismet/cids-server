/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Sirius.server.search;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public abstract class CidsServerSearch implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsServerSearch.class);

    public static String ALL_DOMAINS = "##ALL_CIDS_DOMAINS##";
    public static String USER_DOMAIN = "##CIDS_USERDOMAIN##";

    //~ Instance fields --------------------------------------------------------

    private final HashMap<String, ArrayList<MetaClass>> classesPerDomain = new HashMap<String, ArrayList<MetaClass>>();

    private User user;
    private Hashtable activeLoaclServers;
    private HashMap<String, String> classesInSnippetsPerDomain = new HashMap<String, String>();
    private Collection<MetaClass> validClasses;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Hashtable getActiveLoaclServers() {
        return activeLoaclServers;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  activeLoaclServers  DOCUMENT ME!
     */
    public void setActiveLoaclServers(final Hashtable activeLoaclServers) {
        this.activeLoaclServers = activeLoaclServers;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public User getUser() {
        return user;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  user  DOCUMENT ME!
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<MetaClass> getValidClasses() {
        return validClasses;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  validClasses  DOCUMENT ME!
     */
    public void setValidClasses(final Collection<MetaClass> validClasses) {
        this.validClasses = validClasses;
        classesPerDomain.clear();
        for (final MetaClass mc : validClasses) {
            if (classesPerDomain.containsKey(mc.getDomain())) {
                classesPerDomain.get(mc.getDomain()).add(mc);
            } else {
                final ArrayList<MetaClass> cA = new ArrayList<MetaClass>();
                cA.add(mc);
                classesPerDomain.put(mc.getDomain(), cA);
            }
        }
        classesInSnippetsPerDomain.clear();
        for (final String domain : classesPerDomain.keySet()) {
            final String in = StaticSearchTools.getMetaClassIdsForInStatement(classesPerDomain.get(domain));
            classesInSnippetsPerDomain.put(domain, in);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void setValidClassesFromStrings(final Collection<String> classes) throws IllegalArgumentException {
        for (final String classString : classes) {
            final String[] sa = classString.split("@");
            if ((sa == null) || (sa.length != 2)) {
                throw new IllegalArgumentException("Strings must be of the form of classid@DOMAINNAME");
            }
            final String classId = sa[0];
            final String domain = sa[1];
            final String inStr = classesInSnippetsPerDomain.get(domain);
            if (inStr != null) {
                classesInSnippetsPerDomain.put(domain, inStr + "," + classId);
            } else {
                classesInSnippetsPerDomain.put(domain, classId);
            }
        }
        for (final String domain : classesInSnippetsPerDomain.keySet()) {
            classesInSnippetsPerDomain.put(domain, "(" + classesInSnippetsPerDomain.get(domain) + ")");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap<String, String> getClassesInSnippetsPerDomain() {
        return classesInSnippetsPerDomain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap<String, ArrayList<MetaClass>> getClassesPerDomain() {
        return classesPerDomain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classesInSnippetsPerDomain  DOCUMENT ME!
     */
    public void setClassesInSnippetsPerDomain(final HashMap<String, String> classesInSnippetsPerDomain) {
        this.classesInSnippetsPerDomain = classesInSnippetsPerDomain;
    }

    /**
     * Performs the specified search.
     *
     * <p>Be aware that this method runs in server context and terefore has no access to the Navigator or cismap.</p>
     *
     * @return  The objects matching the specified search.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract Collection performServerSearch() throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Logger getLog() {
        return LOG;
    }
}
