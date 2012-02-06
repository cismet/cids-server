/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser.permission;
import Sirius.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PermissionHolderMap extends java.util.Hashtable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PermissionHolderMap object.
     *
     * @param  size  DOCUMENT ME!
     */
    public PermissionHolderMap(final int size) {
        super(size);
    }
    /**
     * Creates a new PermissionHolderMap object.
     *
     * @param  size        DOCUMENT ME!
     * @param  loadFactor  DOCUMENT ME!
     */
    public PermissionHolderMap(final int size, final float loadFactor) {
        super(size, loadFactor);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PermissionHolder getPermissionHolder(final String key) {
        return (PermissionHolder)get(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     * @param  ph   DOCUMENT ME!
     */
    public void add(final String key, final PermissionHolder ph) {
        put(key, ph);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  m  DOCUMENT ME!
     */
    public void add(final Mapable m) {
        put(m.getKey(), m);
    }

    @Override
    public void rehash() {
        super.rehash();
    }
}
