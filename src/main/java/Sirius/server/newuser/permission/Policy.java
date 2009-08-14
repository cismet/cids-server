/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.newuser.permission;

import de.cismet.tools.CurrentStackTrace;
import java.io.Serializable;
import java.util.HashMap;


/**
 *
 * @author hell
 */
public class Policy implements Serializable {

    private final static int PARANOID = 0;
    private final static int WIKI = 1;
    private int helpermode = 0;
    private int dbID = -1;
    private String name;
    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    HashMap<Permission, Boolean> policyMap = new HashMap<Permission, Boolean>();

    private Policy() {
    }

    private Policy(int helperMode) {
        this.helpermode = helperMode;
        if (helperMode != 0 && helpermode != 1) {
            throw new UnsupportedOperationException("Nur PARANOID oder WIKI moeglich");
        }
    }

    public Policy(HashMap<Permission, Boolean> policyMap, int dbID, String policyName) {
        this.policyMap = policyMap;
        this.dbID = dbID;
        name = policyName;
    }

    /**
     * Returns the decision if there is no permission set in the permission 
     * table according to the policy
     * @param permission
     * @return
     */
    public boolean getDecisionIfNoEntryIsFound(Permission permission) {
        Boolean r = policyMap.get(permission);
        if (r != null) {
            getLog().debug("getDecisionIfNoEntryIsFound(" + permission.getKey() + ") returns:" + r+" --> Policy="+name,new CurrentStackTrace());
            return r;
        } else {
            if (helpermode == WIKI) {
                getLog().debug("getDecisionIfNoEntryIsFound(" + permission.getKey() + ") returns true because of Manunal WIKI Policy", new CurrentStackTrace());
                return true;
            } else {
                getLog().debug("getDecisionIfNoEntryIsFound(" + permission.getKey() + ") returns false because of PARANOID Policy or Bug", new CurrentStackTrace());
                return false; //Safety first
            }
        }
    }

    public int getDbID() {
        return dbID;
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a Paranoid Policy ;-)
     * 
     * @return Policy which is alwas returning false if there is no Permission set
     */
    public static Policy createParanoidPolicy() {
        return new Policy();
    }

    /**
     * Creates a WIKI Policy 
     * 
     * @return Policy which is alwas returning true if there is no Permission set
     */
    public static Policy createWIKIPolicy() {
        return new Policy(WIKI);
    }

    private org.apache.log4j.Logger getLog() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
        }
        return logger;
    }

    public String toString() {
        String r = "Policy: ";
        if (dbID == -1) {
            r += "(artificial: ";
            if (helpermode == 1) {
                r += "WIKI";
            } else {
                r += "PARANOID";
            }
            r += ") ";
        }
        r += "defaultvalues: "+name+"= read-->" + getDecisionIfNoEntryIsFound(PermissionHolder.READPERMISSION) + " write-->" + getDecisionIfNoEntryIsFound(PermissionHolder.WRITEPERMISSION);
        return r;
    }
}
