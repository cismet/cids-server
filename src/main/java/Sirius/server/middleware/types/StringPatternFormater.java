/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.types;

import java.util.Formatter;

/**
 *
 * @author srichter
 */
public final class StringPatternFormater extends AbstractAttributeRepresentationFormater {

    public StringPatternFormater(String pattern, String... fieldNames ) {
        this.fieldNames = fieldNames;
        this.pattern = pattern;
    }
    private final String[] fieldNames;
    private final String pattern;

    @Override
    public String getRepresentation() {
        if(fieldNames == null) {
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
