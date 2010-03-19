/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CustomToStringConverter.java
 *
 * Created on 6. August 2007, 08:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.tools;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.middleware.types.MetaObject;

import de.cismet.cids.annotations.CidsAttribute;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.tostring.*;

import java.lang.reflect.Field;

import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public abstract class CustomToStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    protected CidsBean cidsBean = null;

    private transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String createString();

    public String convert(de.cismet.cids.tools.tostring.StringConvertable o) {
        if (o instanceof MetaObject) {
            cidsBean = ((MetaObject)o).getBean();
        }
        if (log == null) {
            log = org.apache.log4j.Logger.getLogger(this.getClass());
        }
        if (log.isDebugEnabled()) {
            log.debug("convert in CustomToStringConverter ");
        }
        String stringRepresentation = "";
        Class customToString = this.getClass();
        Field[] fields = customToString.getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(CidsAttribute.class)) {
                try {
                    CidsAttribute ca = f.getAnnotation(CidsAttribute.class);
                    String attributeName = ca.value();
                    Object value = null;
                    if (o instanceof MetaObject) {
                        MetaObject mo = (MetaObject)o;
                        value = StaticCidsUtilities.getValueOfAttributeByString(attributeName, mo);
                    } else {
                        Attribute attr = (Attribute)o;
                        value = StaticCidsUtilities.getValueOfAttributeByString(attributeName, attr);
                        Vector v = new Vector();
//                        v.add("LALA");
//                        v.add("S");
//
//                        value=v;
                    }
                    f.set(this, value);
                } catch (Exception e) {
                    log.warn("Fehler beim Zuweisen in toStringMethode", e);
                }
            }
        }
        try {
            stringRepresentation = createString();
        } catch (Exception e) {
            log.warn("Error in a ToStringConverter", e);
            stringRepresentation = null;
        }

        return stringRepresentation;
    }
}
