/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Permission.java
 *
 * Created on 24. September 2004, 15:10
 */
package Sirius.server.newuser.permission;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class Permission implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 4112877525599858208L;

    //~ Instance fields --------------------------------------------------------

    protected String key;
    protected int id;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Permission.
     *
     * @param  id   DOCUMENT ME!
     * @param  key  DOCUMENT ME!
     */
    public Permission(final int id, final String key) {
        this.id = id;
        this.key = key.trim();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean equals(final java.lang.Object o) {
        if (this == o) {
            return true;
        }
        final Permission p = ((Permission)o);

        return (key.equalsIgnoreCase(p.key));
    }

    @Override
    public int hashCode() {
        return key.hashCode();
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
     * Setter for property id.
     *
     * @param  id  New value of property id.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Getter for property key.
     *
     * @return  Value of property key.
     */
    public java.lang.String getKey() {
        return key;
    }

    /**
     * Setter for property key.
     *
     * @param  key  New value of property key.
     */
    public void setKey(final java.lang.String key) {
        this.key = key.trim();
    }

    @Override
    public String toString() {
        return "ID " + id + " KEY " + key;   // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toSQL() {
        return "insert into cs_permission values (" + id + ", " + key + ")";   // NOI18N
    }
}
