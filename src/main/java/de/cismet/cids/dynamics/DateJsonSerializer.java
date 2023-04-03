/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.dynamics;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import java.text.SimpleDateFormat;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DateJsonSerializer extends StdSerializer<java.sql.Date> {

    //~ Static fields/initializers ---------------------------------------------

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsAttributeJsonSerializer object.
     */
    public DateJsonSerializer() {
        super(java.sql.Date.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void serialize(final java.sql.Date date, final JsonGenerator _jg, final SerializerProvider sp)
            throws IOException, JsonGenerationException {
        final String formattedDate = DATE_FORMAT.format(date.getTime());

        _jg.writeString(formattedDate);
    }
}
