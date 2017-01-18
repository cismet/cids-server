/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class LightweightObject extends DefaultObject {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LightweightObject object.
     *
     * @param  o  DOCUMENT ME!
     */
    public LightweightObject(final Sirius.server.localserver.object.Object o) {
        super(o);
    }

    /**
     * Creates a new LightweightObject object.
     *
     * @param  objectID  DOCUMENT ME!
     * @param  classID   DOCUMENT ME!
     */
    public LightweightObject(final int objectID, final int classID) {
        super(objectID, classID);
    }
}
