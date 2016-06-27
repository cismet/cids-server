/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import java.io.Serializable;

import java.util.Collection;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @version     $Revision$, $Date$
 * @Deprecated  DOCUMENT ME!
 */
public class AttributeVector extends Vector implements Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AttributeVector object.
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
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception                  DOCUMENT ME!
     * @throws  NullPointerException       DOCUMENT ME!
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    // FIXME: impl bad
    public Attribute at(final int index) throws Exception {
        if (size() > index) {
            final java.lang.Object attrib = super.get(index);
            if (attrib instanceof Attribute) {
                return (Attribute)attrib;
            }

            throw new NullPointerException();
        }

        throw new IndexOutOfBoundsException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Attribute[] convertToArray() {
        return (Attribute[])toArray(new Attribute[size()]);
    }

    @Override
    public Object clone() {
        return new AttributeVector((Vector)super.clone());
    }
}
