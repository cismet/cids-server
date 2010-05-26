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
package Sirius.server.newuser.permission;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class Policy implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -2566287872445265371L;

    private static final int PARANOID = 0;
    private static final int WIKI = 1;

    //~ Instance fields --------------------------------------------------------

    Map<Permission, Boolean> policyMap = new HashMap<Permission, Boolean>();
    private int helpermode = 0;
    private int dbID = -1;
    private String name;
    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Policy object.
     *
     * @param  policyMap   DOCUMENT ME!
     * @param  dbID        DOCUMENT ME!
     * @param  policyName  DOCUMENT ME!
     */
    public Policy(final Map<Permission, Boolean> policyMap, final int dbID, final String policyName) {
        this.policyMap = policyMap;
        this.dbID = dbID;
        name = policyName;
    }

    /**
     * Creates a new Policy object.
     */
    private Policy() {
    }

    /**
     * Creates a new Policy object.
     *
     * @param   helperMode  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    private Policy(final int helperMode) {
        this.helpermode = helperMode;
        if ((helperMode != 0) && (helpermode != 1)) {
            throw new UnsupportedOperationException("Nur PARANOID oder WIKI moeglich");
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the decision if there is no permission set in the permission table according to the policy.
     *
     * @param   permission  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getDecisionIfNoEntryIsFound(final Permission permission) {
        final Boolean r = policyMap.get(permission);
        if (r != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug(
                    "getDecisionIfNoEntryIsFound("
                    + permission.getKey()
                    + ") returns:"
                    + r
                    + " --> Policy="
                    + name,
                    new CurrentStackTrace());
            }
            return r;
        } else {
            if (helpermode == WIKI) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(
                        "getDecisionIfNoEntryIsFound("
                        + permission.getKey()
                        + ") returns true because of Manunal WIKI Policy",
                        new CurrentStackTrace());
                }
                return true;
            } else {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(
                        "getDecisionIfNoEntryIsFound("
                        + permission.getKey()
                        + ") returns false because of PARANOID Policy or Bug",
                        new CurrentStackTrace());
                }
                return false; // Safety first
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getDbID() {
        return dbID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a Paranoid Policy ;-).
     *
     * @return  Policy which is alwas returning false if there is no Permission set
     */
    public static Policy createParanoidPolicy() {
        return new Policy();
    }

    /**
     * Creates a WIKI Policy.
     *
     * @return  Policy which is alwas returning true if there is no Permission set
     */
    public static Policy createWIKIPolicy() {
        return new Policy(WIKI);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private org.apache.log4j.Logger getLog() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
        }
        return logger;
    }

    @Override
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
        r += "defaultvalues: " + name + "= read-->" + getDecisionIfNoEntryIsFound(PermissionHolder.READPERMISSION)
            + " write-->" + getDecisionIfNoEntryIsFound(PermissionHolder.WRITEPERMISSION);
        return r;
    }
}
