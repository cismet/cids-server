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

import java.util.*;

/**
 * Sucht rekursiv in MetaObject's nach allen MetaObject-Objekten und liefert alle in einem Vector.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class ExtractAllMetaObjectsVisitor implements TypeVisitor {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AttrForName.
     */
    public ExtractAllMetaObjectsVisitor() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * \u00DCberpr\u00FCfrt ob dieses Attribut einen MetaObjekt enth\u00E4lt, wenn ja dann wird dieser an die
     * visitMO(...) deligiert.
     *
     * @param   moa  Das MetaAttribut das besucht wird.
     * @param   o    muss ein MetaObject-Vector sein, dieser wird mit MetaObject gef\u00FCllt.
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

        return o;
    }

    /**
     * Liefert alle MetaObjekte die in diesem und MetaObject enthalten sind. D.h. Alle Elemente dieses MetaObjektes
     * werden rekursiv durchlaufen und f\u00FCgen die enthalteten MetaObjekte in \u00FCbergebenen Vector objekt.
     *
     * @param   moa  Das MetaObject das besucht wird.
     * @param   o    muss ein MetaObject-Vector sein, dieser wird mit MetaObject gef\u00FCllt.
     *
     * @return  liefert diesen alle drunterligenden MetaObjekte in einem Vector.
     */
    @Override
    public Object visitMO(final MetaObject moa, final Object o) {
        if (!moa.isDummy()) {
            ((Vector)o).addElement(moa);
        }

        final ObjectAttribute[] ret = new ObjectAttribute[0];

        final ObjectAttribute[] mas = moa.getAttribs();

        for (int i = 0; i < mas.length; i++) {
            mas[i].accept(this, o);
        }

        return o;
    }
}
