/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser;
import Sirius.util.*;
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class UserGroup implements java.io.Serializable, Mapable {

    //~ Instance fields --------------------------------------------------------

    protected int id;
    protected String domain;
    protected String name;
    protected String description;
    protected boolean isAdmin;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserGroup object.
     *
     * @param  id      DOCUMENT ME!
     * @param  name    DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public UserGroup(int id, String name, String domain) {
        this.id = id;
        this.domain = domain.trim();
        this.name = name.trim();
        this.description = "";
        this.isAdmin = false;
    }

    /**
     * Creates a new UserGroup object.
     *
     * @param  id           DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  domain       DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     */
    public UserGroup(int id, String name, String domain, String description) {
        this(id, name, domain);
        this.description = description;
    }

    //~ Methods ----------------------------------------------------------------

    public String toString() {
        return getKey().toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getName() {
        return name;
    }

    public boolean equals(java.lang.Object ug) {
        UserGroup userGroup = (UserGroup)ug;

        return getKey().equals(userGroup.getKey());
    }

    // Mapable
    public Object getKey() {
        return name + "@" + domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isAdmin() {
        return isAdmin;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Setter for property isAdmin.
     *
     * @param  isAdmin  New value of property isAdmin.
     */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Object constructKey(Mapable m) {
        if (m instanceof UserGroup) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * Getter for property id.
     *
     * @return  Value of property id.
     */
    public int getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static Object[] parseKey(String classKey) throws Exception {
        Object[] result = new Object[2];

        if (classKey.contains("@")) {
            String[] split = classKey.split("@");
            result[0] = split[0];
            result[1] = split[1];
        } else // nehme ich an dass die domain fehlt
        {
            result[0] = classKey;
            result[1] = "LOCAL";
        }
        return result;
    }
}
