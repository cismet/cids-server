/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.middleware.types.MetaObject;

import java.io.Serializable;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class StrassStringConverter extends ToStringConverter implements Serializable {

    //~ Static fields/initializers ---------------------------------------------


    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of StrassStringConverter.
     */
    public StrassStringConverter() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String convert(final StringConvertable o) {
        if (o instanceof MetaObject) {
            final MetaObject mo = (MetaObject)o;

            String stringRepresentation = ""; // NOI18N

            final Collection<Attribute> attrs = mo.getAttributeByName("NAME", 1); // NOI18N

            if (!attrs.isEmpty()) {
                final Attribute attr = attrs.iterator().next();

                stringRepresentation += (attr.toString() + " ");
            }

            return stringRepresentation;
        } else if (o instanceof Attribute) {
            final Attribute a = (Attribute)o;
            return String.valueOf(a.getValue());
        }

        // TODO log problem

        return "";
    }
}
