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

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -5292371264242940827L;

    //~ Instance fields --------------------------------------------------------

/////////////////members//////////////////////////////////////////

    protected int classID;

    protected int typeID;

//protected String fieldName;

//protected int foreignKeyClassID;

    //~ Constructors -----------------------------------------------------------

    /**
     * /////////////constructor///////////////////////////////////////
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
        super(id, name, "", policy);
        this.classID = classID;
        super.visible = true;
        this.typeID = typeID;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ///////////////////////methods///////////////////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public final int getClassID() {
        return classID;
    }

    /**
     * Getter for property fieldName.
     *
     * @return  Value of property fieldName.
     */
// public final java.lang.String getFieldName() {
// return fieldName;
// }
//
///** Setter for property fieldName.
// * @param fieldName New value of property fieldName.
// *
// */
//public final void setFieldName(java.lang.String fieldName) {
//    this.fieldName = fieldName;
//}
//
///** Getter for property foreignKeyClassID.
// * @return Value of property foreignKeyClassID.
// *
// */
//public final int getForeignKeyClassID() {
//    return foreignKeyClassID;
//}
//
///** Setter for property foreignKeyClassID.
// * @param foreignKeyClassID New value of property foreignKeyClassID.
// *
// */
//public final void setForeignKeyClassID(int foreignKeyClassID) {
//    this.foreignKeyClassID = foreignKeyClassID;
//}

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
