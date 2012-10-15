/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser.permission;

import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import java.io.Serializable;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.collections.MultiMap;

/**
 * Bei der Intstanzierung eines PermissionHolders erlaubt dieser zunaechst jeglichen Zugriff (hasPermission ist immer
 * wahr) Sobald ein Recht für ein PermissionHolder Objekt gesetzt wird (addPermission), werden allen anderen Schluesseln
 * die Rechte entzogen (restricted = true).
 *
 * @version  $Revision$, $Date$
 */
public final class PermissionHolder implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            PermissionHolder.class);

    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final Permission READPERMISSION = new Permission(READ, "read");    // NOI18N
    public static final Permission WRITEPERMISSION = new Permission(WRITE, "write"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    /** usergroup maps visible yes/no. */
    private final MultiMap permissions;
    private Policy policy;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PermissionHolder object.
     *
     * @param  policy  DOCUMENT ME!
     */
    public PermissionHolder(final Policy policy) {
        this.policy = policy;
        permissions = new MultiMap();
    }

    /**
     * Creates a new PermissionHolder object.
     */
    private PermissionHolder() {
        permissions = new MultiMap();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * adds an permission reference by lsname+class or method or attribute id.
     *
     * @param  m  DOCUMENT ME!
     */
    public void addPermission(final Mapable m) {
        permissions.put(m.getKey().toString(), READPERMISSION);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  perms  DOCUMENT ME!
     */
    public void addPermissions(final PermissionHolder perms) {
        this.permissions.putAll(perms.permissions);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ug    DOCUMENT ME!
     * @param  perm  DOCUMENT ME!
     */
    public void addPermission(final UserGroup ug, final Permission perm) {
        addPermission(ug.getKey().toString(), perm);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  m     DOCUMENT ME!
     * @param  perm  DOCUMENT ME!
     */
    public void addPermission(final Mapable m, final Permission perm) {
        addPermission(m.getKey().toString(), perm);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key   DOCUMENT ME!
     * @param  perm  DOCUMENT ME!
     */
    public void addPermission(final Object key, final Permission perm) {
        permissions.put(key.toString(), perm);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasReadPermission(final User user) {
        final UserGroup userGroup = user.getUserGroup();
        try {
            if (userGroup != null) {
                return hasPermission(userGroup.getKey().toString(), READPERMISSION);
            } else {
                for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                    if (hasPermission(potentialUserGroup.getKey().toString(), READPERMISSION)) {
                        LOG.fatal("BAM !!!");
                        return true;
                    } else {
                        LOG.fatal("nitt !!!");
                    }
                }
                return false;
            }
        } catch (final Exception e) {
            LOG.error("error in hasReadPermission (ug = " // NOI18N
                        + userGroup
                        + "). Will return false.", e); // NOI18N
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasWritePermission(final User user) {
        final UserGroup userGroup = user.getUserGroup();
        if (userGroup != null) {
            try {
                return hasPermission(userGroup.getKey().toString(), WRITEPERMISSION);
            } catch (final Exception e) {
                LOG.error("Error in hasWritePermission (ug = " // NOI18N
                            + userGroup
                            + "). Will return false.", e); // NOI18N
                return false;
            }
        } else {
            for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                if (hasPermission(potentialUserGroup.getKey().toString(), WRITEPERMISSION)) {
                    return true;
                }
            }
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
                new CurrentStackTrace());
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
        return permissions.contains(key, perm);
    }
}
