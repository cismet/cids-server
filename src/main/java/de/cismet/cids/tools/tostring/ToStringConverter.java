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

        final Collection<ObjectAttribute> names = o.getAttributeByName("name", 1); // NOI18N
        final Iterator<ObjectAttribute> iter = names.iterator();
        if (iter.hasNext()) {
            stringRepresentation.append(iter.next().getValue());
        } else {
            stringRepresentation.append(o.getKey().toString());
        }

        return stringRepresentation.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stringConvertable  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String convert(final StringConvertable stringConvertable) {
        final StringBuilder stringRepresentation = new StringBuilder();

        if (Sirius.server.localserver.object.Object.class.isAssignableFrom(stringConvertable.getClass())) {
            final Collection<ObjectAttribute> names = ((Sirius.server.localserver.object.Object)stringConvertable)
                        .getAttributeByName(
                            "name", // NOI18N
                            1);
            if (names.size() > 0) {
                for (final Attribute cur : names) {
                    stringRepresentation.append(cur.getValue());
                }
            } else {
                final ObjectAttribute oa = ((Sirius.server.localserver.object.Object)stringConvertable)
                            .getAttributeByFieldName("name");
                if (oa != null) {
                    stringRepresentation.append(String.valueOf(oa.getValue()));
                }
            }
        } else if (Sirius.server.localserver.attribute.ObjectAttribute.class.isAssignableFrom(
                        stringConvertable.getClass())) {
            if (((Sirius.server.localserver.attribute.ObjectAttribute)stringConvertable).getMai().getJavaclassname()
                        .equals(
                            GEOMETRY_CLASS_NAME)) {
                final Geometry geom = (Geometry)((ObjectAttribute)stringConvertable).getValue();
                stringRepresentation.append(geom.getGeometryType());
            } else {
                stringRepresentation.append(((ObjectAttribute)stringConvertable).getValue());
            }
        } else {
            // ?!
        }

        return stringRepresentation.toString();
    }
}
