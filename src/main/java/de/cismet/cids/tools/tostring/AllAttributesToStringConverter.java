/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AllAttributesToStringConverter.java
 *
 * Created on 20. M\u00E4rz 2007, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;

import java.io.Serializable;

import java.util.HashMap;

/**
 * Referenced classes of package de.cismet.cids.tools.tostring: ToStringConverter
 *
 * @version  $Revision$, $Date$
 */
public class AllAttributesToStringConverter extends ToStringConverter implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -6299801781259227376L;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String convert(final Sirius.server.localserver.object.Object o, final HashMap classes) {
        String stringRepresentation = "";
        final ObjectAttribute[] attrs = o.getAttribs();
        for (int i = 0; i < attrs.length; i++) {
            if (!attrs[i].referencesObject()) {
                stringRepresentation += (attrs[i].toString() + " ");
            } else {
                stringRepresentation += (((MetaObject)attrs[i].getValue()).toString(classes) + " ");
            }
        }
        return stringRepresentation;
    }

    @Override
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        String stringRepresentation = "";
        if (o instanceof Sirius.server.localserver.object.Object) {
            final ObjectAttribute[] attrs = ((Sirius.server.localserver.object.Object)o).getAttribs();
            for (int i = 0; i < attrs.length; i++) {
                stringRepresentation += (attrs[i].toString() + " ");
            }
        } else if (o instanceof Sirius.server.localserver.attribute.ObjectAttribute) {
            return ((ObjectAttribute)o).getValue().toString();
        }
        return stringRepresentation;
    }
}
