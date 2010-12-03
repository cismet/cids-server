/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.server.newuser.permission.Policy;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ClassAttribute extends Attribute implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    public static final String HISTORY_ENABLED = "history_enabled"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected int classID;

    protected int typeID;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassAttribute object.
     *
     * @param  id       DOCUMENT ME!
     * @param  classID  DOCUMENT ME!
     * @param  name     DOCUMENT ME!
     * @param  typeID   DOCUMENT ME!
     * @param  policy   DOCUMENT ME!
     */
    public ClassAttribute(final String id,
            final int classID,
            final String name,
            final int typeID,
            final Policy policy) {
        super(id, name, "", policy); // NOI18N
        this.classID = classID;
        super.visible = true;
        this.typeID = typeID;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getClassID() {
        return classID;
    }

    /**
     * Getter for property typeID.
     *
     * @return  Value of property typeID.
     */
    public final int getTypeID() {
        return typeID;
    }

    /**
     * Setter for property typeID.
     *
     * @param  typeID  New value of property typeID.
     */
    public final void setTypeID(final int typeID) {
        this.typeID = typeID;
    }
}
