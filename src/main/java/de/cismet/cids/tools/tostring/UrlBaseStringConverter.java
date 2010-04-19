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
    public String convert(Sirius.server.localserver.object.Object o) {
        String stringRepresentation = "";//NOI18N

        ObjectAttribute[] attrs = o.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            if (
                attrs[i].getName().equalsIgnoreCase("prot_prefix")//NOI18N
                        || attrs[i].getName().equalsIgnoreCase("server")//NOI18N
                        || attrs[i].getName().equalsIgnoreCase("path")) {//NOI18N
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
