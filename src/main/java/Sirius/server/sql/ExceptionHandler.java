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
import java.sql.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public abstract class ExceptionHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            ExceptionHandler.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   t  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Throwable handle(final Throwable t) {
        String message = ""; // NOI18N

        if (t instanceof SQLException) {
            final SQLException e = (SQLException)t;

            do {
                message += (e.toString());
                message += ("\nSQL-State: " + e.getSQLState());   // NOI18N
                message += ("\nError-Code :" + e.getErrorCode()); // NOI18N
            } while (e.getNextException() != null);
        }

        logger.error(message, t);

        t.printStackTrace();

        return t;
    }
}
