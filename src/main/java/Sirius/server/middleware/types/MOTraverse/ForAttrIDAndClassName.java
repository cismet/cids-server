/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ForAttrIDAndObjName.java
 *
 * Created on 16. September 2003, 08:38
 */
package Sirius.server.middleware.types.MOTraverse;

import Sirius.server.middleware.types.*;
import Sirius.server.localserver.attribute.*;

/**
 * Sucht nach Attributen mit einem bestimmten ID in einem bestimmten MetaObject von einem bestimmten Typ. Diese Klasse
 * ermittelt ausschlieslich die ObjectAttribute, MetaObject's werden nicht als ObjectAttribute erkannt sondern als ein
 * beh\u00E4lter f\u00FCr weitere ObjectAttribute.
 *
 * @author   awindholz, pdihe
 * @version  $Revision$, $Date$
 */
public class ForAttrIDAndClassName extends ForAttrAndObjName {

    //~ Instance fields --------------------------------------------------------

    private String objectName;
    private String attrID;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ForAttrIDAndObjName.
     *
     * @param  objectName  Name des MetaObject's in dem nach dem ObjectAttribute gesucht werden soll.
     * @param  attrID      id des ObjectAttributes das gesucht werden soll.
     */
    public ForAttrIDAndClassName(String objectName, String attrID) {
        super(objectName);
        this.attrID = attrID;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Liefert (oder auch nicht) ObjectAttribute zur\u00FCck die die \u00FCbergebene ID besitzt und in dem Namentlich
     * angegeben MetaObjekt enthalten ist. Dom\u00E4ne wird nicht beachtet.
     *
     * @param   moa  DOCUMENT ME!
     * @param   o    wird nicht benutzt.
     *
     * @return  MetaAttribut-Array. Es wird jedoch (meist) nur ein Feld exsistieren. Jedoch es ist noch m\u00F6glich
     *          dass mehrere ergebnisse geliefert werden da es wird keine \u00FCberpr\u00FCfung der dom\u00E4ne
     *          vorgenommen.<br>
     *          MetaAttribut-Arrayder Gr\u00F6sse 1 wenn dieses MetaAttribut kein MetaObject als Wert besitzt.<br>
     *          MetaAttribut-Array der Gr\u00F6sse n wenn dieses MetaAttribut einen MetaObject als Wert besitzt. Mit n =
     *          anzahl der im MetaObject enthaltenen ObjectAttribute, die durch rekursives durchsuchen ermittelt wurden.
     *          <br>
     *          MetaAttribut-Array der Gr\u00F6sse 0 wenn dieses MetaAttribut kein MetaObject als Wert besitzt und die
     *          ID des ObjectAttributes nicht dem gesuchtem entspricht.
     */
    public Object visitMA(ObjectAttribute moa, Object o) {
        Object value = moa.getValue();

        if (value instanceof MetaObject) {
            return ((MetaObject)value).accept(this, o);
        } else if (moa.getID().equals(attrID)) {
            ObjectAttribute[] matt = { moa };
            return matt;
        } else {
            return new ObjectAttribute[0];
        }
    }

    /**
     * @param o wird nicht benutzt.
     */
/*    public Object visitMO(MetaObject moa, Object o) {
    }*/

}
