/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ForAttrAndObjAttrName.java
 *
 * Created on 15. September 2003, 16:28
 */
package Sirius.server.middleware.types.MOTraverse;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;

//import org.apache.log4j.*;

/**
 * Rekursive Suche nach einem Attribut mit bestimmten Namen innerhalb eines Komplexen Attributs mit einem bestimmten
 * Namen. D.h. es wird zun\u00E4chst rekursiv nach einem Komplexen Attribut gesucht, wenn entsprechendes gefunden wird,
 * dann wird ausschlieslich in diesem nach dem gesuchten Attribut gesucht. Diese Klasse ermittelt einen einzigen
 * Attribut.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class AttrForNameWithinComplexAttr implements TypeVisitor {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private String complexAttributeName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AttrForNameWithinComplexAttr object.
     *
     * @param  complexAttributeName  objectName name des Komplexen Attributes in dem nach dem MetaAttribut gesucht
     *                               werden soll.
     */
    public AttrForNameWithinComplexAttr(final String complexAttributeName) {
        this.complexAttributeName = complexAttributeName.toLowerCase();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Besucht das MetaObjekt und sucht nach Komplexem Attribut mit \u00FCbergebenem Namen. Ist dieser gefunden dann
     * sucht in dem drunterligendem MetaObject nach dem \u00FCbergebenem Attribut.
     *
     * @param   metaObject     Das MetaObjekt dass durchsucht werden soll.
     * @param   attributeName  Name des Attributes nach dem im komplexen Attribut gesucht werden soll.
     *
     * @return  attribut oder null
     */
    @Override
    public Object visitMO(final MetaObject metaObject, final Object attributeName) {
        final ObjectAttribute[] attr = metaObject.getAttribs();
        String name;
        Object value;

        for (int i = 0; i < attr.length; i++) {
            name = attr[i].getName();
            value = attr[i].getValue();
            if (value instanceof MetaObject) {
                if ((name != null) && name.toLowerCase().equals(complexAttributeName)) {
                    final Object object = search((MetaObject)value, (String)attributeName);
                    return object;
                } else {
                    final Object object = ((MetaObject)value).accept(this, attributeName);
                    if (object != null) {
                        return object;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Wenn \u00FCbergebener Attr ein komlexer Attr ist, dann wird der enthaltene MetaObject mit diesem Visitor
     * traversiert, ansonsten macht nichts.
     *
     * @param   metaObjectAttribute  MetaObject dass besucht werden soll.
     * @param   attributeName        der Name des Attributes nach dem gesucht werden soll.
     *
     * @return  attribut oder null
     */
    @Override
    public Object visitMA(final ObjectAttribute metaObjectAttribute, final Object attributeName) {
        if (metaObjectAttribute.getValue() instanceof MetaObject) {
            return ((MetaObject)metaObjectAttribute.getValue()).accept(this, attributeName);
        }

        return null;
    }

    /**
     * Sucht nach Attributen mit Namen der in attributeName \u00FCbergeben wird, dabei erfolgt keine Rekursive suche.
     *
     * @param   metaObject     Das MetaObjekt dass besucht werden soll.
     * @param   attributeName  String in dem der Name des Attributes angegeben ist nach dem gesucht werden soll.
     *
     * @return  attribut oder null
     */
    private Object search(final MetaObject metaObject, final String attributeName) {
        final ObjectAttribute[] metaAttributes = metaObject.getAttribs();
        String name;
        final String metaObjectAttribute = attributeName.toLowerCase();

        for (int i = 0; i < metaAttributes.length; i++) {
            name = metaAttributes[i].getName();
            if ((name != null) && name.toLowerCase().equals(metaObjectAttribute)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(metaAttributes[i]);
                }
                return metaAttributes[i];
            }
        }

        return null;
    }
}
