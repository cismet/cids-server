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

import java.lang.reflect.Field;


import de.cismet.cids.annotations.CidsAttribute;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.tostring.*;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public abstract class CustomToStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------
    protected CidsBean cidsBean = null;
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CustomToStringConverter.class);

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String createString();

    @Override
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        if (o instanceof MetaObject) {
            cidsBean = ((MetaObject) o).getBean();
        }
//        if (log.isDebugEnabled()) {
//            log.debug("convert in CustomToStringConverter ");
//        }
        String stringRepresentation;
        final Class customToString = this.getClass();
        final Field[] fields = customToString.getDeclaredFields();
        for (final Field f : fields) {
            if (f.isAnnotationPresent(CidsAttribute.class)) {
                try {
                    final CidsAttribute ca = f.getAnnotation(CidsAttribute.class);
                    final String attributeName = ca.value();
                    Object value = null;
                    if (o instanceof MetaObject) {
                        final MetaObject mo = (MetaObject) o;
                        value = StaticCidsUtilities.getValueOfAttributeByString(attributeName, mo);
                    } else {
                        final Attribute attr = (Attribute) o;
                        value = StaticCidsUtilities.getValueOfAttributeByString(attributeName, attr);
                    }
                    f.set(this, value);
                } catch (Exception e) {
                    log.warn("Error while assigning something in toStringMethode", e);//NOI18N
                }
            }
        }
        try {
            stringRepresentation = createString();
        } catch (Exception e) {
            log.warn("Error in a ToStringConverter", e);//NOI18N
            stringRepresentation = null;
        }

        return stringRepresentation;
    }
}
