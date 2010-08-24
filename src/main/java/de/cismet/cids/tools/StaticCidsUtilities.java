/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StaticCidsUtilities.java
 *
 * Created on 6. August 2007, 08:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.tools;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class StaticCidsUtilities {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StaticCidsUtilities.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   attributeName  DOCUMENT ME!
     * @param   attr           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object getValueOfAttributeByString(final String attributeName, final Attribute attr) {
        if (log.isDebugEnabled()) {
            log.debug("getValueOfAttributeByString(String attributeName,Attribute attr)"); // NOI18N
        }
        final String[] attrNames = attributeName.split("\\.");                             // NOI18N
        if (attrNames.length == 1) {
            return attr.getValue();
        } else {
            MetaObject deeper = null;
            for (int i = 0; i < attrNames.length; ++i) {
                final String attrN = attrNames[i];
                if (attrN.endsWith("[]")) {                                                // NOI18N
                    final String nameWithoutBrackets = attrN.replaceAll("\\[\\]", "");     // NOI18N
                    Attribute ma = null;
                    if (i == 0) {
                        ma = attr;
                    } else {
                        ma = (Attribute)deeper.getAttributeByName(nameWithoutBrackets, 1).toArray()[0];
                    }
                    if (ma.isArray()) {
                        final Sirius.server.localserver.object.Object moZwischen =
                            (Sirius.server.localserver.object.Object)ma.getValue();
                        final ObjectAttribute[] oa = moZwischen.getAttribs();
                        // String wieder zusammenkleben
                        String reGlued = "";    // NOI18N
                        for (int j = i + 1; j < attrNames.length; ++j) {
                            reGlued += attrNames[j];
                            if (j < (attrNames.length - 1)) {
                                reGlued += "."; // NOI18N
                            }
                        }
                        // Check ob ein Vector oder ne HashMap zur\u00FCckgeliefert werden m\u00FCssen
                        final String[] again = reGlued.split("\\.");                 // NOI18N
                        if ((again.length >= 2) && again[1].matches(".+\\[.+\\]")) { // NOI18N
                            // Es muss ne HashMap geliefert werden weil der Arraykram noch weitergeht

                            // Key Attribut der HashMap rausfinden
                            String keyAttrName = again[1].replaceAll(".*\\[", ""); // NOI18N
                            keyAttrName = keyAttrName.replaceAll("\\]", "");       // NOI18N
                            final HashMap arrayEintraege = new HashMap();

                            // keyAttr muss zwischen den Klammer entfernt werden, aber nur beim 2ten Eintrag
                            String reReClued = again[0] + "." + again[1].replaceAll("\\[.*\\]", "") + "[]."; // NOI18N
                            for (int j = 2; j < again.length; ++j) {
                                reReClued += again[j];
                                if (j < (again.length - 1)) {
                                    reReClued += ".";                                                        // NOI18N
                                }
                            }

                            for (final ObjectAttribute arrayEintrag : oa) {
                                if ((arrayEintrag.getValue() instanceof MetaObject)
                                            && (((MetaObject)(arrayEintrag.getValue())).getStatus()
                                                != MetaObject.TO_DELETE)) {
                                    final Object key = getValueOfAttributeByString(
                                            again[0]
                                                    + "." // NOI18N
                                                    + keyAttrName,
                                            (MetaObject)arrayEintrag.getValue());
                                    final Object val = getValueOfAttributeByString(
                                            reReClued,
                                            (MetaObject)arrayEintrag.getValue());
                                    if (val != null) {
//                                        Collection c=((MetaObject)arrayEintrag.getValue()).getAttributeByName(keyAttrName,1);
//                                        if (c.size()>0) {
//                                            Attribute keyMa=(Attribute)c.toArray()[0];
                                        arrayEintraege.put(key, val);
//                                        } else {
//                                            log.error("Falscher Attributname (HM): "+keyAttrName+" (Renderer wird nicht komplett funktionieren)");
//                                        }
                                    }
                                }
                            }
                            return arrayEintraege;
                        } else {
                            final Vector arrayEintraege = new Vector();
                            for (final ObjectAttribute arrayEintrag : oa) {
                                if ((arrayEintrag.getValue() instanceof MetaObject)
                                            && (((MetaObject)(arrayEintrag.getValue())).getStatus()
                                                != MetaObject.TO_DELETE)) {
                                    final Object val = getValueOfAttributeByString(
                                            reGlued,
                                            (MetaObject)arrayEintrag.getValue());
                                    // if (val != null) {
                                    arrayEintraege.add(val);
                                    // }
                                }
                            }
                            return arrayEintraege;
                        }
                    }
                } else {
                    final Collection c = deeper.getAttributeByName(attrN, 1);
                    if (c.size() > 0) {
                        Attribute ma = null;
                        if (i == 0) {
                            ma = attr;
                        } else {
                            ma = (Attribute)c.toArray()[0];
                        }
                        if (ma.getValue() instanceof MetaObject) {
                            deeper = (MetaObject)ma.getValue();
                        } else {
                            return ma.getValue();
                        }
                    } else {
                        log.error("Wrong attribute name: " + attrN + " (Renderer will not work properly)"); // NOI18N
                    }
                }
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributeName  DOCUMENT ME!
     * @param   mo             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object getValueOfAttributeByString(final String attributeName, final MetaObject mo) {
        final String[] attrNames = attributeName.split("\\.");                             // NOI18N
        try {
            if (attrNames.length == 1) {
                final Attribute ma = (Attribute)mo.getAttributeByName(attributeName, 1).toArray()[0];
                return ma.getValue();
            } else {
                MetaObject deeper = mo;
                for (int i = 0; i < attrNames.length; ++i) {
                    final String attrN = attrNames[i];
                    if (attrN.endsWith("[]")) {                                            // NOI18N
                        final String nameWithoutBrackets = attrN.replaceAll("\\[\\]", ""); // NOI18N
                        final Attribute ma = (Attribute)deeper.getAttributeByName(nameWithoutBrackets, 1).toArray()[0];
                        if (ma.isArray()) {
                            final MetaObject moZwischen = (MetaObject)ma.getValue();
                            final ObjectAttribute[] oa = moZwischen.getAttribs();
                            // String wieder zusammenkleben
                            String reGlued = "";    // NOI18N
                            for (int j = i + 1; j < attrNames.length; ++j) {
                                reGlued += attrNames[j];
                                if (j < (attrNames.length - 1)) {
                                    reGlued += "."; // NOI18N
                                }
                            }
                            // Check ob ein Vector oder ne HashMap zur\u00FCckgeliefert werden m\u00FCssen
                            final String[] again = reGlued.split("\\.");                 // NOI18N
                            if ((again.length >= 2) && again[1].matches(".+\\[.+\\]")) { // NOI18N
                                // Es muss ne HashMap geliefert werden weil der Arraykram noch weitergeht

                                // Key Attribut der HashMap rausfinden
                                String keyAttrName = again[1].replaceAll(".*\\[", ""); // NOI18N
                                keyAttrName = keyAttrName.replaceAll("\\]", "");       // NOI18N
                                final HashMap arrayEintraege = new HashMap();

                                // keyAttr muss zwischen den Klammer entfernt werden, aber nur beim 2ten Eintrag
                                String reReClued = again[0] + "." + again[1].replaceAll("\\[.*\\]", "") + "[]."; // NOI18N
                                for (int j = 2; j < again.length; ++j) {
                                    reReClued += again[j];
                                    if (j < (again.length - 1)) {
                                        reReClued += ".";                                                        // NOI18N
                                    }
                                }

                                for (final ObjectAttribute arrayEintrag : oa) {
                                    if ((arrayEintrag.getValue() instanceof MetaObject)
                                                && (((MetaObject)(arrayEintrag.getValue())).getStatus()
                                                    != MetaObject.TO_DELETE)) {
                                        final Object key = getValueOfAttributeByString(
                                                again[0]
                                                        + "." // NOI18N
                                                        + keyAttrName,
                                                (MetaObject)arrayEintrag.getValue());
                                        final Object val = getValueOfAttributeByString(
                                                reReClued,
                                                (MetaObject)arrayEintrag.getValue());
                                        if (val != null) {
//                                        Collection c=((MetaObject)arrayEintrag.getValue()).getAttributeByName(keyAttrName,1);
//                                        if (c.size()>0) {
//                                            Attribute keyMa=(Attribute)c.toArray()[0];
                                            arrayEintraege.put(key, val);
//                                        } else {
//                                            log.error("Falscher Attributname (HM): "+keyAttrName+" (Renderer wird nicht komplett funktionieren)");
//                                        }
                                        }
                                    }
                                }
                                return arrayEintraege;
                            } else {
                                final Vector arrayEintraege = new Vector();
                                for (final ObjectAttribute arrayEintrag : oa) {
                                    if ((arrayEintrag.getValue() instanceof MetaObject)
                                                && (((MetaObject)(arrayEintrag.getValue())).getStatus()
                                                    != MetaObject.TO_DELETE)) {
                                        final Object val = getValueOfAttributeByString(
                                                reGlued,
                                                (MetaObject)arrayEintrag.getValue());
//                                        if (val != null) {
                                        arrayEintraege.add(val);
//                                        }
                                    }
                                }
                                return arrayEintraege;
                            }
                        }
                    } else {
                        final Collection c = deeper.getAttributeByName(attrN, 1);
                        if (c.size() > 0) {
                            final Attribute ma = (Attribute)c.toArray()[0];
                            if (ma.getValue() instanceof MetaObject) {
                                deeper = (MetaObject)ma.getValue();
                            } else {
                                return ma.getValue();
                            }
                        } else {
                            log.error(
                                "Wrong attribute name: "
                                        + attrN
                                        + " (Renderer will not work properly)", // NOI18N
                                new CurrentStackTrace());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(
                "Error in getValueOfAttributeByString("
                        + attributeName
                        + ",MetaObject)\nMetaobject=" // NOI18N
                        + mo.getDebugString(),
                e);
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String debugPrintMetaObject(final MetaObject mo) {
        final String ret = ""; // NOI18N
        return ret;
    }
}
