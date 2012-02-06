/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StringResultHandler.java
 *
 * Created on 9. Februar 2004, 13:58
 */
package Sirius.server.sql;
import Sirius.server.search.*;

import java.sql.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class StringResultHandler extends DefaultResultHandler {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of StringResultHandler.
     */
    public StringResultHandler() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object handle(final ResultSet rs, final Query q) throws SQLException, Exception {
        final Vector handledResult = new Vector(100, 100);

        // konstruktorparameter

        final int length = rs.getMetaData().getColumnCount();
        // rs.beforeFirst();

        if (length == 1) {
            while (rs.next()) {
                handledResult.add(rs.getString(1));
            }
        } else {
            while (rs.next()) {
                final String[] values = new String[length];

                for (int i = 0; i < values.length; i++) {
                    values[i] = rs.getString(i + 1);
                }

                handledResult.add(values);
            }
        }

        handledResult.trimToSize();

        return handledResult;
    }
}
