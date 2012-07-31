/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.tostring;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.ObjectAttribute;

import com.vividsolutions.jts.geom.Geometry;

import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ToStringConverter implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final String GEOMETRY_CLASS_NAME = "com.vividsolutions.jts.geom.Geometry";

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
        final StringBuilder stringRepresentation = new StringBuilder();

        final Collection<Attribute> names = o.getAttributeByName("name", 1); // NOI18N
        final Iterator iter = names.iterator();
        if (iter.hasNext()) {
            stringRepresentation.append(((ObjectAttribute)iter.next()).getValue());
        } else {
            stringRepresentation.append(o.getKey().toString());
        }

        return stringRepresentation.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String convert(final StringConvertable o) {
        final StringBuilder stringRepresentation = new StringBuilder();

        if (o instanceof Sirius.server.localserver.object.Object) {
            final Collection<Attribute> names = ((Sirius.server.localserver.object.Object)o).getAttributeByName(
                    "name", // NOI18N
                    1);
            for (final Attribute cur : names) {
                stringRepresentation.append(cur.getValue());
            }
        } else if (o instanceof Sirius.server.localserver.attribute.ObjectAttribute) {
            if (((Sirius.server.localserver.attribute.ObjectAttribute)o).getMai().getJavaclassname().equals(
                            GEOMETRY_CLASS_NAME)) {
                final Geometry geom = (Geometry)((ObjectAttribute)o).getValue();
                stringRepresentation.append(geom.getGeometryType());
            } else {
                stringRepresentation.append(((ObjectAttribute)o).getValue());
            }
        }

        return stringRepresentation.toString();
    }
}
