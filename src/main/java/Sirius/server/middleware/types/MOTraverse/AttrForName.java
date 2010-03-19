/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AllAttrForName.java
 *
 * Created on 15. September 2003, 15:05
 */
package Sirius.server.middleware.types.MOTraverse;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;

/**
 * Sucht rekursiv in MetaObject's nach Attributen mit einem bestimmten Namen, es wird nicht auf ObjectName geachtet.
 * Diese Klasse ermittelt ausschlieslich die ObjectAttribute, MetaObject's werden nicht als ObjectAttribute erkannt
 * sondern als ein beh\u00E4lter f\u00FCr weitere ObjectAttribute.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class AttrForName extends ExtractAllAttr {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AllAttrForName.
     */
    public AttrForName() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Sucht nach Attributen mit dem bestimmten Namen.
     *
     * @param   moa  MetaObject dass besucht werden soll.
     * @param   o    der Name des Attributes nach dem gesucht werden soll.
     *
     * @return  MetaAttribut-Array. <br>
     *          MetaAttribut-Arrayder Gr\u00F6sse 1 wenn dieses MetaAttribut kein MetaObject als Wert besitzt.<br>
     *          MetaAttribut-Array der Gr\u00F6sse n wenn dieses MetaAttribut einen MetaObject als Wert besitzt. Mit n =
     *          anzahl der im MetaObject enthaltenen ObjectAttribute, die durch rekursives durchsuchen ermittelt wurden.
     *          <br>
     *          MetaAttribut-Array der Gr\u00F6sse 0 wenn dieses MetaAttribut kein MetaObject als Wert besitzt und der
     *          Name des ObjectAttributes nicht dem gesuchtem entspricht.
     */
    public Object visitMA(ObjectAttribute moa, Object o) {
        Object value = moa.getValue();

        if (value instanceof MetaObject) {
            return ((MetaObject)value).accept(this, o);
        } else if (moa.getName().equalsIgnoreCase(o.toString())) {
            ObjectAttribute[] matt = { moa };
            return matt;
        } else {
            return new ObjectAttribute[0];
        }
    }

    /**
     * @raram o String mit dem Namen des Parameters nach dem gesucht werden soll.
     */
    // public Object visitMO(MetaObject moa, Object o) { ... }
}
