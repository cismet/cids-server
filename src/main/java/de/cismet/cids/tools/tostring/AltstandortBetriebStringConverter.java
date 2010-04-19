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
 */
package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class AltstandortBetriebStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AdressStringConverter.
     */
    public AltstandortBetriebStringConverter() {
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
            if (attrs[i].getName().equalsIgnoreCase("Betrieb") || attrs[i].getName().equalsIgnoreCase("Betriebe")) {//NOI18N
                stringRepresentation += (attrs[i].toString() + " ");//NOI18N
            } else // surpress
            {
                // stringRepresentation+=( attrs[i].toString() + "?");
                // System.err.println("unerwartetes Attribut implements StringConverter");
            }
        }

        return stringRepresentation;
    }
}
