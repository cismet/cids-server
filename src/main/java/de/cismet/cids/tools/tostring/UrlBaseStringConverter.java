/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.ObjectAttribute;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class UrlBaseStringConverter extends ToStringConverter implements Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UrlBaseStringConverter object.
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
        String stringRepresentation = ""; // NOI18N

        final ObjectAttribute[] attrs = o.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getName().equalsIgnoreCase("prot_prefix")        // NOI18N
                        || attrs[i].getName().equalsIgnoreCase("server")  // NOI18N
                        || attrs[i].getName().equalsIgnoreCase("path")) { // NOI18N
                stringRepresentation += (attrs[i].toString());
            }
        }

        return stringRepresentation;
    }
}
