/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.ObjectAttribute;

import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class UrlStringConverter extends ToStringConverter implements Serializable {

    //~ Static fields/initializers ---------------------------------------------


    private static final transient Logger LOG = Logger.getLogger(UrlStringConverter.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UrlStringConverter.
     */
    public UrlStringConverter() {
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
            if (attrs[i].getName().equalsIgnoreCase("url_base_id")               // NOI18N
                        || attrs[i].getName().equalsIgnoreCase("object_name")) { // NOI18N
                stringRepresentation += (attrs[i].toString());
            } else                                                               // surpress
            {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("unexpected attribute in StringConverter");        // NOI18N
                }
            }
        }

        return stringRepresentation;
    }
}
