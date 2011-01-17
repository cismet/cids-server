/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ExceptionHandler.java
 *
 * Created on 13. November 2003, 20:16
 */
package Sirius.server.sql;

import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public abstract class ExceptionHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ExceptionHandler.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   t  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Throwable handle(final Throwable t) {
        final StringBuilder sb = new StringBuilder();

        if (t instanceof SQLException) {
            final SQLException e = (SQLException)t;

            do {
                sb.append(e.toString());
                sb.append("\nSQL-State: ").append(e.getSQLState());   // NOI18N
                sb.append("\nError-Code :").append(e.getErrorCode()); // NOI18N
            } while (e.getNextException() != null);
        }

        LOG.error(sb.toString(), t);

        return t;
    }
}
