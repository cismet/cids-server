/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AttrForName.java
 *
 * Created on 15. September 2003, 14:35
 */
package Sirius.server.naming.middleware.types.MOTraverse;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;

/**
 * Sucht rekursiv in MetaObject's nach allen Attributen es wird nicht auf Object-Name oder Attributname geachtet. Diese
 * Klasse ermittelt ausschlieslich die ObjectAttribute, MetaObject's werden nicht als ObjectAttribute erkannt sondern
 * als ein beh\u00E4lter f\u00FCr weitere ObjectAttributen.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class Visualizer implements TypeVisitor {

    //~ Static fields/initializers ---------------------------------------------

    private static final String NEW_LINE = System.getProperty("line.separator");

    //~ Instance fields --------------------------------------------------------

    private int step = 0;
    private final String STEP_LENGTH = "   ";

    private String visualized = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AttrForName.
     */
    public Visualizer() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Liefert diesen Attribut oder wenn dieses Attribut einen MetaObject als Wert enth\u00E4lt dann wird dieses
     * besucht(visitMO(...)).
     *
     * @param   moa  Das MetaAttribut das besucht wird.
     * @param   o    wird nicht verwendet.
     *
     * @return  liefert diesen Attribut in einem Array der gr\u00F6se 1 zur\u00FCck oder wenn der Attribut einen
     *          MetaObject als Wert enth\u00E4lt dann alle darin enthaltenen Attribute.
     */
    @Override
    public Object visitMA(final ObjectAttribute moa, final Object o) {
        final Object value = moa.getValue();

        visualized += getPrefix() + STEP_LENGTH + moa.getName();

        if (value instanceof MetaObject) {
            ((MetaObject)value).accept(this, o);
        } else {
            visualized += ": " + value + NEW_LINE;
        }

        return visualized;
    }

    /**
     * Liefert alle Attribute in diesem und allen darunterliegenden MetaObject's.
     *
     * @param   moa  Das MetaObject das besucht wird.
     * @param   o    wird nicht verwendet.
     *
     * @return  liefert alle in diesem und allen darunterliegenden MetaObjecten enthaltenen Attribute.
     */
    @Override
    public Object visitMO(final MetaObject moa, final Object o) {
        step++;

        try {
            visualized += "-->/MetaObject " + moa.getName() + "/" + NEW_LINE;
        } catch (NullPointerException e) {
            visualized += "-->/MetaObject/" + NEW_LINE;
        }

        final ObjectAttribute[] mas = moa.getAttribs();

        for (int i = 0; i < mas.length; i++) {
            mas[i].accept(this, o);
        }

        step--;

        return visualized;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getPrefix() {
        String ret = "";

        for (int i = 0; i < step; i++) {
            ret += STEP_LENGTH;
        }

        return ret;
    }
}
