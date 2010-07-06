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
package Sirius.server.middleware.types.MOTraverse;

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
public class ExtractAllAttr implements TypeVisitor {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AttrForName.
     */
    public ExtractAllAttr() {
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

        if (value instanceof MetaObject) {
            return ((MetaObject)value).accept(this, o);
        }

        final ObjectAttribute[] matt = { moa };

        return matt;
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
        ObjectAttribute[] ret = new ObjectAttribute[0];
        ObjectAttribute[] tmp;

        final ObjectAttribute[] mas = moa.getAttribs();

        for (int i = 0; i < mas.length; i++) {
            tmp = (ObjectAttribute[])mas[i].accept(this, o);
            if (tmp.length > 0) {
                ret = enlargeMA(ret, tmp);
            }
        }

        return ret;
    }

    /**
     * vergr\u00F6sset das Array um ein Feld, besetzt dieses mit dem neuem Attribut und liefert das Array zur\u00FCck.
     *
     * @param   quelle  DOCUMENT ME!
     * @param   neu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static ObjectAttribute[] incrementMA(final ObjectAttribute[] quelle, final ObjectAttribute neu) {
        ObjectAttribute[] ziel = quelle;
        System.arraycopy(quelle, 0, ziel = new ObjectAttribute[quelle.length + 1], 0, quelle.length);
        ziel[ziel.length - 1] = neu;
        return ziel;
    }

    /**
     * Kombiniert 2 Arrays. Quelle A wind an Quelle B angeh\u00E4ngt.
     *
     * @param   quelleA  DOCUMENT ME!
     * @param   quelleB  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static ObjectAttribute[] enlargeMA(final ObjectAttribute[] quelleA, final ObjectAttribute[] quelleB) {
        if ((quelleB == null) && (quelleA != null)) {
            return quelleA;
        }

        final ObjectAttribute[] ziel = new ObjectAttribute[quelleA.length + quelleB.length];

        System.arraycopy(quelleA, 0, ziel, 0, quelleA.length);
        System.arraycopy(quelleB, 0, ziel, quelleA.length, quelleB.length);

        return ziel;
    }
}
