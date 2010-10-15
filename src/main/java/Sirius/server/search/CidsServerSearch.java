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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

/**
 *
 * @author thorsten
 */
public abstract class CidsServerSearch implements Serializable {

    private transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static String ALL_DOMAINS = "##ALL_CIDS_DOMAINS##";
    public static String USER_DOMAIN = "##CIDS_USERDOMAIN##";
    private User user;
    private Hashtable activeLoaclServers;
    private HashMap<String, String> classesInSnippetsPerDomain = new HashMap<String, String>();
    private Collection<MetaClass> validClasses;
    HashMap<String, ArrayList<MetaClass>> classesPerDomain = new HashMap<String, ArrayList<MetaClass>>();

    public Hashtable getActiveLoaclServers() {
        return activeLoaclServers;
    }

    public void setActiveLoaclServers(Hashtable activeLoaclServers) {
        this.activeLoaclServers = activeLoaclServers;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Collection<MetaClass> getValidClasses() {
        return validClasses;
    }

    public void setValidClasses(Collection<MetaClass> validClasses) {
        this.validClasses = validClasses;
        classesPerDomain.clear();
        for (MetaClass mc : validClasses) {
            if (classesPerDomain.containsKey(mc.getDomain())) {
                classesPerDomain.get(mc.getDomain()).add(mc);
            } else {
                ArrayList<MetaClass> cA = new ArrayList<MetaClass>();
                cA.add(mc);
                classesPerDomain.put(mc.getDomain(), cA);
            }
        }
        classesInSnippetsPerDomain.clear();
        for (String domain : classesPerDomain.keySet()) {
            String in = StaticSearchTools.getMetaClassIdsForInStatement(classesPerDomain.get(domain));
            classesInSnippetsPerDomain.put(domain, in);
        }
    }

    public void setValidClassesFromStrings(Collection<String> classes) throws IllegalArgumentException {
        for (String classString : classes) {
            String[] sa = classString.split("@");
            if (sa == null || sa.length != 2) {
                throw new IllegalArgumentException("Strings must be of the form of classid@DOMAINNAME");
            }
            String classId = sa[0];
            String domain = sa[1];
            String inStr = classesInSnippetsPerDomain.get(domain);
            if (inStr != null) {
                classesInSnippetsPerDomain.put(domain, inStr + "," + classId);
            } else {
                classesInSnippetsPerDomain.put(domain, classId);
            }
        }
        for (String domain : classesInSnippetsPerDomain.keySet()) {
            classesInSnippetsPerDomain.put(domain, "("+classesInSnippetsPerDomain.get(domain)+")");
        }
    }

    public HashMap<String, String> getClassesInSnippetsPerDomain() {
        return classesInSnippetsPerDomain;
    }

    public HashMap<String, ArrayList<MetaClass>> getClassesPerDomain() {
        return classesPerDomain;
    }

    

    public abstract Collection performServerSearch();

    protected org.apache.log4j.Logger getLog() {
        if (log == null) {
            log = org.apache.log4j.Logger.getLogger(this.getClass());
        }
        return log;
    }
}
