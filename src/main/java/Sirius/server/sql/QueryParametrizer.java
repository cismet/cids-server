/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import java.sql.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class QueryParametrizer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            QueryParametrizer.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Hauptfunktionalit\u00E4t der Klasse Parametrizer siehe Klassenbeschreibung.<BR>
     *
     * @param     statement   java.lang.String statement
     * @param     parameters  java.lang.Object[] parameters
     *
     * @return    DOCUMENT ME!
     *
     * @throws    java.lang.Exception  DOCUMENT ME!
     *
     * @exeption  java.lang.Exception
     */

    public static final String parametrize(String statement, java.lang.Object[] parameters) throws java.lang.Exception {
        if (statement != null) {
            statement = statement.trim();
        } else {
            logger.error("Stmnt in parametrizer NUll");
        }

        // nothing to parametrize
        if (parameters.length == 0) {
            return statement;
        }

        String parametrizedStmnt = "";

        final char chr = '?';

        int start = 0;
        int hit = statement.indexOf(chr);
        int i = 0;

        while ((hit != -1) && (i < parameters.length)) {
            parametrizedStmnt += statement.substring(start, hit); // statement bis zum ersten parameter

            parametrizedStmnt += parameters[i++].toString();

            start = hit + 1;

            hit = statement.indexOf(chr, start);

            // logger.debug( parametrizedStmnt + " hit :"+hit+" start :"+start);
        }

        parametrizedStmnt += statement.substring(start, statement.length()); // rest after last '?'
        if (logger.isDebugEnabled()) {
            logger.debug("INFO Stment :  " + parametrizedStmnt);
        }

        return parametrizedStmnt;
    } // end parametrize()

// ---------------------------------------

/*


public static void main(String[] args) throws Exception
{
    Object[] param = {"altlasten","5"};
   System.out.println( parametrize("select * from ? where id = ? and emil = 34",param ) );

}
*/

} // end class
