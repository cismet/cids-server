/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util.image;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class IntMapsImage extends java.util.Hashtable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IntMapsImage object.
     */
    public IntMapsImage() {
        super();
    }

    /**
     * Creates a new IntMapsImage object.
     *
     * @param  initialCapacity  DOCUMENT ME!
     * @param  loadFactor       DOCUMENT ME!
     */
    public IntMapsImage(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  id      DOCUMENT ME!
     * @param  aImage  DOCUMENT ME!
     */
    public void add(int id, Image aImage) {
        super.put(new Integer(id), aImage);
    } // end add

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception                       DOCUMENT ME!
     * @throws  java.lang.NullPointerException  DOCUMENT ME!
     */
    public Image getImageValue(int id) throws Exception {
        Integer key = new Integer(id);

        if (super.containsKey(key)) {
            java.lang.Object candidate = super.get(key);

            if (candidate instanceof Image) {
                return ((Image)candidate);
            }

            throw new java.lang.NullPointerException("Entry is not a Image:" + id);
        } // endif

        throw new java.lang.NullPointerException("No entry :" + id);
    }
    /**
     * ///// containsIntKey/////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsIntKey(int key) {
        return super.containsKey(new Integer(key));
    }
}
