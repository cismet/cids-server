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
import de.cismet.tools.CurrentStackTrace;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author hell
 */
public class StaticCidsUtilities {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StaticCidsUtilities.class);

    public static Object getValueOfAttributeByString(String attributeName, Attribute attr) {
        log.debug("getValueOfAttributeByString(String attributeName,Attribute attr)");
        String[] attrNames = attributeName.split("\\.");
        if (attrNames.length == 1) {
            return attr.getValue();
        } else {
            MetaObject deeper = null;
            for (int i = 0; i < attrNames.length; ++i) {
                String attrN = attrNames[i];
                if (attrN.endsWith("[]")) {
                    String nameWithoutBrackets = attrN.replaceAll("\\[\\]", "");
                    Attribute ma = null;
                    if (i == 0) {
                        ma = attr;
                    } else {
                        ma = (Attribute) deeper.getAttributeByName(nameWithoutBrackets, 1).toArray()[0];
                    }
                    if (ma.isArray()) {
                        Sirius.server.localserver.object.Object moZwischen = (Sirius.server.localserver.object.Object) ma.getValue();
                        ObjectAttribute[] oa = moZwischen.getAttribs();
                        //String wieder zusammenkleben
                        String reGlued = "";
                        for (int j = i + 1; j < attrNames.length; ++j) {
                            reGlued += attrNames[j];
                            if (j < attrNames.length - 1) {
                                reGlued += ".";
                            }
                        }
                        //Check ob ein Vector oder ne HashMap zur\u00FCckgeliefert werden m\u00FCssen
                        String[] again = reGlued.split("\\.");
                        if (again.length >= 2 && again[1].matches(".+\\[.+\\]")) {
                            //Es muss ne HashMap geliefert werden weil der Arraykram noch weitergeht

                            //Key Attribut der HashMap rausfinden
                            String keyAttrName = again[1].replaceAll(".*\\[", "");
                            keyAttrName = keyAttrName.replaceAll("\\]", "");
                            HashMap arrayEintraege = new HashMap();

                            //keyAttr muss zwischen den Klammer entfernt werden, aber nur beim 2ten Eintrag
                            String reReClued = again[0] + "." + again[1].replaceAll("\\[.*\\]", "") + "[].";
                            for (int j = 2; j < again.length; ++j) {
                                reReClued += again[j];
                                if (j < again.length - 1) {
                                    reReClued += ".";
                                }
                            }

                            for (ObjectAttribute arrayEintrag : oa) {
                                if (arrayEintrag.getValue() instanceof MetaObject && ((MetaObject)(arrayEintrag.getValue())).getStatus()!=MetaObject.TO_DELETE) {
                                    Object key = getValueOfAttributeByString(again[0] + "." + keyAttrName, (MetaObject) arrayEintrag.getValue());
                                    Object val = getValueOfAttributeByString(reReClued, (MetaObject) arrayEintrag.getValue());
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
                            Vector arrayEintraege = new Vector();
                            for (ObjectAttribute arrayEintrag : oa) {
                                if (arrayEintrag.getValue() instanceof MetaObject && ((MetaObject)(arrayEintrag.getValue())).getStatus()!=MetaObject.TO_DELETE) {
                                    Object val = getValueOfAttributeByString(reGlued, (MetaObject) arrayEintrag.getValue());
                                    //if (val != null) {
                                        arrayEintraege.add(val);
                                    //}
                                }
                            }
                            return arrayEintraege;
                        }
                    }
                } else {
                    Collection c = deeper.getAttributeByName(attrN, 1);
                    if (c.size() > 0) {
                        Attribute ma = null;
                        if (i == 0) {
                            ma = attr;
                        } else {
                            ma = (Attribute) c.toArray()[0];
                        }
                        if (ma.getValue() instanceof MetaObject) {
                            deeper = (MetaObject) ma.getValue();
                        } else {
                            return ma.getValue();
                        }
                    } else {
                        log.error("Falscher Attributname: " + attrN + " (Renderer wird nicht komplett funktionieren)");
                    }
                }
            }
        }
        return null;
    }

    public static Object getValueOfAttributeByString(String attributeName, MetaObject mo) {
        String[] attrNames = attributeName.split("\\.");
        try {
            if (attrNames.length == 1) {
                Attribute ma = (Attribute) mo.getAttributeByName(attributeName, 1).toArray()[0];
                return ma.getValue();
            } else {
                MetaObject deeper = mo;
                for (int i = 0; i < attrNames.length; ++i) {
                    String attrN = attrNames[i];
                    if (attrN.endsWith("[]")) {
                        String nameWithoutBrackets = attrN.replaceAll("\\[\\]", "");
                        Attribute ma = (Attribute) deeper.getAttributeByName(nameWithoutBrackets, 1).toArray()[0];
                        if (ma.isArray()) {
                            MetaObject moZwischen = (MetaObject) ma.getValue();
                            ObjectAttribute[] oa = moZwischen.getAttribs();
                            //String wieder zusammenkleben
                            String reGlued = "";
                            for (int j = i + 1; j < attrNames.length; ++j) {
                                reGlued += attrNames[j];
                                if (j < attrNames.length - 1) {
                                    reGlued += ".";
                                }
                            }
                            //Check ob ein Vector oder ne HashMap zur\u00FCckgeliefert werden m\u00FCssen
                            String[] again = reGlued.split("\\.");
                            if (again.length >= 2 && again[1].matches(".+\\[.+\\]")) {
                                //Es muss ne HashMap geliefert werden weil der Arraykram noch weitergeht

                                //Key Attribut der HashMap rausfinden
                                String keyAttrName = again[1].replaceAll(".*\\[", "");
                                keyAttrName = keyAttrName.replaceAll("\\]", "");
                                HashMap arrayEintraege = new HashMap();

                                //keyAttr muss zwischen den Klammer entfernt werden, aber nur beim 2ten Eintrag
                                String reReClued = again[0] + "." + again[1].replaceAll("\\[.*\\]", "") + "[].";
                                for (int j = 2; j < again.length; ++j) {
                                    reReClued += again[j];
                                    if (j < again.length - 1) {
                                        reReClued += ".";
                                    }
                                }

                                for (ObjectAttribute arrayEintrag : oa) {
                                    if (arrayEintrag.getValue() instanceof MetaObject&& ((MetaObject)(arrayEintrag.getValue())).getStatus()!=MetaObject.TO_DELETE) {
                                        Object key = getValueOfAttributeByString(again[0] + "." + keyAttrName, (MetaObject) arrayEintrag.getValue());
                                        Object val = getValueOfAttributeByString(reReClued, (MetaObject) arrayEintrag.getValue());
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
                                Vector arrayEintraege = new Vector();
                                for (ObjectAttribute arrayEintrag : oa) {
                                    if (arrayEintrag.getValue() instanceof MetaObject && ((MetaObject)(arrayEintrag.getValue())).getStatus()!=MetaObject.TO_DELETE) {
                                        Object val = getValueOfAttributeByString(reGlued, (MetaObject) arrayEintrag.getValue());
//                                        if (val != null) {
                                            arrayEintraege.add(val);
//                                        }
                                    }
                                }
                                return arrayEintraege;
                            }
                        }
                    } else {
                        Collection c = deeper.getAttributeByName(attrN, 1);
                        if (c.size() > 0) {
                            Attribute ma = (Attribute) c.toArray()[0];
                            if (ma.getValue() instanceof MetaObject) {
                                deeper = (MetaObject) ma.getValue();
                            } else {
                                return ma.getValue();
                            }
                        } else {
                            log.error("Falscher Attributname: " + attrN + " (Renderer wird nicht komplett funktionieren)",new CurrentStackTrace());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Fehler in getValueOfAttributeByString(" + attributeName + ",MetaObject)\nMetaobject="+mo.getDebugString(), e);
        }
        return null;
    }

    public static String debugPrintMetaObject(MetaObject mo) {
        String ret = "";
        return ret;
    }
}
