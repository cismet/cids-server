/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UrlBaseStringConverter.java
 *
 * Created on 11. Mai 2004, 13:45
 */
package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class UrlBaseStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 6673513990961520521L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UrlBaseStringConverter.
     */
    public UrlBaseStringConverter() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String convert(final Sirius.server.localserver.object.Object o) {
        String stringRepresentation = "";

        final ObjectAttribute[] attrs = o.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getName().equalsIgnoreCase("prot_prefix")
                        || attrs[i].getName().equalsIgnoreCase("server")
                        || attrs[i].getName().equalsIgnoreCase("path")) {
                stringRepresentation += (attrs[i].toString());
            } else // surpress
            {
                // stringRepresentation+=( attrs[i].toString() + "?");
                // System.err.println("unerwartetes Attribut implements StringConverter");
            }
        }

        return stringRepresentation;
    }
}
