/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser.permission;

import Sirius.server.newuser.UserGroup;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.cismet.commons.utils.StackUtils;

/**
 * Bei der Intstanzierung eines PermissionHolders erlaubt dieser zunaechst jeglichen Zugriff (hasPermission ist immer
 * wahr) Sobald ein Recht für ein PermissionHolder Objekt gesetzt wird (addPermission), werden allen anderen Schluesseln
 * die Rechte entzogen (restricted = true).
 *
 * @version  $Revision$, $Date$
 */
public final class PermissionHolder implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(PermissionHolder.class);

    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final Permission READPERMISSION = new Permission(READ, "read");    // NOI18N
    public static final Permission WRITEPERMISSION = new Permission(WRITE, "write"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    /** usergroup maps visible yes/no. */
    private final Map<String, LinkedHashSet<Permission>> permissions;
    private Policy policy;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PermissionHolder object.
     *
     * @param  policy  DOCUMENT ME!
     */
    public PermissionHolder(final Policy policy) {
        this.policy = policy;
        permissions = new HashMap<String, LinkedHashSet<Permission>>();
    }

    /**
     * Creates a new PermissionHolder object.
     */
    private PermissionHolder() {
        this(null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * adds an permission reference by lsname+class or method or attribute id.
     *
     * @param  m  DOCUMENT ME!
     */
    public void addPermission(final Mapable m) {
        addPermission(m.getKey(), READPERMISSION);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  perms  DOCUMENT ME!
     */
    public void addPermissions(final PermissionHolder perms) {
        for (final Entry<String, LinkedHashSet<Permission>> entry : perms.permissions.entrySet()) {
            final String key = entry.getKey();
            for (final Permission perm : entry.getValue()) {
                addPermission(key, perm);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ug    DOCUMENT ME!
     * @param  perm  DOCUMENT ME!
     */
    public void addPermission(final UserGroup ug, final Permission perm) {
        addPermission(ug.getKey(), perm);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  m     DOCUMENT ME!
     * @param  perm  DOCUMENT ME!
     */
    public void addPermission(final Mapable m, final Permission perm) {
        addPermission(m.getKey(), perm);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  keyObj  DOCUMENT ME!
     * @param  perm    DOCUMENT ME!
     */
    public void addPermission(final Object keyObj, final Permission perm) {
        final String key = keyObj.toString();
        if (!permissions.containsKey(key)) {
            permissions.put(key, new LinkedHashSet<Permission>());
        }

        permissions.get(key).add(perm);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasReadPermission(final UserGroup ug) {
        try {
            return hasPermission(ug.getKey(), READPERMISSION);
        } catch (final Exception e) {
            LOG.error("error in hasReadPermission (ug = " + ug + "). Will return false.", e); // NOI18N

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasWritePermission(final UserGroup ug) {
        try {
            return hasPermission(ug.getKey(), WRITEPERMISSION);
        } catch (final Exception e) {
            LOG.error("Error in hasWritePermission (ug = " + ug + "). Will return false.", e); // NOI18N

            return false;
        }
    }

    /**
     * checks if theres a Permission for an ordered pair of lsname+id.
     *
     * @param   key   DOCUMENT ME!
     * @param   perm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasPermission(final Object key, final Permission perm) {
        if (getPolicy() == null) {
            LOG.warn(
                "No Policy was set. Set PARANOID Policy. "           // NOI18N
                        + "Attention. This could lead to something " // NOI18N
                        + "that you not want.",                      // NOI18N
                StackUtils.getDebuggingThrowable());
            setPolicy(Policy.createParanoidPolicy());
        }

        if (containsPermission(key, perm)) {
            return !getPolicy().getDecisionIfNoEntryIsFound(perm);
        } else {
            return getPolicy().getDecisionIfNoEntryIsFound(perm);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  policy  DOCUMENT ME!
     */
    public void setPolicy(final Policy policy) {
        this.policy = policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key   DOCUMENT ME!
     * @param   perm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean containsPermission(final Object key, final Permission perm) {
        final Collection c = permissions.get(key.toString());

        return (c == null) ? false : c.contains(perm);
    }
}
