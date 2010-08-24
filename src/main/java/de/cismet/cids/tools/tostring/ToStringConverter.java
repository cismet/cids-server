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
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ToStringConverter.class);

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
        StringBuilder stringRepresentation = new StringBuilder();

        // ObjectAttribute[] attrs = o.getAttribs();
        final Collection<Attribute> names = o.getAttributeByName("name", 1);
        final Iterator iter = names.iterator();
        if (iter.hasNext()) {
            stringRepresentation.append(((ObjectAttribute) iter.next()).getValue());
        } else {
            stringRepresentation.append(o.getKey().toString());
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
        return stringRepresentation.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {

//        if (logger.isDebugEnabled()) {
//            logger.debug("convert von ToStringconverter gerufen");
//        }
        final StringBuilder stringRepresentation = new StringBuilder();

        if (o instanceof Sirius.server.localserver.object.Object) {
            final Collection<Attribute> names = ((Sirius.server.localserver.object.Object) o).getAttributeByName("name", 1);
            for (Attribute cur : names) {
                stringRepresentation.append(cur.getValue());
            }
        } else if (o instanceof Sirius.server.localserver.attribute.ObjectAttribute) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("call convert for ObjectAttribute");
//            }
            stringRepresentation.append(((ObjectAttribute) o).getValue());
        }
//        else {
//            logger.warn("Unknown Type for StringConversion ::" + o.getClass());
//        }
        return stringRepresentation.toString();
    }
}
