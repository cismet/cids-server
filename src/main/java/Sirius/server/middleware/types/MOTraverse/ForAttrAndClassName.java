/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ForAttrAndObjName.java
 *
 * Created on 15. September 2003, 16:28
 */
package Sirius.server.middleware.types.MOTraverse;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;

/**
 * Rekursive Suche in allen Objekten von einem bestimmten Typ nach Attributen mit einem Bestimmten Namen. Diese Klasse
 * ermittelt ausschlieslich die ObjectAttribute, MetaObject's werden nicht als ObjectAttribute erkannt sondern als ein
 * beh\u00E4lter f\u00FCr weitere ObjectAttribute.
 *
 * @author   awindholz, pdihe
 * @version  $Revision$, $Date$
 */
public class ForAttrAndClassName extends AttrForName {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private String objectName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ForAttrAndClassName object.
     *
     * @param  objectName  name des MetaObjektes in dem nach dem MetaAttribut gesucht werden soll.
     */
    public ForAttrAndClassName(String objectName) {
        this.objectName = objectName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Besucht das MetaObjekt und sucht nach ObjectAttributen mit \u00FCbergebenem Namen.
     *
     * @param   mo  Das MetaObjekt dass besucht werden soll.
     * @param   o   String in dem der Name des Attributes angegeben ist nach dem gesucht werden soll.
     *
     * @return  MetaAttribut-Array mit bisher gefundenen ObjectAttributen mit dem \u00FCbergebenem Namen die sich
     *          innerhalb des MetaObjektes befinden der im Konstruktor angegeben wurde.
     */
    public Object visitMO(MetaObject mo, Object o) {
/*        String[] ob = (String[])o;
        String objectName = (String)ob[0];*/
        if (logger.isDebugEnabled()) {
            logger.debug("visitMO: " + mo.getClass().getName() + " / " + o + "/ objectName: " + objectName);
        }

        String className = mo.getClass().getName();

        if ((className != null) && className.equalsIgnoreCase(objectName)) {
            // sucht nach Attribut in diesem MetaObjekt und drunterliegenden Attributen
            return searchMetaAttribute(mo, o);
        } else {
            // sucht nach MetaObject & Attribut nur in drunterliegenden Attributen.
            return searchMetaObject(mo, o);
        }
    }

    /**
     * Sucht nach Attributen mit Namen der in o \u00FCbergeben wird, dabei erfolgt eine Rekursive suche. Im
     * \u00FCbergebenen MetaObject wird auch gesucht.
     *
     * @param   mo  Das MetaObjekt dass besucht werden soll.
     * @param   o   String in dem der Name des Attributes angegeben ist nach dem gesucht werden soll.
     *
     * @return  MetaAttribut-Array mit bisher gefundenen ObjectAttributen mit dem \u00FCbergebenem Namen die sich
     *          innerhalb des MetaObjektes befinden der im Konstruktor angegeben wurde.
     */
    private Object searchMetaAttribute(MetaObject mo, Object o) {
        if (logger.isDebugEnabled()) {
            logger.debug("searchMetaAttribute: " + mo.getName() + " / " + o);
        }

        ObjectAttribute[] ret = new ObjectAttribute[0];
        ObjectAttribute[] mas = mo.getAttribs();
        ObjectAttribute[] tmp;

        for (int i = 0; i < mas.length; i++) {
            tmp = (ObjectAttribute[])mas[i].accept(this, o);
            if (tmp.length > 0) {
                ret = enlargeMA(ret, tmp);
            }
        }
        return ret;
    }

    /**
     * Sucht nach Attributen mit Namen der in o \u00FCbergeben wird, dabei erfolgt eine Rekursive suche. Im
     * \u00FCbergebenen MetaObject wird NICHT gesucht.
     *
     * @param   mo  Das MetaObjekt dass besucht werden soll.
     * @param   o   String in dem der Name des Attributes angegeben ist nach dem gesucht werden soll.
     *
     * @return  DOCUMENT ME!
     */
    private Object searchMetaObject(MetaObject mo, Object o) {
        if (logger.isDebugEnabled()) {
            logger.debug("searchMetaObject: " + mo.getName() + " / " + o);
        }

        ObjectAttribute[] ret = new ObjectAttribute[0];
        ObjectAttribute[] mas = mo.getAttribs();
        ObjectAttribute[] tmp;
        Object value;

        for (int i = 0; i < mas.length; i++) {
            value = mas[i].getValue();
            if (value instanceof MetaObject) {
                tmp = (ObjectAttribute[])mas[i].accept(this, o);

                if (tmp.length > 0) {
                    ret = enlargeMA(ret, tmp);
                }
            }
        }

        return ret;
    }
}
