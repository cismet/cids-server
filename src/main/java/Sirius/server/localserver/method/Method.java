/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.method;

import Sirius.util.*;

import Sirius.server.newuser.permission.*;

import Sirius.util.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Method implements java.io.Serializable, Cloneable, Mapable {

    //~ Instance fields --------------------------------------------------------

    protected int id;

    protected PermissionHolder permissions;

    protected ArrayList classKeys = new ArrayList();

    // mapable by name
    protected String plugin_id;

    protected String method_id;

    protected String description;

    boolean o_multiple;

    boolean c_multiple;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------------- public Method(int id){this.id
     * = id;} //dummy.
     *
     * @param  id          DOCUMENT ME!
     * @param  plugin_id   DOCUMENT ME!
     * @param  method_id   DOCUMENT ME!
     * @param  c_multiple  DOCUMENT ME!
     * @param  o_multiple  DOCUMENT ME!
     * @param  policy      DOCUMENT ME!
     */
    public Method(int id, String plugin_id, String method_id, boolean c_multiple, boolean o_multiple, Policy policy) {
        this.id = id;
        this.plugin_id = plugin_id;
        this.method_id = method_id;
        if (policy == null) {
            policy = Policy.createParanoidPolicy();
        }
        permissions = new PermissionHolder(policy);
        description = "";
        this.c_multiple = c_multiple; // beliebig viele klassen beliebig viele Objekte
        this.o_multiple = o_multiple; // 1 klasse mehrer Objekte
    }

    /**
     * Creates a new Method object.
     *
     * @param  id           DOCUMENT ME!
     * @param  plugin_id    DOCUMENT ME!
     * @param  method_id    DOCUMENT ME!
     * @param  c_multiple   DOCUMENT ME!
     * @param  o_multiple   DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  policy       DOCUMENT ME!
     */
    public Method(
            int id,
            String plugin_id,
            String method_id,
            boolean c_multiple,
            boolean o_multiple,
            String description,
            Policy policy) {
        this(id, plugin_id, method_id, c_multiple, o_multiple, policy);
        this.description = description;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * -----------------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public final String getDescription() {
        return description;
    }

    /**
     * ------------------------------------------------------------------------------
     *
     * @param  description  DOCUMENT ME!
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * -----------------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public final PermissionHolder getPermissions() {
        return permissions;
    }

//-----------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getID() {
        return id;
    }

    public Object getKey() {
        return method_id + "@" + plugin_id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isMultiple() {
        return o_multiple;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isClassMultiple() {
        return c_multiple;
    }

    public String toString() {
        return "Name ::" + getKey() + " id::" + id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  m  DOCUMENT ME!
     */
    public final void addPermission(Mapable m) {
        permissions.addPermission(m);
    }

//      final public void removePermission(String localServerName, int userGroupID)
//      {
//              permissions.addPermission(localServerName,userGroupID,false);
//      }

    public Object constructKey(Mapable m) {
        if (m instanceof Method) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void addClassKey(String key) {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("add class key" + key + " to method " + this);
            }
        }
        classKeys.add(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getClassKeys() {
        return classKeys;
    }
}
