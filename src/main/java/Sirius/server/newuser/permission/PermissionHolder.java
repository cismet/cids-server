/*
 * PermissionHolder.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on ???
 *
 */

package Sirius.server.newuser.permission;

import Sirius.server.newuser.UserGroup;
import Sirius.util.Mapable;
import de.cismet.tools.collections.MultiMap;
import de.cismet.tools.CurrentStackTrace;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 * Bei der Intstanzierung eines PermissionHolders erlaubt dieser zunaechst
 * jeglichen Zugriff (hasPermission ist immer wahr)
 * Sobald ein Recht für ein PermissionHolder Objekt gesetzt
 * wird (addPermission), werden allen anderen Schluesseln die Rechte
 * entzogen (restricted = true).
 */
public final class PermissionHolder implements Serializable {

    private static final transient Logger LOG = Logger.getLogger(
            PermissionHolder.class);

    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final Permission READPERMISSION =
            new Permission(READ, "read"); // NOI18N
    public static final Permission WRITEPERMISSION =
            new Permission(WRITE, "write"); // NOI18N

    /** usergroup maps visible yes/no	*/
    private final MultiMap permissions;
    private Policy policy;

    private PermissionHolder() {
        permissions = new MultiMap();
    }

    public PermissionHolder(final Policy policy) {
        this.policy = policy;
        permissions = new MultiMap();
    }

    /**  
     * adds an permission reference by lsname+class or method or attribute id
     */
    public void addPermission(final Mapable m) {

        permissions.put(m.getKey().toString(), READPERMISSION);
    }

    public void addPermissions(final PermissionHolder perms) {
        this.permissions.putAll(perms.permissions);
    }

    public void addPermission(final UserGroup ug, final Permission perm) {

        addPermission(ug.getKey().toString(), perm);
    }

    public void addPermission(final Mapable m, final Permission perm) {

        addPermission(m.getKey().toString(), perm);
    }

    public void addPermission(final Object key, final Permission perm) {
        permissions.put(key.toString(), perm);
    }

    public boolean hasReadPermission(final UserGroup ug) {
        try {
            return hasPermission(ug.getKey().toString(), READPERMISSION);
        } catch (final Exception e) {
            LOG.error("error in hasReadPermission (ug = " // NOI18N
                    + ug
                    + "). Will return false.", e); // NOI18N
            return false;
        }
    }

    public boolean hasWritePermission(final UserGroup ug) {
        try {
            return hasPermission(ug.getKey().toString(), READPERMISSION);
        } catch (final Exception e) {
            LOG.error("Error in hasWritePermission (ug = " // NOI18N
                    + ug
                    + "). Will return false.", e); // NOI18N
            return false;
        }

    }

    /**	
     * checks if theres a Permission for an ordered pair of lsname+id
     */
    public boolean hasPermission(final Object key, final Permission perm) {
        if (getPolicy()==null) {
            LOG.warn("No Policy was set. Set PARANOID Policy. " // NOI18N
                    + "Attention. This could lead to something " // NOI18N
                    + "that you not want.", new CurrentStackTrace());
            setPolicy(Policy.createParanoidPolicy());
        }
        if (containsPermission(key, perm)) {
            return !getPolicy().getDecisionIfNoEntryIsFound(perm);
        } else {
            return getPolicy().getDecisionIfNoEntryIsFound(perm);
        }
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(final Policy policy) {
        this.policy = policy;
    }

    private boolean containsPermission(final Object key, final Permission perm){
        return permissions.contains(key, perm);
    }
}
