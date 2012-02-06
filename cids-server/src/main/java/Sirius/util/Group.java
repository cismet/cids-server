/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Group {

    //~ Instance fields --------------------------------------------------------

    private String group;
    private int[] items;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Group object.
     *
     * @param  group  DOCUMENT ME!
     * @param  items  DOCUMENT ME!
     */
    public Group(final String group, final Groupable[] items) {
        this.group = group;
        this.items = getIDs(items);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * -------------------------------------------------------------------------------
     *
     * @param   items  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int[] getIDs(final Groupable[] items) {
        final int[] ids = new int[items.length];

        // extract ids from groupables
        for (int i = 0; i < ids.length; i++) {
            ids[i] = items[i].getId();
        }

        return ids;
    }

//------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getGroup() {
        return group;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int[] getIDs() {
        return items;
    }
}
