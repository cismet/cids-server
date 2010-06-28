/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver._class;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ClassMap extends java.util.Hashtable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8694508933671049122L;

    //~ Constructors -----------------------------------------------------------

    /**
     * constructor.
     */
    public ClassMap() {
        super();
    }
    /**
     * constructor.
     *
     * @param  capacity  DOCUMENT ME!
     */
    public ClassMap(final int capacity) {
        super(capacity);
    }
    /**
     * constructor.
     *
     * @param  capacity  DOCUMENT ME!
     * @param  factor    DOCUMENT ME!
     */
    public ClassMap(final int capacity, final float factor) {
        super(capacity, factor);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * //////////////////////////////////////////////////
     *
     * @param   key    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception            DOCUMENT ME!
     * @throws  java.lang.Exception  DOCUMENT ME!
     */
    public void add(final int key, final Class value) throws Exception {
        final Integer Key = new Integer(key);
        super.put(Key, value);
        if (!super.containsKey(Key)) {
            throw new java.lang.Exception("Couldn't add class ID:" + key);
        }
    } // end add

    /**
     * /////////////////////////////////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Class getClass(final int key) throws Exception {
        final Integer Key = new Integer(key); // map accepts objects only
        if (super.containsKey(Key)) {
            final java.lang.Object candidate = super.get(Key);

            // if (candidate instanceof Class)
            return (Class)candidate;
        } else {
            return null;
        }
        // throw new java.lang.NullPointerException("Entry is not a Class ID :" +key);
        // }// endif

        // throw new java.lang.NullPointerException("No entry ClassID :"+key); // to be changed in further versions
        // when exception concept is accomplished
    } // end getClass

    /**
     * ///// containsIntKey/////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsIntKey(final int key) {
        return super.containsKey(new Integer(key));
    }

    @Override
    public void rehash() {
        super.rehash();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector getAll() {
        return new Vector(this.values());
    }
} // end of class ClassMap
