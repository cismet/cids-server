/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ToString.java
 *
 * Created on 10. Mai 2004, 16:02
 */
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8894419084787216662L;

    //~ Instance fields --------------------------------------------------------

    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   o        DOCUMENT ME!
     * @param   classes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String convert(final Sirius.server.localserver.object.Object o, final HashMap classes) {
        String stringRepresentation = "";

        // ObjectAttribute[] attrs = o.getAttribs();
        final Collection names = o.getAttributeByName("name", 1);
        final Iterator iter = names.iterator();

        if (iter.hasNext()) {
            stringRepresentation += ((ObjectAttribute)iter.next()).getValue();
        } else {
            stringRepresentation += o.getKey().toString();
        }

//
//        for(int i = 0; i< attrs.length;i++)
//        {
//            if(!attrs[i].referencesObject())
//                stringRepresentation+=(attrs[i].toString()+ " ");
//            else
//                stringRepresentation+= ( ( (MetaObject)attrs[i].getValue()).toString(classes) + " " );
//
//        }
//
        return stringRepresentation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        setLogger();

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("convert von ToStringconverter gerufen");
            }
        }

        String stringRepresentation = "";

        if (o instanceof Sirius.server.localserver.object.Object) {
            final Collection names = ((Sirius.server.localserver.object.Object)o).getAttributeByName("name", 1);
            final Iterator iter = names.iterator();

            if (iter.hasNext()) {
                stringRepresentation += ((ObjectAttribute)iter.next()).getValue();
            } else {
                stringRepresentation += "";
            }
//            ObjectAttribute[] attrs = ((Sirius.server.localserver.object.Object)o).getAttribs();
//
//            for(int i = 0; i< attrs.length;i++)
//            {
//
//                stringRepresentation+=( attrs[i].toString() + " ");
//
//
//            }
        } else if (o instanceof Sirius.server.localserver.attribute.ObjectAttribute) {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("call convert for ObjectAttribute");
                }
            }
            stringRepresentation += ((ObjectAttribute)o).getValue();
        } else {
            if (logger != null) {
                logger.warn("Unknown Type for StringConversion ::" + o.getClass());
            }
        }

        return stringRepresentation;
    }

    /**
     * DOCUMENT ME!
     */
    public void setLogger() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
        }
    }
}
