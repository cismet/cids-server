/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * BoundingBoxStringConverter.java
 *
 * Created on 11. Mai 2004, 11:52
 */
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class BoundingBoxStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of BoundingBoxStringConverter.
     */
    public BoundingBoxStringConverter() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    public String convert(de.cismet.cids.tools.tostring.StringConvertable o) {
        String stringRepresentation = "";//NOI18N
        ObjectAttribute[] attrs = null;

        if (o instanceof Sirius.server.localserver.object.Object) {
            attrs = ((Sirius.server.localserver.object.Object)o).getAttribs();
        } else // attribute
        {
            java.lang.Object attrValue = ((Sirius.server.localserver.attribute.ObjectAttribute)o).getValue();
            attrs = ((Sirius.server.localserver.object.Object)attrValue).getAttribs();
        }

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getName().equalsIgnoreCase("x1")) {//NOI18N
                stringRepresentation += ("(" + attrs[i].toString() + ",");//NOI18N
            } else if (attrs[i].getName().equalsIgnoreCase("x2") || attrs[i].getName().equalsIgnoreCase("y1")) {//NOI18N
                stringRepresentation += (attrs[i].toString() + ",");//NOI18N
            } else if (attrs[i].getName().equalsIgnoreCase("y2")) {//NOI18N
                stringRepresentation += (attrs[i].toString() + ")");//NOI18N
            } else // surpress
            {
                if (logger.isDebugEnabled()) {
                    // stringRepresentation+=( attrs[i].toString() + "?");
                    logger.debug("unexpected attribute implements StringConverter");//NOI18N
                }

                // return
            }
        }

        if (stringRepresentation.length() > 0) {
            return stringRepresentation;
        } else {
            return "(,,,)";//NOI18N
        }
    }
}
