/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver._class;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ClassMap extends HashMap {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassMap object.
     */
    public ClassMap() {
    }

    /**
     * Creates a new ClassMap object.
     *
     * @param  capacity  DOCUMENT ME!
     */
    public ClassMap(final int capacity) {
        super(capacity);
    }

    /**
     * Creates a new ClassMap object.
     *
     * @param  capacity  DOCUMENT ME!
     * @param  factor    DOCUMENT ME!
     */
    public ClassMap(final int capacity, final float factor) {
        super(capacity, factor);
    }

    //~ Methods ----------------------------------------------------------------

    //J-
    // TODO: jalopy trouble
    /**
     * DOCUMENT ME!
     *
     * @param   key    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception  java.lang.Exception DOCUMENT ME!
     */
    public void add(final int key, final Class value) throws Exception {
        super.put(key, value);
        if (!super.containsKey(key)) {
            throw new Exception("Couldn't add class ID:" + key); // NOI18N
        }
    }
    //J+

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Class getClass(final int key) {
        final Integer Key = new Integer(key); // map accepts objects only
        if (super.containsKey(Key)) {
            final java.lang.Object candidate = super.get(Key);

            return (Class)candidate;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsIntKey(final int key) {
        return super.containsKey(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List getAll() {
        return new ArrayList(this.values());
    }
}
