/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AdressStringConverter.java
 *
 * Created on 11. Mai 2004, 13:31
 *test
 */
package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.MetaObject;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class AdressStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 3190668550117541149L;

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AdressStringConverter.
     */
    public AdressStringConverter() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        final MetaObject mo = (MetaObject)o;

        String stringRepresentation = "";//NOI18N

        final ObjectAttribute[] attrs = mo.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            // besser getAttributeByname
            if (attrs[i].getName().equalsIgnoreCase("strasse") || attrs[i].getName().equalsIgnoreCase("hausnummer")) {//NOI18N
                stringRepresentation += (attrs[i].toString() + " ");//NOI18N
            }

//            else //surpress
//            {
//                stringRepresentation+=( attrs[i].toString() + "?");
//              logger.error("unerwartetes Attribut");
//            }

        }

        return stringRepresentation;
    }
}
