/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.method;

import java.util.*;
import java.util.Vector;

/**
 * //////// Class MethodVector//////////////////////////////////////.
 *
 * @version  $Revision$, $Date$
 */
public class MethodVector extends java.util.Vector {

    //~ Constructors -----------------------------------------------------------

    /**
     * constructors.
     */
    public MethodVector() {
        super();
    }
    /**
     * Creates a new MethodVector object.
     *
     * @param  initialCapacity  DOCUMENT ME!
     */
    public MethodVector(int initialCapacity) {
        super(initialCapacity);
    }
    /**
     * Creates a new MethodVector object.
     *
     * @param  c  DOCUMENT ME!
     */
    public MethodVector(Collection c) {
        super(c);
    }
    /**
     * Creates a new MethodVector object.
     *
     * @param  initialCapacity    DOCUMENT ME!
     * @param  capacityIncrement  DOCUMENT ME!
     */
    public MethodVector(int initialCapacity, int capacityIncrement) {
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
    public Method at(int index) throws Exception {
        if (size() > index) {
            java.lang.Object method = super.get(index);
            if (method instanceof Method) {
                return (Method)method;
            }
            throw new java.lang.NullPointerException();
        }
        throw new java.lang.IndexOutOfBoundsException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Method[] convertToArray() {
        return (Method[])toArray(new Method[size()]);
    } // end of convertToArray
} // end of class AttributeVector
