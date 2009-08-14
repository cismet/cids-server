package Sirius.server.newuser.permission;

import Sirius.server.newuser.UserGroup;
import de.cismet.tools.collections.MultiMap;
import Sirius.util.*;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.collections.SyncLinkedList;
import java.util.*;

/**
 * Bei der Intstanzierung eines PermissionHolders erlaubt dieser zunaechst jeglichen Zugriff (hasPermission ist immer wahr)
 * Sobald ein Recht f\u00FCr ein PermissionHolder Object gesetzt wird (addPermission), werden allen anderen Schluesseln die Rechte
 * entzogen (restricted = true).
 *
 */
public class PermissionHolder implements java.io.Serializable {

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private Policy policy;
    public static final int READ = 0;
    public static final int WRITE = 1;
    public static Permission READPERMISSION = new Permission(0, "read");
    public static Permission WRITEPERMISSION = new Permission(1, "write");
    /** usergroup maps visible yes/no	*/
    protected MultiMap permissions;
    ///** visible in general			*/
    //  protected boolean restricted;
    private PermissionHolder() {

        permissions = new MultiMap();
    //	  restricted  = false;
    }

    public PermissionHolder(Policy policy) {
        this.policy = policy;
        permissions = new MultiMap();
    //	  restricted  = false;
    }
    //-----------------------------------------------------------------------------
    /**  adds an permission reference by lsname+class or method or attribute id*/
    public final void addPermission(Mapable m) {

        permissions.put(m.getKey().toString(), READPERMISSION);
    }
    //------------------------------------------------------------------
    public final void addPermissions(PermissionHolder perms) {
        this.permissions.putAll(perms.permissions);
    }

    public final void addPermission(UserGroup ug, Permission perm) {

        addPermission(ug.getKey().toString(), perm);
    }

    public final void addPermission(Mapable m, Permission perm) {

        addPermission(m.getKey().toString(), perm);
    }

    public final void addPermission(Object key, Permission perm) {
        //logger.debug("addPermissison ::  "+key+"  "+perm);
        permissions.put(key.toString(), perm);
    }

    public final boolean hasReadPermission(UserGroup ug) {
        try {
            return hasPermission(ug.getKey().toString(), READPERMISSION);
        } catch (Exception e) {
             if (logger!=null) logger.error("Error in hasReadPermission (ug=" + ug + "). Will return false.", e);
            return false;
        }
    }

    public final boolean hasWritePermission(UserGroup ug) {
        try {
            return hasPermission(ug.getKey().toString(), READPERMISSION);
        } catch (Exception e) {
            logger.error("Error in hasWritePermission (ug=" + ug + "). Will return false.", e);
            return false;
        }

    }

    /**	checks if theres a Permission for an ordered pair of lsname+id	*/
    public final boolean hasPermission(Object key, Permission perm) throws Exception {
        if (getPolicy()==null) {
            logger.warn("No Policy was set. Set PARANOID Policy. Attention. This could lead to something that you not want.",new CurrentStackTrace());
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

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
    //------------------------------------------------------------------------------
    protected boolean containsPermission(Object key, Permission perm) {
        return permissions.contains(key, perm);
    }

//    public String toString() {
//        String tmp = " KEYS :: \n";
//        Iterator iter = permissions.values().iterator();
//        Iterator keyIter = permissions.keySet().iterator();
//
//
//        while (keyIter.hasNext()) {
//            tmp += keyIter.next() + "\n";
//        }
//        tmp += "----------------\nValues :: \n";
//        while (iter.hasNext()) {
//            SyncLinkedList l = (SyncLinkedList) iter.next();
//
//            Iterator subIter = l.iterator();
//
//            while (subIter.hasNext()) {
//                tmp += subIter.next() + "\n";
//            }
//
//        }
//
//        return tmp;
//
//    }
}
