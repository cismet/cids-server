/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.types;

import java.util.Formatter;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class StringPatternFormater extends AbstractAttributeRepresentationFormater {

    //~ Instance fields --------------------------------------------------------

    private final String[] fieldNames;
    private final String pattern;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StringPatternFormater object.
     *
     * @param  pattern     DOCUMENT ME!
     * @param  fieldNames  DOCUMENT ME!
     */
    public StringPatternFormater(String pattern, String... fieldNames) {
        this.fieldNames = fieldNames;
        this.pattern = pattern;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getRepresentation() {
        if (fieldNames == null) {
            return "Fieldname array is null!";
        }
        final Object[] values = new Object[fieldNames.length];
        for (int i = 0; i < fieldNames.length; ++i) {
            values[i] = getAttribute(fieldNames[i]);
        }
        final Formatter formatter = new Formatter(new StringBuilder());
        return formatter.format(pattern, values).toString();
    }
}
