/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UrlStringConverter.java
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
public class UrlStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -2796385759299324453L;

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

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
        String stringRepresentation = "";

        final ObjectAttribute[] attrs = o.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getName().equalsIgnoreCase("url_base_id")
                        || attrs[i].getName().equalsIgnoreCase("object_name")) {
                stringRepresentation += (attrs[i].toString());
            } else // surpress
            {
                if (logger.isDebugEnabled()) {
                    // stringRepresentation+=( attrs[i].toString() + "?");
                    logger.debug("unerwartetes Attribut im StringConverter");
                }
            }
        }

        return stringRepresentation;
    }
}
