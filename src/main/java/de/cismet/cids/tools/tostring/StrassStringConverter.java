/***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
/*
 * StrassStringConverter.java
 *
 * Created on 11. Mai 2004, 13:44
 */
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.MetaObject;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class StrassStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------
    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -1178728757714994216L;
    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            GeometryStringConverter.class);

    //~ Constructors -----------------------------------------------------------
    /**
     * Creates a new instance of StrassStringConverter.
     */
    public StrassStringConverter() {
        super();
    }

    //~ Methods ----------------------------------------------------------------
    @Override
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        if (o instanceof MetaObject) {
            final MetaObject mo = (MetaObject) o;

            String stringRepresentation = "";

            final Collection<Attribute> attrs = mo.getAttributeByName("NAME", 1);

            if (!attrs.isEmpty()) {
                final Attribute attr = attrs.iterator().next();

                stringRepresentation += (attr.toString() + " ");
            }

            return stringRepresentation;
        } else if (o instanceof Attribute) {
            Attribute a = (Attribute) o;
            return String.valueOf(a.getValue());
        }
        //TODO log problem
        return "";
    }
}
