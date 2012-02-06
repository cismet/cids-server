/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
    public StringPatternFormater(final String pattern, final String... fieldNames) {
        this.fieldNames = fieldNames;
        this.pattern = pattern;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getRepresentation() {
        if (fieldNames == null) {
            return "Fieldname array is null!"; // NOI18N
        }
        final Object[] values = new Object[fieldNames.length];
        for (int i = 0; i < fieldNames.length; ++i) {
            values[i] = getAttribute(fieldNames[i]);
        }
        final Formatter formatter = new Formatter(new StringBuilder());
        return formatter.format(pattern, values).toString();
    }
}
