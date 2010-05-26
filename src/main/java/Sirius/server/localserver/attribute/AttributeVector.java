/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import java.util.*;
import java.util.Vector;

/**
 * //////// Class AttributeVector//////////////////////////////////////.
 *
 * @version  $Revision$, $Date$
 */
public class AttributeVector extends java.util.Vector implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 595320627200541410L;

    //~ Constructors -----------------------------------------------------------

    /**
     * constructors.
     */
    public AttributeVector() {
        super();
    }
    /**
     * Creates a new AttributeVector object.
     *
     * @param  initialCapacity  DOCUMENT ME!
     */
    public AttributeVector(final int initialCapacity) {
        super(initialCapacity);
    }
    /**
     * Creates a new AttributeVector object.
     *
     * @param  c  DOCUMENT ME!
     */
    public AttributeVector(final Collection c) {
        super(c);
    }
    /**
     * Creates a new AttributeVector object.
     *
     * @param  initialCapacity    DOCUMENT ME!
     * @param  capacityIncrement  DOCUMENT ME!
     */
    public AttributeVector(final int initialCapacity, final int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * //////////////////methods/////////////////////////////////////////
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception                            DOCUMENT ME!
     * @throws  java.lang.NullPointerException       DOCUMENT ME!
     * @throws  java.lang.IndexOutOfBoundsException  DOCUMENT ME!
     */
    public Attribute at(final int index) throws Exception {
        if (size() > index) {
            final java.lang.Object attrib = super.get(index);
            if (attrib instanceof Attribute) {
                return (Attribute)attrib;
            }
            throw new java.lang.NullPointerException();
        }
        throw new java.lang.IndexOutOfBoundsException();
    }
    /**
     * /////// converts to LinkArray/////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public Attribute[] convertToArray() {
        return (Attribute[])toArray(new Attribute[size()]);
    } // end of convertToArray

    @Override
    public java.lang.Object clone() {
        return new AttributeVector((Vector)super.clone());
    }
} // end of class AttributeVector
